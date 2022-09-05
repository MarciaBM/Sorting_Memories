package SortingMemoriesFX;

import duplicatedFiles.DuplicatedFiles;
import file.FileProperties;
import file.Tools;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SortingMemoriesController {
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

    private File folder;
    private DuplicatedFiles df;
    private Iterator<Map.Entry<Integer, CopyOnWriteArrayList<FileProperties>>> groups;
    private final List<CheckBox> checkBoxes;
    private int size;
    private int master;
    private int index;
    private Stage stage;

    private Map<CheckBox, FileProperties> filesCurrentGroup = new HashMap<>();

    @FXML
    Pane mainPane;

    @FXML
    GridPane mainMenu;

    @FXML
    Button ofby;

    @FXML
    Button ofbm;

    @FXML
    Button ofbd;

    @FXML
    Button continueButton;

    @FXML
    Button def;

    @FXML
    ImageView ddf;

    @FXML
    ImageView donate;

    @FXML
    ImageView logo;

    @FXML
    HBox searchBox;

    @FXML
    VBox progressBox;

    @FXML
    TextField rootDirectory;

    @FXML
    Button searchButton;

    @FXML
    Pane stagesPane;

    @FXML
    TextField percentageText;

    @FXML
    GridPane stages;

    @FXML
    CheckBox checkBox1;

    @FXML
    CheckBox checkBox6;

    @FXML
    CheckBox checkBox2;

    @FXML
    CheckBox checkBox3;

    @FXML
    CheckBox checkBox4;

    @FXML
    CheckBox checkBox5;

    @FXML
    CheckBox checkBox7;

    @FXML
    CheckBox checkBox8;

    @FXML
    CheckBox checkBox9;

    @FXML
    CheckBox checkBox10;

    @FXML
    Button nextGroup;

    @FXML
    Button stopHere;

    @FXML
    Text progressBarText;

    @FXML
    Text pbPercentage;
    @FXML
    ProgressBar progressBar;

    @FXML
    ImageView loadingGif;

    @FXML
    ScrollPane scrollViewImages;

    @FXML
    Pane paneChooseToDelete;

    @FXML
    Text groupsCount;


    @FXML
    VBox vboxToChoose;

    public File getFolder() {
        return folder;
    }

    public SortingMemoriesController() {
        checkBoxes = new ArrayList<>();
    }

    public ImageView getLogo() {
        return logo;
    }

    public TextField getRootDirectory() {
        return rootDirectory;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    public Button getOfby() {
        return ofby;
    }

    public Button getOfbm() {
        return ofbm;
    }

    public Button getOfbd() {
        return ofbd;
    }

    public Button getDef() {
        return def;
    }

    public ImageView getDdf() {
        return ddf;
    }

    public ImageView getDonate() {
        return donate;
    }

    @FXML
    public void searchDirectory(ActionEvent actionEvent) {
        JFileChooser f = new JFileChooser();
        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        f.showSaveDialog(null);
        folder = f.getSelectedFile();
        rootDirectory.setText(folder.getAbsolutePath());
    }

    /*Delete empty folders*/
    @FXML
    void defAction(ActionEvent event) throws ScriptException {
            checkDirectory();
            alertDialog(Alert.AlertType.INFORMATION, Tools.deleteEmptyFolders(folder, 0) + CONFIRMATION_EMPTY_FOLDERS);
    }

    /*Delete Duplicated files*/
    @FXML
    void ddfAction(ActionEvent event) throws IOException {

        // checkDirectory();
        /*    textDir.setEnabled(false);
            frame.setAlwaysOnTop(false);
            searchButton.setEnabled(false);
            panel2.setVisible(false);
            panel1.setVisible(false);
            progressBarPanel.setVisible(false);
            logPanel.setVisible(false);
            log.setText("");*/

        Optional<ButtonType> res = alertDialog(Alert.AlertType.CONFIRMATION, WARNING_STAGES);
        if (res.get() == ButtonType.OK) {
           showStages();
        }
    }

    private void showStages() throws IOException {
        mainMenu.setVisible(false);
        progressBox.setVisible(true);
        progressBarText.setText(LOADING_FILES);
        searchButton.setDisable(true);
        rootDirectory.setDisable(true);
        checkBoxes.add(checkBox1);
        checkBoxes.add(checkBox2);
        checkBoxes.add(checkBox3);
        checkBoxes.add(checkBox4);
        checkBoxes.add(checkBox5);
        checkBoxes.add(checkBox6);
        checkBoxes.add(checkBox7);
        checkBoxes.add(checkBox8);
        checkBoxes.add(checkBox9);
        checkBoxes.add(checkBox10);
        df = new DuplicatedFiles(folder);
        new Thread() {
            public void run() {
                try {
                    int sumVideos = 0;
                    //load all files metadata
                    sumVideos = deleteDuplicatedFiles();
                    int finalSumVideos = sumVideos;
                    //choosing stages
                    Platform.runLater(() -> printStages(finalSumVideos));
                    stagesPane.setVisible(true);
                    progressBox.setVisible(false);
                } catch (InterruptedException | IOException e) {
                    handleException(e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //after choosing stages
    @FXML
    void continueButtonAction(ActionEvent event) throws ScriptException {
            boolean exists = false;
            boolean all = true;
            for (int i = 0; i < checkBoxes.size() - 1; i++) {
                if (checkBoxes.get(i).isSelected())
                    exists = true;
                else
                    all = false;
            }

            if (checkBox10.isSelected()) {
                if (exists) {
                    alertDialog(Alert.AlertType.WARNING, WRONG_SELECTION);
                    stagesPane.setVisible(true);
                    return;
                }
                df.setIsImage(false);

            } else if (all) {
                df.setIsImage(true);
                df.addStage(-1);
            } else {
                df.setIsImage(true);
                for (int i = 0; i < checkBoxes.size() - 1; i++) {
                    if (checkBoxes.get(i).isSelected())
                        df.addStage(i + 1);
                }
            }

            int percentage = getPercentage();
            stagesPane.setVisible(false);
            if (df.getIsImage()) {
                iteratingMaps(percentage);
            } else {
                Platform.runLater(() -> {
                    iteratingMaps(-1);
                });
            }
    }

    @FXML
    void nextButtonAction(ActionEvent event) throws IOException {
        chooseToDelete();
    }


    /*Organize by day*/
    @FXML
    void ofbdAction(ActionEvent event) {

    }

    /*Organize by month*/
    @FXML
    void ofbmAction(ActionEvent event) {

    }

    /*Organize by year*/
    @FXML
    void ofbyAction(ActionEvent event) {

    }


    private void checkDirectory() throws ScriptException {
        if (folder == null) {
            File aux = new File(rootDirectory.getText());
            if (!aux.isDirectory()) {
                alertDialog(Alert.AlertType.WARNING, INSERT_FOLDER);
                throw new ScriptException(INSERT_FOLDER);
            } else
                folder = aux;
        }
    }

    private Optional<ButtonType> alertDialog(Alert.AlertType alertType, String text) {
        Alert alert = new Alert(alertType);
        alert.setTitle(alertType.toString());
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        return alert.showAndWait();
    }

    //when loading files metadata... first step
    private int deleteDuplicatedFiles() throws InterruptedException, IOException {
        AtomicInteger sumVideos = new AtomicInteger();
        int total = (int) df.fileCount(folder.toPath());
        AtomicInteger counter = new AtomicInteger();
        File[] array = folder.listFiles();
        assert array != null;
        List<File> files = new ArrayList<>(Arrays.asList(array));
        int processors = Runtime.getRuntime().availableProcessors() * MULTIPLICATION;
        int portion;
        if (files.size() <= processors)
            portion = 1;
        else
            portion = (files.size()) / processors;
        List<Thread> threads = new ArrayList<>();
        AtomicInteger aux = new AtomicInteger();

        //  setProgressBar(LOADING_FILES, total);
        for (int i = 0; i < Math.min(files.size(), processors); i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                try {
                    int begin = finalI * portion, end;
                    if (finalI == Math.min(files.size(), processors) - 1)
                        end = files.size();
                    else
                        end = begin + portion;
                    for (File f : files.subList(begin, end)) {
                        aux.getAndIncrement();
                        Iterator<File> it = Tools.getFile(f, new ArrayList<>());
                        while (it.hasNext()) {
                            File file = it.next();
                            counter.getAndIncrement();
                            synchronized (this) {
                                double progress = (double) counter.get() / total;
                                progressBar.setProgress(progress);
                                int p = (int) (progress * 100);
                                pbPercentage.setText(p + "%");
                            }
                            sumVideos.addAndGet(df.deleteDuplicatedFiles(file));
                        }
                    }
                }catch (IOException e){
                    handleException(e.getMessage());
                    e.printStackTrace();
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread t : threads)
            t.join();

        assert aux.get() == files.size();
        // progressBarPanel.setVisible(false);
        return sumVideos.get();
    }

    private void printStages(int sumVideos) {
        Iterator<List<FileProperties>> it = df.getAllImages();
        for (int i = 0; i < 9; i++) {
            int size = it.next().size();
            CheckBox cb = checkBoxes.get(i);
            cb.setText("Stage " + (i + 1) + ": " + size + " images");

        }
        checkBox10.setText("Stage V - Total videos and others: " + sumVideos + " files");
    }

    private int getPercentage() throws ScriptException {
        String answer = percentageText.getText();
        if (answer.matches("[0-9]+")) {
            int percentage = Integer.parseInt(answer);
            if (percentage >= 0 && percentage <= 100)
                return percentage;
            alertDialog(Alert.AlertType.WARNING, WRONG_PERCENTAGE);
            throw new ScriptException("");
        }
        alertDialog(Alert.AlertType.WARNING, WRONG_PERCENTAGE);
        throw new ScriptException("");
    }


    //loading stages...
    private void iteratingMaps(int percentage) {
        new Thread() {
            public void run() {
                try {
                    Iterator<List<FileProperties>> it;
                    progressBox.setVisible(true);
                    progressBar.setVisible(true);

                    if (df.getIsImage())
                        it = df.getAllImages();
                    else
                        it = df.getAllVideos();
                    int stage = 0;
                    FileProperties fileP;
                    while (it.hasNext()) {
                        stage++;
                        List<FileProperties> files = it.next();
                        int processors = Runtime.getRuntime().availableProcessors() * MULTIPLICATION;
                        if (df.hasStage(stage) || df.hasStage(-1) || !df.getIsImage()) {
                            System.out.println("Loading stage " + stage + "...\n");
                            progressBarText.setText("Loading stage " + stage);

                            if (df.getIsImage()) {
                                int portion;
                                if (files.size() <= processors)
                                    portion = 1;
                                else
                                    portion = (files.size()) / processors;
                                List<Thread> threads = new ArrayList<>();
                                AtomicInteger counter = new AtomicInteger();

                                for (int i = 0; i < Math.min(files.size(), processors); i++) {
                                    int finalI = i;
                                    Thread thread = new Thread(() -> {
                                        int begin = finalI * portion, end;
                                        if (finalI == Math.min(files.size(), processors) - 1)
                                            end = files.size();
                                        else
                                            end = begin + portion;
                                        for (FileProperties fp : files.subList(begin, end)) {
                                            counter.getAndIncrement();
                                            synchronized (this) {
                                                double progress = (double) counter.get() / files.size();
                                                progressBar.setProgress(progress);
                                                int p = (int) (progress * 100);
                                                pbPercentage.setText(p + "%");
                                            }
                                            try {
                                                df.definingHash(fp);
                                            } catch (IOException e) {
                                                handleException(e.getMessage());
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    threads.add(thread);
                                    thread.start();
                                }

                                for (Thread t : threads)
                                    t.join();
                                assert counter.get() == files.size();
                            }
                            System.out.println("Comparing files from stage " + stage + "...\n");
                            progressBarText.setText("Comparing files from stage " + stage);
                            for (int i = 0; i < files.size() - 1; i++) {
                                fileP = files.get(i);
                                if (!fileP.getSeen()) {
                                    df.createChoosingGroup(i);
                                    AtomicBoolean found = new AtomicBoolean(false);
                                    int portion;
                                    int nFiles = files.size() - i - 1;
                                    if (nFiles <= processors)
                                        portion = 1;
                                    else
                                        portion = nFiles / processors;
                                    List<Thread> threads = new ArrayList<>();

                                    int finalI = i;
                                    FileProperties finalFileP = fileP;
                                    AtomicInteger counter = new AtomicInteger();

                                    for (int k = 0; k < Math.min(processors, nFiles); k++) {
                                        int finalK = k;
                                        Thread thread = new Thread(() -> {
                                            int begin = finalI + 1 + (finalK * portion), end;
                                            if (finalK == Math.min(processors, nFiles) - 1)
                                                end = files.size();
                                            else
                                                end = begin + portion;

                                            for (FileProperties f : files.subList(begin, end)) {
                                                counter.getAndIncrement();
                                                if (df.compareFiles(finalFileP, f, percentage, finalI))
                                                    found.set(true);
                                            }
                                        });
                                        threads.add(thread);
                                        thread.start();
                                    }

                                    for (Thread t : threads)
                                        t.join();

                                    assert nFiles == counter.get();

                                    if (found.get())
                                        df.addToChoosingGroup(i, fileP);
                                    else
                                        df.removeChoosingGroup(i);

                                    System.gc();
                                }
                                synchronized (this) {
                                    double progress = (double) i / (files.size() - 1);
                                    progressBar.setProgress(progress);
                                    int p = (int) (progress * 100);
                                    pbPercentage.setText(p + "%");
                                }
                            }
                        }
                    }
                    groups = df.getToDelete();
                    progressBox.setVisible(false);
                    //log.setText("");
                    if (!groups.hasNext()) {
                        //logPanel.setVisible(false);
                        JOptionPane.showMessageDialog(null, "There aren't duplicated files!");
                        //textDir.setEnabled(true);
                        //searchButton.setEnabled(true);
                    } else {
                        //log.append("Files to be \"deleted\":\n");
                        System.out.println("Files to be \"deleted\":\n");
                        paneChooseToDelete.setVisible(true);
                        scrollViewImages.setVisible(true);
                        nextGroup.setVisible(true);
                        stopHere.setVisible(true);
                        index = 1;
                        Platform.runLater(() -> {
                            try {
                                chooseToDelete();
                            } catch (IOException e) {
                                handleException(e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    handleException(e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public void setStage(Stage stageC) {
        stage = stageC;
    }

    private void chooseToDelete() throws IOException {
        if (!filesCurrentGroup.isEmpty()) {
            for (Map.Entry<CheckBox, FileProperties> entry : filesCurrentGroup.entrySet()) {
                if (entry.getKey().isSelected())
                    entry.getValue().setToDelete(true);
            }
            filesCurrentGroup.clear();
        }
        if (groups.hasNext()) {
            Map.Entry<Integer, CopyOnWriteArrayList<FileProperties>> entry = groups.next();
            CopyOnWriteArrayList<FileProperties> list = entry.getValue();
            groupsCount.setVisible(true);
            groupsCount.setText("Group " + index + " of " + df.getChoosingGroupsSize());
            index++;

            VBox vBox = new VBox();
            vBox.setSpacing(30);
            for (FileProperties fp : list) {
                VBox inside = new VBox();
                long bytes = fp.getFile().length();
                long kilobytes = (bytes / 1024);
                long megabytes = (kilobytes / 1024);
                String text = fp.getFile().getAbsolutePath() + "\n" + kilobytes + "KB/" + megabytes + "MB";

                File f = fp.getFile();
                Image image = new Image(f.toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setPreserveRatio(true);
                imageView.setFitHeight(250);
                inside.getChildren().add((imageView));
                CheckBox cb = new CheckBox(text);
                filesCurrentGroup.put(cb, fp);
                inside.getChildren().add(cb);
                inside.setSpacing(10);
                vBox.getChildren().add(inside);
                scrollViewImages.setContent(vBox);
            }

        } else {
            stopSelection();
        }
    }

    @FXML
    void stopSelectionAction(ActionEvent event) throws IOException {
        stopSelection();
    }

    private void stopSelection() throws IOException {
        df.deleteFiles();
        Object[] options = {"Back to Stages", "Main Menu", "Exit"};
        int option = JOptionPane.showOptionDialog(null, "Selected files were \"deleted\"!", null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (option == 0) {
            paneChooseToDelete.setVisible(false);
            showStages();
        } else if (option == 1) {
            System.out.println(1);
        } else {
            System.exit(0);
        }
    }

    private void handleException (String message){

    }
}