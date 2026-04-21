#!/usr/bin/env python3
"""
Clean duplicate rows in contract_reminder.

Rule:
- Group by (contract_id, recipient_user_id)
- Only consider active rows (status = 0 AND (is_read = 0 OR is_read IS NULL))
- Keep the newest row (max id) in each group
- Remove the rest only when --apply is passed

Connection:
- Reads DB_URL / DB_USERNAME / DB_PASSWORD from environment by default
- Supports overriding with --db-url / --db-user / --db-password

Examples:
  python3 scripts/cleanup-duplicate-reminders.py
  python3 scripts/cleanup-duplicate-reminders.py --apply
  python3 scripts/cleanup-duplicate-reminders.py --apply --backup-dir /tmp/reminder-backups
"""

from __future__ import annotations

import argparse
import os
import re
import subprocess
import sys
from datetime import datetime
from dataclasses import dataclass
from pathlib import Path


@dataclass
class DbConn:
    host: str
    port: int
    database: str
    username: str
    password: str


def parse_jdbc_url(jdbc_url: str) -> tuple[str, int, str]:
    """
    Parse:
    jdbc:mysql://localhost:3306/contract_db?useUnicode=true...
    """
    m = re.match(r"^jdbc:mysql://([^/:?]+)(?::(\d+))?/([^?]+)", jdbc_url.strip())
    if not m:
        raise ValueError(f"Unsupported DB_URL format: {jdbc_url}")
    host = m.group(1)
    port = int(m.group(2) or "3306")
    database = m.group(3)
    return host, port, database


def mysql_cmd(conn: DbConn, sql: str) -> list[str]:
    cmd = [
        "mysql",
        "-h",
        conn.host,
        "-P",
        str(conn.port),
        "-u",
        conn.username,
        f"-p{conn.password}",
        "-D",
        conn.database,
        "-N",
        "-e",
        sql,
    ]
    proc = subprocess.run(cmd, text=True, capture_output=True, check=False)
    if proc.returncode != 0:
        stderr = proc.stderr.strip() or "(no stderr)"
        raise RuntimeError(f"mysql command failed: {stderr}")
    return [line.strip() for line in proc.stdout.splitlines() if line.strip()]


def build_conn(args: argparse.Namespace) -> DbConn:
    db_url = args.db_url or os.getenv("DB_URL")
    db_user = args.db_user or os.getenv("DB_USERNAME")
    db_password = args.db_password or os.getenv("DB_PASSWORD")
    if not db_url or not db_user or db_password is None:
        raise ValueError("Missing DB config. Provide --db-url/--db-user/--db-password or set DB_URL/DB_USERNAME/DB_PASSWORD.")
    host, port, database = parse_jdbc_url(db_url)
    return DbConn(host=host, port=port, database=database, username=db_user, password=db_password)


def duplicate_groups_sql() -> str:
    return """
SELECT
  contract_id,
  recipient_user_id,
  COUNT(*) AS cnt,
  MAX(id) AS keep_id
FROM contract_reminder
WHERE status = 0
  AND (is_read = 0 OR is_read IS NULL)
  AND contract_id IS NOT NULL
  AND recipient_user_id IS NOT NULL
GROUP BY contract_id, recipient_user_id
HAVING COUNT(*) > 1
ORDER BY cnt DESC, keep_id DESC;
""".strip()


def duplicate_delete_sql() -> str:
    return """
DELETE cr
FROM contract_reminder cr
JOIN (
  SELECT contract_id, recipient_user_id, MAX(id) AS keep_id
  FROM contract_reminder
  WHERE status = 0
    AND (is_read = 0 OR is_read IS NULL)
    AND contract_id IS NOT NULL
    AND recipient_user_id IS NOT NULL
  GROUP BY contract_id, recipient_user_id
  HAVING COUNT(*) > 1
) keepers
  ON cr.contract_id = keepers.contract_id
 AND cr.recipient_user_id = keepers.recipient_user_id
WHERE cr.id <> keepers.keep_id
  AND cr.status = 0
  AND (cr.is_read = 0 OR cr.is_read IS NULL);
""".strip()


