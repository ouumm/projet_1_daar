"""Benchmark: egrep vs clone (dfa, nfa, kmp). Usage: python3 tests/bench.py FICHIER [runs]"""
import subprocess, sys, time, statistics, csv, os
f = sys.argv[1]; runs = int(sys.argv[2]) if len(sys.argv) > 2 else 10
env = dict(os.environ, LC_ALL="C")
L = ["dfa", "nfa", "kmp", "bm"]
cases = [("Chihuahua", L), ("Pizzi", L), ("Pepperoni", L), ("Pizza", L), ("Poppers", L),
         ("S(a|g|r)+on", ["dfa", "nfa"]), ("S.*n", ["dfa", "nfa"]), ("(a|b)*c", ["dfa", "nfa"])]
import re
def run(cmd):
    """egrep : temps mur. clone : temps de recherche interne (--stats), hors demarrage de la JVM."""
    if cmd[0] == "egrep":
        t = time.perf_counter(); subprocess.run(cmd, stdout=subprocess.DEVNULL, env=env); return (time.perf_counter() - t) * 1000
    r = subprocess.run(cmd + ["--stats"], stdout=subprocess.DEVNULL, stderr=subprocess.PIPE, env=env, text=True)
    return float(re.search(r"recherche=([\d.]+) ms", r.stderr).group(1))
rows = []
for re_, algos in cases:
    for name, cmd in [("egrep", ["egrep", "-c", re_, f])] + [(a, ["./egrep-clone", "-c", "--algo", a, re_, f]) for a in algos]:
        ts = [run(cmd) for _ in range(runs)]
        rows.append((re_, name, statistics.mean(ts), statistics.stdev(ts) if runs > 1 else 0))
        print(f"{re_:14s} {name:6s} {rows[-1][2]:9.1f} ms  +/- {rows[-1][3]:.1f}")
with open("bench.csv", "w", newline="") as o:
    w = csv.writer(o); w.writerow(["regex", "algo", "mean_ms", "std_ms"]); w.writerows(rows)
try:
    import matplotlib; matplotlib.use("Agg"); import matplotlib.pyplot as plt
    regs = [c[0] for c in cases]; names = ["egrep", "dfa", "nfa", "kmp", "bm"]; wd = 0.16
    fig, ax = plt.subplots(figsize=(11, 4))
    for i, n in enumerate(names):
        xs, ys, es = [], [], []
        for j, r in enumerate(regs):
            for row in rows:
                if row[0] == r and row[1] == n: xs.append(j + i * wd); ys.append(row[2]); es.append(row[3])
        ax.bar(xs, ys, wd, yerr=es, label=n, capsize=2)
    ax.set_xticks([j + 2 * wd for j in range(len(regs))]); ax.set_xticklabels(regs)
    ax.set_yscale("log"); ax.set_ylabel("temps (ms, echelle log)"); ax.legend(); fig.tight_layout(); fig.savefig("bench.png", dpi=150)
except ImportError:
    print("matplotlib absent: pas de graphique")
