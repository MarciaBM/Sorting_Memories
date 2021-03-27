package duplicatedFiles;

import file.FileProperties;
import file.FilePropertiesClass;
import file.Tools;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.img_hash.AverageHash;
import org.opencv.img_hash.ImgHashBase;
import org.opencv.imgcodecs.Imgcodecs;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DuplicatedFilesClass implements DuplicatedFiles {
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
    private static final int MAX_PROGRESS_BAR = 50;

    private final ImgHashBase ihb;
    private final Map<Float, List<FileProperties>> images;
    private final Map<Long, List<FileProperties>> videos;
    private final File root;
    private final boolean isWindows;
    private final List<Process> processes;
    private boolean isImage;
    private final List<Integer> stages;
    private final List<FileProperties> toDelete;
    private String app;


    public DuplicatedFilesClass(File root){
        isWindows= System.getProperty("os.name").toLowerCase().contains("win");
        loadLib();
        this.root=root;
        app=null;
        ihb=AverageHash.create();
        videos = new HashMap<>();
        images = new HashMap<>();
        processes= new LinkedList<>();
        stages=new ArrayList<>();
        toDelete = new LinkedList<>();
        images.put(AR16_9, new ArrayList<>());
        images.put(AR9_16, new ArrayList<>());
        images.put(AR4_3, new ArrayList<>());
        images.put(AR3_4, new ArrayList<>());
        images.put(AR3_2,new ArrayList<>());
        images.put(AR2_3,new ArrayList<>());
        images.put(AR6_10,new ArrayList<>());
        images.put(AR1_1, new ArrayList<>());
        images.put(AR_OTHER, new ArrayList<>());

    }

    @Override
    public Iterator<List<FileProperties>> getAllImages(){
        return images.values().iterator();
    }

    @Override
    public Iterator<List<FileProperties>> getAllVideos(){
        return videos.values().iterator();
    }

    @Override
    public Iterator<FileProperties> getToDelete(){
        return toDelete.iterator();
    }

    @Override
    public void addToToDelete(FileProperties fp){
        toDelete.add(fp);
    }

    @Override
    public boolean getIsWindows(){
        return isWindows;
    }

    @Override
    public void setIsImage(boolean b){
        isImage=b;
    }

    @Override
    public boolean getIsImage(){
        return isImage;
    }

    @Override
    public void addProcess(FileProperties fp){
        if(isImage)
            processes.add(showPicture(fp));
    }

    @Override
    public void addStage(int stage){
        stages.add(stage);
    }

    @Override
    public boolean hasStage(int stage){
        return stages.contains(stage);
    }

    @Override
    public String progressBar(int actual, int maxLength){
        StringBuilder progressBar = new StringBuilder();

        for(var i = 0; i<MAX_PROGRESS_BAR; i++) // set default values
            if(i==0)
                progressBar.append("[");
            else if(i==MAX_PROGRESS_BAR-1)
                progressBar.append("]");
            else
                progressBar.append("-");

        int conversionToScale = (actual * MAX_PROGRESS_BAR) / maxLength;

        progressBar.append("\t"+((conversionToScale*100)/MAX_PROGRESS_BAR)+" % \t");

        for(int x = 0; x < conversionToScale; x++)
            if (x > 0)
                progressBar.setCharAt(x, '█');

        return "\r"+progressBar;
    }

    private String getMimeType(File f)  {
        try {
            String mimetype = Files.probeContentType(f.toPath());
            if(mimetype == null)
                return "";
            return (mimetype.split("/")[0]);
        } catch (IOException e) {
            return "";
        }
    }

    private boolean isDuplicatedVideo(File current, File secondFile) {
        String mimeType1=getMimeType(current), mimeType2=getMimeType(secondFile);
        if(!mimeType1.equals(mimeType2))
            return false;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if(isWindows)
                processBuilder.command("fc","/b",current.getAbsolutePath(), secondFile.getAbsolutePath());
            else
                processBuilder.command("diff", current.getAbsolutePath(), secondFile.getAbsolutePath());
            Process process = processBuilder.start();
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            List<String> result = new ArrayList<>();

            while ((line = reader.readLine()) != null)
                result.add(line);

            if(isWindows)
                return result.get(1).contains("no differences encountered");
            return result.size() == 0;
        } catch (IOException e) {
            return false;
        }
    }

    private void loadLib(){
        File lib = null;
        if(isWindows)
            lib=new File("libs"+File.separator+"opencv_java440.dll");
        else if(System.getProperty("os.name").toLowerCase().contains("mac"))
            System.out.println("libs"+File.separator+"libopencv_java440.dylib");
        else
            lib=new File("libs"+File.separator+"libopencv_java440.so");

        System.load(lib.getAbsolutePath());
    }

    @Override
    public String getApp(){
        return app;
    }

    @Override
    public void setApp(String app){
        this.app=app;
    }

    @Override
    public int deleteDuplicatedFiles(){
        int sumVideos=0;

        for(File f:root.listFiles()) {
            Iterator<File> it = Tools.getFile(f, new ArrayList<>());
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

                        images.get(valueInMap).add(new FilePropertiesClass(file, false, false,proportion, Tools.getExifDate(file),new Mat()));

                    }
                }else if(!getMimeType(file).equals("")){
                    sumVideos++;
                    long size=file.length();
                    if(!videos.containsKey(size))
                        videos.put(size,new ArrayList<>());
                    videos.get(size).add(new FilePropertiesClass(file,false,false,-1.0f, Tools.getExifDate(file),null));
                }
            }
        }
        return sumVideos;
    }

    @Override
    public Dimension getImageDimension(File imgFile){
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

    private Mat getHash (Mat mat, Mat hash){
        try {
            ihb.compute(mat,hash);
            return hash;
        } catch (CvException e){
            return null;
        }
    }

    @Override
    public void definingHash(FileProperties fp){
        Mat mat;
        mat = Imgcodecs.imread(fp.getFile().getAbsolutePath());
        fp.setHash(getHash(mat,fp.getHash()));
        mat.release();
    }

    @Override
    public boolean compareFiles(List<FileProperties> files, FileProperties fileP,
                                int percentage, int j){
        boolean found=false;
        FileProperties secondFileP = files.get(j);
        if (!secondFileP.getSeen() && !secondFileP.getToDelete()) {
            boolean cantCompare = false;
            if (fileP.getDate() != null && secondFileP.getDate() != null)
                if (TimeUnit.DAYS.convert(Math.abs(fileP.getDate().getNano() - secondFileP.getDate().getNano()), TimeUnit.NANOSECONDS) > 1)
                    cantCompare = true;
            if (isImage) {
                if (Math.abs(fileP.getProportion() - secondFileP.getProportion()) <= 0.02f && !cantCompare) {
                    if (secondFileP.getHash()!=null && fileP.getHash()!=null) {
                        if (100.0 - (ihb.compare(fileP.getHash(),secondFileP.getHash()) * 100.0 / 64.0) >= percentage) {
                            found=true;
                            toDelete.add(secondFileP);
                            secondFileP.setSeen(true);
                        }
                    }
                }
            } else {
                if (!cantCompare)
                    if (isDuplicatedVideo(fileP.getFile(), secondFileP.getFile())) {
                        found=true;
                        toDelete.add(secondFileP);
                        secondFileP.setSeen(true);
                    }
            }
        }
        return found;
    }

    @Override
    public void permissionApp() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("chmod", "+x", "getApps.sh");
        processBuilder.start();
    }

    @Override
    public Iterator<String> apps() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("./getApps.sh");
        Process process = processBuilder.start();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<String> result = new ArrayList<>();

        while ((line = reader.readLine()) != null)
            result.add(line);

        return result.iterator();
    }

    private Process showPicture(FileProperties fp){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if(getApp()==null){
                String expr = "rundll32 \"C:\\Program Files (x86)\\Windows Photo Viewer\\PhotoViewer.dll\", ImageView_Fullscreen " + fp.getFile().getAbsolutePath();
                Runtime.getRuntime().exec(expr);
                //processBuilder.command(fp.getFile().getAbsolutePath());
                return null;
            }else
                processBuilder.command(app,fp.getFile().getAbsolutePath());
            return processBuilder.start();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void deleteFiles(){
        boolean found = false;
        Iterator<List<FileProperties>> it;
        if(isImage)
            it=images.values().iterator();
        else
            it=videos.values().iterator();
        while(it.hasNext()) {
            List<FileProperties> list = it.next();
            for (FileProperties key :list) {
                if (key.getToDelete()) {
                    String toDeletePath = root + File.separator + "to delete";
                    new File(toDeletePath).mkdirs(); //create folders
                    key.getFile().renameTo(new File(toDeletePath + File.separator + key.getFile().getName()));
                    found = true;
                }
            }
        }
        if(!found)
            System.out.println("There aren't any duplicated files");
    }

    @Override
    public void stopProcesses() throws IOException {
        for (Process p: processes ) {
            if(p!=null)
                p.destroy();
        }
        processes.clear();
        if(!isWindows)
            Runtime.getRuntime().exec("pkill " +app);
        else{
            for(FileProperties fp : toDelete)
                Runtime.getRuntime().exec("taskkill /f /fi \"WINDOWTITLE eq "+fp.getFile().getName()+"*\" /t");
        }
        toDelete.clear();
    }

    @Override
    public boolean analyzeAnswer(String answer){
        boolean accepted=false;

        if (answer.matches("[0-9 ]+|K|D|k|d")) {
            accepted = true;
            if(answer.equalsIgnoreCase("D")){
                for (FileProperties fp:toDelete )
                    fp.setToDelete(true);
            }else if (!answer.equalsIgnoreCase("K")) {
                String[] numbers = answer.split(" ");
                for (String s : numbers) {
                    int index = Integer.parseInt(s) - 1;
                    if(index<toDelete.size() && index >=0)
                        toDelete.get(index).setToDelete(true);
                }
            }
        } return accepted;
    }

}
