/**
 * Домен policy: агрегаты, value objects, доменные события, доменные исключения.
 * Policy — event-sourced агрегат; Quote — короткоживущий агрегат в Redis.
 * Не зависит от Spring, Kafka и транспорта — проверяется ArchitectureTest.
 */
package dev.ubi.policy.domain;
