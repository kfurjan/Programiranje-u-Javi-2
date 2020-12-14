package hr.algebra.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author efurkev
 */
public class Player implements Serializable {

    private static final long serialVersionUID = 3L;

    private Snake snake;
    private Orientation orientation;

    public Player(Snake snake, Orientation orientation) {
        this.snake = snake;
        this.orientation = orientation;
    }

    public Player() {}

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

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(snake);
        oos.writeObject(orientation);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.snake = (Snake) ois.readObject();
        this.orientation = (Orientation) ois.readObject();
    }
}
