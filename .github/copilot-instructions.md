# ledger-lite Copilot instructions

- This is a tiny Java 21 payments ledger for GitHub Copilot demos and training.
- Keep changes focused, idiomatic, and easy to explain to Java developers.
- Money rules:
  - Use `BigDecimal` for money; never use `double`, `float`, or `new BigDecimal(double)`.
  - Scale monetary values to the currency default fraction digits where a currency is known.
  - Use `RoundingMode.HALF_EVEN` for fee and money rounding.
  - Splits and allocations must sum exactly to the original total.
- Idempotency rules:
  - The same idempotency key returns the same result.
  - Exactly one gateway charge may happen for a key, including under concurrent retries.
  - Reject null or blank idempotency keys with `IllegalArgumentException`.
- SQL must use `PreparedStatement`; do not concatenate user input into SQL.
- Build/test with `mvn -q test`; it must pass for completed fixes.
- Never weaken, delete, or skip existing tests.
- Add tests for new behavior or coverage gaps.
- Do not add runtime dependencies unless the design truly requires them.
