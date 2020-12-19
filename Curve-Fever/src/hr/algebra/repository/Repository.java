package hr.algebra.repository;

import hr.algebra.model.Player;
import javafx.scene.paint.Color;

/**
 *
 * @author efurkev
 */
public interface Repository {

    public Player getFirstPlayer();

    public Player getSecondPlayer();

    public Color getFirstPlayerColor();

    public Color getSecondPlayerColor();
}
