# UI Test Plan

This file is the source of truth for console UI test cases run by the project-specific `test-ui` skill.

For each command, `expected_output` lists the exact response lines printed after the `You: ` prompt. Empty strings represent blank lines, and leading spaces are significant. Each test case starts a fresh instance of Nerrad and ends with `bye` so the program exits normally.

## Test case: Add and list a todo

### Aim

Verify that a task without a date or time is stored and displayed with the Todo type icon.

### Commands and expected outputs

```json
[
  {
    "command": "todo borrow book",
    "expected_output": [
      "",
      "  Got it. I've added this task:",
      "    [T][ ] borrow book",
      "  Now you have 1 tasks in the list.",
      "____________________________________________________________"
    ]
  },
  {
    "command": "list",
    "expected_output": [
      "",
      "  Here are the tasks in your list:",
      "  1.[T][ ] borrow book",
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

## Test case: Add and list a deadline

### Aim

Verify that a deadline stores and displays its due date/time exactly as entered.

### Commands and expected outputs

```json
[
  {
    "command": "deadline return book /by Sunday",
    "expected_output": [
      "",
      "  Got it. I've added this task:",
      "    [D][ ] return book (by: Sunday)",
      "  Now you have 1 tasks in the list.",
      "____________________________________________________________"
    ]
  },
  {
    "command": "list",
    "expected_output": [
      "",
      "  Here are the tasks in your list:",
      "  1.[D][ ] return book (by: Sunday)",
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

## Test case: Add and list an event

### Aim

Verify that an event stores and displays both its start and end date/time exactly as entered.

### Commands and expected outputs

```json
[
  {
    "command": "event project meeting /from Mon 2pm /to 4pm",
    "expected_output": [
      "",
      "  Got it. I've added this task:",
      "    [E][ ] project meeting (from: Mon 2pm to: 4pm)",
      "  Now you have 1 tasks in the list.",
      "____________________________________________________________"
    ]
  },
  {
    "command": "list",
    "expected_output": [
      "",
      "  Here are the tasks in your list:",
      "  1.[E][ ] project meeting (from: Mon 2pm to: 4pm)",
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

## Test case: Store and update mixed task types polymorphically

### Aim

Verify that todos, deadlines, and events can coexist in one Task array while retaining their type-specific displays and shared done status behavior.

### Commands and expected outputs

```json
[
  {
    "command": "todo read book",
    "expected_output": [
      "",
      "  Got it. I've added this task:",
      "    [T][ ] read book",
      "  Now you have 1 tasks in the list.",
      "____________________________________________________________"
    ]
  },
  {
    "command": "deadline return book /by June 6th",
    "expected_output": [
      "",
      "  Got it. I've added this task:",
      "    [D][ ] return book (by: June 6th)",
      "  Now you have 2 tasks in the list.",
      "____________________________________________________________"
    ]
  },
  {
    "command": "event project meeting /from Aug 6th 2pm /to 4pm",
    "expected_output": [
      "",
      "  Got it. I've added this task:",
      "    [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)",
      "  Now you have 3 tasks in the list.",
      "____________________________________________________________"
    ]
  },
  {
    "command": "mark 1",
    "expected_output": [
      "",
      "  Nice! I've marked this task as done:",
      "    [T][X] read book",
      "____________________________________________________________"
    ]
  },
  {
    "command": "mark 3",
    "expected_output": [
      "",
      "  Nice! I've marked this task as done:",
      "    [E][X] project meeting (from: Aug 6th 2pm to: 4pm)",
      "____________________________________________________________"
    ]
  },
  {
    "command": "list",
    "expected_output": [
      "",
      "  Here are the tasks in your list:",
      "  1.[T][X] read book",
      "  2.[D][ ] return book (by: June 6th)",
      "  3.[E][X] project meeting (from: Aug 6th 2pm to: 4pm)",
      "____________________________________________________________"
    ]
  },
  {
    "command": "unmark 3",
    "expected_output": [
      "",
      "  OK, I've marked this task as not done yet:",
      "    [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)",
      "____________________________________________________________"
    ]
  },
  {
    "command": "list",
    "expected_output": [
      "",
      "  Here are the tasks in your list:",
      "  1.[T][X] read book",
      "  2.[D][ ] return book (by: June 6th)",
      "  3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)",
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
