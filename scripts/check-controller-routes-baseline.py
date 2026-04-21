#!/usr/bin/env python3
"""
Validate controller route mappings against a committed baseline file.

Usage:
  python3 scripts/check-controller-routes-baseline.py
"""

from __future__ import annotations

import difflib
import subprocess
import sys
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parent.parent
SCANNER = REPO_ROOT / "scripts" / "scan-controller-routes.py"
BASELINE = REPO_ROOT / "scripts" / "controller-routes.baseline.tsv"


def run_scanner() -> list[str]:
    proc = subprocess.run(
        [sys.executable, str(SCANNER)],
        cwd=REPO_ROOT,
        check=False,
        text=True,
        capture_output=True,
    )
    if proc.returncode != 0:
        sys.stderr.write(proc.stderr)
        raise RuntimeError("Failed to scan controller routes")
    return [line.rstrip("\n") for line in proc.stdout.splitlines()]


def read_baseline() -> list[str]:
    if not BASELINE.exists():
        raise FileNotFoundError(f"Baseline not found: {BASELINE}")
    return [line.rstrip("\n") for line in BASELINE.read_text(encoding="utf-8").splitlines()]


def main() -> int:
    try:
        current = run_scanner()
        baseline = read_baseline()
    except Exception as exc:  # pragma: no cover
        print(f"ERROR: {exc}", file=sys.stderr)
        return 2

    if current == baseline:
        print("OK: controller route baseline is up to date.")
        return 0

    print("ERROR: controller routes changed but baseline was not updated.")
    print("Run the following command and commit the updated baseline:")
    print("  python3 scripts/scan-controller-routes.py > scripts/controller-routes.baseline.tsv")
    print("")
    diff = difflib.unified_diff(
        baseline,
        current,
        fromfile="scripts/controller-routes.baseline.tsv (committed)",
        tofile="scripts/controller-routes.baseline.tsv (current)",
        lineterm="",
    )
    for line in diff:
        print(line)
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
