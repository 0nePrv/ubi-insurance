# Event storming: итоги

Документ фиксирует решения по домену, чтобы они жили в репозитории, а не в истории чатов.
Меняется вместе с кодом: если реализация расходится с документом, правится одно из двух.

## Единый язык

| Термин | Значение |
|---|---|
| Quote | Котировка: цена на конкретные условия, живёт ограниченное время |
| Policy | Полис |
| Coverage | Покрытие (что страхуем) |
| Premium | Премия (сколько платит страхователь) |
| Endorsement | Изменение полиса посреди срока |
| Term | Срок действия полиса |
| Device | Телематическое устройство или мобильное приложение |
| Trip | Поездка: сегмент телеметрии между остановками |
| Driving event | Опасный эпизод: резкое торможение, превышение, телефон за рулём |
| Driver score | Скоринг водителя, [0, 1], выше — безопаснее |
| Claim | Убыток |
| FNOL | First notice of loss — первое заявление об убытке |
| Reserve | Резерв под убыток |
| Adjuster | Урегулировщик |
| Deductible | Франшиза |
| Payout | Выплата |

Домен условный, по смыслу ближе к КАСКО: у ОСАГО тарифы регулирует государство.

## Потоки

### 1. Котировка → полис (хореография)

`RequestQuote` → `QuoteIssued` (цена из rating по gRPC, фиксируется версия тарифа)
→ `BindPolicy(quoteId, idempotencyKey)` → `PolicyDrafted`
→ billing: `InvoiceIssued` → webhook провайдера → `PaymentReceived` | `PaymentFailed`
→ policy: `PolicyIssued` | `PolicyBindRejected` (компенсация, в том числе по таймауту 24 ч)
→ telematics: `DeviceBound`.

### 2. Телематика → скоринг → премия

Сырые точки — не доменные события: живут в Kafka и ClickHouse.
Kafka Streams (сессионные окна) → `TripCompleted`, `DrivingEventDetected`, `CrashDetected`
→ `DriverScoreChanged` → по закрытию периода policy выпускает `PremiumAdjusted` → billing.

### 3. Изменения посреди срока

`VehicleReplaced` / `DriverAdded` (с датой вступления в силу) + `PremiumAdjusted` → доплата или возврат.
Отмена: `PolicyCancelled` → `RefundIssued` → `DeviceUnbound`.

### 4. Урегулирование убытка (оркестрация, оркестратор в claims)

| Шаг | Участник | Успех | Неуспех / компенсация | Тип шага |
|---|---|---|---|---|
| 1 | Клиент или подтверждённый `CrashDetected` | `ClaimOpened` | — | — |
| 2 | policy: покрытие на дату убытка | `CoverageConfirmed` | `CoverageDenied` → `ClaimRejected` | чтение |
| 3 | Spring AI + фрод-модель | `ClaimTriaged`, `FraudScored` | `ClaimFlaggedForInvestigation` | — |
| 4 | billing | `ReserveCreated` | `ReserveReleased` | компенсируемый |
| 5 | Урегулировщик, SLA 48 ч | `ClaimAssessed` | `ClaimRejected`; таймаут → `ClaimEscalated` | решение |
| 6 | billing | `PayoutExecuted` | повтор с тем же ключом идемпотентности | pivot |
| 7 | claims + billing | `ClaimClosed`, остаток резерва освобождается | только повтор | повторяемый |

Выплату нельзя компенсировать, поэтому всё рискованное стоит до неё (классификация шагов по Ричардсону).

## Агрегаты

| Агрегат | Сервис | Инварианты | Хранение |
|---|---|---|---|
| Policy | policy | изменения только внутри срока; отменённый неизменен; 1–5 водителей; скачок премии ≤ ±20% за период | event sourcing |
| Quote | policy | неизменна; цена привязана к версии тарифа | Redis с TTL |
| Tariff | rating | неизменен после публикации; периоды не пересекаются | Kotlin DSL + таблицы коэффициентов |
| DeviceBinding | telematics | одно активное устройство на полис | Postgres |
| DriverScore | telematics | вычисляемое значение | state store + проекция |
| Claim | claims | допустимые переходы; выплата ≤ лимит − франшиза | JPA + история |
| Invoice | billing | сумма неизменна; оплата ≤ суммы | Postgres |
| LedgerTransaction | billing | дебет = кредит; только добавление, исправление сторно | Postgres |

## Hot spots: принятые решения

Приняты варианты по умолчанию; пересмотреть можно до начала реализации соответствующего потока.

1. Премия пересчитывается ежемесячно, скачок ограничен ±20%.
2. Полис без устройства: 14 дней льготного периода, затем коэффициент 1.15.
3. Изменения задним числом — до 30 дней (ради них и нужна битемпоральность event store).
4. `CrashDetected` создаёт инцидент; Claim открывается только после подтверждения клиентом.
5. Одна машина — один активный полис: таблица `vehicle_reservation` с PK по VIN в БД policy.
