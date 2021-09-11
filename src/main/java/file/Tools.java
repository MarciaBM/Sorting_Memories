package file;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Tools {

    public static LocalDateTime getExifDate(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (directory != null) {
                Date dateTemp = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                return dateTemp != null ? Instant.ofEpochMilli(dateTemp.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
            } else {
                return null;
            }
        } catch (ImageProcessingException | IOException ex) {
            return null;
        }
    }

    public static Iterator<File> getFile(File file, List<File> recursiveFileList) {
        if (file.isFile()) {
            recursiveFileList.add(file);
        } else {
            for (File f : file.listFiles())
                getFile(f, recursiveFileList);
        }
        return recursiveFileList.iterator();
    }

    public static int deleteEmptyFolders(File folder, int counter) {
        if (folder.isDirectory()) {
            if(folder.listFiles() != null) {
                if (folder.listFiles().length > 1) {
                    File[] folders = folder.listFiles();
                    for (File insideFolder : folders) {
                        counter = deleteEmptyFolders(insideFolder, counter);
                    }
                } else if (folder.listFiles().length == 0) {
                    folder.delete();
                    counter++;
                } else if (folder.listFiles().length == 1) {
                    File[] folders = folder.listFiles();
                    File file = folders[0];
                    if (file.isFile() && file.getName().equals("desktop.ini")) {
                        file.delete();
                        folder.delete();
                        counter++;
                    } else {
                        for (File insideFolder : folders) {
                            counter = deleteEmptyFolders(insideFolder, counter);
                        }
                    }
                }
            }
        }
        return counter;
    }
}
