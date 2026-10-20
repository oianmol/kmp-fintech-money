# kmp-fintech-money

[![CI](https://github.com/oianmol/kmp-fintech-money/actions/workflows/ci.yml/badge.svg)](https://github.com/oianmol/kmp-fintech-money/actions/workflows/ci.yml)

Companion repo for two videos:

- 📺 **Why I'd choose KMP for a fintech app in 2026 (and when I wouldn't)** — <link>
- 📺 **How I evaluate LLM output before trusting it** — <link>

## What this demonstrates

1. **Write the money rules once.** Fees, limits, parsing and rounding live in `commonMain`, and **one test suite runs on both the JVM and an iOS simulator** (see the CI badge). When someone asks "how do you know both apps calculate fees the same way?", this is the answer.
2. **Money is a type, not a Double.** Amounts are an integer count of minor units plus a currency. Arithmetic is overflow-checked and refuses to mix currencies, and every currency carries its own exponent: JPY has 0 decimal places, KWD and BHD have 3.
3. **Share the decisions, keep the trust boundary native.** `SecureStore` is only an interface. Keychain and Keystore implementations belong to the platforms.
4. **Passing tests aren't the same as tests that catch bugs.** `llm-eval/` grades a test suite by injecting real bugs and checking whether the tests notice.

## Start here

| File | What to look at |
|---|---|
| [`Money.kt`](shared/src/commonMain/kotlin/money/Money.kt) | Minor units, currency-aware parse and format |
| [`Arithmetic.kt`](shared/src/commonMain/kotlin/money/Arithmetic.kt) | Why `Math.addExact` doesn't work in commonMain, and explicit rounding modes |
| [`FeeCalculator.kt`](shared/src/commonMain/kotlin/money/FeeCalculator.kt) | Percentage + fixed fee, min/max, rounding up so you never undercharge |
| [`LimitChecker.kt`](shared/src/commonMain/kotlin/money/LimitChecker.kt) | Inclusive limits as a sealed result type |
| [`commonTest/`](shared/src/commonTest/kotlin/money) | The one test suite |
| [`llm-eval/`](llm-eval) | Mutants, eval script, prompt and results |

## Run it

Needs JDK 17+. The iOS tests need macOS with Xcode.

```bash
./gradlew allTests        # JVM + iOS simulator
./gradlew jvmTest         # JVM only (fast)
```

## The LLM eval

```bash
./llm-eval/run-eval.sh                                 # grade the hand-written suite
./llm-eval/run-eval.sh llm-eval/generated/run-01.kt    # grade an LLM-written test file on its own
IOS=1 ./llm-eval/run-eval.sh ...                       # also run on the iOS simulator
```

The script checks that the suite passes on the correct code, then applies each bug in `llm-eval/mutants/` one at a time. A failing test means the bug was **killed**. Passing tests mean it **survived** and would have shipped. The hand-written suite kills 3/3, and CI enforces that.

See [`llm-eval/prompt.md`](llm-eval/prompt.md) for the exact prompt and procedure, and [`llm-eval/results.md`](llm-eval/results.md) for the results.

## Not production code

This is a teaching repo. A real payments codebase also needs: a currency list from ISO 4217 (not seven hardcoded entries), FX handling, idempotency, audit logging, and fee rules that come from a server you can change without shipping an app.

MIT licensed.
