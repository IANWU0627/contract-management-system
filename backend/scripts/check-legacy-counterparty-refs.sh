#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

echo "Scanning for legacy references to contract.counterparty ..."
echo

# We only care about business-layer references. Migration/schema/docs are excluded.
RESULTS="$(
  rg -n "getCounterparty\(|setCounterparty\(|contract\.counterparty|counterparty\s+VARCHAR|contract\(counterparty\)" backend \
    --glob '!**/target/**' \
    --glob '!**/dist/**' \
    --glob '!**/node_modules/**' \
    --glob '!**/scripts/**' \
    --glob '!**/src/main/resources/db/migration/**' \
    --glob '!**/src/main/resources/schema.sql' \
    --glob '!**/src/main/resources/db/migration/V1__init_schema.sql' \
    --glob '!**/docs/**' \
    --glob '!**/*.md' || true
)"

if [[ -z "$RESULTS" ]]; then
  echo "OK: No business-layer legacy references found."
  exit 0
fi

echo "Found potential legacy references:"
echo "$RESULTS"
echo
echo "Please migrate these usages to contract_counterparty-based logic."
exit 1
