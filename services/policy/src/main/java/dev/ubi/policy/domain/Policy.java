package dev.ubi.policy.domain;

import dev.ubi.policy.domain.event.*;
import dev.ubi.policy.domain.exception.*;
import dev.ubi.policy.domain.vo.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Агрегат «Полис».
 *
 * <p>Разделение обязанностей (decide/evolve):
 * <ul>
 *   <li>команды читают состояние, проверяют инварианты и порождают события — только они бросают исключения;</li>
 *   <li>{@link #apply(PolicyEvent)} безусловно применяет свершившийся факт к состоянию и не проверяет ничего:
 *       он выполняется при каждом восстановлении из истории, в том числе спустя годы после смены правил.</li>
 * </ul>
 *
 * <p>Две оси времени:
 * <ul>
 *   <li>{@code effectiveAt} — с какого момента изменение действует (бизнес-время, попадает в событие);</li>
 *   <li>{@code recordedAt} — когда команда выдана (системное время, в событие не попадает:
 *       его пишет event store в колонку {@code recorded_at}). Домену нужен только для проверок.</li>
 * </ul>
 */
public final class Policy {

    /** Максимальный шаг изменения премии за один пересчёт. Rating предлагает, Policy решает. */
    private static final BigDecimal MAX_STEP = BigDecimal.valueOf(0.2);

    private static final int MIN_DRIVERS = 1;
    private static final int MAX_DRIVERS = 5;

    /** Насколько далеко вперёд разрешено начинать срок полиса. */
    private static final Period MAX_TERM_START_AHEAD = Period.ofDays(60);

    /** Насколько далеко вперёд разрешено датировать изменение (плановая замена машины и т.п.). */
    private static final Period MAX_FORWARD_DATING = Period.ofDays(30);

    private PolicyId id;
    private PolicyStatus status;
    private Term term;
    private Money monthlyPremium;
    private Vehicle vehicle;
    private final Map<DriverId, Driver> drivers = new LinkedHashMap<>();

    /** Число применённых событий. Используется как ожидаемая версия при append в event store. */
    private int version;
    private int loadedVersion;

    private Policy() {
    }

    // ------------------------------------------------------------------ создание

    /**
     * Статическая фабрика: предыдущего состояния нет, читать нечего — экземпляр не нужен.
     * Возвращает события, а не агрегат: прикладной слой пишет их в store и при необходимости
     * получает агрегат через {@link #rehydrate(List)}.
     */
    public static List<PolicyEvent> draft(PolicyId id,
                                          Term term,
                                          Money premium,
                                          Vehicle vehicle,
                                          Map<DriverId, Driver> drivers,
                                          Instant recordedAt) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(term, "term");
        Objects.requireNonNull(vehicle, "vehicle");
        Objects.requireNonNull(recordedAt, "recordedAt");

        if (premium.isZero()) {
            throw new ZeroPremiumException();
        }
        if (drivers.size() < MIN_DRIVERS) {
            throw new DriverRequiredException();
        }
        if (drivers.size() > MAX_DRIVERS) {
            throw new DriverLimitExceededException(MAX_DRIVERS);
        }

        // Страховая защита не продаётся на уже прошедший период: за него исход уже известен.
        var start = term.startInstant();
        var maximumAhead = recordedAt.plus(MAX_TERM_START_AHEAD);
        if (start.isBefore(recordedAt) || start.isAfter(maximumAhead)) {
            throw new TermStartOutOfRangeException(start, maximumAhead, recordedAt);
        }

        return List.of(new PolicyDrafted(id, recordedAt, term, premium, vehicle, drivers));
    }

    public static Policy rehydrate(List<? extends PolicyEvent> events) {
        if (events == null || events.isEmpty()) {
            throw new EmptyEventStreamException();
        }
        if (!(events.getFirst() instanceof PolicyDrafted)) {
            throw new UnexpectedFirstEventException(events.getFirst());
        }
        var policy = new Policy();
        events.forEach(policy::apply);
        policy.loadedVersion = policy.version;
        return policy;
    }

    // ------------------------------------------------------------------ команды

    /**
     * Выпуск полиса после получения оплаты.
     * Повторный вызов — ошибка, а не no-op: дубликаты {@code PaymentReceived} гасит inbox уровнем выше,
     * поэтому второй issue означает настоящую проблему (двойной платёж, гонка, баг в саге).
     */
    public List<PolicyEvent> issue(Instant recordedAt) {
        requireStatus(PolicyStatus.DRAFT);
        return emit(new PolicyIssued(id, recordedAt));
    }

    /** Отклонение неоплаченного черновика: освобождает бронь VIN и завершает сагу оформления. */
    public List<PolicyEvent> rejectBind(String reason, Instant recordedAt) {
        requireStatus(PolicyStatus.DRAFT);
        return emit(new PolicyBindRejected(id, recordedAt, reason));
    }

    public List<PolicyEvent> addDriver(DriverId driverId, Driver driver,
                                       Instant effectiveAt, Instant recordedAt) {
        requireEndorsable(effectiveAt, recordedAt);
        if (drivers.containsKey(driverId)) {
            throw new DriverAlreadyExistsException(driverId);
        }
        if (drivers.size() + 1 > MAX_DRIVERS) {
            throw new DriverLimitExceededException(MAX_DRIVERS);
        }
        return emit(new DriverAdded(id, effectiveAt, driverId, driver));
    }

    /**
     * Изменение данных уже допущенного водителя — отдельное намерение, не добавление:
     * тот же человек, изменились его реквизиты. Позже может распасться на события по виду
     * изменения (продление прав влияет на премию, исправление фамилии — нет).
     */
    public List<PolicyEvent> updateDriverDetails(DriverId driverId, Driver driver,
                                                 Instant effectiveAt, Instant recordedAt) {
        requireEndorsable(effectiveAt, recordedAt);
        requireDriverPresent(driverId);
        return emit(new DriverDetailsUpdated(id, effectiveAt, driverId, driver));
    }

    public List<PolicyEvent> removeDriver(DriverId driverId, Instant effectiveAt, Instant recordedAt) {
        requireEndorsable(effectiveAt, recordedAt);
        requireDriverPresent(driverId);
        if (drivers.size() - 1 < MIN_DRIVERS) {
            throw new DriverRequiredException();
        }
        return emit(new DriverRemoved(id, effectiveAt, driverId));
    }

    public List<PolicyEvent> replaceVehicle(Vehicle newVehicle, Instant effectiveAt, Instant recordedAt) {
        requireEndorsable(effectiveAt, recordedAt);
        if (vehicle.vin().equals(newVehicle.vin())) {
            throw new VehicleUnchangedException(newVehicle.vin());
        }
        return emit(new VehicleReplaced(id, effectiveAt, newVehicle));
    }

    /**
     * Пересчёт премии по скорингу телематики.
     *
     * <p>Единственная команда, где {@code effectiveAt} в прошлом законен: телеметрия за май
     * досчитывается в начале июня, и премия за май пересчитывается с 1 мая. Риска нет —
     * речь о деньгах, а не о покрытии. Обратное запрещено: вперёд премию не двигаем.
     *
     * <p>В событии сохраняются обе суммы: {@code proposed} — мнение модели, {@code applied} —
     * решение бизнеса после ограничения шага. По ним потом видно, как часто ограничение срабатывает.
     */
    public List<PolicyEvent> adjustPremium(Money proposed, Instant effectiveAt,
                                           String tariffVersion, Instant recordedAt) {
        requireStatus(PolicyStatus.IN_FORCE);
        requireWithinTerm(effectiveAt);
        if (effectiveAt.isAfter(recordedAt)) {
            throw new ForwardDatingLimitExceededException(effectiveAt, recordedAt);
        }
        var applied = monthlyPremium.withStepLimit(proposed, MAX_STEP);
        return emit(new PremiumAdjusted(id, effectiveAt, proposed, applied, tariffVersion));
    }

    public List<PolicyEvent> cancel(String reason, Instant effectiveAt, Instant recordedAt) {
        requireEndorsable(effectiveAt, recordedAt);
        return emit(new PolicyCancelled(id, effectiveAt, reason));
    }

    // ------------------------------------------------------------------ applying events

    private List<PolicyEvent> emit(PolicyEvent... events) {
        for (var event : events) {
            apply(event);
        }
        return List.of(events);
    }

    void apply(PolicyEvent event) {
        switch (event) {
            case PolicyDrafted e -> {
                id = e.policyId();
                status = PolicyStatus.DRAFT;
                term = e.term();
                monthlyPremium = e.premium();
                vehicle = e.vehicle();
                drivers.putAll(e.drivers());
            }
            case PolicyIssued _         -> status = PolicyStatus.IN_FORCE;
            case PolicyBindRejected _   -> status = PolicyStatus.REJECTED;
            case PolicyCancelled _      -> status = PolicyStatus.CANCELLED;
            case DriverAdded e          -> drivers.put(e.driverId(), e.driver());
            case DriverDetailsUpdated e -> drivers.put(e.driverId(), e.driver());
            case DriverRemoved e        -> drivers.remove(e.driverId());
            case VehicleReplaced e      -> vehicle = e.vehicle();
            case PremiumAdjusted e      -> monthlyPremium = e.applied();
        }
        version++;
    }

    // ------------------------------------------------------------------ invariants check

    private void requireStatus(PolicyStatus expected) {
        if (status != expected) {
            throw new InvalidPolicyStatusException(status, expected);
        }
    }

    private void requireWithinTerm(Instant effectiveAt) {
        if (!term.contains(effectiveAt)) {
            throw new EffectiveAtOutsideTermException(effectiveAt, term);
        }
    }

    private void requireDriverPresent(DriverId driverId) {
        if (!drivers.containsKey(driverId)) {
            throw new DriverNotFoundException(driverId);
        }
    }

    /**
     * Общие правила для изменений действующего полиса (endorsement).
     *
     * <p>Backdating запрещён: покрытие за прошедший период продавать нельзя. Разрыв между
     * покупкой машины и уведомлением закрывается условием договора об автоматическом покрытии,
     * а не датой задним числом.
     *
     * <p>Forward-dating разрешён в пределах {@link #MAX_FORWARD_DATING}: плановая замена машины
     * со следующего понедельника — законный сценарий.
     */
    private void requireEndorsable(Instant effectiveAt, Instant recordedAt) {
        requireStatus(PolicyStatus.IN_FORCE);
        requireWithinTerm(effectiveAt);
        if (effectiveAt.isBefore(recordedAt)) {
            throw new BackdatedEndorsementException(effectiveAt, recordedAt);
        }
        if (effectiveAt.isAfter(recordedAt.plus(MAX_FORWARD_DATING))) {
            throw new ForwardDatingLimitExceededException(effectiveAt, recordedAt);
        }
    }

    // ------------------------------------------------------------------ state accessors

    public PolicyId id() {
        return id;
    }

    public PolicyStatus status() {
        return status;
    }

    public Term term() {
        return term;
    }

    public Money monthlyPremium() {
        return monthlyPremium;
    }

    public Vehicle vehicle() {
        return vehicle;
    }

    public List<Driver> drivers() {
        return List.copyOf(drivers.values());
    }

    public int version() {
        return version;
    }

    public int loadedVersion() {
        return loadedVersion;
    }
}
