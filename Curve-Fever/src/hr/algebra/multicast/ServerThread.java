package hr.algebra.multicast;

import hr.algebra.controller.GameBoardController;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.SerializationUtils;

/**
 *
 * @author efurkev
 */
public class ServerThread extends Thread {

    private static final Properties PROPERTIES = new Properties();
    private static final String PROPERTIES_FILE = "socket.properties";
    private static final String FIRST_PLAYER_PORT = "CLIENT_PORT";
    private static final String SECOND_PLAYER_PORT = "SERVER_PORT";
    private static final String GROUP = "GROUP";

    static {
        try {
            PROPERTIES.load(new FileInputStream(PROPERTIES_FILE));
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private final GameBoardController controller;

    public ServerThread(GameBoardController gameBoardController) {
        this.controller = gameBoardController;
    }

    @Override
    public void run() {
        String PORT = controller.getRbPlayerOne().isSelected()
                ? FIRST_PLAYER_PORT : SECOND_PLAYER_PORT;

        try (DatagramSocket server = new DatagramSocket()) {
            InetAddress groupAddress = InetAddress.getByName(PROPERTIES.getProperty(GROUP));

            byte[] buffer = SerializationUtils.serialize(controller.getGameEngine());
            DatagramPacket packet = new DatagramPacket(
                    buffer,
                    buffer.length,
                    groupAddress,
                    Integer.valueOf(PROPERTIES.getProperty(PORT))
            );

            server.send(packet);
        } catch (SocketException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
