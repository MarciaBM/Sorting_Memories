import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main {

    private static boolean isDuplicatedProcess(String pathFile1, String pathFile2) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("diff", pathFile1, pathFile2);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line= reader.readLine();
        return line == null;
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

    private static File getFile(File file) {
        if(file.isFile()) {
            return file;
        }else{
            File[] files=file.listFiles();
            for(File f:files)
                return getFile(f);
        }
        return null;
    }

    private static void deleteDuplicatedFiles(Scanner in,File folder) throws IOException{
        Map<File,Boolean> files = new HashMap<>();
        for(File f:folder.listFiles()) {
            Iterator<File> it = getFile(f, new ArrayList<>());
            while(it.hasNext())
                files.put(it.next(), false);
        }

        File current;
        List<File> files = new LinkedList<>();
        filesIt.forEachRemaining(files::add);

        for(int i = 0;i<files.size()-1;i++) {
            File file = list.get(i);
            if (!files.get(file)) {
                current = file;
                List<File> toDelete = new ArrayList<>();
                toDelete.add(current);

                for (int j = i + 1; j < files.size(); j++) {
                    File secondFile = list.get(j);
                    if (isDuplicatedProcess(current.getAbsolutePath(), secondFile.getAbsolutePath()))
                        toDelete.add(secondFile);
                }


                if (toDelete.size() > 1) {
                    System.out.println("Duplicated files have been found, please choose the ones you want to keep (separate them with space):");
                    for (int m = 0; m < toDelete.size(); m++) {
                        int aux = m + 1;
                        System.out.println(aux + ": " + toDelete.get(m).getAbsolutePath());
                    }
                    System.out.println("E - Keep them all");
                    String answer = in.nextLine();
                    if (!answer.equals("E")) {
                        String[] numbers = answer.split(" ");
                        for (String s : numbers) {
                            int index = Integer.parseInt(s) - 1;
                            files.put(toDelete.get(index),true);
                        }
                    }
                }
            }
        }

        Iterator<File> keys = files.keySet().iterator();
        while(keys.hasNext()){
            File key = keys.next();
            if(files.get(key))
                key.delete();
        }
    }

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        String directory = args[0];
        File folder = new File(directory);

        if(folder.isDirectory()) {

            //delete empty folders
            System.out.println(deleteEmptyFolders(folder,0) + " empty folders were deleted.");

            try {
                //real shit
                folder = new File(directory);
                deleteDuplicatedFiles(in, folder);
                System.out.println(deleteEmptyFolders(folder, 0) + " empty folders were deleted.");


            } catch (Exception e) {
                System.out.println(e);

            }
        }else
            System.out.println("Please insert a folder's directory");
    }
}
