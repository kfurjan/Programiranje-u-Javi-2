package hr.algebra.repository;

import hr.algebra.model.Orientation;
import hr.algebra.model.Player;
import hr.algebra.model.Position;
import hr.algebra.model.Snake;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.scene.paint.Color;

public class FileRepository implements Repository {

    private static final int SNAKE_WIDTH = 4;
    private static final int INITIAL_POSITION_FIRST_X = 10;
    private static final int INITIAL_POSITION_SECOND_X = 1150;
    private static final int INITIAL_POSITION_Y = 250;

    @Override
    public Player getFirstPlayer() {
        return new Player(new Snake(
                SNAKE_WIDTH, new ArrayList<Position>(Arrays.asList(new Position(INITIAL_POSITION_FIRST_X, INITIAL_POSITION_Y)))
        ), Orientation.HORIZONTAL_RIGHT);
    }

    @Override
    public Player getSecondPlayer() {
        return new Player(new Snake(
                SNAKE_WIDTH, new ArrayList<Position>(Arrays.asList(new Position(INITIAL_POSITION_SECOND_X, INITIAL_POSITION_Y)))
        ), Orientation.HORIZONTAL_LEFT);
    }

    @Override
    public Color getFirstPlayerColor() {
        return Color.rgb(41, 98, 255); // material blue
    }

    @Override
    public Color getSecondPlayerColor() {
        return Color.rgb(213, 8, 0);  // material red
    }
}
