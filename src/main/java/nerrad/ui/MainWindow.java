package nerrad.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import nerrad.Nerrad;

/**
 * Controls Nerrad's main graphical chat window.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox dialogContainer;

    @FXML
    private TextField userInput;

    @FXML
    private Button sendButton;

    private Nerrad nerrad;

    /**
     * Configures scrolling after FXML has injected the visual controls.
     */
    @FXML
    public void initialize() {
        dialogContainer.heightProperty().addListener((observable) -> scrollPane.setVvalue(1.0));
        dialogContainer.prefWidthProperty().bind(scrollPane.widthProperty().subtract(2.0));
    }

    /**
     * Injects Nerrad's command-processing logic and displays its greeting.
     *
     * @param nerrad Chatbot used to process user commands.
     */
    public void setNerrad(Nerrad nerrad) {
        this.nerrad = nerrad;
        dialogContainer.getChildren().add(DialogBox.getNerradDialog(nerrad.getWelcomeMessage()));
        if (nerrad.hasLoadingError()) {
            userInput.setDisable(true);
            sendButton.setDisable(true);
        }
    }

    /**
     * Adds a user command and Nerrad's response to the chat area.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        String response = nerrad.getResponse(input);
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input),
                DialogBox.getNerradDialog(response)
        );
        userInput.clear();
        userInput.requestFocus();

        if (nerrad.isExitCommand(input)) {
            Platform.exit();
        }
    }
}
