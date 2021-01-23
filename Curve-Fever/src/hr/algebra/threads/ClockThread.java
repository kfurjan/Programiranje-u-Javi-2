package hr.algebra.threads;


import hr.algebra.controller.GameBoardController;
import javafx.application.Platform;

/**
 *
 * @author efurkev
 */
public class ClockThread extends Thread {

    private final GameBoardController controller;

    public ClockThread(GameBoardController controller) {
        this.controller = controller;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            sendTime();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                return;
            }
        }
    }

    private void sendTime() {
        Platform.runLater(()-> controller.updateTime());        
    }
}
