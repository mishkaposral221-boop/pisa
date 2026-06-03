#!/usr/bin/env python3
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent / "src/client/java/rich"

START = re.compile(
    r"(?P<indent>^[ \t]*)((?:public |private |protected )?)record (?P<name>\w+)\(\)\s*(?P<impl>implements [\w.]+ )?\{",
    re.MULTILINE,
)


def fix(text: str) -> str:
    out = []
    pos = 0
    changed = False
    while True:
        m = START.search(text, pos)
        if not m:
            out.append(text[pos:])
            break
        out.append(text[pos : m.start()])
        indent = m.group("indent")
        close = re.compile(r"^" + re.escape(indent) + r"\}", re.MULTILINE)
        end = close.search(text, m.end())
        if not end:
            out.append(text[m.start() : m.end()])
            pos = m.end()
            continue
        block = text[m.end() : end.start()]
        fields: list[tuple[str, str]] = []
        for line in block.splitlines():
            fm = re.match(r"^[ \t]*private final ([\w.<>,\[\]?]+) (\w+);", line)
            if fm:
                fields.append((fm.group(1), fm.group(2)))
        if not fields:
            out.append(text[m.start() : end.end()])
            pos = end.end()
            continue
        impl = m.group("impl") or ""
        ps = ", ".join(f"{t} {n}" for t, n in fields)
        out.append(f"{indent}{m.group(2)}record {m.group('name')}({ps}) {impl}{{\n{indent}}}")
        changed = True
        pos = end.end()
    return "".join(out) if changed else text


def main():
    n = 0
    for path in ROOT.rglob("*.java"):
        text = path.read_text(encoding="utf-8")
        new = fix(text)
        if new != text:
            path.write_text(new, encoding="utf-8", newline="\n")
            n += 1
    print(f"fixed {n}")


if __name__ == "__main__":
    main()
