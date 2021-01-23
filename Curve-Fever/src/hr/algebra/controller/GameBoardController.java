package hr.algebra.controller;

import hr.algebra.factory.RepositoryFactory;
import hr.algebra.model.GameEngine;
import hr.algebra.model.Player;
import hr.algebra.model.PlayerType;
import hr.algebra.model.RmiType;
import hr.algebra.multicast.ClientThread;
import hr.algebra.multicast.ServerThread;
import hr.algebra.repository.FileRepository;
import hr.algebra.repository.Repository;
import hr.algebra.rmi.ChatClient;
import hr.algebra.rmi.ChatServer;
import hr.algebra.threads.ClockThread;
import hr.algebra.utils.DOMUtils;
import hr.algebra.utils.FileUtils;
import hr.algebra.utils.MessageUtils;
import hr.algebra.utils.ReflectionUtils;
import hr.algebra.utils.SerializationUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author efurkev
 */
public class GameBoardController implements Initializable {

    @FXML
    private Canvas canvas;

    private Repository repository;
    private Timeline timeline = null;
    private GameEngine gameEngine = null;
    private GraphicsContext gc = null;
    private PlayerType playerType = null;

    private ClientThread clientThread;
    private ServerThread serverThread;

    private static final double INTERVAL = 0.25; // seconds

    public String winner = null;
    private static final String FIRST = "FIRST";
    private static final String SECOND = "SECOND";
    private static final String JAVA = "java";
    private static final String SRC_PATH = "src/";
    private static final String GAME_OVER = "Game over";
    private static final String SNAKE_CRASHED = "Snake crashed";
    private static final String SNAKE_HAS_CRASHED = "Snake has crashed";
    private static final String GAME_ENGINE_IS_NOT_DESERIALIZED = "GameEngine is not deserialized";
    private static final String PLAYER_1_WON = "Player 1 won";
    private static final String PLAYER_1_WON_DESC = "Player 1 has won the game";
    private static final String PLAYER_2_WON = "Player 2 won";
    private static final String PLAYER_2_WON_DESC = "Player 2 has won the game";

    private final String TIME_FORMAT = "HH:mm:ss";
    private static final String MESSAGE_FORMAT = "%s (%s): %s";
    private static final String DATE_TIME_FORMAT = "dd. MMM. yyyy HH:mm:ss";
    private static final String SERVER_NAME = "Server";
    private static final String CLIENT_NAME = "Client";
    private static final int MESSAGE_LENGTH = 78;
    private static final int FONT_SIZE = 15;

    private RmiType rmiType;
    private ObservableList<Node> messages;

    private ChatServer chatServer;
    private ChatClient chatClient;

