import re
import subprocess
import tempfile
import json
from pathlib import Path

root = Path(r"C:/Users/Ryan Li/gcp-lib")
instances = [
    root / "testInstances/3_mat_test.txt",
    root / "testInstances/6_mat_test.txt",
    root / "testInstances/7_mat_test.txt",
    root / "testInstances/8_mat_test.txt",
]


def parse_instance(path: Path):
    lines = [ln.strip() for ln in path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    n = int(lines[0].split()[0])
    edges = []
    for ln in lines[1:]:
        parts = ln.split()
        if parts[0].lower() == "e" and len(parts) >= 3:
            u, v = int(parts[1]), int(parts[2])
        elif len(parts) >= 2:
            u, v = int(parts[0]), int(parts[1])
        else:
            continue
        if u != v:
            edges.append((u, v))
    return n, edges


entry_re = re.compile(r"Colors:\s*(\d+)\s*\nColoring\s*\[(.*?)\]", re.S)


def validate_coloring(coloring, edges):
    if any(c == 0 for c in coloring[1:]):
        return False, "uncolored"
    for u, v in edges:
        if coloring[u] == coloring[v]:
            return False, f"conflict({u},{v})"
    return True, "ok"


results = []
for inst in instances:
    n, edges = parse_instance(inst)

    param_text = "\n".join(
        [
            "heuristic = Blochilger2008Heuristic",
            f"instance = {inst.as_posix()}",
            "seed = 1",
            "runtime = 2.0",
            "verbosity = 0",
            "maxiterations = 2000",
            "frequency = 10",
            "threshold = 5",
            "increment = 1",
            "initialtenure = 5",
            "",
        ]
    )

    with tempfile.NamedTemporaryFile(
        "w", suffix=".txt", delete=False, encoding="utf-8", dir=str(root / ".testlogs")
    ) as tf:
        tf.write(param_text)
        param_path = Path(tf.name)

    try:
        proc = subprocess.run(
            ["java", "-cp", "bin", "main", str(param_path)],
            cwd=str(root),
            capture_output=True,
            text=True,
            timeout=60,
        )
    except subprocess.TimeoutExpired:
        results.append({"instance": inst.name, "error": "timeout"})
        continue
    finally:
        param_path.unlink(missing_ok=True)

    out = proc.stdout + "\n" + proc.stderr
    matches = list(entry_re.finditer(out))

    best_valid_k = None
    valid_count = 0
    checked_entries = 0

    for m in matches:
        k = int(m.group(1))
        arr_text = m.group(2).strip()
        vals = [s.strip() for s in arr_text.split(",") if s.strip()]
        try:
            coloring = [int(x) for x in vals]
        except ValueError:
            continue
        if len(coloring) < n + 1:
            continue

        checked_entries += 1
        ok, _ = validate_coloring(coloring, edges)
        if ok:
            valid_count += 1
            if best_valid_k is None or k < best_valid_k:
                best_valid_k = k

    results.append(
        {
            "instance": inst.name,
            "exit_code": proc.returncode,
            "colors_token_count": out.count("Colors:"),
            "entries_found": len(matches),
            "entries_checked": checked_entries,
            "valid_entries": valid_count,
            "best_valid_k": best_valid_k,
            "status": "success" if "ran successfully" in out.lower() else "no-success-banner",
            "output_prefix": out[:200],
        }
    )

print(json.dumps(results, indent=2))
