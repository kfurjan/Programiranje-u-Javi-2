package hr.algebra.repository;

import hr.algebra.model.Player;

/**
 *
 * @author efurkev
 */
public interface Repository {

    public Player getFirstPlayer();

    public Player getSecondPlayer();
}
