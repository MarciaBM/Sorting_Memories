import org.opencv.core.Mat;

import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;

public interface FileProperties {
    boolean getToDelete();

    boolean getSeen();

    void setToDelete(boolean toDelete);

    void setSeen(boolean seen);

    File getFile();

    float getProportion();

    LocalDateTime getDate();

    Mat getHash();

    void setHash(Mat hash);
}
