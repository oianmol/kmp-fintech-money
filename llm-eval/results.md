# Results

The 3 injected bugs (`mutants/`):

| Mutant | Bug | What a test needs to catch it |
|---|---|---|
| 01-fee-rounds-down | `RoundingMode.UP` truncates instead, so fees with a fractional cent undercharge | An amount whose percentage fee isn't a whole number of cents (e.g. 1.5% of 10.01) |
| 02-limit-off-by-one | The per-transaction limit becomes exclusive, so sending exactly the limit is rejected | A transfer of *exactly* the limit |
| 03-assumes-two-decimals | `Money.parse` assumes 2 decimal places for every currency | Any JPY, KWD or BHD amount |

## Hand-written suite (reference)

| Suite | Baseline | 01 rounding | 02 boundary | 03 exponent | Killed |
|---|---|---|---|---|---|
| `shared/src/commonTest` | pass | KILLED | KILLED | KILLED | 3/3 |

## LLM runs

- Model:
- Date:

| Run | Compiles + passes | 01 rounding | 02 boundary | 03 exponent | Killed |
|---|---|---|---|---|---|
| 01 | | | | | |
| 02 | | | | | |
| 03 | | | | | |
| 04 | | | | | |
| 05 | | | | | |
| 06 | | | | | |
| 07 | | | | | |
| 08 | | | | | |
| 09 | | | | | |
| 10 | | | | | |
| **Total** | /10 | /10 | /10 | /10 | |

## Failure notes

<!-- Read the generated tests and name what they missed: only round numbers? only USD? no exact-boundary case? -->
