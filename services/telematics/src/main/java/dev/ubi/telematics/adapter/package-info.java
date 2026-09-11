/**
 * Адаптеры. {@code adapter.in} — входящие (REST, gRPC, Kafka consumer),
 * {@code adapter.out} — исходящие (Postgres, Kafka producer через outbox, gRPC-клиенты).
 * Никто не зависит от адаптеров, адаптеры зависят от application и domain.
 */
package dev.ubi.telematics.adapter;
