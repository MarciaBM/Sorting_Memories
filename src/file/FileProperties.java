package file;

import org.opencv.core.Mat;

import java.io.File;
import java.time.LocalDateTime;

public interface FileProperties {
    boolean getToDelete();

    void setToDelete(boolean toDelete);

    boolean getSeen();

    void setSeen(boolean seen);

    File getFile();

    LocalDateTime getDate();
}
