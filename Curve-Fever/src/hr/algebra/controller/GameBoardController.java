package hr.algebra.controller;

import hr.algebra.model.GameEngine;
import hr.algebra.model.Orientation;
import hr.algebra.model.Player;
import hr.algebra.model.Position;
import hr.algebra.model.Snake;
import hr.algebra.utils.MessageUtils;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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

    private Timeline timeline = null;
    private Player player = null;
    private GameEngine gameEngine = null;
    private GraphicsContext gc = null;

    private static final double INTERVAL = 0.75; // seconds

    private static final int SNAKE_WIDTH = 4;
    private static final int INITIAL_POSITION_X = 10;
    private static final int INITIAL_POSITION_Y = 250;

    private static final String GAME_OVER = "Game over";
    private static final String SNAKE_CRASHED = "Snake crashed";
    private static final String SNAKE_HAS_CRASHED = "Snake has crashed";

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initGraphicsContext();
        initGameEngine();
        initPlayer();
    }

    private void initGraphicsContext() {
        gc = canvas.getGraphicsContext2D();
    }

    private void initGameEngine() {
        gameEngine = new GameEngine();
    }

    private void initPlayer() {
        player = new Player(
                new Snake(
                        SNAKE_WIDTH,
                        Color.RED,
                        new ArrayList<Position>(Arrays.asList(new Position(INITIAL_POSITION_X, INITIAL_POSITION_Y)))
                ),
                Orientation.HORIZONTAL_RIGHT
        );
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
                        gameEngine.draw(gc, player, KeyCode.META);
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
                gameEngine.draw(gc, player, event.getCode());
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
        initPlayer();
    }

    private void stopGame() {
        timeline.stop();
        canvas.getScene().setOnKeyPressed(null);
    }

    private void clearCanvas() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
}
