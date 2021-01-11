package hr.algebra;

import hr.algebra.factory.RepositoryFactory;
import hr.algebra.repository.FileRepository;
import hr.algebra.repository.Repository;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author efurkev
 */
public class CurveFever extends Application {

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static final String TITLE = "Curve fever";
    private static final String VIEW_PATH = "view/GameBoard.fxml";

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(VIEW_PATH));
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        primaryStage.setTitle(TITLE);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            try {
                Repository repository = RepositoryFactory.getRepository(FileRepository.class.getName());
                repository.clearData();
                System.exit(0);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
                Logger.getLogger(CurveFever.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
