# ADR-0003: Gateway на Spring Boot 4.0, остальные сервисы на 4.1

**Status:** Accepted (временное)
**Date:** 2026-09-11

## Context

На дату решения:
- Spring AI 2.0.1 собран под Spring Boot 4.1.1; в Boot 4.1 появились собственные gRPC-стартеры.
- Последний релиз Spring Cloud (2025.1.3) собран под Boot 4.0.8; линия под Boot 4.1 ещё в SNAPSHOT.
- Spring Cloud проверяет совместимость версии Boot при старте.

## Decision

Две платформы: `:platform:main` (Boot 4.1.1 + Spring AI) для всех сервисов и `:platform:gateway`
(Boot 4.0.8 + Spring Cloud 2025.1.3) только для gateway. Каждый модуль явно объявляет свою платформу.

## Options Considered

### Option A: всё на Boot 4.0
**Cons:** нет Spring AI 2.0 и встроенного gRPC; откат назад ради одного сервиса. Отвергнуто.

### Option B: всё на 4.1, Spring Cloud с отключённой проверкой совместимости
**Cons:** неподдерживаемая комбинация на входной точке системы. Отвергнуто.

## Consequences

- Демонстрирует реальное преимущество микросервисов: сервисы обновляются независимо.
- Spring Boot Gradle plugin один на всю сборку (4.1.1), поэтому загрузчик в jar gateway — от 4.1.1. Известный компромисс.
- Action item: при выходе Spring Cloud под Boot 4.1 удалить `platform/gateway` и перевести gateway на `:platform:main`.
