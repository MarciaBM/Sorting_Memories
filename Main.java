import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    private static String getMimeType(File f)  {
        try {
            String mimetype = Files.probeContentType(f.toPath());
            if(mimetype == null)
                return "";
            return (mimetype.split("/")[0]);
        } catch (IOException e) {
            return "";
        }
    }

    private static boolean isDuplicatedImage(BufferedImage first, BufferedImage second, int userPercentage) {
        try {
            if (first.getHeight() > second.getHeight())
                first = resizeImage(first, second.getWidth(), second.getHeight());
            else if (first.getHeight() < second.getHeight())
                second = resizeImage(second, first.getWidth(), first.getHeight());
            else if(first.getWidth() > second.getWidth())
                first = resizeImage(first, second.getWidth(), second.getHeight());
            else if (first.getWidth() < second.getWidth())
                second = resizeImage(second, first.getWidth(), first.getHeight());

            return getPhotoPercentage(first,second) >= userPercentage;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isDuplicatedVideo(File current, File secondFile) {
        String mimeType1=getMimeType(current), mimeType2=getMimeType(secondFile);
        if(!mimeType1.equals(mimeType2))
            return false;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("diff", current.getAbsolutePath(), secondFile.getAbsolutePath());
            Process process = processBuilder.start();
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<String> result = new ArrayList<>();

            while ((line = reader.readLine()) != null)
                result.add(line);

            return result.size() == 0;
        } catch (IOException e) {
            return false;
        }
    }

    //code by geeksforgeeks.org
    private static double getPhotoPercentage(BufferedImage first,BufferedImage second){
        int width1 = first.getWidth();
        int height1 = first.getHeight();

        long difference = 0;
        for (int y = 0; y < height1; y++) {
            for (int x = 0; x < width1; x++) {
                int rgbA = first.getRGB(x, y);
                int rgbB = second.getRGB(x, y);
                int redA = (rgbA >> 16) & 0xff;
                int greenA = (rgbA >> 8) & 0xff;
                int blueA = (rgbA) & 0xff;
                int redB = (rgbB >> 16) & 0xff;
                int greenB = (rgbB >> 8) & 0xff;
                int blueB = (rgbB) & 0xff;
                difference += Math.abs(redA - redB);
                difference += Math.abs(greenA - greenB);
                difference += Math.abs(blueA - blueB);
            }
        }

        double total_pixels = width1 * height1 * 3;
        double avg_different_pixels = difference /
                total_pixels;

        double percentage = (avg_different_pixels /
                255) * 100;
        return 100-percentage;
    }

    private static int deleteEmptyFolders(File folder, int counter) {
        if (folder.isDirectory()) {
            if(folder.listFiles().length > 0) {
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

    private static Iterator<File> getFile(File file, List<File> recursiveFileList) {
        if(file.isFile()) {
            recursiveFileList.add(file);
        }else{
            for(File f:file.listFiles())
                getFile(f,recursiveFileList);
        }
        return recursiveFileList.iterator();
    }

    private static Map<Float, List<FileProperties>> populateMapsWithAspectRatio(){
        Map<Float, List<FileProperties>> images = new HashMap<>();
        images.put((float)16/9, new ArrayList<>());
        images.put((float)9/16, new ArrayList<>());
        images.put((float)4/3, new ArrayList<>());
        images.put((float)3/4, new ArrayList<>());
        images.put((float)3/2,new ArrayList<>());
        images.put((float)2/3,new ArrayList<>());
        images.put(0.6f,new ArrayList<>());
        images.put(1.00f, new ArrayList<>());
        images.put(-1.00f, new ArrayList<>());
        return images;
    }

    private static void getMaps(Scanner in,File folder){
        Map<Float, List<FileProperties>> images = populateMapsWithAspectRatio();
        Map<Long, List<FileProperties>> videos = new HashMap<>();
        int i = 0;
        for(File f:folder.listFiles()) {
            Iterator<File> it = getFile(f, new ArrayList<>());
            while (it.hasNext()) {
                i++;
                System.out.println(i);
                File file=it.next();
                if(getMimeType(file).equals("image")) {
                    Dimension d = getImageDimension(file);
                    if(d!=null){
                        float proportion = (float)d.height/(float)d.width, valueInMap = -1.00f;
                        final float marginProportion = 0.02f;
                        Set<Float> aspectRatios = images.keySet();

                        for (float aR : aspectRatios)
                            if(Math.abs(proportion-aR)<=marginProportion)
                                valueInMap = aR;

                        images.get(valueInMap).add(new FileProperties(file, false, false,proportion,getExifDate(file)));
                    }
                }else if(!getMimeType(file).equals("")){
                    long size=file.length();
                    if(!videos.containsKey(size))
                        videos.put(size,new ArrayList<>());
                    videos.get(size).add(new FileProperties(file,false,false,-1.0f,getExifDate(file)));
                }
            }
        }
        int percentage=getPercentage(in);
        deleteDuplicatedFiles(in,images.values().iterator(),true,percentage);
        if(!System.getProperty("os.name").toLowerCase().contains("win"))
            deleteDuplicatedFiles(in,videos.values().iterator(),false,percentage);
    }

    public static Dimension getImageDimension(File imgFile){
        int pos = imgFile.getName().lastIndexOf(".");
        try {
            if (pos == -1)
                throw new IOException("No extension for file: " + imgFile.getAbsolutePath());
            String suffix = imgFile.getName().substring(pos + 1);
            Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
            if (iter.hasNext()) {
                ImageReader reader = iter.next();
                ImageInputStream stream = new FileImageInputStream(imgFile);
                reader.setInput(stream);
                int width = reader.getWidth(reader.getMinIndex());
                int height = reader.getHeight(reader.getMinIndex());
                reader.dispose();
                return new Dimension(width, height);
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    private static void deleteDuplicatedFiles(Scanner in,Iterator<List<FileProperties>> iterator,boolean isImage,int percentage){
        int n = 0;
        while(iterator.hasNext()){
            List<FileProperties> files = iterator.next();
            for(int i = 0;i<files.size()-1;i++) {
                FileProperties fileP = files.get(i);
                n++;
                System.out.println("File: " + n);
                try {
                    BufferedImage first = null;
                    if(isImage)
                        first = ImageIO.read(fileP.getFile());
                    if(first != null || !isImage) {
                        if (!files.get(i).getSeen() && !files.get(i).getToDelete()) {
                            List<File> toDelete = new ArrayList<>();
                            toDelete.add(fileP.getFile());
                            for (int j = i + 1; j < files.size(); j++) {
                                System.out.println("File " + n + ": " + j + "/" + files.size());
                                FileProperties secondFileP = files.get(j);
                                if (!files.get(j).getSeen() && !files.get(j).getToDelete()) {
                                    boolean cantCompare = false;
                                    if (fileP.getDate()!=null && secondFileP.getDate()!=null)
                                        if(TimeUnit.DAYS.convert(Math.abs(fileP.getDate().getTime()-secondFileP.getDate().getTime()), TimeUnit.MILLISECONDS)>1) {
                                            cantCompare = true;
                                            System.out.println("yeeeeeeeeeeeei");
                                        }
                                    if (isImage) {
                                        if(Math.abs(files.get(i).getProportion()-files.get(j).getProportion())<=0.02f && !cantCompare) {
                                            if (isDuplicatedImage(first, ImageIO.read(secondFileP.getFile()), percentage)) {
                                                toDelete.add(secondFileP.getFile());
                                                files.get(j).setSeen(true);
                                            }
                                        }
                                    } else {
                                        if(!cantCompare)
                                            if (isDuplicatedVideo(fileP.getFile(), secondFileP.getFile())) {
                                                toDelete.add(secondFileP.getFile());
                                                files.get(j).setSeen(true);
                                            }
                                    }
                                }
                            }
                            chooseToDelete(in, toDelete, files);
                        }
                    }
                } catch (IOException ignored) { }
            }
            deleteFiles(files);
        }
    }

    private static Date getExifDate(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (directory != null)
                return directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            else
                return null;
        } catch (ImageProcessingException | IOException ex) {
            return null;
        }
    }

    private static int getPercentage(Scanner in){
        System.out.println("Enter the equality percentage to compare the photos:");
        while(true) {
            String answer = in.nextLine().trim();
            if(answer.matches("[0-9]+")){
                int percentage = Integer.parseInt(answer);
                if(percentage >=0 && percentage <=100)
                    return percentage;
            }
        }
    }

    private static void chooseToDelete(Scanner in,List<File> toDelete, List<FileProperties> files){
        if (toDelete.size() > 1) {
            System.out.println("Duplicated files have been found, please choose the ones you want to delete (separate them with space):");
            for (int m = 0; m < toDelete.size(); m++) {
                int aux = m + 1;
                System.out.println(aux + ": " + toDelete.get(m).getAbsolutePath());
            }
            System.out.println("E: Keep them all");
            //user
            boolean accepted=false;
            while(!accepted){
                String answer = in.nextLine().trim();
                if (answer.matches("[0-9 ]+|E")) {
                    accepted = true;
                    if (!answer.equals("E")) {
                        String[] numbers = answer.split(" ");
                        for (String s : numbers) {
                            int index = Integer.parseInt(s) - 1;
                            if(index>=files.size() || index <0){
                                accepted=false;
                                break;
                            }
                            files.get(getIndex(files,toDelete.get(index))).setToDelete(true);
                        }
                    }
                }
            }
        }
    }

    private static int getIndex(List<FileProperties> files,File file){
        for(int i = 0;i<files.size();i++){
            if(files.get(i).getFile()==file)
                return i;
        }
        return 0;
    }

    private static void deleteFiles(List<FileProperties> files){
        boolean found = false;
        for (FileProperties key : files) {
            if (key.getToDelete()) {
                key.getFile().delete();
                found=true;
            }
        }
        if(!found)
            System.out.println("There aren't any duplicated files");
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

    public static void main(String[] args) {

        try {
            Scanner in = new Scanner(System.in);
            if(args.length == 0)
                throw new ScriptException("Please insert a directory");
            if(args.length > 1)
                throw new ScriptException("Upss too many arguments!");

            String directory = args[0];
            File folder = new File(directory);

            if(!folder.isDirectory())
                throw new ScriptException("Please insert a folder's directory");

            //delete empty folders
            System.out.println(deleteEmptyFolders(folder,0) + " empty folders were deleted.");

            //delete duplicated files
            folder = new File(directory);
            getMaps(in, folder);

            System.out.println(deleteEmptyFolders(folder, 0) + " empty folders were deleted.");

        } catch (ScriptException e) {
            System.out.println(e.getMessage());
        }
    }
}

class FileProperties{
    private boolean toDelete;
    private boolean seen;
    private File file;
    private float proportion;
    private Date date;

    public FileProperties(File file,boolean toDelete, boolean seen, float proportion, Date date){
        this.seen = seen;
        this.toDelete = toDelete;
        this.file=file;
        this.proportion = proportion;
        this.date=date;
    }

    public boolean getToDelete(){
        return toDelete;
    }

    public boolean getSeen(){
        return seen;
    }

    public void setToDelete(boolean toDelete){
        this.toDelete = toDelete;
    }

    public void setSeen(boolean seen){
        this.seen = seen;
    }

    public File getFile(){
        return file;
    }

    public float getProportion(){
        return proportion;
    }

    public Date getDate(){
        return date;
    }
}

class ScriptException extends Exception{
    public ScriptException(String message){
        super(message);
    }
}
