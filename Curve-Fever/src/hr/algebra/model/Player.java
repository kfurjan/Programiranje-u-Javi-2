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
    private boolean crashed = false;

    public Player(Snake snake, Orientation orientation) {
        this.snake = snake;
        this.orientation = orientation;
    }

    public Player() {
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

    public boolean isCrashed() {
        return crashed;
    }

    public void setCrashed(boolean hasCrashed) {
        this.crashed = hasCrashed;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(snake);
        oos.writeObject(orientation);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.snake = (Snake) ois.readObject();
        this.orientation = (Orientation) ois.readObject();
    }

    @Override
    public String toString() {
        return "Player{" + "snake=" + snake + ", orientation=" + orientation + ", crashed=" + crashed + '}';
    }    
}
