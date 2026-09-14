package dev.ubi.policy;

import dev.ubi.policy.domain.Policy;
import dev.ubi.policy.domain.event.*;
import dev.ubi.policy.domain.vo.*;

import java.time.*;
import java.util.*;

/**
 * Тестовые данные для домена и репозитория policy.
 *
 * <p>Два правила, из которых вырос этот класс:
 * <ul>
 *   <li><b>Идентификаторы всегда свежие.</b> Общий статический {@code PolicyId} означает, что все
 *       тесты пишут в один поток событий. Пока тесты транзакционные, это незаметно; тест на
 *       конкурентный append транзакционным быть не может и сразу начнёт мешать остальным.</li>
 *   <li><b>Моменты времени фиксированы и согласованы с инвариантами.</b> Начало срока лежит
 *       в пределах 60 дней от {@link #RECORDED_AT}, {@link #IN_TERM} попадает внутрь срока
 *       и не в прошлом относительно записи. Ни один тест не должен подбирать даты сам.</li>
 * </ul>
 */
public final class Fixtures {

    private Fixtures() {
    }

    // ------------------------------------------------------------------ время

    public static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    /** Момент оформления: когда команда выдана. */
    public static final Instant RECORDED_AT =
        ZonedDateTime.of(2026, 6, 1, 12, 0, 0, 0, ZONE).toInstant();

    /** Начало срока: через 14 дней после оформления — внутри разрешённых 60. */
    public static final ZonedDateTime TERM_START =
        ZonedDateTime.of(2026, 6, 15, 0, 0, 0, 0, ZONE);

    public static final Term TERM = new Term(TERM_START, TermMode.YEAR);

    /**
     * Момент внутри срока. Годится и как {@code effectiveAt}, и как {@code recordedAt}
     * для эндорсмента: backdating запрещён, поэтому они должны совпадать.
     */
    public static final Instant IN_TERM =
        ZonedDateTime.of(2026, 8, 1, 10, 0, 0, 0, ZONE).toInstant();

    /** За пределами срока — для проверки EffectiveAtOutsideTermException. */
    public static final Instant AFTER_TERM =
        ZonedDateTime.of(2028, 1, 1, 0, 0, 0, 0, ZONE).toInstant();

    // ------------------------------------------------------------------ деньги

    public static final Money PREMIUM = Money.ofMinorRub(500_000);          // 5 000,00 ₽
    public static final Money PROPOSED_PREMIUM = Money.ofMinorRub(900_000); // предложение модели
    public static final Money CLAMPED_PREMIUM = Money.ofMinorRub(600_000);  // после лимита +20%

    public static final String TARIFF_VERSION = "AUTO-UBI:3";

    // ------------------------------------------------------------------ идентификаторы

    public static PolicyId newPolicyId() {
        return new PolicyId(UUID.randomUUID());
    }

    public static DriverId newDriverId() {
        return new DriverId(UUID.randomUUID());
    }

    // ------------------------------------------------------------------ машины и водители

    public static final Vehicle VEHICLE = vehicle("1HGCM82633A004352", 150);
    public static final Vehicle ANOTHER_VEHICLE = vehicle("JH4KA7561PC008269", 200);

    public static Vehicle vehicle(String vin, int powerHp) {
        return new Vehicle(Vin.of(vin), powerHp, 2021, "RU-77");
    }

    public static final Driver ALICE = driver("Alice", "Ivanova", 1996, 2020);
    public static final Driver BOB = driver("Bob", "Ivanov", 2006, 2025);

    /** DrivingLicense пока пустая запись — все экземпляры равны. Заполнить, когда появятся поля. */
    public static Driver driver(String firstName, String lastName, int birthYear, int licenseYear) {
        return new Driver(firstName, lastName,
            LocalDate.of(birthYear, Month.MARCH, 1),
            new DrivingLicense(),
            LocalDate.of(licenseYear, Month.MAY, 1));
    }

    public static Map<DriverId, Driver> singleDriver(DriverId driverId) {
        return Map.of(driverId, ALICE);
    }

    // ------------------------------------------------------------------ потоки событий

    /**
     * Черновик: один элемент в потоке, версия агрегата после восстановления — 1.
     * Идёт через {@link Policy#draft}, а не через конструктор события: так фикстура
     * не разойдётся с инвариантами, если они изменятся.
     */
    public static List<PolicyEvent> draftedStream(PolicyId id, DriverId driverId) {
        return Policy.draft(id, TERM, PREMIUM, VEHICLE, singleDriver(driverId), RECORDED_AT);
    }

    /** Черновик + выпуск: два события, версия 2. Отправная точка для тестов эндорсментов. */
    public static List<PolicyEvent> issuedStream(PolicyId id, DriverId driverId) {
        var events = new ArrayList<PolicyEvent>(draftedStream(id, driverId));
        var policy = Policy.rehydrate(events);
        events.addAll(policy.issue(RECORDED_AT.plus(Duration.ofHours(1))));
        return List.copyOf(events);
    }

    public static Policy draftedPolicy(PolicyId id, DriverId driverId) {
        return Policy.rehydrate(draftedStream(id, driverId));
    }

    public static Policy issuedPolicy(PolicyId id, DriverId driverId) {
        return Policy.rehydrate(issuedStream(id, driverId));
    }

    // ------------------------------------------------------------------ по одному событию каждого типа

    /**
     * Ровно по одному экземпляру каждого из девяти событий — источник для round-trip теста
     * сериализации ({@code @MethodSource}). Если в домене появится новое событие,
     * {@code switch} в {@code PolicyEventSerde} перестанет компилироваться, а этот список
     * придётся дополнить вручную: компилятор о нём не знает.
     */
    public static List<PolicyEvent> oneOfEachEvent() {
        var id = newPolicyId();
        var driverId = newDriverId();
        return List.of(
            new PolicyDrafted(id, RECORDED_AT, TERM, PREMIUM, VEHICLE, singleDriver(driverId)),
            new PolicyIssued(id, RECORDED_AT),
            new PolicyBindRejected(id, RECORDED_AT, "payment timeout"),
            new DriverAdded(id, IN_TERM, driverId, BOB),
            new DriverDetailsUpdated(id, IN_TERM, driverId, ALICE),
            new DriverRemoved(id, IN_TERM, driverId),
            new VehicleReplaced(id, IN_TERM, ANOTHER_VEHICLE),
            new PremiumAdjusted(id, IN_TERM, PROPOSED_PREMIUM, CLAMPED_PREMIUM, TARIFF_VERSION),
            new PolicyCancelled(id, IN_TERM, "vehicle sold"));
    }
}
