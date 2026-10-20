#!/usr/bin/env bash
# Grades a test suite by whether it catches real bugs, not just whether it passes.
#
#   ./llm-eval/run-eval.sh                          # grade the hand-written suite in commonTest
#   ./llm-eval/run-eval.sh llm-eval/generated/run-01.kt   # grade an LLM-written test file ON ITS OWN
#   IOS=1 ./llm-eval/run-eval.sh ...                # also run on the iOS simulator (slower)
#
# Steps: 1) the suite must compile and pass on the original code (baseline)
#        2) apply each mutant in llm-eval/mutants/, run the suite, revert
#           test fails  -> mutant KILLED   (the tests caught the bug)
#           test passes -> mutant SURVIVED (the bug would have shipped)
set -uo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

TEST_DIR="shared/src/commonTest/kotlin"
STASH="$ROOT/llm-eval/out/.stash"
TASK="${IOS:+allTests}"; TASK="${TASK:-jvmTest}"
GENERATED="${1:-}"
LABEL="${GENERATED:+$(basename "$GENERATED" .kt)}"; LABEL="${LABEL:-hand-written}"
APPLIED=""

mkdir -p llm-eval/out

if [[ -d "$STASH" ]]; then
  echo "Found $STASH from an interrupted run. It holds the hand-written tests:"
  echo "restore them with: rm -rf $TEST_DIR && mv $STASH $TEST_DIR"
  exit 1
fi

restore() {
  if [[ -n "$APPLIED" ]]; then git apply -R "$APPLIED"; APPLIED=""; fi
  if [[ -d "$STASH" ]]; then
    rm -rf "$TEST_DIR"
    mv "$STASH" "$TEST_DIR"
  fi
}
trap restore EXIT INT TERM

if [[ -n "$GENERATED" ]]; then
  [[ -f "$GENERATED" ]] || { echo "No such file: $GENERATED"; exit 1; }
  # Grade the generated file alone: move the hand-written tests out of the way.
  mv "$TEST_DIR" "$STASH"
  mkdir -p "$TEST_DIR/generated"
  cp "$GENERATED" "$TEST_DIR/generated/"
fi

run_tests() { ./gradlew -q --console=plain ":shared:$TASK" --rerun-tasks >/dev/null 2>&1; }

echo "Grading: $LABEL  (task: $TASK)"
printf '%-28s %s\n' "baseline" "..."
if ! run_tests; then
  echo "BASELINE FAILED: the suite doesn't compile or doesn't pass on correct code."
  echo "$(date +%F),$LABEL,baseline-failed,,," >> llm-eval/out/runs.csv
  exit 2
fi
printf '%-28s %s\n' "baseline" "PASS"

killed=0; total=0; row=""
for patch in llm-eval/mutants/*.patch; do
  name="$(basename "$patch" .patch)"
  git apply "$patch" && APPLIED="$patch"
  if run_tests; then result="SURVIVED"; else result="KILLED"; killed=$((killed + 1)); fi
  git apply -R "$patch"; APPLIED=""
  total=$((total + 1))
  printf '%-28s %s\n' "$name" "$result"
  row="$row,$result"
done

echo "Mutants killed: $killed/$total"
echo "$(date +%F),$LABEL,pass$row" >> llm-eval/out/runs.csv
