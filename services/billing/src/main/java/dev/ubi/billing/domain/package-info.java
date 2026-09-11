/**
 * Домен billing: агрегаты, value objects, доменные события, доменные исключения.
 * Invoice, LedgerTransaction (дебет = кредит, только добавление, исправление сторно).
 * Не зависит от Spring, Kafka и транспорта — проверяется ArchitectureTest.
 */
package dev.ubi.billing.domain;
