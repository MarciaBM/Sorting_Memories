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

    boolean getIsWindows();

    void setIsImage(boolean b);

    boolean getIsImage();

    void addProcess(FileProperties fp);

    void addStage(int stage);

    boolean hasStage(int stage);

    int getStagesSize();

    String progressBar(int actual, int maxLength);

    String getApp();

    void setApp(String app);

    int deleteDuplicatedFiles();

    Dimension getImageDimension(File imgFile);

    void definingHash(FileProperties fp);

    boolean compareFiles(List<FileProperties> files, FileProperties fileP,
                         int percentage, int j);

    void permissionApp() throws IOException;

    Iterator<String> apps() throws IOException;

    void deleteFiles();

    void stopProcesses() throws IOException;

    boolean analyzeAnswer(String answer);
}
