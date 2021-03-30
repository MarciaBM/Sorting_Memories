package file;

import java.io.File;
import java.time.LocalDateTime;

public class VideoPropertiesClass extends FilePropertiesClass implements VideoProperties{
    public VideoPropertiesClass(File file, boolean toDelete, boolean seen, LocalDateTime date) {
        super(file, toDelete, seen, date);
    }
}
