package hr.algebra.controller;

import hr.algebra.factory.RepositoryFactory;
import hr.algebra.model.GameEngine;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
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

    private Color FIRST_PLAYER_COLOR = Color.RED;
    private Color SECOND_PLAYER_COLOR = Color.GREENYELLOW;

    private static final double INTERVAL = 0.5; // seconds

    private static final String JAVA = "java";
    private static final String SRC_PATH = "src/";
    private static final String GAME_OVER = "Game over";
    private static final String SNAKE_CRASHED = "Snake crashed";
    private static final String SNAKE_HAS_CRASHED = "Snake has crashed";
    private static final String GAME_ENGINE_IS_NOT_DESERIALIZED = "GameEngine is not deserialized";

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initRepository();
        initGameEngine();
        initGraphicsContext();
    }

    private void initRepository() {
        try {
            repository = RepositoryFactory.getRepository(FileRepository.class.getName());
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            Logger.getLogger(GameBoardController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initGameEngine() {
        gameEngine = new GameEngine(repository.getFirstPlayer());
    }

    private void initGraphicsContext() {
        gc = canvas.getGraphicsContext2D();
    }

    @FXML
    private void startGame() {
        initTimeline();
        initSceneEventHandler();
    }

    private void initTimeline() {
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(INTERVAL), e -> {
                    try {
                        gameEngine.draw(gc, gameEngine.getFirstPlayer(), KeyCode.META, FIRST_PLAYER_COLOR);
                    } catch (Exception ex) {
                        stopGame();
                        MessageUtils.showInfoMessage(GAME_OVER, SNAKE_CRASHED, SNAKE_HAS_CRASHED);
                    }
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void initSceneEventHandler() {
        canvas.getScene().setOnKeyPressed((KeyEvent event) -> {
            try {
                gameEngine.draw(gc, gameEngine.getFirstPlayer(), event.getCode(), FIRST_PLAYER_COLOR);
            } catch (Exception ex) {
                stopGame();
                MessageUtils.showInfoMessage(GAME_OVER, SNAKE_CRASHED, SNAKE_HAS_CRASHED);
            }
        });
    }

    @FXML
    private void resetGame() {
        stopGame();
        clearCanvas();
        initGameEngine();
    }

    private void stopGame() {
        timeline.stop();
        canvas.getScene().setOnKeyPressed(null);
    }

    private void clearCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    @FXML
    private void loadGame() {
        try {
            gameEngine = deserializeGameEngine();
            gameEngine.drawAllPositions(gc, gameEngine.getFirstPlayer(), FIRST_PLAYER_COLOR);
            initTimeline();
            initSceneEventHandler();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(GameBoardController.class.getName()).log(Level.SEVERE, null, ex);
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
}
