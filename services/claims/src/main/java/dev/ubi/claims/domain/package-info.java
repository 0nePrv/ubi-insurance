/**
 * Домен claims: агрегаты, value objects, доменные события, доменные исключения.
 * Claim — конечный автомат без event sourcing (осознанно, см. docs/adr/0002).
 * Не зависит от Spring, Kafka и транспорта — проверяется ArchitectureTest.
 */
package dev.ubi.claims.domain;