def duplicate_rows_export_sql() -> str:
    return """
SELECT
  cr.id,
  cr.contract_id,
  cr.recipient_user_id,
  cr.contract_no,
  cr.contract_title,
  cr.expire_date,
  cr.remind_days,
  cr.reminder_type,
  cr.status,
  cr.is_read,
  cr.created_at,
  cr.updated_at
FROM contract_reminder cr
JOIN (
  SELECT contract_id, recipient_user_id, MAX(id) AS keep_id
  FROM contract_reminder
  WHERE status = 0
    AND (is_read = 0 OR is_read IS NULL)
    AND contract_id IS NOT NULL
    AND recipient_user_id IS NOT NULL
  GROUP BY contract_id, recipient_user_id
  HAVING COUNT(*) > 1
) keepers
  ON cr.contract_id = keepers.contract_id
 AND cr.recipient_user_id = keepers.recipient_user_id
WHERE cr.id <> keepers.keep_id
  AND cr.status = 0
  AND (cr.is_read = 0 OR cr.is_read IS NULL)
ORDER BY cr.contract_id, cr.recipient_user_id, cr.id;
""".strip()


def export_duplicate_rows(conn: DbConn, output_dir: Path) -> Path:
    output_dir.mkdir(parents=True, exist_ok=True)
    ts = datetime.now().strftime("%Y%m%d-%H%M%S")
    output_path = output_dir / f"reminder-duplicates-backup-{ts}.tsv"

    sql = duplicate_rows_export_sql()
    cmd = [
        "mysql",
        "-h",
        conn.host,
        "-P",
        str(conn.port),
        "-u",
        conn.username,
        f"-p{conn.password}",
        "-D",
        conn.database,
        "-N",
        "-e",
        sql,
    ]
    proc = subprocess.run(cmd, text=True, capture_output=True, check=False)
    if proc.returncode != 0:
        stderr = proc.stderr.strip() or "(no stderr)"
        raise RuntimeError(f"Export backup failed: {stderr}")

    output_path.write_text(proc.stdout, encoding="utf-8")
    return output_path


def main() -> int:
    parser = argparse.ArgumentParser(description="Cleanup duplicate active rows in contract_reminder")
    parser.add_argument("--apply", action="store_true", help="Actually execute DELETE (default is dry-run)")
    parser.add_argument("--db-url", help="Override DB_URL")
    parser.add_argument("--db-user", help="Override DB_USERNAME")
    parser.add_argument("--db-password", help="Override DB_PASSWORD")
    parser.add_argument(
        "--backup-dir",
        default="scripts/cleanup-backups",
        help="Directory for pre-delete backup TSV when using --apply",
    )
    args = parser.parse_args()

    try:
        conn = build_conn(args)
    except Exception as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 2

    print("== contract_reminder duplicate scan ==")
    print(f"Target DB: mysql://{conn.username}@{conn.host}:{conn.port}/{conn.database}")
    print(f"Mode: {'APPLY' if args.apply else 'DRY-RUN'}")
    print("")

    try:
        rows = mysql_cmd(conn, duplicate_groups_sql())
    except Exception as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 3

    if not rows:
        print("No duplicate active reminder groups found.")
        return 0

    total_groups = 0
    total_rows_to_delete = 0
    print("Duplicate groups (contract_id, recipient_user_id, count, keep_id):")
    for line in rows:
        parts = line.split("\t")
        if len(parts) != 4:
            continue
        contract_id, recipient_user_id, cnt_raw, keep_id = parts
        cnt = int(cnt_raw)
        total_groups += 1
        total_rows_to_delete += max(0, cnt - 1)
        print(f"- contract_id={contract_id}, recipient_user_id={recipient_user_id}, count={cnt}, keep_id={keep_id}")

    print("")
    print(f"Summary: {total_groups} groups, {total_rows_to_delete} rows can be removed.")

    if not args.apply:
        print("")
        print("Dry-run only. Re-run with --apply to execute deletion.")
        return 0

    try:
        backup_file = export_duplicate_rows(conn, Path(args.backup_dir))
        print(f"Backup saved: {backup_file}")
    except Exception as exc:
        print(f"ERROR: backup export failed, abort apply: {exc}", file=sys.stderr)
        return 7

    try:
        mysql_cmd(conn, duplicate_delete_sql())
    except Exception as exc:
        print(f"ERROR: delete failed: {exc}", file=sys.stderr)
        return 4

    print("Delete executed. Re-checking duplicates...")
    try:
        remaining = mysql_cmd(conn, duplicate_groups_sql())
    except Exception as exc:
        print(f"WARNING: cleanup done but re-check failed: {exc}", file=sys.stderr)
        return 5

    if remaining:
        print(f"WARNING: {len(remaining)} duplicate groups still remain, please inspect manually.", file=sys.stderr)
        return 6

    print("Done. No duplicate active reminder groups remain.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
