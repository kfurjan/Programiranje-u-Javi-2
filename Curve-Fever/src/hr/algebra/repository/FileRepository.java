package hr.algebra.repository;

import hr.algebra.model.Orientation;
import hr.algebra.model.Player;
import hr.algebra.model.PlayerType;
import hr.algebra.model.Position;
import hr.algebra.model.RmiType;
import hr.algebra.model.Snake;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;

public class FileRepository implements Repository {

    private static final int SNAKE_WIDTH = 4;
    private static final int INITIAL_POSITION_FIRST_X = 10;
    private static final int INITIAL_POSITION_SECOND_X = 1150;
    private static final int INITIAL_POSITION_Y = 250;

    private static final String PLAYER_1 = "Player1";
    private static final String PLAYER_2 = "Player2";
    private static final String PLAYERS_FILE = "players.txt";

    private static final String SERVER = "SERVER";
    private static final String CLIENT = "CLIENT";
    private static final String RMI_FILE = "rmi_types.txt";

    @Override
    public PlayerType getPlayerType() {
        try {
            Path playersPath = Paths.get(PLAYERS_FILE);

            if (!Files.exists(playersPath)) {
                Files.createFile(playersPath);
                Files.write(playersPath, Arrays.asList(PLAYER_1), StandardCharsets.UTF_8);
                return PlayerType.PLAYER_1;
            } else if (Files.readAllLines(playersPath).get(0).equals(PLAYER_1)
                    && Files.readAllLines(playersPath).size() == 1) {
                Files.write(playersPath, Arrays.asList(PLAYER_1, PLAYER_2), StandardCharsets.UTF_8);
                return PlayerType.PLAYER_2;
            }
            return null;

        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public Player getFirstPlayer() {
        return new Player(new Snake(
                SNAKE_WIDTH, new ArrayList<>(Arrays.asList(new Position(INITIAL_POSITION_FIRST_X, INITIAL_POSITION_Y)))
        ), Orientation.HORIZONTAL_RIGHT);
    }

    @Override
    public Player getSecondPlayer() {
        return new Player(new Snake(
                SNAKE_WIDTH, new ArrayList<>(Arrays.asList(new Position(INITIAL_POSITION_SECOND_X, INITIAL_POSITION_Y)))
        ), Orientation.HORIZONTAL_LEFT);
    }

    @Override
    public Color getFirstPlayerColor() {
        return Color.rgb(41, 98, 255); // material blue
    }

    @Override
    public Color getSecondPlayerColor() {
        return Color.rgb(213, 8, 0);  // material red
    }

    @Override
    public RmiType getRmiType() {
        try {
            Path rmiPath = Paths.get(RMI_FILE);

            if (!Files.exists(rmiPath)) {
                Files.createFile(rmiPath);
                Files.write(rmiPath, Arrays.asList(SERVER), StandardCharsets.UTF_8);
                return RmiType.SERVER;
            } else if (Files.readAllLines(rmiPath).get(0).equals(SERVER)
                    && Files.readAllLines(rmiPath).size() == 1) {
                Files.write(rmiPath, Arrays.asList(SERVER, CLIENT), StandardCharsets.UTF_8);
                return RmiType.CLIENT;
            }
            return null;

        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public void clearData() {
        try {
            Files.deleteIfExists(Paths.get(PLAYERS_FILE));
            Files.deleteIfExists(Paths.get(RMI_FILE));
        } catch (IOException ex) {
            Logger.getLogger(FileRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