    @FXML
    private ScrollPane spContainer;
    @FXML
    private VBox vbMessages;
    @FXML
    private TextField tfMessage;
    @FXML
    private Button btnSend;
    @FXML
    private Label lblClock;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initRepository();
        initGraphicsContext();
        startClockThread();
    }

    private void initRepository() {
        try {
            repository = RepositoryFactory.getRepository(FileRepository.class.getName());
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            Logger.getLogger(GameBoardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initGraphicsContext() {
        gc = canvas.getGraphicsContext2D();
    }

    private void startClockThread() {
        new ClockThread(this).start();
    }

    @FXML
    private void startGame() {
        initGameEngine();
        initClientThread();
        initTimeline();
        initSceneEventHandler();
    }

    private void initGameEngine() {
        playerType = repository.getPlayerType();
        if (playerType != null) {
            switch (playerType) {
                case PLAYER_1:
                    gameEngine = new GameEngine(repository.getFirstPlayer(), null);
                    playerType = PlayerType.PLAYER_1;
                    break;
                case PLAYER_2:
                    gameEngine = new GameEngine(null, repository.getSecondPlayer());
                    playerType = PlayerType.PLAYER_2;
                    break;
                default:
            }
        }
    }

    private void initClientThread() {
        clientThread = new ClientThread(this, repository);
        clientThread.setDaemon(true);
        clientThread.start();
    }

    private void initTimeline() {
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(INTERVAL), e -> {
                    drawPlayers(KeyCode.META);
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void drawPlayers(KeyCode keyCode) {
        try {
            if (gameEngine.getFirstPlayer() != null) {
                gameEngine.draw(gc, gameEngine.getFirstPlayer(), keyCode, repository.getFirstPlayerColor());
            }
            if (gameEngine.getSecondPlayer() != null) {
                gameEngine.draw(gc, gameEngine.getSecondPlayer(), keyCode, repository.getSecondPlayerColor());
            }
            initServerThread();
            if (winner != null) {
                announceGameResult(winner);
            }
        } catch (Exception ex) {
            stopGame();
            MessageUtils.showInfoMessage(GAME_OVER, SNAKE_CRASHED, SNAKE_HAS_CRASHED);
        }
    }

    private void initServerThread() {
        serverThread = new ServerThread(this);
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void initSceneEventHandler() {
        canvas.getScene().setOnKeyPressed((KeyEvent event) -> {
            drawPlayers(event.getCode());
        });
    }

    @FXML
    public void resetGame() {
        stopGame();
        clearCanvas();
        resetGameEngine();
    }

    private void stopGame() {
        timeline.stop();
        canvas.getScene().setOnKeyPressed(null);
        clientThread.setStopThread(true);
    }

    private void resetGameEngine() {
        gameEngine = null;
    }

    private void clearCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    @FXML
    private void loadGame() {
        try {
            gameEngine = deserializeGameEngine();

            if (gameEngine.getFirstPlayer() != null) {
                gameEngine.drawAllPositions(gc, gameEngine.getFirstPlayer(), repository.getFirstPlayerColor());
            }
            if (gameEngine.getSecondPlayer() != null) {
                gameEngine.drawAllPositions(gc, gameEngine.getSecondPlayer(), repository.getSecondPlayerColor());
            }

            initTimeline();
            initSceneEventHandler();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(GameBoardController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            stopGame();
            MessageUtils.showInfoMessage(GAME_OVER, SNAKE_CRASHED, SNAKE_HAS_CRASHED);
        }
    }

    private GameEngine deserializeGameEngine() throws ClassNotFoundException, IOException {
        File file = FileUtils.uploadFileDialog(canvas.getScene().getWindow(), "ser");
        if (file != null) {
            return (GameEngine) SerializationUtils.read(file.getAbsolutePath());
        }
        throw new IOException(GAME_ENGINE_IS_NOT_DESERIALIZED);
    }

    @FXML
    private void exitGame() {
        try {
            stopGame();
            serialize();
            Platform.exit();
        } catch (IOException ex) {
            Logger.getLogger(GameBoardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void serialize() throws IOException {
        File file = FileUtils.saveFileDialog(canvas.getScene().getWindow(), "ser");
        if (file != null) {
            SerializationUtils.write(gameEngine, file.getAbsolutePath());
        }
    }

    @FXML
    private void createJavaDocs() {
        try {
            FileUtils.getAllClassPaths(SRC_PATH).forEach(path -> {

                String docName = path.substring(path.lastIndexOf("/") + 1) + "-package.html";
                String classPath = path;
                String classPackage = path.substring(path.indexOf("/") + 1).replace("/", ".").concat(".");

                try {
                    createDocumentation(docName, classPath, classPackage);
                } catch (IOException ex) {
                    Logger.getLogger(GameBoardController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

            MessageUtils.showInfoMessage("Reflection", "Docs created", "Check them!");
        } catch (IOException ex) {
            Logger.getLogger(GameBoardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createDocumentation(String docName, String classPath, String classPackage) throws IOException {

        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(docName));
                DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(classPath))) {

            StringBuilder report = new StringBuilder();
            report.append("<html>").append(System.lineSeparator()).append(System.lineSeparator());

            stream.forEach(file -> {
                String filename = file.getFileName().toString();
                if (!filename.endsWith(JAVA)) {
                    return;
                }
                String className = filename.substring(0, filename.indexOf("."));
                report
                        .append("<h1>")
                        .append(className)
                        .append("</h1>")
                        .append("</br>")
                        .append(System.lineSeparator());

                try {
                    Class<?> clazz = Class.forName(classPackage.concat(className));
                    ReflectionUtils.readClassAndMembersInfo(clazz, report);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(GameBoardController.class.getName()).log(Level.SEVERE, null, ex);
                }

                report.append(System.lineSeparator()).append(System.lineSeparator());
            });

            report.append("</html>");
            bw.write(report.toString());
        }
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }

    public GraphicsContext getGraphicsContext() {
        return gc;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public void announceGameResult(String playerName) {
        stopGame();
        clearCanvas();
        resetGameEngine();
        showWinnerMessage(playerName);
    }

    private void showWinnerMessage(String playerName) {
        if (playerName.equals(FIRST)) {
            MessageUtils.showInfoMessage(GAME_OVER, PLAYER_1_WON, PLAYER_1_WON_DESC);
        } else if (playerName.equals(SECOND)) {
            MessageUtils.showInfoMessage(GAME_OVER, PLAYER_2_WON, PLAYER_2_WON_DESC);
        }
    }

    public Optional<Player> getCurrentPlayer() {
        if (gameEngine == null) {
            return Optional.empty();
        }

        if (gameEngine.getFirstPlayer() != null) {
            return Optional.of(gameEngine.getFirstPlayer());
        } else if (gameEngine.getSecondPlayer() != null) {
            return Optional.of(gameEngine.getSecondPlayer());
        } else {
            return Optional.empty();
        }
    }

    @FXML
    private void openChat() throws IOException {
        rmiType = repository.getRmiType();
        if (rmiType != null) {
            switch (rmiType) {
                case SERVER:
                    initChatServer();
                    break;
                case CLIENT:
                    initChatClient();
                    break;
                default:
            }
        }
    }

    public void initChatServer() {
        chatServer = new ChatServer(this);
        messages = FXCollections.observableArrayList();
        Bindings.bindContentBidirectional(messages, vbMessages.getChildren());
        tfMessage.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.length() >= MESSAGE_LENGTH) {
                        ((StringProperty) observable).setValue(oldValue);
                    }
                }
        );
    }

    public void initChatClient() {
        chatClient = new ChatClient(this);
        messages = FXCollections.observableArrayList();
        Bindings.bindContentBidirectional(messages, vbMessages.getChildren());
        tfMessage.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.length() >= MESSAGE_LENGTH) {
                        ((StringProperty) observable).setValue(oldValue);
                    }
                }
        );
    }

    @FXML
    private void saveDOM() {
        if (playerType != null) {
            switch (playerType) {
                case PLAYER_1:
                    DOMUtils.savePlayer(gameEngine.getFirstPlayer());
                    playerType = PlayerType.PLAYER_1;
                    break;
                case PLAYER_2:
                    DOMUtils.savePlayer(gameEngine.getSecondPlayer());
                    break;
                default:
            }
        }
        resetGame();
    }

    @FXML
    private void loadDOM() {
        try {
            if (playerType != null) {
                switch (playerType) {
                    case PLAYER_1:
                        gameEngine = new GameEngine(DOMUtils.loadPlayer(), null);
                        gameEngine.drawAllPositions(gc, gameEngine.getFirstPlayer(), repository.getFirstPlayerColor());
                        break;
                    case PLAYER_2:
                        gameEngine = new GameEngine(null, DOMUtils.loadPlayer());
                        gameEngine.drawAllPositions(gc, gameEngine.getSecondPlayer(), repository.getSecondPlayerColor());
                        break;
                    default:
                }
                initTimeline();
                initSceneEventHandler();
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(GameBoardController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(GameBoardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void send(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            sendMessage();
        }
    }

    @FXML
    private void sendMessage() {
        if (rmiType != null) {
            switch (rmiType) {
                case SERVER:
                    if (tfMessage.getText().trim().length() > 0) {
                        chatServer.sendMessage(tfMessage.getText().trim());
                        addMessage(tfMessage.getText().trim(), SERVER_NAME, Color.BLACK);
                        tfMessage.clear();
                    }
                    break;
                case CLIENT:
                    if (tfMessage.getText().trim().length() > 0) {
                        chatClient.sendMessage(tfMessage.getText().trim());
                        addMessage(tfMessage.getText().trim(), CLIENT_NAME, Color.BLACK);
                        tfMessage.clear();
                    }
                    break;
                default:
            }
        }
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

    public void updateTime() {
        lblClock.setText(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
    }
}
