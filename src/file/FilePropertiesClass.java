package file;

import java.io.File;
import java.time.LocalDateTime;

public abstract class FilePropertiesClass implements FileProperties {
    private final File file;
    private final LocalDateTime date;
    private boolean toDelete;
    private boolean seen;

    public FilePropertiesClass(File file, boolean toDelete, boolean seen, LocalDateTime date) {
        this.seen = seen;
        this.toDelete = toDelete;
        this.file = file;
        this.date = date;
    }

    @Override
    public boolean getToDelete() {
        return toDelete;
    }

    @Override
    public void setToDelete(boolean toDelete) {
        this.toDelete = toDelete;
    }

    @Override
    public boolean getSeen() {
        return seen;
    }

    @Override
    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public LocalDateTime getDate() {
        return date;
    }
}
