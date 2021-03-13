import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.img_hash.AverageHash;
import org.opencv.img_hash.ImgHashBase;
import org.opencv.imgcodecs.Imgcodecs;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final int PANEL_HEIGHT = 600;
    private static final float PROPORTION_MARGIN=0.02f;
    private static final float AR16_9 = (float)16/9;
    private static final float AR9_16 = (float)9/16;
    private static final float AR4_3 = (float)4/3;
    private static final float AR3_4 = (float)3/4;
    private static final float AR3_2 = (float)3/2;
    private static final float AR2_3 = (float)2/3;
    private static final float AR1_1 = 1.0f;
    private static final float AR_OTHER = -1.0f;
    private static final float AR6_10 = 0.6f;

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
        images.put(AR16_9, new ArrayList<>());
        images.put(AR9_16, new ArrayList<>());
        images.put(AR4_3, new ArrayList<>());
        images.put(AR3_4, new ArrayList<>());
        images.put(AR3_2,new ArrayList<>());
        images.put(AR2_3,new ArrayList<>());
        images.put(AR6_10,new ArrayList<>());
        images.put(AR1_1, new ArrayList<>());
        images.put(AR_OTHER, new ArrayList<>());
        return images;
    }

    private static List<Integer> printStages(Scanner in, Iterator<List<FileProperties>> images, int sumVideos){
        System.out.println("Images:");
        int n = 1, sum=0;
        while(images.hasNext()) {
            int size=images.next().size();
            System.out.println("Stage " + n + ": " + size + " files.");
            n++;
            sum+=size;
        }
        System.out.println("Total images: " + sum);
        System.out.println("Stage V - Total videos and others: " + sumVideos + "\n");
        System.out.println("Which stages do you want to run? (Separate numbers with spaces or 'A' to run all of them including videos):");

        while(true) {
            String choice = in.nextLine().trim();
            if (choice.matches("[1-9 ]+|A|V")){
                List<Integer> result = new ArrayList<>();
                if(choice.equals("A"))
                    result.add(-1);
                else if(choice.equals("V"))
                    result.add(-2);
                else{
                    String[] answer = choice.split(" ");
                    for (String s : answer) result.add(Integer.parseInt(s));
                }
                return result;
            }
        }
    }

    private static void loadLib(){
        File lib = null;
        if(System.getProperty("os.name").toLowerCase().contains("win"))
            lib=new File("opencv_java451.dll");
        else if(System.getProperty("os.name").toLowerCase().contains("mac"))
            System.out.println("Library unavailable, sorry.");
        else
            lib=new File("libopencv_java451.so");

        System.load(lib.getAbsolutePath());
    }

    private static void getMaps(Scanner in,File folder){
        loadLib();
        ImgHashBase ihb = AverageHash.create();
        Map<Float, List<FileProperties>> images = populateMapsWithAspectRatio();
        Map<Long, List<FileProperties>> videos = new HashMap<>();
        int sumVideos=0;

        System.out.println("WARNING: We will divide the images whole process into 9 stages, so how this is a long process you can execute them separately.\n" +
                " The files you'll choose to delete will be moved to a folder named 'to delete', this is a security procedure,\n so at the end you will only have to delete the folder." +
                " If you already have a folder with this name please rename it.");
        System.out.println("Loading files...");
        for(File f:folder.listFiles()) {
            Iterator<File> it = getFile(f, new ArrayList<>());
            while (it.hasNext()) {
                File file=it.next();
                if(getMimeType(file).equals("image")) {
                    Dimension d = getImageDimension(file);
                    if(d!=null) {
                        float proportion = (float) d.height / (float) d.width, valueInMap = AR_OTHER;
                        Set<Float> aspectRatios = images.keySet();

                        for (float aR : aspectRatios)
                            if (Math.abs(proportion - aR) <= PROPORTION_MARGIN)
                                valueInMap = aR;

                        images.get(valueInMap).add(new FileProperties(file, false, false,d,getExifDate(file)));

                    }
                }else if(!getMimeType(file).equals("")){
                    sumVideos++;
                    long size=file.length();
                    if(!videos.containsKey(size))
                        videos.put(size,new ArrayList<>());
                    videos.get(size).add(new FileProperties(file,false,false,null,getExifDate(file)));
                }
            }
        }
        List<Integer> result=printStages(in,images.values().iterator(),sumVideos);
        int percentage=getPercentage(in);
        deleteDuplicatedFiles(in,images.values().iterator(),result,folder,true,percentage,ihb);
        if(!System.getProperty("os.name").toLowerCase().contains("win") && (result.contains(-1) || result.contains(-2)))
            deleteDuplicatedFiles(in,videos.values().iterator(),result,folder,false,percentage,ihb);
    }

    public static Dimension getImageDimension(File imgFile){
        int pos = imgFile.getName().lastIndexOf(".");
        try {
            if (pos == -1)
                return null;
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

    private static void getHash (ImgHashBase ihb, Mat mat, Mat hash){
        try {
            ihb.compute(mat,hash);
        } catch (CvException e){
            hash.release();
        }
    }

    private static void deleteDuplicatedFiles2(List<FileProperties> files, FileProperties fileP, boolean isImage,
                                               Mat hash1, int percentage, int j, List<FileProperties> toDelete, ImgHashBase ihb){

        FileProperties secondFileP = files.get(j);
        if (!secondFileP.getSeen() && !secondFileP.getToDelete()) {
            boolean cantCompare = false;
            if (fileP.getDate() != null && secondFileP.getDate() != null)
                if (TimeUnit.DAYS.convert(Math.abs(fileP.getDate().getNano() - secondFileP.getDate().getNano()), TimeUnit.NANOSECONDS) > 1)
                    cantCompare = true;
            if (isImage) {
                if (Math.abs(fileP.getProportion() - secondFileP.getProportion()) <= 0.02f && !cantCompare) {
                    if (secondFileP.getHash()!=null && hash1!=null) {
                        if (100.0 - (ihb.compare(hash1,secondFileP.getHash()) * 100.0 / 64.0) >= percentage) {
                            toDelete.add(secondFileP);
                            secondFileP.setSeen(true);
                        }
                    }
                }
            } else {
                if (!cantCompare)
                    if (isDuplicatedVideo(fileP.getFile(), secondFileP.getFile())) {
                        toDelete.add(secondFileP);
                        secondFileP.setSeen(true);
                    }
            }
        }
    }

    private static void deleteDuplicatedFiles(Scanner in,Iterator<List<FileProperties>> iterator, List<Integer> stages, File folder,boolean isImage,int percentage, ImgHashBase ihb){

        int n, stage=0;
        FileProperties fileP;
        Mat mat, hash = new Mat(), aux;
        List<FileProperties> toDelete = new ArrayList<>();

        while(iterator.hasNext()){
            stage++;
            n=0;
            List<FileProperties> files = iterator.next();
            if(stages.contains(stage) || stages.contains(-1) || !isImage) {
                if(isImage) {
                    System.out.println("Stage " + stage + ": loading data (this might take a while)");
                    for(FileProperties fp:files){
                        mat = Imgcodecs.imread(fp.getFile().getAbsolutePath());
                        getHash(ihb,mat,hash);
                        if(!hash.empty()) {
                            aux=hash;
                            fp.setHash(aux);
                        }
                        mat.release();
                    }
                }else
                    System.out.println("Videos and others:");

                for (int i = 0; i < files.size() - 1; i++) {
                    fileP = files.get(i);
                    n++;
                    System.out.println("File: " + n);
                    if (!fileP.getSeen() && !fileP.getToDelete()) {
                        toDelete.clear();
                        toDelete.add(fileP);
                        for (int j = i+1; j < files.size(); j++)
                            deleteDuplicatedFiles2(files,fileP,isImage,fileP.getHash(), percentage,j,toDelete,ihb);
                        chooseToDelete(in, toDelete, files, isImage);
                    }

                }
                deleteFiles(files,folder);
            }
        }
    }

    private static LocalDateTime getExifDate(File file) {
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

    private static void getPicture(FileProperties file){
        int width = (int)(file.getDimension().getWidth()*PANEL_HEIGHT/file.getDimension().getHeight());
        Image image = new ImageIcon(file.getFile().getAbsolutePath()).getImage();
        JPanel jPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, width, PANEL_HEIGHT,this);
            }
        };
        JFrame f = new JFrame();
        f.setSize(new Dimension(width, PANEL_HEIGHT));
        f.setTitle(file.getFile().getAbsolutePath());
        f.add(jPanel);
        f.setVisible(true);
    }

    private static void chooseToDelete(Scanner in,List<FileProperties> toDelete, List<FileProperties> files, boolean isImage){
        if (toDelete.size() > 1) {
            System.out.println("Duplicated files have been found, please choose the ones you want to delete (separate them with space):");
            for (int m = 0; m < toDelete.size(); m++) {
                int aux = m + 1;
                System.out.println(aux + ": " + toDelete.get(m).getFile().getAbsolutePath());
                if(isImage)
                    getPicture(toDelete.get(m));
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
                            if(index>=toDelete.size() || index <0){
                                accepted=false;
                                break;
                            }
                            files.get(getIndex(files,toDelete.get(index).getFile())).setToDelete(true);
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

    private static void deleteFiles(List<FileProperties> files, File folder){
        boolean found = false;
        for (FileProperties key : files) {
            if (key.getToDelete()) {
                String toDeletePath=folder+File.separator+"to delete";
                new File(toDeletePath).mkdirs(); //create folders
                key.getFile().renameTo(new File(toDeletePath+File.separator+key.getFile().getName()));
                found=true;
            }
        }
        if(!found)
            System.out.println("There aren't any duplicated files");
    }

    public static boolean isNameFolderAYear(String nameFolder){
        try {
            int year = Integer.parseInt(nameFolder);
            if(year>=1800 && year<=LocalDateTime.now().getYear())
                return true;
        }catch(NumberFormatException e){
            return false;
        }
        return false;
    }

    public static String gotOrganized(File file, File folder, String folderName,String[] pathFile, boolean isDateNull){
        if(!file.getAbsolutePath().contains(folder.getAbsolutePath() + File.separator + folderName+File.separator) &&
                !file.getAbsolutePath().contains(folder.getAbsolutePath() + File.separator + "not organize"+File.separator)){
            Path concatenatedPath = getConcatenatedPath(pathFile, folder.getName(), folderName);
            pathFile[1]=pathFile[1].replaceAll("\\\\","/");
            if(!isDateNull || !isNameFolderAYear(pathFile[1].split("/")[1])) {
                String finalPath = concatenatedPath.getParent().toString();
                if (finalPath != null) {
                    new File(finalPath).mkdirs(); //create folders
                    file.renameTo(new File(concatenatedPath.toString()));
                    return concatenatedPath.toString();
                }
            }
        }
        return null;
    }

    public static void organizeFiles(Scanner in, File folder) throws IOException {

        System.out.println("WARNING: if you want that some photos or folders to NOT being organized by year,\nput them on a folder named 'not organize' in the root folder" +
                "\n(press any key to continue).");
        in.next();
        List<String> moved=new ArrayList<>();
        for (File f : folder.listFiles()) {
            Iterator<File> it = getFile(f, new ArrayList<>());
            while (it.hasNext()) {
                File file = it.next();
                LocalDateTime dateFile = getExifDate(file);
                String[] pathFile = file.getPath().split(folder.getName());

                String path;
                if (dateFile != null){
                    path = gotOrganized(file, folder, String.valueOf(dateFile.getYear()), pathFile, false);
                }else {
                    path = gotOrganized(file, folder, "Unknown date", pathFile, true);
                }
                if(path!=null)
                    moved.add(path);
            }
        }
        System.out.println(moved.size()+" photos were organized, new paths:");
        for(String s:moved)
            System.out.println(s);
    }


    private static Path getConcatenatedPath(String[] pathFile, String folderName, String middleName) {
        return Paths.get(pathFile[0] + folderName + File.separator + middleName + File.separator + pathFile[1]);
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

            String command ="";
            while(!command.equals("E")){
                System.out.println("Choose what you want to do:");
                System.out.println("[1] - Delete empty folders");
                System.out.println("[2] - Delete duplicated files");
                System.out.println("[3] - Organize files by year");
                System.out.println("[E] - Exit");
                command = in.next();

                switch (command) {
                    case "1":
                        System.out.println(deleteEmptyFolders(folder, 0) + " empty folders were deleted.");
                        break;
                    case "2":
                        folder = new File(directory);
                        getMaps(in, folder);
                        break;
                    case "3":
                        folder = new File(directory);
                        organizeFiles(in, folder);
                        deleteEmptyFolders(folder, 0);

                        break;
                    case "E":
                        System.out.println("Hope you come back :)");
                        break;
                    default:
                        System.out.println("Invalid command");
                }
            }

        } catch (ScriptException | IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

class FileProperties{
    private boolean toDelete;
    private boolean seen;
    private File file;
    private Dimension dimension;
    private LocalDateTime date;
    private Mat hash;

    public FileProperties(File file, boolean toDelete, boolean seen, Dimension dimension, LocalDateTime date) {
        this.seen = seen;
        this.toDelete = toDelete;
        this.file=file;
        this.dimension=dimension;
        this.date=date;
        hash=null;
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
        return (float)dimension.height/(float)dimension.width;
    }

    public Dimension getDimension(){
        return dimension;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Mat getHash(){
        return hash;
    }

    public void setHash(Mat hash){
        this.hash=hash;
    }
}

class ScriptException extends Exception {
    public ScriptException(String message) {
        super(message);
    }
}
