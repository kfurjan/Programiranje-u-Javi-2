package hr.algebra.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

/**
 *
 * @author efurkev
 */
public class GameEngine implements Serializable {

    private static final long serialVersionUID = 4L;

    private static final int FORWARD = 5;
    private static final int CANVAS_HEIGHT = 400;
    private static final int CANVAS_WIDTH = 1160;
    private static final String EXCEPTION_MESSAGE = "Snake is out of bounds or has crashed!";

    private Player firstPlayer;
    private Player secondPlayer;

    public GameEngine() {
    }

    public GameEngine(Player firstPlayer) {
        this.firstPlayer = firstPlayer;
    }

    public GameEngine(Player firstPlayer, Player secondPlayer) {
        this(firstPlayer);
        this.secondPlayer = secondPlayer;
    }

    public Player getFirstPlayer() {
        return firstPlayer;
    }

    public Player getSecondPlayer() {
        return secondPlayer;
    }

    public void draw(GraphicsContext gc, Player player, KeyCode keyCode, Color color) throws Exception {

        gc.setStroke(color);
        gc.setLineWidth(player.getSnake().getLineWidth());

        List<Position> positions = player.getSnake().getPositions();
        Position current = positions.get(positions.size() - 1);
        Optional<Position> next = calculateNextPostion(keyCode, player, current);

        if (next.isPresent()
                && next.get().isInBounds(CANVAS_WIDTH, CANVAS_HEIGHT)
                && !player.getSnake().hitItself(next.get())) {
            gc.strokeLine(current.getX(), current.getY(), next.get().getX(), next.get().getY());
            positions.add(next.get());
        } else {
            throw new Exception(EXCEPTION_MESSAGE);
        }
    }

    private Optional<Position> calculateNextPostion(KeyCode keyCode, Player player, Position current) {
        switch (keyCode) {
            case META:
                switch (player.getOrientation()) {
                    case HORIZONTAL_LEFT:
                        return Optional.of(new Position(current.getX() - FORWARD, current.getY()));
                    case HORIZONTAL_RIGHT:
                        return Optional.of(new Position(current.getX() + FORWARD, current.getY()));
                    case VERTICAL_TOP:
                        return Optional.of(new Position(current.getX(), current.getY() - FORWARD));
                    case VERTICAL_BOTTOM:
                        return Optional.of(new Position(current.getX(), current.getY() + FORWARD));
                    default:
                        return Optional.empty();
                }
            case LEFT:
                switch (player.getOrientation()) {
                    case HORIZONTAL_LEFT:
                        player.setOrientation(Orientation.VERTICAL_BOTTOM);
                        return Optional.of(new Position(current.getX(), current.getY() + FORWARD));
                    case HORIZONTAL_RIGHT:
                        player.setOrientation(Orientation.VERTICAL_TOP);
                        return Optional.of(new Position(current.getX(), current.getY() - FORWARD));
                    case VERTICAL_TOP:
                        player.setOrientation(Orientation.HORIZONTAL_LEFT);
                        return Optional.of(new Position(current.getX() - FORWARD, current.getY()));
                    case VERTICAL_BOTTOM:
                        player.setOrientation(Orientation.HORIZONTAL_RIGHT);
                        return Optional.of(new Position(current.getX() + FORWARD, current.getY()));
                    default:
                        return Optional.empty();
                }
            case RIGHT:
                switch (player.getOrientation()) {
                    case HORIZONTAL_LEFT:
                        player.setOrientation(Orientation.VERTICAL_TOP);
                        return Optional.of(new Position(current.getX(), current.getY() - FORWARD));
                    case HORIZONTAL_RIGHT:
                        player.setOrientation(Orientation.VERTICAL_BOTTOM);
                        return Optional.of(new Position(current.getX(), current.getY() + FORWARD));
                    case VERTICAL_TOP:
                        player.setOrientation(Orientation.HORIZONTAL_RIGHT);
                        return Optional.of(new Position(current.getX() + FORWARD, current.getY()));
                    case VERTICAL_BOTTOM:
                        player.setOrientation(Orientation.HORIZONTAL_LEFT);
                        return Optional.of(new Position(current.getX() - FORWARD, current.getY()));
                    default:
                        return Optional.empty();
                }
            default:
                return Optional.empty();
        }
    }

    public void drawAllPositions(GraphicsContext gc, Player player, Color color) throws Exception {
        gc.setStroke(color);
        gc.setLineWidth(player.getSnake().getLineWidth());
        List<Position> positions = player.getSnake().getPositions();

        for (int i = 1; i < positions.size(); i++) {
            gc.strokeLine(positions.get(i - 1).getX(), positions.get(i - 1).getY(),
                    positions.get(i).getX(), positions.get(i).getY());
        }
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(firstPlayer);
        oos.writeObject(secondPlayer);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.firstPlayer = (Player) ois.readObject();
        this.secondPlayer = (Player) ois.readObject();
    }
}
