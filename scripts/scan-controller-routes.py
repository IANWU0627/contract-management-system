#!/usr/bin/env python3
"""
Scan Spring-style @RequestMapping / @*Mapping in backend controllers.
Prints: METHOD  full_path  source_file

Usage (from repo root):
  python3 scripts/scan-controller-routes.py
  python3 scripts/scan-controller-routes.py | sort -k2
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path


def repo_root() -> Path:
    return Path(__file__).resolve().parent.parent


CLASS_MAPPING = re.compile(
    r"@RequestMapping\s*\(\s*(\{(?:\s*\"[^\"]+\"\s*,?\s*)+\}|\"[^\"]*\")\s*\)",
    re.MULTILINE,
)

METHOD_BLOCK = re.compile(
    r"@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)\s*(\([^)]*\))?",
    re.MULTILINE,
)

STRING_LITERALS = re.compile(r"\"([^\"]*)\"")


def extract_bases(rq_arg: str) -> list[str]:
    rq_arg = rq_arg.strip()
    if rq_arg.startswith("{"):
        return STRING_LITERALS.findall(rq_arg) or [""]
    if rq_arg.startswith('"'):
        return [STRING_LITERALS.match(rq_arg).group(1)] if STRING_LITERALS.match(rq_arg) else [""]
    return [""]


def method_path(paren_block: str | None) -> str:
    if not paren_block:
        return ""
    inner = paren_block.strip()[1:-1].strip()
    if not inner:
        return ""
    m = STRING_LITERALS.match(inner)
    if m:
        return m.group(1)
    return ""


def verb_from_name(name: str) -> str:
    return name.replace("Mapping", "").upper()


def join_paths(base: str, sub: str) -> str:
    base = base.rstrip("/")
    if not sub:
        return base or "/"
    sub = sub.lstrip("/")
    if not base:
        return "/" + sub if sub else "/"
    return f"{base}/{sub}"


def scan_file(path: Path) -> list[tuple[str, str, str]]:
    text = path.read_text(encoding="utf-8", errors="replace")
    rows: list[tuple[str, str, str]] = []

    # Split on @RestController so one file with multiple controllers is handled.
    segments = re.split(r"@RestController\s*", text)
    for seg in segments[1:]:
        m = CLASS_MAPPING.search(seg)
        if not m:
            continue
        bases = extract_bases(m.group(1))
        if not bases:
            bases = [""]

        tail = seg[m.end() :]
        brace_depth = 0
        end_idx = 0
        for i, ch in enumerate(tail):
            if ch == "{":
                brace_depth += 1
            elif ch == "}":
                brace_depth -= 1
                if brace_depth < 0:
                    end_idx = i
                    break
        if end_idx == 0:
            class_body = tail
        else:
            class_body = tail[:end_idx]

        for mm in METHOD_BLOCK.finditer(class_body):
            v = verb_from_name(mm.group(1))
            sub = method_path(mm.group(2))
            for b in bases:
                full = join_paths(b, sub)
                rows.append((v, full, path.name))

    return rows


def main() -> int:
    ap = argparse.ArgumentParser(description="List HTTP mappings from Java controllers (heuristic).")
    ap.add_argument(
        "--controller-dir",
        type=Path,
        default=repo_root() / "backend/src/main/java/com/contracthub/controller",
        help="Directory containing *Controller.java",
    )
    args = ap.parse_args()

    if not args.controller_dir.is_dir():
        print(f"Not a directory: {args.controller_dir}", file=sys.stderr)
        return 1

    all_rows: list[tuple[str, str, str]] = []
    for p in sorted(args.controller_dir.glob("*.java")):
        all_rows.extend(scan_file(p))

    for method, path, src in sorted(all_rows, key=lambda x: (x[1], x[0], x[2])):
        print(f"{method}\t{path}\t{src}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
