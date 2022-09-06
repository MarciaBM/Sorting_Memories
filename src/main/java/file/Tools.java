package file;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.awt.*;
import java.awt.image.*;
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
            if (folder.listFiles().length > 0) {
                File[] folders = folder.listFiles();
                for (File insideFolder : folders) {
                    counter = deleteEmptyFolders(insideFolder, counter);
                }
            }
            if (folder.listFiles().length == 0) {
                folder.delete();
                counter++;
            }
        }
        return counter;
    }

    //taken from https://stackoverflow.com/questions/48653584/cannot-load-jpeg-with-java-created-by-samsung-phone
    public static BufferedImage getBufferedImage(String filename) throws InterruptedException {
        final java.awt.Image image = Toolkit.getDefaultToolkit().createImage(filename);

        final int[] RGB_MASKS = {0xFF0000, 0xFF00, 0xFF};
        final ColorModel RGB_OPAQUE =
                new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);

        PixelGrabber pg = new PixelGrabber(image, 0, 0, -1, -1, true);
        pg.grabPixels();
        int width = pg.getWidth(), height = pg.getHeight();
        DataBuffer buffer = new DataBufferInt((int[]) pg.getPixels(), pg.getWidth() * pg.getHeight());
        WritableRaster raster = Raster.createPackedRaster(buffer, width, height, width, RGB_MASKS, null);
        return new BufferedImage(RGB_OPAQUE, raster, false, null);
    }
}
