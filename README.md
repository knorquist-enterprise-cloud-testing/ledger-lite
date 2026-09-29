# ledger-lite — a tiny payments ledger for GitHub Copilot demos

> **Training repository:** this repository intentionally contains money-handling bugs and an intentionally vulnerable JDBC class for GitHub Copilot demos and training. It is not for production use.

`ledger-lite` is a small Java 21/Maven project used to show Copilot fixing realistic payment-ledger defects across GitHub, the Copilot CLI, and desktop coding sessions.

## Build and test

Prerequisites:

- Java 21
- Maven 3.9+

Run:

```bash
mvn test
```

The build starts red on purpose. The failing tests describe the seeded defects: fee rounding, split allocation, idempotent concurrent charges, and currency-aware receipt formatting.

## Project layout

- `src/main/java/com/example/ledger` — ledger classes
- `src/test/java/com/example/ledger` — deterministic JUnit Jupiter tests
- `.github/copilot-instructions.md` — coding rules for Copilot sessions
