package hr.algebra.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import javafx.scene.paint.Color;

/**
 *
 * @author efurkev
 */

public interface ChatService extends Remote {
    Color getColor() throws RemoteException;
    String getName() throws RemoteException;
    void send(String message) throws RemoteException;
}