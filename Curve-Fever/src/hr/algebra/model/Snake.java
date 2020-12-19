package hr.algebra.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author efurkev
 */
public class Snake implements Serializable {

    private static final long serialVersionUID = 1L;

    private int lineWidth;
    private List<Position> positions;

    public Snake(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public Snake(int lineWidth, List<Position> positions) {
        this(lineWidth);
        this.positions = positions;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }

    public boolean hitItself(Position position) {
        return positions.contains(position);
    }

    public boolean hitAnotherSnake(Position position, List<Position> otherPositions) {
        return otherPositions.contains(position);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeInt(lineWidth);
        oos.writeObject(new ArrayList(positions));
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.lineWidth = ois.readInt();
        this.positions = (List<Position>) ois.readObject();
    }
}
