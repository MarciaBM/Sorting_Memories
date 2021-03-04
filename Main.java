import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;

public class Main {

    private static String getMimeType(File f) throws IOException {
        String mimetype = Files.probeContentType(f.toPath());
        return (mimetype.split("/")[0]);
    }

    private static boolean isDuplicatedProcess(File current, File secondFile, int userPercentage) throws IOException{

        ProcessBuilder processBuilder = new ProcessBuilder();
        String command, line;
        boolean mimeTypeCurrent = getMimeType(current).equals("image"),
                mimeTypeSecond = getMimeType(secondFile).equals("image");

        if(mimeTypeCurrent && mimeTypeSecond) {
            command = "idiff";
            processBuilder.command(command,"-p",current.getAbsolutePath(), secondFile.getAbsolutePath());
        } else if(mimeTypeCurrent != mimeTypeSecond) {
            return false;
        } else {
            command = "diff";
            processBuilder.command(command,current.getAbsolutePath(), secondFile.getAbsolutePath());
        }

        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<String> result = new ArrayList<>();

        while ((line = reader.readLine()) != null)
            result.add(line);

        if(command.equals("diff")) {
            return result.size() == 0;
        } else if(!(result.get(result.size()-1).equals("PASS"))){ //failure
            String firstSplitParentheses =  result.get(result.size()-2).split("\\(")[1];
            String finalSplit = firstSplitParentheses.split("\\)")[0];
            double percentage =  100 - Double.parseDouble(finalSplit.substring(0,finalSplit.length()-1));
            return percentage >= userPercentage;
        }
        return true;
    }

    private static int deleteEmptyFolders(File folder, int counter) {
        if (folder.isDirectory()) {
            if(folder.listFiles().length > 0) {
                File[] folders = folder.listFiles();
                for (File insideFolder : folders) {
                    counter = deleteEmptyFolders(insideFolder, counter);
                }
            }
            if (folder.listFiles().length == 0) {
                folder.delete();
                counter++;
            }
        }
        return counter;
    }

    private static Iterator<File> getFile(File file, List<File> recursiveFileList) {
        if(file.isFile()) {
            recursiveFileList.add(file);
        }else{
            for(File f:file.listFiles())
                getFile(f,recursiveFileList);
        }
        return recursiveFileList.iterator();
    }

    private static void deleteDuplicatedFiles(Scanner in,File folder) throws IOException{
        Map<File, FileProperties> files = new HashMap<>();
        for(File f:folder.listFiles()) {
            Iterator<File> it = getFile(f, new ArrayList<>());
            while (it.hasNext()) files.put(it.next(), new FileProperties(false, false));
        }
        File current;
        List<File> list = new ArrayList<>(files.keySet());
        int percentage = getPercentage(in);

        for(int i = 0;i<files.size()-1;i++) {
            File file = list.get(i);
            System.out.println("File: " + i + "/" + files.size());
            if (!files.get(file).getSeen() && !files.get(file).getToDelete()) {
                current = file;
                List<File> toDelete = new ArrayList<>();
                toDelete.add(current);

                for (int j = i + 1; j < files.size(); j++) {
                    File secondFile = list.get(j);
                    if(!files.get(secondFile).getSeen() && !files.get(secondFile).getToDelete()) {
                        if (isDuplicatedProcess(current, secondFile,percentage)) {
                            toDelete.add(secondFile);
                            files.get(secondFile).setSeen(true);
                        }
                    }
                }
                chooseToDelete(in,toDelete,files);
            }
        }
        deleteFiles(files);
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

    private static void chooseToDelete(Scanner in,List<File> toDelete, Map<File, FileProperties> files){
        if (toDelete.size() > 1) {
            System.out.println("Duplicated files have been found, please choose the ones you want to delete (separate them with space):");
            for (int m = 0; m < toDelete.size(); m++) {
                int aux = m + 1;
                System.out.println(aux + ": " + toDelete.get(m).getAbsolutePath());
            }
            System.out.println("E: Keep them all");
            //user
            boolean accepted=false;
            while(!accepted){
                String answer = in.nextLine().trim();
                if (answer.matches("[0-9 ]+|E")) {
                    accepted = true;
                    if (!answer.equals("E")) {
                        String[] numbers = answer.split(" ");
                        for (String s : numbers) {
                            int index = Integer.parseInt(s) - 1;
                            if(index>=files.size() || index <0){
                                accepted=false;
                                break;
                            }
                            files.get(toDelete.get(index)).setToDelete(true);
                        }
                    }
                }
            }
        }
    }

    private static void deleteFiles(Map<File, FileProperties> files){
        boolean found = false;
        for (File key : files.keySet()) {
            if (files.get(key).getToDelete()) {
                key.delete();
                found=true;
            }
        }
        if(!found)
            System.out.println("There aren't any duplicated files");
    }

    public static void main(String[] args) {

        try {
            Scanner in = new Scanner(System.in);
            if(args.length == 0)
                throw new ScriptException("Please insert a directory");
            if(args.length > 1)
                throw new ScriptException("Upss too many arguments!");

            String directory = args[0];
            File folder = new File(directory);

            if(!folder.isDirectory())
                throw new ScriptException("Please insert a folder's directory");

            //delete empty folders
            System.out.println(deleteEmptyFolders(folder,0) + " empty folders were deleted.");

            //delete duplicated files
            folder = new File(directory);
            deleteDuplicatedFiles(in, folder);

            System.out.println(deleteEmptyFolders(folder, 0) + " empty folders were deleted.");

        } catch (ScriptException | IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

class FileProperties{
    private boolean toDelete;
    private boolean seen;
    public FileProperties(boolean toDelete, boolean seen){
        this.seen = seen;
        this.toDelete = toDelete;
    }

    public boolean getToDelete(){
        return toDelete;
    }

    public boolean getSeen(){
        return seen;
    }

    public void setToDelete(boolean toDelete){
        this.toDelete = toDelete;
    }

    public void setSeen(boolean seen){
        this.seen = seen;
    }
}

class ScriptException extends Exception{
    public ScriptException(String message){
        super(message);
    }
}
