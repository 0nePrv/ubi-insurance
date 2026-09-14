package dev.ubi.policy.adapter.out.persistence.serde;

import dev.ubi.policy.domain.event.*;
import tools.jackson.databind.ObjectMapper;

/**
 * Перевод доменных событий в строки event store и обратно.
 *
 * <p>Вызывается явно, из репозитория: никакой автоматики и никаких аннотаций в домене.
 * {@code @JsonTypeInfo} с именем класса сознательно не используется — тип события
 * хранится отдельной колонкой как доменное имя.
 */
public final class PolicyEventSerde {

    private static final int CURRENT_SCHEMA_VERSION = 1;

    private final ObjectMapper mapper;

    public PolicyEventSerde(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public SerializedEvent serialize(PolicyEvent event) {
        return new SerializedEvent(
            eventTypeOf(event),
            CURRENT_SCHEMA_VERSION,
            mapper.writeValueAsString(event),
            event.effectiveAt()
        );
    }

    public PolicyEvent deserialize(SerializedEvent stored) {
        // Место для upcasting. Когда формат события изменится:
        //   1) заморозить старую форму как запись <Event>PayloadV1 в этом пакете;
        //   2) ветвиться здесь по stored.schemaVersion();
        //   3) читать старые строки в V1-запись и приводить к текущему домену.
        // Пока схема одна — всё, что не совпадает, читать нечем.
        if (stored.schemaVersion() != CURRENT_SCHEMA_VERSION) {
            throw new UnsupportedSchemaVersionException(stored.eventType(), stored.schemaVersion());
        }
        return mapper.readValue(stored.payload(), payloadTypeOf(stored.eventType()));
    }

    private static String eventTypeOf(PolicyEvent event) {
        return switch (event) {
            case PolicyDrafted _         -> "PolicyDrafted";
            case PolicyIssued _          -> "PolicyIssued";
            case PolicyBindRejected _    -> "PolicyBindRejected";
            case PolicyCancelled _       -> "PolicyCancelled";
            case DriverAdded _           -> "DriverAdded";
            case DriverDetailsUpdated _  -> "DriverDetailsUpdated";
            case DriverRemoved _         -> "DriverRemoved";
            case VehicleReplaced _       -> "VehicleReplaced";
            case PremiumAdjusted _       -> "PremiumAdjusted";
        };
    }

    private static Class<? extends PolicyEvent> payloadTypeOf(String eventType) {
        return switch (eventType) {
            case "PolicyDrafted"        -> PolicyDrafted.class;
            case "PolicyIssued"         -> PolicyIssued.class;
            case "PolicyBindRejected"   -> PolicyBindRejected.class;
            case "PolicyCancelled"      -> PolicyCancelled.class;
            case "DriverAdded"          -> DriverAdded.class;
            case "DriverDetailsUpdated" -> DriverDetailsUpdated.class;
            case "DriverRemoved"        -> DriverRemoved.class;
            case "VehicleReplaced"      -> VehicleReplaced.class;
            case "PremiumAdjusted"      -> PremiumAdjusted.class;
            default -> throw new UnknownEventTypeException(eventType);
        };
    }
}
