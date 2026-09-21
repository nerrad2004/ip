package nerrad.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import nerrad.loan.Loan;
import nerrad.loan.LoanType;
import nerrad.task.Todo;

/**
 * Tests the user-facing messages assembled by {@link Ui}.
 */
class UiTest {
    private final Ui ui = new Ui();

    @Test
    void getTaskAndLoanListMessages_displayItemsWithOneBasedNumbers() {
        Todo todo = new Todo("read book");
        Loan loan = new Loan(LoanType.BORROWED, "Ben", new BigDecimal("5"), "bus");

        assertEquals("  Here are the tasks in your list:\n  1.[T][ ] read book",
                ui.getTaskListMessage(List.of(todo)));
        assertEquals("  Here are your loan records:\n  1.[BORROWED][OUTSTANDING] Ben: S$5.00 (bus)",
                ui.getLoanListMessage(List.of(loan)));
    }

    @Test
    void getErrorMessage_prefixesUserFacingErrors() {
        assertEquals("  OOPS!!! An example error.", ui.getErrorMessage("An example error."));
    }
}
