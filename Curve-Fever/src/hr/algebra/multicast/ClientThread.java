package hr.algebra.multicast;

import hr.algebra.controller.GameBoardController;
import hr.algebra.model.GameEngine;
import hr.algebra.model.Player;
import hr.algebra.model.PlayerType;
import hr.algebra.model.Position;
import hr.algebra.repository.Repository;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.SerializationUtils;

/**
 *
 * @author efurkev
 */
public class ClientThread extends Thread {

    private static final int BUFFER_SIZE = 8192;
    private static final Properties PROPERTIES = new Properties();
    private static final String PROPERTIES_FILE = "socket.properties";
    private static final String FIRST_PLAYER_PORT = "CLIENT_PORT";
    private static final String SECOND_PLAYER_PORT = "SERVER_PORT";
    private static final String GROUP = "GROUP";
    private static final String FIRST = "FIRST";
    private static final String SECOND = "SECOND";

    static {
        try {
            PROPERTIES.load(new FileInputStream(PROPERTIES_FILE));
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean stopThread = false;
    private final Repository repository;
    private final GameBoardController controller;

    public ClientThread(GameBoardController gameBoardController, Repository repository) {
        this.controller = gameBoardController;
        this.repository = repository;
    }

    public void setStopThread(boolean stopThread) {
        this.stopThread = stopThread;
    }

    @Override
    public void run() {
        String PORT = controller.getPlayerType() == PlayerType.PLAYER_1
                ? SECOND_PLAYER_PORT : FIRST_PLAYER_PORT;

        try (MulticastSocket client = new MulticastSocket(Integer.valueOf(PROPERTIES.getProperty(PORT)))) {
            InetAddress groupAddress = InetAddress.getByName(PROPERTIES.getProperty(GROUP));

            while (!stopThread) {
                client.joinGroup(groupAddress);

                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                client.receive(packet);
                GameEngine clientGameEngine = SerializationUtils.deserialize(packet.getData());

                if (clientGameEngine != null) {
                    drawPlayerIfExists(clientGameEngine);
                } else {
                    controller.resetGame();
                }

                client.leaveGroup(groupAddress);
            }
        } catch (SocketException | UnknownHostException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void drawPlayerIfExists(GameEngine gameEngine) throws Exception {
        Optional<Player> optCurrentPlayer = controller.getCurrentPlayer();

        if (optCurrentPlayer.isPresent()) {
            Player currentPlayer = optCurrentPlayer.get();

            if (gameEngine.getFirstPlayer() != null) {
                if (winnerExists(gameEngine.getFirstPlayer())) {
                    String playerName
                            = gameEngine.getFirstPlayer().getSnake().getPositions().size()
                            < currentPlayer.getSnake().getPositions().size()
                            ? SECOND : FIRST;

                    controller.setWinner(playerName);
                } else {
                    gameEngine.drawAllPositions(
                            controller.getGraphicsContext(),
                            gameEngine.getFirstPlayer(),
                            repository.getFirstPlayerColor()
                    );
                }
            }

            if (gameEngine.getSecondPlayer() != null) {
                if (winnerExists(gameEngine.getSecondPlayer())) {
                    String playerName
                            = gameEngine.getSecondPlayer().getSnake().getPositions().size()
                            < currentPlayer.getSnake().getPositions().size()
                            ? FIRST : SECOND;

                    controller.setWinner(playerName);
                } else {
                    gameEngine.drawAllPositions(
                            controller.getGraphicsContext(),
                            gameEngine.getSecondPlayer(),
                            repository.getSecondPlayerColor()
                    );
                }
            }
        }
    }

    private boolean winnerExists(Player clientPlayer) {
        Optional<Player> optCurrentPlayer = controller.getCurrentPlayer();

        if (optCurrentPlayer.isPresent()) {
            Player currentPlayer = optCurrentPlayer.get();
            List<Position> currentPositions = currentPlayer.getSnake().getPositions();
            Position currentPosition = currentPositions.get(currentPositions.size() - 1);

            List<Position> clientPositions = clientPlayer.getSnake().getPositions();

            if (currentPlayer.getSnake().hitAnotherSnake(currentPosition, clientPositions)) {
                return true;
            }
        }

        return false;
    }
}
