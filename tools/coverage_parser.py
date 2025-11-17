#!/usr/bin/env python3
"""Parse JaCoCo XML report and emit a JSON summary and exit non-zero when below threshold.

Usage:
  python3 tools/coverage_parser.py --input target/site/jacoco/jacoco.xml --min-line 80

This script also writes a small summary to the GitHub Actions GITHUB_OUTPUT file
if present so workflow steps can reference the summary as an output.
"""
import argparse
import json
import os
import sys
import xml.etree.ElementTree as ET


def parse_jacoco(xml_path: str):
    if not os.path.exists(xml_path):
        raise FileNotFoundError(xml_path)
    tree = ET.parse(xml_path)
    root = tree.getroot()

    # Find line counters: sum covered and missed
    covered = 0
    missed = 0
    for counter in root.findall('.//counter'):
        if counter.get('type') == 'LINE':
            c = int(counter.get('covered', '0'))
            m = int(counter.get('missed', '0'))
            covered += c
            missed += m

    total = covered + missed
    pct = 0.0
    if total > 0:
        pct = covered / total * 100.0

    return {
        'covered': covered,
        'missed': missed,
        'total': total,
        'line_coverage_percent': round(pct, 2),
    }


def write_github_output(summary: str):
    output_path = os.environ.get('GITHUB_OUTPUT')
    if output_path:
        try:
            with open(output_path, 'a') as f:
                # set an output named 'summary'
                f.write(f"summary={summary}\n")
        except Exception:
            pass


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--input', required=True)
    p.add_argument('--min-line', required=False, type=float, default=0.0)
    p.add_argument('--json-out', required=False, default='target/site/jacoco/coverage-summary.json')
    args = p.parse_args()

    try:
        result = parse_jacoco(args.input)
    except FileNotFoundError:
        print(f"Jacoco XML not found at {args.input}")
        sys.exit(1)

    # Ensure output dir exists
    out_dir = os.path.dirname(args.json_out)
    if out_dir and not os.path.exists(out_dir):
        os.makedirs(out_dir, exist_ok=True)

    with open(args.json_out, 'w') as f:
        json.dump(result, f, indent=2)

    summary = (
        f"Line coverage: {result['line_coverage_percent']}% "
        f"(covered={result['covered']} missed={result['missed']} total={result['total']})"
    )

    print(summary)
    # write to GITHUB_OUTPUT for workflows that rely on step outputs
    write_github_output(summary)

    if args.min_line and result['line_coverage_percent'] < args.min_line:
        print(f"Coverage {result['line_coverage_percent']}% is below threshold {args.min_line}%")
        sys.exit(2)


if __name__ == '__main__':
    main()
