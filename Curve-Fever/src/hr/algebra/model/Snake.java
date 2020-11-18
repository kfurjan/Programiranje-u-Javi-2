package hr.algebra.model;

import java.util.List;
import javafx.scene.paint.Color;

/**
 *
 * @author efurkev
 */
public class Snake {

    private int lineWidth;
    private Color color;
    private List<Position> positions;

    public Snake(int lineWidth, Color color) {
        this.lineWidth = lineWidth;
        this.color = color;
    }

    public Snake(int lineWidth, Color color, List<Position> positions) {
        this(lineWidth, color);
        this.positions = positions;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
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
}
