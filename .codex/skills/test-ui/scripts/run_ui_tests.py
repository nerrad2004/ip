#!/usr/bin/env python3
"""Run fail-fast console UI tests described in test/ui-test-plan.md."""

from __future__ import annotations

import argparse
import difflib
import json
import re
import subprocess
import sys
import tempfile
from dataclasses import dataclass
from pathlib import Path


TEST_HEADING = re.compile(r"^## Test case:\s*(.+?)\s*$", re.MULTILINE)


@dataclass(frozen=True)
class TestStep:
    """One chatbot command and its exact expected response lines."""

    command: str
    expected_output: list[str]


@dataclass(frozen=True)
class TestCase:
    """A named UI scenario containing an aim and ordered command steps."""

    name: str
    aim: str
    steps: list[TestStep]


class PlanError(ValueError):
    """Raised when the Markdown test plan does not follow the required format."""


def parse_args() -> argparse.Namespace:
    """Parse command-line options for locating and running the chatbot."""
    parser = argparse.ArgumentParser(
        description="Compile a Java 25 chatbot and run fail-fast console UI tests."
    )
    parser.add_argument(
        "--repo",
        type=Path,
        default=Path.cwd(),
        help="repository root (default: current directory)",
    )
    parser.add_argument(
        "--plan",
        type=Path,
        default=Path("test/ui-test-plan.md"),
        help="test plan path, relative to the repository root by default",
    )
    parser.add_argument(
        "--source-dir",
        type=Path,
        default=Path("src/main/java"),
        help="Java source directory, relative to the repository root by default",
    )
    parser.add_argument(
        "--main-class",
        default="Nerrad",
        help="fully qualified Java main class (default: Nerrad)",
    )
    parser.add_argument(
        "--prompt",
        default="You: ",
        help="text printed immediately before each command is read",
    )
    parser.add_argument(
        "--timeout",
        type=float,
        default=10.0,
        help="maximum seconds allowed for one test case (default: 10)",
    )
    return parser.parse_args()


def resolve_from_repo(repo: Path, path: Path) -> Path:
    """Resolve a possibly relative path against the repository root."""
    return path if path.is_absolute() else repo / path


def parse_plan(plan_path: Path) -> list[TestCase]:
    """Parse and validate test cases from the Markdown plan."""
    text = plan_path.read_text(encoding="utf-8")
    matches = list(TEST_HEADING.finditer(text))
    if not matches:
        raise PlanError("No '## Test case:' sections were found.")

    test_cases: list[TestCase] = []
    for index, match in enumerate(matches):
        name = match.group(1)
        section_end = matches[index + 1].start() if index + 1 < len(matches) else len(text)
        section = text[match.end():section_end]

        aim_match = re.search(
            r"^### Aim\s*$\n(.*?)(?=^###\s|\Z)", section, re.MULTILINE | re.DOTALL
        )
        if not aim_match or not aim_match.group(1).strip():
            raise PlanError(f"Test case '{name}' must have a non-empty '### Aim'.")
        aim = " ".join(aim_match.group(1).strip().splitlines())

        steps_match = re.search(
            r"^### Commands and expected outputs\s*$\n\s*```json\s*\n(.*?)^```\s*$",
            section,
            re.MULTILINE | re.DOTALL,
        )
        if not steps_match:
            raise PlanError(
                f"Test case '{name}' must contain a JSON code block under "
                "'### Commands and expected outputs'."
            )

        try:
            raw_steps = json.loads(steps_match.group(1))
        except json.JSONDecodeError as error:
            raise PlanError(f"Invalid JSON in test case '{name}': {error}") from error

        if not isinstance(raw_steps, list) or not raw_steps:
            raise PlanError(f"Test case '{name}' must contain at least one command.")

        steps: list[TestStep] = []
        for step_number, raw_step in enumerate(raw_steps, start=1):
            if not isinstance(raw_step, dict):
                raise PlanError(f"Test case '{name}', step {step_number} must be an object.")
            command = raw_step.get("command")
            expected = raw_step.get("expected_output")
            if not isinstance(command, str):
                raise PlanError(
                    f"Test case '{name}', step {step_number}: 'command' must be a string."
                )
            if not isinstance(expected, list) or not all(
                isinstance(line, str) for line in expected
            ):
                raise PlanError(
                    f"Test case '{name}', step {step_number}: "
                    "'expected_output' must be a list of strings."
                )
            steps.append(TestStep(command=command, expected_output=expected))

        test_cases.append(TestCase(name=name, aim=aim, steps=steps))

    return test_cases


def command_version(command: str) -> str:
    """Return combined version output for a Java executable."""
    result = subprocess.run(
        [command, "-version"],
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
        check=False,
    )
    if result.returncode != 0:
        raise RuntimeError(f"Could not run {command} -version: {result.stderr.strip()}")
    return (result.stdout + result.stderr).strip()


