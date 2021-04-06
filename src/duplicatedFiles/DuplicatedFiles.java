package duplicatedFiles;

import file.FileProperties;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public interface DuplicatedFiles {
    Iterator<List<FileProperties>> getAllImages();

    Iterator<List<FileProperties>> getAllVideos();

    Iterator<FileProperties> getToDelete();

    void addToToDelete(FileProperties fp);

    OSType getOSType();

    boolean getIsImage();

    void setIsImage(boolean b);

    void addStage(int stage);

    boolean hasStage(int stage);

    String getApp();

    void setApp(String app);

    int deleteDuplicatedFiles();

    Dimension getImageDimension(File imgFile);

    void definingHash(FileProperties fp);

    boolean compareFiles(List<FileProperties> files, FileProperties fileP,
                         int percentage, int j);

    void permissionApp() throws IOException;

    Iterator<String> apps() throws IOException;

    void showPicture(FileProperties fp);

    void deleteFiles();

    void closePreviews() throws IOException, InterruptedException;

    boolean analyzeAnswer(String answer);
}
