package javafx;

import file.Tools;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import organizeFiles.Organizer;
import organizeFiles.OrganizerClass;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;

import static javafx.Main.*;


public class AppController implements Initializable {

    private String folderPath;
    @FXML
    private Button searchPath, def, ddf;
    @FXML
    private TextField pathRootFolder;
    @FXML
    private Pane appPane;

    @FXML
    void pathChooser(ActionEvent event) {
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            Scene scene = searchPath.getScene();
            String abspath = directoryChooser.showDialog(scene.getWindow()).getAbsolutePath();
            pathRootFolder.setText(abspath);
            folderPath = abspath;
        } catch (NullPointerException e) {
            pathRootFolder.setText("No folder was selected");
            folderPath = "";
        }
    }

    private boolean checkFolderPath() {
        try {
            File f = new File(folderPath);
            if (f.isDirectory())
                return true;
            else {
                AlertDialogController.print(INSERT_FOLDER);
                return false;
            }
        } catch (NullPointerException c) {
            AlertDialogController.print(INSERT_DIRECTORY);
            return false;
        }
    }

    @FXML
    void deleteEmptyFolders(ActionEvent event) {
        if (checkFolderPath())
            AlertDialogController.print(Tools.deleteEmptyFolders(new File(folderPath), 0) + Main.CONFIRMATION_EMPTY_FOLDERS);
    }

    @FXML
    void organizeFiles(ActionEvent event) {
        if (checkFolderPath()) {
            File f = new File(folderPath);
            Organizer organizer = new OrganizerClass(f);
            AlertDialogController.print(WARNING_ORGANIZING);
            int counter = 0;
            System.out.println(ORGANIZING);
            Iterator<String> it = organizer.organizeFiles();
            while (it.hasNext()) {
                // System.out.println(it.next());
                counter++;
            }
            Tools.deleteEmptyFolders(f, 0);
            System.out.println("ola 2");
            AlertDialogController.print(counter + ORGANIZED);
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        AlertDialogController.setup(appPane);
    }
}