def require_java_25() -> None:
    """Fail clearly unless both runtime and compiler report Java 25."""
    for command in ("java", "javac"):
        version = command_version(command)
        if not re.search(r"(?:version\s+\"?|javac\s+)25(?:[.\"\s]|$)", version):
            raise RuntimeError(
                f"{command} must use Java 25, but reported:\n{version}"
            )


def compile_sources(source_dir: Path, build_dir: Path) -> None:
    """Compile every Java source beneath source_dir into build_dir."""
    sources = sorted(source_dir.rglob("*.java"))
    if not sources:
        raise RuntimeError(f"No Java sources found under {source_dir}")
    result = subprocess.run(
        ["javac", "-d", str(build_dir), *(str(source) for source in sources)],
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
        check=False,
    )
    if result.returncode != 0:
        details = (result.stdout + result.stderr).strip()
        raise RuntimeError(f"Compilation failed:\n{details}")


def normalize_newlines(text: str) -> str:
    """Normalize platform-specific line endings without changing other spacing."""
    return text.replace("\r\n", "\n").replace("\r", "\n")


def response_lines(response: str) -> list[str]:
    """Convert a command response to exact comparable lines."""
    normalized = normalize_newlines(response)
    if normalized.endswith("\n"):
        normalized = normalized[:-1]
    return normalized.split("\n")


def print_transcript(test_case: TestCase, output: str) -> None:
    """Print a complete record of supplied input and captured console output."""
    print(f"\n=== Test case: {test_case.name} ===")
    print(f"Aim: {test_case.aim}")
    print("--- Console input ---")
    for step in test_case.steps:
        print(step.command)
    print("--- Console output ---")
    print(normalize_newlines(output), end="" if output.endswith(("\n", "\r")) else "\n")
    print("--- End transcript ---")


def print_failure(
    test_case: TestCase,
    step_number: int,
    step: TestStep,
    actual_lines: list[str],
) -> None:
    """Report the first mismatch with readable actual and expected output."""
    print(f"FAIL: {test_case.name}, command {step_number}: {step.command!r}")
    print("Expected output lines:")
    print(json.dumps(step.expected_output, indent=2))
    print("Actual output lines:")
    print(json.dumps(actual_lines, indent=2))
    print("Difference (expected -> actual):")
    difference = difflib.unified_diff(
        step.expected_output,
        actual_lines,
        fromfile="expected",
        tofile="actual",
        lineterm="",
    )
    print("\n".join(difference))


def run_test_case(
    test_case: TestCase,
    build_dir: Path,
    main_class: str,
    prompt: str,
    timeout: float,
) -> bool:
    """Run one fresh chatbot process and compare every command response."""
    console_input = "\n".join(step.command for step in test_case.steps) + "\n"
    try:
        result = subprocess.run(
            ["java", "-cp", str(build_dir), main_class],
            input=console_input,
            capture_output=True,
            text=True,
            encoding="utf-8",
            errors="replace",
            timeout=timeout,
            check=False,
        )
    except subprocess.TimeoutExpired as error:
        captured = (error.stdout or "") + (error.stderr or "")
        print_transcript(test_case, captured)
        print(f"FAIL: test case exceeded the {timeout:g}-second timeout.")
        return False

    output = result.stdout + result.stderr
    print_transcript(test_case, output)
    if result.returncode != 0:
        print(f"FAIL: chatbot exited with status {result.returncode}.")
        return False

    sections = normalize_newlines(output).split(prompt)
    responses = sections[1:]
    if len(responses) != len(test_case.steps):
        print(
            "FAIL: expected "
            f"{len(test_case.steps)} prompt(s), but found {len(responses)}. "
            f"Check the configured prompt {prompt!r}."
        )
        return False

    for step_number, (step, response) in enumerate(
        zip(test_case.steps, responses), start=1
    ):
        actual_lines = response_lines(response)
        if actual_lines != step.expected_output:
            print_failure(test_case, step_number, step, actual_lines)
            return False

    print("PASS")
    return True


def main() -> int:
    """Load the plan, compile once, and stop at the first failed test case."""
    args = parse_args()
    repo = args.repo.resolve()
    plan_path = resolve_from_repo(repo, args.plan).resolve()
    source_dir = resolve_from_repo(repo, args.source_dir).resolve()

    try:
        test_cases = parse_plan(plan_path)
        require_java_25()
        with tempfile.TemporaryDirectory(prefix="nerrad-ui-test-") as temp_dir:
            build_dir = Path(temp_dir)
            compile_sources(source_dir, build_dir)
            print(f"Loaded {len(test_cases)} test case(s) from {plan_path}")
            for test_case in test_cases:
                if not run_test_case(
                    test_case,
                    build_dir,
                    args.main_class,
                    args.prompt,
                    args.timeout,
                ):
                    print("Test session terminated after the first failure.")
                    return 1
    except (OSError, PlanError, RuntimeError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        return 2

    print(f"\nAll {len(test_cases)} test case(s) passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
