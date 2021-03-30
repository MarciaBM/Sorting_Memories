package file;

import org.opencv.core.Mat;

import java.io.File;
import java.time.LocalDateTime;

public class ImagePropertiesClass extends FilePropertiesClass implements ImageProperties{

    private final float proportion;
    private Mat hash;

    public ImagePropertiesClass(File file, boolean toDelete, boolean seen, float proportion, LocalDateTime date, Mat hash) {
        super(file, toDelete, seen, date);
        this.hash = hash;
        this.proportion = proportion;
    }

    @Override
    public float getProportion() {
        return proportion;
    }

    @Override
    public Mat getHash() {
        return hash;
    }

    @Override
    public void setHash(Mat hash) {
        this.hash = hash;
    }
}
