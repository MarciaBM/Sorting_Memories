package javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static final String CONFIRMATION_EMPTY_FOLDERS = " empty folders were deleted.";
    public static final String WARNING_ORGANIZING = "WARNING: if you want that some photos or folders to NOT being organized by year,\nput them on a folder named 'not organize' in the root folder" +
            "\n(press any key to continue).";
    public static final String ORGANIZING = "Organizing photos...";
    public static final String ORGANIZED = " photos were organized";
    public static final String PERCENTAGE = "Enter the equality percentage to compare the photos:";
    public static final String IMAGES = "Images:";
    public static final String TOTAL_IMAGES = "Total images: ";
    public static final String STAGE_V = "Stage V - Total videos and others: %d\n\n";
    public static final String ASK_STAGES = "Which stages do you want to run? (Separate numbers with spaces or 'A' to run all of except videos):";
    public static final String STAGE = "Stage %d: %d files.\n";
    public static final String CHOOSE_APP = "Please choose an application to open pictures\n(if the photos don't show up reboot the program and choose another app):";
    public static final String DESKTOP = "\\.desktop";
    public static final String APP = "[%d] - %s\n";
    public static final String VIDEOS = "Videos and others:";
    public static final String LOADING_STAGE = "Stage %d: loading data (this might take a while)\n";
    public static final String INVALID_COMM = "Invalid command";
    public static final String COME_BACK = "Hope you come back :)";
    public static final String INSERT_DIRECTORY = "Please insert a directory";
    public static final String MANY_ARGUMENTS = "Upss too many arguments.";
    public static final String INSERT_FOLDER = "Please insert a folder's directory";
    public static final String MENU = "Choose what you want to do:\n[1] - Delete empty folders\n[2] - Delete duplicated files\n[3] - Organize files by year\n[E] - Exit";
    public static final String LOADING_FILES = "Loading files...";
    public static final String WARNING_STAGES = "WARNING: We will divide the images whole process into 9 stages, so how this is a long process you can execute them separately.\n" +
            " The files you'll choose to delete will be moved to a folder named 'to delete', this is a security procedure,\n so at the end you will only have to delete the folder." +
            " If you already have a folder with this name please rename it.";
    public static final String DUPLICATED_FILES = "\nDuplicated files have been found, please choose the ones you want to delete (separate them with space):";
    public static final String KEEP = "K: Keep them all";
    public static final String DELETE = "D: Delete them all";
    private static final int MAX_PROGRESS_BAR = 50;
    private final static int WINDOW_WIDTH = 750;
    private final static int WINDOW_HEIGHT = 200; //500

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXML/app.fxml"));
        primaryStage.setResizable(false);
        primaryStage.setTitle("Sorting Memories");
        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();
    }

}


