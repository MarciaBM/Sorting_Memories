import duplicatedFiles.DuplicatedFiles;
import duplicatedFiles.DuplicatedFilesClass;
import duplicatedFiles.OSType;
import file.FileProperties;
import file.Tools;
import organizeFiles.Organizer;
import organizeFiles.OrganizerClass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final int MAX_PROGRESS_BAR = 50;
    private static final String CONFIRMATION_EMPTY_FOLDERS = " empty folders were deleted.";
    private static final String WARNING_ORGANIZING = "WARNING: if you want that some photos or folders to NOT being organized by year,\nput them on a folder named 'not organize' in the root folder" +
            "\n(press any key to continue).";
    private static final String ORGANIZING = "Organizing photos...";
    private static final String ORGANIZED = " photos were organized";
    private static final String PERCENTAGE = "Enter the equality percentage to compare the photos:";
    private static final String IMAGES = "Images:";
    private static final String TOTAL_IMAGES = "Total images: ";
    private static final String STAGE_V = "Stage V - Total videos and others: %d\n\n";
    private static final String ASK_STAGES = "Which stages do you want to run? (Separate numbers with spaces or 'A' to run all of except videos):";
    private static final String STAGE = "Stage %d: %d files.\n";
    private static final String CHOOSE_APP = "Please choose an application to open pictures\n(if the photos don't show up reboot the program and choose another app):";
    private static final String DESKTOP = "\\.desktop";
    private static final String APP = "[%d] - %s\n";
    private static final String VIDEOS = "Videos and others:";
    private static final String LOADING_STAGE = "Stage %d: loading data (this might take a while)\n";
    private static final String INVALID_COMM = "Invalid command";
    private static final String COME_BACK="Hope you come back :)";
    private static final String INSERT_DIRECTORY="Please insert a directory";
    private static final String MANY_ARGUMENTS="Upss too many arguments.";
    private static final String INSERT_FOLDER="Please insert a folder's directory";
    private static final String MENU="Choose what you want to do:\n[1] - Delete empty folders\n[2] - Delete duplicated files\n[3] - Organize files by year\n[E] - Exit";
    private static final String LOADING_FILES="Loading files...";
    private static final String WARNING_STAGES="WARNING: We will divide the images whole process into 9 stages, so how this is a long process you can execute them separately.\n" +
            " The files you'll choose to delete will be moved to a folder named 'to delete', this is a security procedure,\n so at the end you will only have to delete the folder." +
            " If you already have a folder with this name please rename it.";
    private static final String DUPLICATED_FILES="\nDuplicated files have been found, please choose the ones you want to delete (separate them with space):";
    private static final String KEEP="K: Keep them all";
    private static final String DELETE= "D: Delete them all";

    private static void deleteEmptyFolders(File folder) {
        System.out.println(Tools.deleteEmptyFolders(folder, 0) + CONFIRMATION_EMPTY_FOLDERS);
    }

    private static void organizeFiles(Scanner in, Organizer organizer) throws IOException {
        System.out.println(WARNING_ORGANIZING);
        in.next();
        int counter = 0;
        System.out.println(ORGANIZING);
        Iterator<String> it = organizer.organizeFiles();

        while (it.hasNext()) {
            System.out.println(it.next());
            counter++;
        }

        System.out.println(counter + ORGANIZED);
    }

    private static int getPercentage(Scanner in) {
        System.out.println(PERCENTAGE);
        while (true) {
            String answer = in.nextLine().trim();
            if (answer.matches("[0-9]+")) {
                int percentage = Integer.parseInt(answer);
                if (percentage >= 0 && percentage <= 100)
                    return percentage;
            }
        }
    }

    private static void printStages(Scanner in, DuplicatedFiles df, int sumVideos) {
        System.out.println(IMAGES);
        int n = 1, sum = 0;
        Iterator<List<FileProperties>> it = df.getAllImages();
        while (it.hasNext()) {
            int size = it.next().size();
            System.out.printf(STAGE, n, size);
            n++;
            sum += size;
        }
        System.out.println(TOTAL_IMAGES + sum);
        System.out.printf(STAGE_V, sumVideos);
        System.out.println(ASK_STAGES);
        boolean accepted = false;
        while (!accepted) {
            String choice = in.nextLine().trim();
            if (choice.matches("[1-9 ]+|A|V|a|v")) {
                accepted = true;
                if (choice.equalsIgnoreCase("A")) {
                    df.setIsImage(true);
                    df.addStage(-1);
                } else if (choice.equalsIgnoreCase("V")) {
                    df.setIsImage(false);
                } else {
                    df.setIsImage(true);
                    String[] answer = choice.split(" ");
                    for (String s : answer) df.addStage(Integer.parseInt(s));
                }
            }
        }
    }

    private static String progressBar(int actual, int maxLength) {
        StringBuilder progressBar = new StringBuilder();

        for (var i = 0; i < MAX_PROGRESS_BAR; i++) // set default values
            if (i == 0)
                progressBar.append("[");
            else if (i == MAX_PROGRESS_BAR - 1)
                progressBar.append("]");
            else
                progressBar.append("-");

        int conversionToScale = (actual * MAX_PROGRESS_BAR) / maxLength;

        progressBar.append("\t").append((conversionToScale * 100) / MAX_PROGRESS_BAR).append(" % \t");

        for (int x = 0; x < conversionToScale; x++)
            if (x > 0)
                progressBar.setCharAt(x, '█');

        return "\r" + progressBar;
    }

    private static void chooseImagesApp(Scanner in, DuplicatedFiles df) throws IOException {
        df.permissionApp();
        Iterator<String> it = df.apps();
        List<String> list = new ArrayList<>();
        System.out.println(CHOOSE_APP);
        int counter = 0;
        while (it.hasNext()) {
            String app = it.next().split(DESKTOP)[0];
            if (!app.equals("")) {
                counter++;
                list.add(app);
                System.out.printf(APP, counter, app);
            }
        }
        boolean accepted = false;
        while (!accepted) {
            String answer = in.nextLine().trim();
            if (answer.matches("[1-9]")) {
                int choice = Integer.parseInt(answer);
                if (choice > 0 && choice <= list.size()) {
                    accepted = true;
                    df.setApp(list.get(choice - 1));
                }
            }
        }
    }

    private static void iteratingMaps(Scanner in, DuplicatedFiles df,
                                      int percentage) throws IOException, InterruptedException {
        int n, stage = 0;
        FileProperties fileP;
        Iterator<List<FileProperties>> it;

        if (df.getIsImage())
            it = df.getAllImages();
        else
            it = df.getAllVideos();

        while (it.hasNext()) {
            stage++;
            n = 0;
            List<FileProperties> files = it.next();
            if (df.hasStage(stage) || df.hasStage(-1) || !df.getIsImage()) {
                if (df.getIsImage()) {
                    System.out.printf(LOADING_STAGE, stage);
                    for (FileProperties fp : files) {
                        System.out.print(progressBar(files.indexOf(fp), files.size()));
                        df.definingHash(fp);
                    }
                } else
                    System.out.println(VIDEOS);

                for (int i = 0; i < files.size() - 1; i++) {
                    boolean found = false;
                    fileP = files.get(i);
                    n++;
                    System.out.print("\rFile: " + n);
                    if (!fileP.getSeen() && !fileP.getToDelete()) {
                        for (int j = i + 1; j < files.size(); j++) {
                            if (df.compareFiles(files, fileP, percentage, j))
                                found = true;
                        }
                        if (found) {
                            df.addToToDelete(fileP);
                            chooseToDelete(in, df);
                        }
                        System.gc();
                    }
                }
                df.deleteFiles();
            }
        }
    }

    private static void chooseToDelete(Scanner in, DuplicatedFiles df) throws IOException, InterruptedException {
        System.out.println(DUPLICATED_FILES);
        int i = 0, aux;
        Iterator<FileProperties> it = df.getToDelete();
        while (it.hasNext()) {
            FileProperties fp = it.next();
            aux = i + 1;
            System.out.println(aux + ": " + fp.getFile().getAbsolutePath());
            df.showPicture(fp);
            i++;
        }
        System.out.println(KEEP);
        System.out.println(DELETE);
        //user
        boolean accepted = false;
        while (!accepted) {
            String answer = in.nextLine().trim();
            accepted = df.analyzeAnswer(answer);
        }
        df.closePreviews();
    }

    private static void deleteDuplicatedFiles(Scanner in, DuplicatedFiles df) throws IOException, InterruptedException {
        System.out.println(WARNING_STAGES);
        System.out.println(LOADING_FILES);
        int sumVideos = df.deleteDuplicatedFiles();
        printStages(in, df, sumVideos);

        if (df.getIsImage()) {
            if (df.getOSType() == OSType.MACOS)
                df.setApp("open");
            else if (df.getOSType() == OSType.LINUX)
                chooseImagesApp(in, df);

            iteratingMaps(in, df, getPercentage(in));
        } else
            iteratingMaps(in, df, -1);

    }

    public static void main(String[] args) throws InterruptedException {

        try {
            Scanner in = new Scanner(System.in);
            if (args.length == 0)
                throw new ScriptException(INSERT_DIRECTORY);

            if (args.length > 1)
                throw new ScriptException(MANY_ARGUMENTS);

            String directory = args[0];
            File folder = new File(directory);

            if (!folder.isDirectory())
                throw new ScriptException(INSERT_FOLDER);

            String command = "";
            while (!command.equalsIgnoreCase("E")) {
                System.out.println(MENU);
                command = in.next();

                switch (command) {
                    case "1":
                        deleteEmptyFolders(folder);
                        break;
                    case "2":
                        deleteEmptyFolders(folder);
                        deleteDuplicatedFiles(in, new DuplicatedFilesClass(folder));
                        break;
                    case "3":
                        organizeFiles(in, new OrganizerClass(folder));
                        deleteEmptyFolders(folder);
                        break;
                    case "E":
                    case "e":
                        System.out.println(COME_BACK);
                        break;
                    default:
                        System.out.println(INVALID_COMM);
                }
            }
        } catch (ScriptException | IOException e) {
            System.out.println(e.getMessage());
        }
    }
}