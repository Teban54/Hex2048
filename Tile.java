import java.awt.*;

public class Tile {
    private int value; // Stored in power of 2 (e.g. 10 for 1024)
    private Point pos;

    private static String[] images;

    public Tile(Point position, int initialValue) {
        pos = position;
        value = initialValue;
    }

    public Point getPos() {
        return pos;
    }
    public int getValue() {
        return value;
    }

    public void moveTo(Point newPos) {
        pos = newPos;
        // TODO: Graphics
    }

    public void upgrade() {
        value++;
        // TODO: Graphics
    }

    public void erase() {
        // For graphics in future.
    }
}
