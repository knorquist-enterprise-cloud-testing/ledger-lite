# Try it yourself

Use GitHub Copilot in IntelliJ, VS Code, or Copilot CLI with this intentionally red training repository.

## 1. Make the build green

Plan mode first is fine. Use this one-shot prompt:

> The build is red. Make `mvn -q test` pass by fixing the money-handling bugs — floating-point fee math, the lost penny in bill splitting, duplicate charges on concurrent retries with the same idempotency key, and currency-unaware receipts. Follow .github/copilot-instructions.md. Don't change or weaken existing tests; add tests for gaps. Summarize what you changed and why.

Then run:

```bash
mvn -q test
```

## 2. Review for SQL injection

Ask Copilot to run a security review of `TransactionRepository`. The expected fix is to use `PreparedStatement` instead of concatenating customer input into SQL.

## 3. Implement partial refunds

Open issue #5 and ask Copilot to implement partial refunds with tests. Keep the existing intentionally failing tests for issues #1–#4 unchanged.
