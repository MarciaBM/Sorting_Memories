package file;

import org.opencv.core.Mat;

public interface ImageProperties extends FileProperties{

    float getProportion();

    Mat getHash();

    void setHash(Mat hash);
}
