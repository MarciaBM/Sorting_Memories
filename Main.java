import java.io.*;
import java.util.*;

public class Main{

    private static void deleteEmptyFolders(File folder){
        System.out.println(Tools.deleteEmptyFolders(folder, 0) + " empty folders were deleted.");
    }

    private static void organizeFiles(Scanner in, Organizer organizer) throws IOException {
        System.out.println("WARNING: if you want that some photos or folders to NOT being organized by year,\nput them on a folder named 'not organize' in the root folder" +
                "\n(press any key to continue).");
        in.next();
        int counter=0;
        System.out.println("Organizing photos...");
        Iterator<String> it = organizer.organizeFiles();

        while(it.hasNext()) {
            System.out.println(it.next());
            counter++;
        }

        System.out.println(counter+" photos were organized");
    }

    private static int getPercentage(Scanner in){
        System.out.println("Enter the equality percentage to compare the photos:");
        while(true) {
            String answer = in.nextLine().trim();
            if(answer.matches("[0-9]+")){
                int percentage = Integer.parseInt(answer);
                if(percentage >=0 && percentage <=100)
                    return percentage;
            }
        }
    }

    private static void printStages(Scanner in, DuplicatedFiles df, int sumVideos){
        System.out.println("Images:");
        int n = 1, sum=0;
        Iterator<List<FileProperties>> it=df.getAllImages();
        while(it.hasNext()) {
            int size=it.next().size();
            System.out.println("Stage " + n + ": " + size + " files.");
            n++;
            sum+=size;
        }
        System.out.println("Total images: " + sum);
        System.out.println("Stage V - Total videos and others: " + sumVideos + "\n");
        System.out.println("Which stages do you want to run? (Separate numbers with spaces or 'A' to run all of except videos):");
        boolean accepted=false;
        while(!accepted) {
            String choice = in.nextLine().trim();
            if (choice.matches("[1-9 ]+|A|V")){
                accepted=true;
                if(choice.equalsIgnoreCase("A")) {
                    df.setIsImage(true);
                    df.addStage(-1);
                }else if(choice.equalsIgnoreCase("V")) {
                    df.setIsImage(false);
                }else{
                    df.setIsImage(true);
                    String[] answer = choice.split(" ");
                    for (String s : answer) df.addStage(Integer.parseInt(s));
                }
            }
        }
    }

    private static void chooseImagesApp(Scanner in, DuplicatedFiles df) throws IOException {
        df.permissionApp();
        Iterator<String> it = df.apps();
        List<String> list=new ArrayList<>();
        System.out.println("Please choose an application to open pictures\n(if the photos don't show up reboot the program and choose another app):");
        int counter=0;
        while (it.hasNext()){
            String app = it.next().split("\\.desktop")[0];
            if(!app.equals("")) {
                counter++;
                list.add(app);
                System.out.println("[" + counter + "] - " + app);
            }
        }
        boolean accepted=false;
        while(!accepted) {
            String answer = in.nextLine().trim();
            if (answer.matches("[1-9]")) {
                int choice = Integer.parseInt(answer);
                if(choice>0 && choice<=list.size()) {
                    accepted = true;
                    df.setApp(list.get(choice-1));
                }
            }
        }
    }

    private static void iteratingMaps(Scanner in, DuplicatedFiles df,
                                      int percentage) throws IOException {
        int n, stage=0;
        FileProperties fileP;
        Iterator<List<FileProperties>> it;

        if(df.getIsImage())
            it=df.getAllImages();
        else
            it=df.getAllVideos();

        while(it.hasNext()){
            stage++;
            n=0;
            List<FileProperties> files = it.next();
            if(df.hasStage(stage) || df.hasStage(-1) || !df.getIsImage()) {
                if(df.getIsImage()) {
                    System.out.println("Stage " + stage + ": loading data (this might take a while)");
                    for(FileProperties fp:files){
                        System.out.print(df.progressBar(files.indexOf(fp), files.size()));
                        df.definingHash(fp);
                    }
                }else
                    System.out.println("Videos and others:");

                for (int i = 0; i < files.size() - 1; i++) {
                    boolean found=false;
                    fileP = files.get(i);
                    n++;
                    System.out.print("\rFile: " + n);
                    if (!fileP.getSeen() && !fileP.getToDelete()) {
                        for (int j = i+1; j < files.size(); j++) {
                            if(df.compareFiles(files, fileP, percentage, j))
                                found=true;
                        }
                        if(found) {
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

    private static void chooseToDelete(Scanner in, DuplicatedFiles df) throws IOException {
        System.out.println("\nDuplicated files have been found, please choose the ones you want to delete (separate them with space):");
        int i=0,aux;
        Iterator<FileProperties> it = df.getToDelete();
        while(it.hasNext()) {
            FileProperties fp = it.next();
            aux = i + 1;
            System.out.println(aux + ": " + fp.getFile().getAbsolutePath());
            df.addProcess(fp);
            i++;
        }
        System.out.println("K: Keep them all");
        System.out.println("D: Delete them all");
        //user
        boolean accepted=false;
        while(!accepted){
            String answer = in.nextLine().trim();
            accepted=df.analyzeAnswer(answer);
        }
        df.stopProcesses();
    }

    private static void deleteDuplicatedFiles(Scanner in, DuplicatedFiles df) throws IOException {
        System.out.println("WARNING: We will divide the images whole process into 9 stages, so how this is a long process you can execute them separately.\n" +
                " The files you'll choose to delete will be moved to a folder named 'to delete', this is a security procedure,\n so at the end you will only have to delete the folder." +
                " If you already have a folder with this name please rename it.");
        System.out.println("Loading files...");
        int sumVideos=df.deleteDuplicatedFiles();
        printStages(in,df,sumVideos);

        if(df.getIsImage()) {
            if(!df.getIsWindows())
                chooseImagesApp(in,df);
            iteratingMaps(in, df, getPercentage(in));
        }else if(!df.getIsWindows() && !df.getIsImage())
            iteratingMaps(in,df,-1);

    }

    public static void main(String[] args) {

        try {
            Scanner in = new Scanner(System.in);
            if(args.length == 0)
                throw new ScriptException("Please insert a directory");

            if(args.length > 1)
                throw new ScriptException("Upss too many arguments.");

            String directory = args[0];
            File folder = new File(directory);

            if(!folder.isDirectory())
                throw new ScriptException("Please insert a folder's directory");

            String command ="";
            while(!command.equalsIgnoreCase("E")){
                System.out.println("Choose what you want to do:");
                System.out.println("[1] - Delete empty folders");
                System.out.println("[2] - Delete duplicated files");
                System.out.println("[3] - Organize files by year");
                System.out.println("[E] - Exit");
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
                        System.out.println("Hope you come back :)");
                        break;
                    default:
                        System.out.println("Invalid command");
                }
            }
        } catch (ScriptException | IOException e) {
            System.out.println(e.getMessage());
        }
    }
}