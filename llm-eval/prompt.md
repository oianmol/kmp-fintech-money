# Prompt used for every run

Use this exact prompt for every run, so runs and models can be compared fairly. Paste the full contents of `Currency.kt`, `Arithmetic.kt`, `Money.kt`, `FeeCalculator.kt` and `LimitChecker.kt` where it says `<SOURCE>`.

---

You are a senior Kotlin engineer. Write unit tests for the following Kotlin Multiplatform code.

Requirements:
- Use `kotlin.test` only (no JUnit, no mocking libraries). The tests run in `commonTest` on JVM and iOS.
- Put everything in a single file, in `package money`.
- Cover `FeeCalculator` and `LimitChecker`. Test `Money` wherever the other two depend on it.
- Output only the Kotlin file, with no explanation.

<SOURCE>

---

## Procedure

1. New chat for each run (no memory between runs).
2. Save the output as `llm-eval/generated/run-NN.kt`, unedited. If it doesn't compile, record that, don't fix it.
3. `./llm-eval/run-eval.sh llm-eval/generated/run-NN.kt`
4. Copy the line from `llm-eval/out/runs.csv` into `results.md`.

Do 10 runs. Note the model and date in `results.md`.
