# ADR 001: Store money as integer minor units
- **Status:** accepted

## Context
Reconciliation compares amounts for exact equality across sources. Binary floating point
cannot represent 0.10 exactly, so sums drift and equal amounts can compare unequal.

## Decision
Store amounts as `BIGINT` minor units (cents) plus an ISO 4217 currency code. In Java, use
`long` in the domain model, converting at API boundaries.

## Alternatives considered
- `double` — rejected: rounding errors break exact matching.
- `NUMERIC` + `BigDecimal` — viable and exact; rejected for the core ledger because integer
  math is faster, simpler to index, and forces explicit handling of currency exponents
  (JPY has 0 decimals, BHD has 3).

## Consequences
Every boundary must know the currency's exponent. Multi-currency math needs explicit FX
handling. Overflow is not a realistic concern at `BIGINT` range.
