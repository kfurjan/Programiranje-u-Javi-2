package hr.algebra.repository;

import hr.algebra.model.Player;
import hr.algebra.model.PlayerType;
import hr.algebra.model.RmiType;
import javafx.scene.paint.Color;

/**
 *
 * @author efurkev
 */
public interface Repository {

    public PlayerType getPlayerType();

    public Player getFirstPlayer();

    public Player getSecondPlayer();

    public Color getFirstPlayerColor();

    public Color getSecondPlayerColor();

    public RmiType getRmiType();

    public void clearData();
}
