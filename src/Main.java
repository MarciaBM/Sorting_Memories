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

    private static void deleteEmptyFolders(File folder) {
        if (folder.isDirectory()) {
            if (folder.listFiles().length == 0) {
                folder.delete();
            } else {
                File[] folders = folder.listFiles();
                for(File insideFolder : folders) {
                    deleteEmptyFolders(insideFolder);
                }
            }
        }
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

    private static void deleteDuplicatedFiles(Scanner in,Iterator<File> filesIt) throws IOException{
        File current;
        List<File> files = new LinkedList<>();
        filesIt.forEachRemaining(files::add);

        for(int i = 0;i<files.size()-1;i++){
            File file = files.get(i);
            current = file;
            List<File> toDelete = new ArrayList<>();
            toDelete.add(current);
            for(int j = i+1;j<files.size();j++){
                File secondFile=files.get(j);
                if(isDuplicatedProcess(current.getAbsolutePath(), secondFile.getAbsolutePath()))
                    toDelete.add(secondFile);
            }
            if(toDelete.size()>1) {
                System.out.println("Duplicated files have been found, please choose the ones you want to keep (separate them with space):");
                for (int m = 0; m < toDelete.size(); m++) {
                    int aux=m+1;
                    System.out.println(aux + ": " + toDelete.get(m).getAbsolutePath());
                }
                System.out.println("E - Keep them all");
                String answer = in.next();
                if(!answer.equals("E")){
                    String [] numbers = answer.split(" ");
                    for(String s:numbers){
                        int index = Integer.parseInt(s)-1;
                        toDelete.get(index).delete();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        //corrigir bugs com o mapa
        Scanner in = new Scanner(System.in);
        String directory = args[0];
        File folder = new File(directory);

        //delete empty folders
        deleteEmptyFolders(folder);

        try {
            //real shit
            folder = new File(directory);
            List<File> files = new LinkedList<>();
            for(File f:folder.listFiles())
                files.add(getFile(f));
            deleteDuplicatedFiles(in,files.iterator());

        } catch (Exception e) {
            System.out.println(e);

        }
    }
}
