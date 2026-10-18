#!/usr/bin/env python3
"""Turns Gradle/Kotlin errors in build logs into GitHub annotations, so failures are readable on the run page."""
import re
import sys


def esc(s: str) -> str:
    return s.replace('%', '%25').replace('\r', '%0D').replace('\n', '%0A')


lines = []
for path in sys.argv[1:]:
    try:
        with open(path, errors='replace') as f:
            lines += [l.rstrip('\n') for l in f]
    except FileNotFoundError:
        pass

found = []
for i, l in enumerate(lines):
    if re.match(r'^e: ', l):
        found.append(l[3:].replace('file:///home/runner/work/harmonica-playalong/harmonica-playalong/', ''))
    elif ' FAILED' in l and '>' in l:
        found.append(' '.join(x.strip() for x in lines[i:i + 4]))
    elif l.startswith('* What went wrong:'):
        found.append(' '.join(x.strip() for x in lines[i + 1:i + 8] if x.strip() and not x.startswith('*')))

if not found:
    found = lines[-40:]

chunks = [found[i:i + 12] for i in range(0, len(found), 12)][:9]
for n, chunk in enumerate(chunks, 1):
    print(f"::error title=Build errors ({n}/{len(chunks)})::{esc(chr(10).join(chunk))}")
