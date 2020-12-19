package hr.algebra.controller;

import hr.algebra.factory.RepositoryFactory;
import hr.algebra.model.GameEngine;
import hr.algebra.model.Player;
import hr.algebra.multicast.ClientThread;
import hr.algebra.multicast.ServerThread;
import hr.algebra.repository.FileRepository;
import hr.algebra.repository.Repository;
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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author efurkev
 */
public class GameBoardController implements Initializable {
    
    @FXML
    private Canvas canvas;
    @FXML
    private ToggleGroup players;
    @FXML
    private RadioButton rbPlayerOne, rbPlayerTwo;
    
    private Repository repository;
    private Timeline timeline = null;
    private GameEngine gameEngine = null;
    private GraphicsContext gc = null;
    
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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initRepository();
        initGraphicsContext();
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
    
    @FXML
    private void startGame() {
        initGameEngine();
        initClientThread();
        initTimeline();
        initSceneEventHandler();
        disableRadioButtons();
    }
    
    private void initGameEngine() {
        if (rbPlayerOne.isSelected()) {
            gameEngine = new GameEngine(repository.getFirstPlayer(), null);
        } else if (rbPlayerTwo.isSelected()) {
            gameEngine = new GameEngine(null, repository.getSecondPlayer());
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
    
    private void disableRadioButtons() {
        rbPlayerOne.setDisable(true);
        rbPlayerTwo.setDisable(true);
    }
    
    @FXML
    public void resetGame() {
        stopGame();
        clearCanvas();
        resetGameEngine();
        enableRadioButtons();
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
    
    private void enableRadioButtons() {
        rbPlayerOne.setDisable(false);
        rbPlayerTwo.setDisable(false);
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
            disableRadioButtons();
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
    
    public RadioButton getRbPlayerOne() {
        return rbPlayerOne;
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
        enableRadioButtons();
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
}
