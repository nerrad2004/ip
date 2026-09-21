package nerrad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests shared command processing used by Nerrad's console and graphical interfaces.
 */
class NerradTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void getResponse_taskCommands_updatesTasksAndFormatsReplies() {
        Nerrad nerrad = createNerrad();

        assertEquals("  Got it. I've added this task:\n"
                        + "    [T][ ] read book\n"
                        + "  Now you have 1 tasks in the list.",
                nerrad.getResponse("todo read book"));
        assertEquals("  Nice! I've marked this task as done:\n    [T][X] read book",
                nerrad.getResponse("mark 1"));
        assertEquals("  Here are the tasks in your list:\n  1.[T][X] read book",
                nerrad.getResponse("list"));
    }

    @Test
    void getResponse_invalidCommandAndBye_returnsAppropriateReplies() {
        Nerrad nerrad = createNerrad();

        assertEquals("  OOPS!!! I'm sorry, but I don't know what that means :-(",
                nerrad.getResponse("unknown"));
        assertTrue(nerrad.isExitCommand("bye"));
        assertFalse(nerrad.isExitCommand("list"));
        assertEquals("  Bye! Hope to see you again soon!!!", nerrad.getResponse("bye"));
    }

    @Test
    void getResponse_loanCommands_persistAndSettleLoan() {
        Nerrad nerrad = createNerrad();

        assertEquals("  Got it. I've recorded this loan:\n"
                        + "    [LENT][OUTSTANDING] Alex: S$12.50 (lunch)\n"
                        + "  You now have 1 loan records.",
                nerrad.getResponse("loan lend Alex 12.50 /for lunch"));
        assertEquals("  Nice! I've marked this loan as settled:\n"
                        + "    [LENT][SETTLED] Alex: S$12.50 (lunch)",
                nerrad.getResponse("settle-loan 1"));

        Nerrad restartedNerrad = createNerrad();
        assertEquals("  Here are your loan records:\n"
                        + "  1.[LENT][SETTLED] Alex: S$12.50 (lunch)",
                restartedNerrad.getResponse("loans"));
    }

    @Test
    void constructor_corruptedLoanData_reportsLoadingError() throws IOException {
        Path loanFile = temporaryDirectory.resolve("data/loans.txt");
        Files.createDirectories(loanFile.getParent());
        Files.writeString(loanFile, "L | LENT | invalid status | Alex | 10 | lunch");

        Nerrad nerrad = createNerrad();

        assertTrue(nerrad.hasLoadingError());
        assertEquals("  OOPS!!! I could not load your saved tasks.", nerrad.getWelcomeMessage());
    }

    /**
     * Creates a chatbot that saves data inside the test's temporary directory.
     *
     * @return Chatbot with isolated storage.
     */
    private Nerrad createNerrad() {
        return new Nerrad(temporaryDirectory.resolve("data/nerrad.txt").toString());
    }
}
