package hr.algebra.model;

/**
 *
 * @author efurkev
 */
public class Player {

    private Snake snake;
    private Orientation orientation;

    public Player(Snake snake, Orientation orientation) {
        this.snake = snake;
        this.orientation = orientation;
    }

    public Snake getSnake() {
        return snake;
    }

    public void setSnake(Snake snake) {
        this.snake = snake;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }
}
