package hr.algebra.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author efurkev
 */
public class FileUtils {

    private static final String LOAD = "Load";
    private static final String SAVE = "Save";

    public static File uploadFileDialog(Window owner, String... ext) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(FileSystemView.getFileSystemView().getHomeDirectory());

        Stream.of(ext).forEach(e -> {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(e.toUpperCase(), "*." + e));
        });

        fileChooser.setTitle(LOAD);
        return fileChooser.showOpenDialog(owner);
    }

    public static File saveFileDialog(Window owner, String... ext) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(FileSystemView.getFileSystemView().getHomeDirectory());

        Stream.of(ext).forEach(e -> {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(e.toUpperCase(), "*." + e));
        });

        fileChooser.setTitle(SAVE);
        File file = fileChooser.showSaveDialog(owner);

        if (file == null) {
            file.createNewFile();
        }

        return file;
    }

    public static Set<String> getAllClassPaths(String directory) throws IOException {
        Set<String> paths = new HashSet<>();

        Files.walk(Paths.get(directory))
                .filter(f -> f.toString().endsWith("java"))
                .map(f -> f.toString().replace("\\", "/"))
                .map(f -> f.substring(0, f.lastIndexOf("/")))
                .forEach(paths::add);

        return paths;
    }
}
