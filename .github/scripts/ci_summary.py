"""Genera el resumen Markdown del CI: totales de tests y cobertura por bounded context."""
import csv
import glob
import xml.etree.ElementTree as ET
from collections import defaultdict
from pathlib import Path

BASE_PACKAGE_DEPTH = 4  # io.github.agilesoftware5.systemacademy


def tests_summary() -> list[str]:
    totals = defaultdict(int)
    failed = []
    for report in glob.glob("target/surefire-reports/TEST-*.xml"):
        suite = ET.parse(report).getroot()
        for key in ("tests", "failures", "errors", "skipped"):
            totals[key] += int(suite.get(key, 0))
        for case in suite.iter("testcase"):
            if case.find("failure") is not None or case.find("error") is not None:
                failed.append(f"{case.get('classname', '').split('.')[-1]}.{case.get('name')}")

    if not totals:
        return ["### Tests", "", "No se generaron reportes (el build falló antes de los tests)."]

    status = "✅" if not failed else "❌"
    lines = [
        f"### {status} Tests",
        "",
        "| Total | Fallidos | Errores | Omitidos |",
        "|---|---|---|---|",
        f"| {totals['tests']} | {totals['failures']} | {totals['errors']} | {totals['skipped']} |",
    ]
    if failed:
        lines += ["", "**Fallaron:**", *[f"- `{name}`" for name in failed]]
    return lines


def coverage_summary() -> list[str]:
    report = Path("target/site/jacoco/jacoco.csv")
    if not report.exists():
        return ["### Cobertura", "", "Sin reporte de cobertura."]

    by_context = defaultdict(lambda: [0, 0])
    for row in csv.DictReader(report.open()):
        parts = row["PACKAGE"].split(".")
        context = parts[BASE_PACKAGE_DEPTH] if len(parts) > BASE_PACKAGE_DEPTH else "(raíz)"
        covered, missed = int(row["LINE_COVERED"]), int(row["LINE_MISSED"])
        by_context[context][0] += covered
        by_context[context][1] += covered + missed

    covered = sum(c for c, _ in by_context.values())
    total = sum(t for _, t in by_context.values())
    lines = [
        f"### Cobertura de líneas: {100 * covered / total:.1f}%",
        "",
        "| Contexto | Cobertura |",
        "|---|---|",
    ]
    for context, (c, t) in sorted(by_context.items()):
        lines.append(f"| {context} | {100 * c / t:.1f}% |")
    lines += ["", "_Reporte HTML completo en el artifact `reportes-tests-y-cobertura`._"]
    return lines


if __name__ == "__main__":
    print("\n".join(tests_summary() + [""] + coverage_summary()))
