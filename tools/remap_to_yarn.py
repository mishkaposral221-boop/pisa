#!/usr/bin/env python3
"""Remap intermediary names in Java sources to Yarn (full paths only for classes)."""
from __future__ import annotations

import re
import sys
import zipfile
from collections import defaultdict
from pathlib import Path

YARN_JAR = Path.home() / (
    ".gradle/caches/modules-2/files-2.1/net.fabricmc/yarn/1.21.11+build.5/"
    "eaa7b705bf19aaf417e5cd3796af796621e1faf7/yarn-1.21.11+build.5-v2.jar"
)
SRC_ROOT = Path(__file__).resolve().parent.parent / "src/client/java"

METHOD_RE = re.compile(r"\b(method_\d+)\b")
FIELD_RE = re.compile(r"\b(field_\d+)\b")
COMP_RE = re.compile(r"\b(comp_\d+)\b")


def load_mappings(jar_path: Path):
    classes: dict[str, str] = {}
    methods: dict[str, str] = {}
    fields: dict[str, str] = {}
    comps: dict[str, str] = {}
    method_conflicts: dict[str, set[str]] = defaultdict(set)
    field_conflicts: dict[str, set[str]] = defaultdict(set)

    with zipfile.ZipFile(jar_path) as zf:
        text = zf.read("mappings/mappings.tiny").decode("utf-8")

    for line in text.splitlines():
        if line.startswith("c\t"):
            _, inter, named, *_ = line.split("\t")
            classes[inter] = named
        elif line.startswith("\tm\t"):
            parts = line.split("\t")
            if len(parts) < 5:
                continue
            inter_method, named_method = parts[3], parts[4]
            if inter_method == named_method:
                continue
            method_conflicts[inter_method].add(named_method)
            methods.setdefault(inter_method, named_method)
        elif line.startswith("\tf\t"):
            parts = line.split("\t")
            if len(parts) < 5:
                continue
            inter_field, named_field = parts[3], parts[4]
            if inter_field == named_field:
                continue
            if inter_field.startswith("comp_"):
                comps.setdefault(inter_field, named_field)
            else:
                field_conflicts[inter_field].add(named_field)
                fields.setdefault(inter_field, named_field)

    return classes, methods, fields, comps


def build_simple_class_map(classes: dict[str, str]) -> dict[str, str]:
    """Map class_1234 -> MinecraftClient (unique per intermediary id)."""
    simple: dict[str, str] = {}
    for inter, named in classes.items():
        if not inter.startswith("net/minecraft/"):
            continue
        token = inter.split("/")[-1]
        if not token.startswith("class_"):
            continue
        simple_name = named.split("/")[-1].split("$")[-1]
        simple[token] = simple_name
    return simple


def build_class_replacements(classes: dict[str, str]) -> list[tuple[str, str]]:
    """Only replace fully-qualified intermediary paths (avoids class_242 -> wrong class)."""
    reps: list[tuple[str, str]] = []
    for inter, named in classes.items():
        if not inter.startswith("net/minecraft/"):
            continue
        inter_dot = inter.replace("/", ".")
        named_dot = named.replace("/", ".").replace("$", ".")
        if "$" in inter:
            outer, inner = inter.split("$", 1)
            outer_dot = outer.replace("/", ".")
            inner_simple = inner.split("/")[-1]
            named_outer = named.split("$")[0].replace("/", ".")
            named_inner = named.split("$")[-1].split("/")[-1]
            reps.append((f"{outer_dot}.{inner_simple}", f"{named_outer}.{named_inner}"))
        reps.append((inter_dot, named_dot))
    reps.sort(key=lambda x: len(x[0]), reverse=True)
    seen: set[str] = set()
    unique: list[tuple[str, str]] = []
    for old, new in reps:
        if old not in seen:
            seen.add(old)
            unique.append((old, new))
    return unique


CLASS_RE = re.compile(r"\b(class_\d+)\b")


def remap_file(
    content: str,
    class_reps: list[tuple[str, str]],
    simple_classes: dict[str, str],
    methods: dict[str, str],
    fields: dict[str, str],
    comps: dict[str, str],
) -> str:
    for old, new in class_reps:
        content = content.replace(old, new)

    content = CLASS_RE.sub(lambda m: simple_classes.get(m.group(1), m.group(1)), content)
    content = METHOD_RE.sub(lambda m: methods.get(m.group(1), m.group(1)), content)
    content = FIELD_RE.sub(lambda m: fields.get(m.group(1), m.group(1)), content)
    content = COMP_RE.sub(lambda m: comps.get(m.group(1), m.group(1)), content)
    return content


def main():
    jar = Path(sys.argv[1]) if len(sys.argv) > 1 else YARN_JAR
    if not jar.is_file():
        print(f"Missing yarn jar: {jar}", file=sys.stderr)
        sys.exit(1)

    classes, methods, fields, comps = load_mappings(jar)
    class_reps = build_class_replacements(classes)
    simple_classes = build_simple_class_map(classes)
    print(f"Loaded {len(classes)} classes, {len(methods)} methods, {len(fields)} fields")
    print(f"Class path replacements: {len(class_reps)}, simple tokens: {len(simple_classes)}")

    changed = 0
    for path in sorted(SRC_ROOT.rglob("*.java")):
        text = path.read_text(encoding="utf-8")
        new_text = remap_file(text, class_reps, simple_classes, methods, fields, comps)
        if new_text != text:
            path.write_text(new_text, encoding="utf-8", newline="\n")
            changed += 1
    print(f"Updated {changed} files")


if __name__ == "__main__":
    main()
