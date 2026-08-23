# UI Test Plan

This file is the source of truth for console UI test cases run by the project-specific `test-ui` skill.

For each command, `expected_output` lists the exact response lines printed after the `You: ` prompt. Empty strings represent blank lines, and leading spaces are significant. Each test case starts a fresh instance of Nerrad and ends with `bye` so the program exits normally.

## Test case: Add tasks and list them

### Aim

Verify that Nerrad stores two task descriptions and lists both tasks as not done.

### Commands and expected outputs

```json
[
  {
    "command": "read book",
    "expected_output": [
      "",
      "  added: read book",
      "____________________________________________________________"
    ]
  },
  {
    "command": "return book",
    "expected_output": [
      "",
      "  added: return book",
      "____________________________________________________________"
    ]
  },
  {
    "command": "list",
    "expected_output": [
      "",
      "  Here are the tasks in your list:",
      "  1. [ ] read book",
      "  2. [ ] return book",
      "____________________________________________________________"
    ]
  },
  {
    "command": "bye",
    "expected_output": [
      "",
      "  Bye! Hope to see you again soon!!!",
      "____________________________________________________________"
    ]
  }
]
```

## Test case: Mark and unmark a task

### Aim

Verify that marking a task displays and stores the done status, and unmarking it reverses that status.

### Commands and expected outputs

```json
[
  {
    "command": "buy bread",
    "expected_output": [
      "",
      "  added: buy bread",
      "____________________________________________________________"
    ]
  },
  {
    "command": "mark 1",
    "expected_output": [
      "",
      "  Nice! I've marked this task as done:",
      "    [X] buy bread",
      "____________________________________________________________"
    ]
  },
  {
    "command": "list",
    "expected_output": [
      "",
      "  Here are the tasks in your list:",
      "  1. [X] buy bread",
      "____________________________________________________________"
    ]
  },
  {
    "command": "unmark 1",
    "expected_output": [
      "",
      "  OK, I've marked this task as not done yet:",
      "    [ ] buy bread",
      "____________________________________________________________"
    ]
  },
  {
    "command": "list",
    "expected_output": [
      "",
      "  Here are the tasks in your list:",
      "  1. [ ] buy bread",
      "____________________________________________________________"
    ]
  },
  {
    "command": "bye",
    "expected_output": [
      "",
      "  Bye! Hope to see you again soon!!!",
      "____________________________________________________________"
    ]
  }
]
```
