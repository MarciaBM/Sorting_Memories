package organizeFiles;

import file.Tools;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OrganizerClass implements Organizer {

    private final File root;

    public OrganizerClass(File root){
        this.root=root;
    }

    private boolean isNameFolderAYear(String nameFolder){
        try {
            int year = Integer.parseInt(nameFolder);
            if(year>=1800 && year<= LocalDateTime.now().getYear())
                return true;
        }catch(NumberFormatException e){
            return false;
        }
        return false;
    }

    private String gotOrganized(File file, String folderName, String[] pathFile, boolean isDateNull){
        if(!file.getAbsolutePath().contains(root.getAbsolutePath() + File.separator + folderName+File.separator) &&
                !file.getAbsolutePath().contains(root.getAbsolutePath() + File.separator + "not organize"+File.separator)){
            Path concatenatedPath = getConcatenatedPath(pathFile, root.getName(), folderName);
            pathFile[1]=pathFile[1].replaceAll("\\\\","/");
            if(!isDateNull || !isNameFolderAYear(pathFile[1].split("/")[1])) {
                String finalPath = concatenatedPath.getParent().toString();
                if (finalPath != null) {
                    new File(finalPath).mkdirs(); //create folders
                    file.renameTo(new File(concatenatedPath.toString()));
                    return concatenatedPath.toString();
                }
            }
        }
        return null;
    }

    @Override
    public Iterator<String> organizeFiles() {
        List<String> moved=new ArrayList<>();
        for (File f : root.listFiles()) {
            Iterator<File> it = Tools.getFile(f, new ArrayList<>());
            while (it.hasNext()) {
                File file = it.next();
                LocalDateTime dateFile = Tools.getExifDate(file);
                String[] pathFile = file.getPath().split(root.getName());

                String path;
                if (dateFile != null){
                    path = gotOrganized(file, String.valueOf(dateFile.getYear()), pathFile, false);
                }else {
                    path = gotOrganized(file, "Unknown date", pathFile, true);
                }
                if(path!=null)
                    moved.add(path);
            }
        }
        return moved.iterator();
    }

    private Path getConcatenatedPath(String[] pathFile, String folderName, String middleName) {
        return Paths.get(pathFile[0] + folderName + File.separator + middleName + File.separator + pathFile[1]);
    }
}
