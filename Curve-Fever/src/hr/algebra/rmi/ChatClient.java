package hr.algebra.rmi;

import com.sun.jndi.rmi.registry.RegistryContextFactory;
import hr.algebra.controller.GameBoardController;
import hr.algebra.jndi.InitialDirContextCloseable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import javax.naming.Context;
import javax.naming.NamingException;

/**
 *
 * @author efurkev
 */
public final class ChatClient {

    private static final String SERVER_NAME = "Server";
    private static final String RMI_CLIENT = "client";
    private static final String RMI_SERVER = "server";
    private static final int REMOTE_PORT = 1099;
    private static final int RANDOM_PORT_HINT = 0;

    private static final String RMI_URL = "rmi://localhost:1099";

    private ChatService client;
    private ChatService server;
    private Registry registry;

    private final GameBoardController controller;

    public ChatClient(GameBoardController chatController) {
        this.controller = chatController;
        publishClient();
        fetchServer();
    }

    public void publishClient() {
        client = new ChatService() {
            @Override
            public Color getColor() throws RemoteException {
                return Color.CHOCOLATE;
            }

            @Override
            public String getName() throws RemoteException {
                return SERVER_NAME;
            }

            @Override
            public void send(String message) throws RemoteException {
                controller.postMessage(message, getName(), getColor());
            }
        };

        try {
            registry = LocateRegistry.getRegistry(REMOTE_PORT);
            ChatService stub = (ChatService) UnicastRemoteObject.exportObject(client, RANDOM_PORT_HINT);
            registry.rebind(RMI_CLIENT, stub);

        } catch (RemoteException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void fetchServer() {
        final Hashtable<String, String> properties = new Hashtable<>();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, RegistryContextFactory.class.getName());
        properties.put(Context.PROVIDER_URL, RMI_URL);

        try (InitialDirContextCloseable context = new InitialDirContextCloseable(properties)) {
            server = (ChatService) context.lookup(RMI_SERVER);
        } catch (NamingException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendMessage(String message) {
        try {
            server.send(message);
        } catch (RemoteException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
