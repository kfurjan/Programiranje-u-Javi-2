package hr.algebra.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author efurkev
 */
public class Position implements Serializable {

    private static final long serialVersionUID = 2L;

    private double x;
    private double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean isInBounds(int width, int height) {
        return 0 <= this.x && this.x < width && 0 <= this.y && this.y < height;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 47 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Position other = (Position) obj;
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        return true;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeDouble(x);
        oos.writeDouble(y);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.x = ois.readDouble();
        this.y = ois.readDouble();
    }

    @Override
    public String toString() {
        return "{" +  x + ", " + y + '}';
    }
}
