package dev.ubi.policy.adapter.out.persistence.serde;

import dev.ubi.policy.domain.vo.*;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;
import tools.jackson.databind.module.SimpleModule;

import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.UUID;

/**
 * Правила представления доменных value objects в JSON для event store.
 *
 * <p>Живёт в адаптере, а не в домене: домен не знает о Jackson — это проверяет ArchitectureTest.
 * Записи событий сериализуются автоматически по компонентам, здесь описаны только те VO,
 * чьё представление по умолчанию нас не устраивает.
 *
 * <p>Это НЕ тот же ObjectMapper, что обслуживает REST. Формат хранения должен быть стабильным
 * годами, представление в API живёт своей жизнью — см. {@link EventSerdeConfiguration}.
 */
public final class PolicyDomainModule extends SimpleModule {

    public PolicyDomainModule() {
        super("policy-domain");

        addSerializer(PolicyId.class, new ValueSerializer<>() {
            @Override
            public void serialize(PolicyId value, JsonGenerator gen, SerializationContext ctx) {
                gen.writeString(value.value().toString());
            }
        });
        addDeserializer(PolicyId.class, new ValueDeserializer<>() {
            @Override
            public PolicyId deserialize(JsonParser p, DeserializationContext ctx) {
                return new PolicyId(UUID.fromString(p.getString()));
            }
        });

        addSerializer(DriverId.class, new ValueSerializer<>() {
            @Override
            public void serialize(DriverId value, JsonGenerator gen, SerializationContext ctx) {
                gen.writeString(value.value().toString());
            }
        });
        addDeserializer(DriverId.class, new ValueDeserializer<>() {
            @Override
            public DriverId deserialize(JsonParser p, DeserializationContext ctx) {
                return new DriverId(UUID.fromString(p.getString()));
            }
        });

        addKeySerializer(DriverId.class, new ValueSerializer<>() {
            @Override
            public void serialize(DriverId value, JsonGenerator gen, SerializationContext ctx) {
                gen.writeName(value.value().toString());
            }
        });
        addKeyDeserializer(DriverId.class, new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext ctx) {
                return new DriverId(UUID.fromString(key));
            }
        });

        addSerializer(Vin.class, new ValueSerializer<>() {
            @Override
            public void serialize(Vin value, JsonGenerator gen, SerializationContext ctx) {
                gen.writeString(value.value());
            }
        });
        addDeserializer(Vin.class, new ValueDeserializer<>() {
            @Override
            public Vin deserialize(JsonParser p, DeserializationContext ctx) {
                return Vin.of(p.getString());   // компактный конструктор валидирует и при чтении
            }
        });

        // Деньги: целое число минорных единиц. BigDecimal после stripTrailingZeros дал бы
        // научную нотацию (1.2E+4) — нечитаемо в jsonb и рискованно при обратном чтении.
        addSerializer(Money.class, new ValueSerializer<>() {
            @Override
            public void serialize(Money value, JsonGenerator gen, SerializationContext ctx) {
                gen.writeStartObject();
                gen.writeNumberProperty("minorUnits", value.value().longValueExact());
                gen.writeStringProperty("currency", value.currency().getCurrencyCode());
                gen.writeEndObject();
            }
        });
        addDeserializer(Money.class, new ValueDeserializer<>() {
            @Override
            public Money deserialize(JsonParser p, DeserializationContext ctx) {
                var json = ctx.readValue(p, MoneyJson.class);
                return Money.ofMinor(json.minorUnits(), Currency.getInstance(json.currency()));
            }
        });

        // Term: важно сохранить часовой пояс, а не только момент времени — от него зависит
        // contains(). ZonedDateTime.toString() пишет зону в квадратных скобках, parse читает обратно.
        addSerializer(Term.class, new ValueSerializer<>() {
            @Override
            public void serialize(Term value, JsonGenerator gen, SerializationContext ctx) {
                gen.writeStartObject();
                gen.writeStringProperty("startedAt", value.startedAt().toString());
                gen.writeStringProperty("mode", value.mode().name());
                gen.writeEndObject();
            }
        });
        addDeserializer(Term.class, new ValueDeserializer<>() {
            @Override
            public Term deserialize(JsonParser p, DeserializationContext ctx) {
                var json = ctx.readValue(p, TermJson.class);
                return new Term(ZonedDateTime.parse(json.startedAt()), TermMode.valueOf(json.mode()));
            }
        });
    }

    private record MoneyJson(long minorUnits, String currency) {
    }

    private record TermJson(String startedAt, String mode) {
    }
}
