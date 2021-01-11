package hr.algebra.controller;

import hr.algebra.rmi.ChatServer;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * FXML Controller class
 *
 * @author efurkev
 */
public class RmiServertController implements Initializable {

    private final String TIME_FORMAT = "HH:mm:ss";
    private static final String MESSAGE_FORMAT = "%s (%s): %s";
    private static final String SERVER_NAME = "Server";
    private static final int MESSAGE_LENGTH = 78;
    private static final int FONT_SIZE = 15;

    private ObservableList<Node> messages;

    private ChatServer chatServer;

    @FXML
    private TextField tfMessage;

    @FXML
    private ScrollPane spContainer;

    @FXML
    private VBox vbMessages;

    @FXML
    private void send(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            sendMessage();
        }
    }

    @FXML
    private void sendMessage() {
        if (tfMessage.getText().trim().length() > 0) {
            chatServer.sendMessage(tfMessage.getText().trim());
            addMessage(tfMessage.getText().trim(), SERVER_NAME, Color.BLACK);
            tfMessage.clear();
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chatServer = new ChatServer(this);
        messages = FXCollections.observableArrayList();
        // manipulation on messages list reflects immediately on vbMessages
        Bindings.bindContentBidirectional(messages, vbMessages.getChildren());
        // make sure text is not longer then message length
        tfMessage.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.length() >= MESSAGE_LENGTH) {
                        ((StringProperty) observable).setValue(oldValue);
                    }
                }
        );
    }

    private void addMessage(String message, String name, Color color) {
        Label label = new Label();
        label.setFont(new Font(FONT_SIZE));
        label.setTextFill(color);
        label.setText(String.format(MESSAGE_FORMAT, LocalTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT)), name, message));
        messages.add(label);
        moveScrollPane();
    }

    private void moveScrollPane() {
        spContainer.applyCss();
        spContainer.layout();
        spContainer.setVvalue(1D);
    }

    public void postMessage(String message, String name, Color color) {
        Platform.runLater(() -> addMessage(message, name, color));
    }
}
