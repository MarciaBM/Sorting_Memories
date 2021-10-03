package SortingMemoriesFX;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SortingMemories extends Application {
    private static final String CONFIRMATION_EMPTY_FOLDERS = " empty folders were deleted.";
    private static final String WARNING_ORGANIZING = "WARNING: if you want that some photos or folders to NOT being organized by year,\nput them on a folder named 'not organize' in the root folder. Continue?";
    private static final String ORGANIZING = "Organizing photos...";
    private static final String ORGANIZED = " photos were organized";
    private static final String CHOOSE_APP = "Please choose an application to open pictures\n(if the photos don't show up reboot the program and choose another app):";
    private static final String DESKTOP = "\\.desktop";
    private static final String VIDEOS = "Videos and others:";
    private static final String INSERT_FOLDER = "Please insert a folder's directory";
    private static final String LOADING_FILES = "Loading all files metadata...";
    private static final String WARNING_STAGES = "WARNING: We will divide the images whole process into 9 stages, so how this is a long process you can execute them separately.\n" +
            " The files you'll choose to delete will be moved to a folder named 'to delete', this is a security procedure,\n so at the end you will only have to delete the folder." +
            " If you already have a folder with this name please rename it. Do you want to continue?";
    private static final String WRONG_SELECTION = "Choose just images stages or just videos stages.";
    private static final String WRONG_PERCENTAGE = "Wrong percentage value.";
    private static final int MULTIPLICATION = 5;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SortingMemories.class.getResource("sorting-memories.fxml"));
        stage.setTitle("Sorting Memories!");
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.show();
        SortingMemoriesController sortingMemoriesController = fxmlLoader.getController();
        sortingMemoriesController.setStage(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}