package dev.ubi.policy.adapter.out.persistence.serde;

import java.time.Instant;

/**
 * Событие в том виде, в каком оно ложится в строку таблицы {@code policy_events}.
 * Версия агрегата и {@code recorded_at} проставляются репозиторием при записи,
 * поэтому сюда не входят.
 *
 * @param eventType     доменное имя события, НЕ имя Java-класса: переименование класса
 *                      или переезд пакета не должны ломать чтение старой истории
 * @param schemaVersion версия формата payload; растёт при несовместимом изменении
 * @param payload       JSON для колонки jsonb
 * @param effectiveAt   бизнес-время, дублируется в отдельную колонку для запросов «на дату»
 */
public record SerializedEvent(String eventType, int schemaVersion, String payload, Instant effectiveAt) {
}
