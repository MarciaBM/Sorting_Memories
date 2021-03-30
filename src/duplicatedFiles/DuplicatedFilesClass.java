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
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DuplicatedFilesClass implements DuplicatedFiles {
    private static final float PROPORTION_MARGIN = 0.02f;
    private static final float AR16_9 = (float) 16 / 9;
    private static final float AR9_16 = (float) 9 / 16;
    private static final float AR4_3 = (float) 4 / 3;
    private static final float AR3_4 = (float) 3 / 4;
    private static final float AR3_2 = (float) 3 / 2;
    private static final float AR2_3 = (float) 2 / 3;
    private static final float AR1_1 = 1.0f;
    private static final float AR_OTHER = -1.0f;
    private static final float AR6_10 = 0.6f;
    private static final String OS_NAME = "os.name";
    private static final String WINDOWS = "windows";
    private static final String MACOS = "mac";
    private static final String RIGHT_SLASH = "/";
    private static final String PATH_LIBS = "libs";
    private static final String LIB_WIN = "opencv_java451.dll";
    private static final String LIB_MACOS = "libopencv_java440.dylib";
    private static final String LIB_LINUX = "libopencv_java451.so";
    private static final String NO_DIFF="no differences encountered";
    private static final String KILL_WIN="taskkill /f /fi \"WINDOWTITLE eq ";
    private static final String KILL_MACOS="killall Preview";
    private static final String KILL_LINUX="pkill ";
    private static final String IMAGE_WIN="rundll32 \"C:\\Program Files (x86)\\Windows Photo Viewer\\PhotoViewer.dll\", ImageView_Fullscreen ";
    private static final String NO_DUPLICATED_FILES="There aren't any duplicated files";
    private static final String TO_DELETE="to delete";
    private static final String GET_APPS_SCRIPT="getApps.sh";
    private static final String DIFF="diff";
    private static final String DIFF_WIN="fc";
    private static final String IMAGE="image";

    private final ImgHashBase ihb;
    private final Map<Float, List<FileProperties>> images;
    private final Map<Long, List<FileProperties>> videos;
    private final File root;
    private final List<Integer> stages;
    private final List<FileProperties> toDelete;
    private OSType osType;
    private boolean isImage;
    private String app;


    public DuplicatedFilesClass(File root) {
        this.root = root;
        defineOS();
        loadLib();
        app = null;
        ihb = AverageHash.create();
        videos = new HashMap<>();
        images = new HashMap<>();
        stages = new ArrayList<>();
        toDelete = new LinkedList<>();
        images.put(AR16_9, new ArrayList<>());
        images.put(AR9_16, new ArrayList<>());
        images.put(AR4_3, new ArrayList<>());
        images.put(AR3_4, new ArrayList<>());
        images.put(AR3_2, new ArrayList<>());
        images.put(AR2_3, new ArrayList<>());
        images.put(AR6_10, new ArrayList<>());
        images.put(AR1_1, new ArrayList<>());
        images.put(AR_OTHER, new ArrayList<>());

    }

    private void defineOS() {
        if (System.getProperty(OS_NAME).toLowerCase().contains(WINDOWS))
            osType = OSType.WINDOWS;
        else if (System.getProperty(OS_NAME).toLowerCase().contains(MACOS))
            osType = OSType.MACOS;
        else
            osType = OSType.LINUX;
    }

    @Override
    public Iterator<List<FileProperties>> getAllImages() {
        return images.values().iterator();
    }

    @Override
    public Iterator<List<FileProperties>> getAllVideos() {
        return videos.values().iterator();
    }

    @Override
    public Iterator<FileProperties> getToDelete() {
        return toDelete.iterator();
    }

    @Override
    public void addToToDelete(FileProperties fp) {
        toDelete.add(fp);
    }

    @Override
    public OSType getOSType() {
        return osType;
    }

    @Override
    public boolean getIsImage() {
        return isImage;
    }

    @Override
    public void setIsImage(boolean b) {
        isImage = b;
    }

    @Override
    public void addStage(int stage) {
        stages.add(stage);
    }

    @Override
    public boolean hasStage(int stage) {
        return stages.contains(stage);
    }

    private String getMimeType(File f) {
        try {
            String mimetype = Files.probeContentType(f.toPath());
            if (mimetype == null)
                return "";
            return (mimetype.split(RIGHT_SLASH)[0]);
        } catch (IOException e) {
            return "";
        }
    }

    private boolean isDuplicatedVideo(File current, File secondFile) {
        String mimeType1 = getMimeType(current), mimeType2 = getMimeType(secondFile);
        if (!mimeType1.equals(mimeType2))
            return false;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (osType == OSType.WINDOWS)
                processBuilder.command(DIFF_WIN, "/b", current.getAbsolutePath(), secondFile.getAbsolutePath());
            else
                processBuilder.command(DIFF, current.getAbsolutePath(), secondFile.getAbsolutePath());
            List<String> result = runProcess(processBuilder);
            if (osType == OSType.WINDOWS)
                return result.get(1).contains(NO_DIFF);
            return result.size() == 0;
        } catch (IOException e) {
            return false;
        }
    }

    private void loadLib() {
        File lib;
        if (osType == OSType.WINDOWS)
            lib = new File(PATH_LIBS + File.separator + LIB_WIN);
        else if (osType == OSType.MACOS)
            lib = new File(PATH_LIBS + File.separator + LIB_MACOS);
        else
            lib = new File(PATH_LIBS + File.separator + LIB_LINUX);

        System.load(lib.getAbsolutePath());
    }

    @Override
    public String getApp() {
        return app;
    }

    @Override
    public void setApp(String app) {
        this.app = app;
    }

    @Override
    public int deleteDuplicatedFiles() {
        int sumVideos = 0;

        for (File f : root.listFiles()) {
            Iterator<File> it = Tools.getFile(f, new ArrayList<>());
            while (it.hasNext()) {
                File file = it.next();
                if (getMimeType(file).equals(IMAGE)) {
                    Dimension d = getImageDimension(file);
                    if (d != null) {
                        float proportion = (float) d.height / (float) d.width, valueInMap = AR_OTHER;
                        Set<Float> aspectRatios = images.keySet();

                        for (float aR : aspectRatios)
                            if (Math.abs(proportion - aR) <= PROPORTION_MARGIN)
                                valueInMap = aR;

                        images.get(valueInMap).add(new FilePropertiesClass(file, false, false, proportion, Tools.getExifDate(file), new Mat()));

                    }
                } else if (!getMimeType(file).equals("")) {
                    sumVideos++;
                    long size = file.length();
                    if (!videos.containsKey(size))
                        videos.put(size, new ArrayList<>());
                    videos.get(size).add(new FilePropertiesClass(file, false, false, -1.0f, Tools.getExifDate(file), null));
                }
            }
        }
        return sumVideos;
    }

    @Override
    public Dimension getImageDimension(File imgFile) {
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

    private Mat getHash(Mat mat, Mat hash) {
        try {
            ihb.compute(mat, hash);
            return hash;
        } catch (CvException e) {
            return null;
        }
    }

    @Override
    public void definingHash(FileProperties fp) {
        Mat mat;
        mat = Imgcodecs.imread(fp.getFile().getAbsolutePath());
        fp.setHash(getHash(mat, fp.getHash()));
        mat.release();
    }

    @Override
    public boolean compareFiles(List<FileProperties> files, FileProperties fileP,
                                int percentage, int j) {
        boolean found = false;
        FileProperties secondFileP = files.get(j);
        if (!secondFileP.getSeen() && !secondFileP.getToDelete()) {
            boolean cantCompare = false;
            if (fileP.getDate() != null && secondFileP.getDate() != null)
                if (TimeUnit.DAYS.convert(Math.abs(fileP.getDate().getNano() - secondFileP.getDate().getNano()), TimeUnit.NANOSECONDS) > 1)
                    cantCompare = true;
            if (isImage) {
                if (Math.abs(fileP.getProportion() - secondFileP.getProportion()) <= 0.02f && !cantCompare) {
                    if (secondFileP.getHash() != null && fileP.getHash() != null) {
                        if (100.0 - (ihb.compare(fileP.getHash(), secondFileP.getHash()) * 100.0 / 64.0) >= percentage) {
                            found = true;
                            toDelete.add(secondFileP);
                            secondFileP.setSeen(true);
                        }
                    }
                }
            } else {
                if (!cantCompare)
                    if (isDuplicatedVideo(fileP.getFile(), secondFileP.getFile())) {
                        found = true;
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
        processBuilder.command("chmod", "+x", GET_APPS_SCRIPT);
        processBuilder.start();
    }

    private List<String> runProcess(ProcessBuilder processBuilder) throws IOException{
        Process process = processBuilder.start();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<String> result = new ArrayList<>();

        while ((line = reader.readLine()) != null)
            result.add(line);
        return result;
    }

    @Override
    public Iterator<String> apps() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("./"+GET_APPS_SCRIPT);
        return runProcess(processBuilder).iterator();
    }

    @Override
    public void showPicture(FileProperties fp) {
        try {
            if(isImage) {
                if (getApp() == null) {
                    String expr = IMAGE_WIN + fp.getFile().getAbsolutePath();
                    Runtime.getRuntime().exec(expr);
                } else {
                    ProcessBuilder processBuilder = new ProcessBuilder();
                    processBuilder.command(app, fp.getFile().getAbsolutePath());
                    processBuilder.start();
                }
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void deleteFiles() {
        boolean found = false;
        Iterator<List<FileProperties>> it;
        if (isImage)
            it = images.values().iterator();
        else
            it = videos.values().iterator();
        while (it.hasNext()) {
            List<FileProperties> list = it.next();
            for (FileProperties key : list) {
                if (key.getToDelete()) {
                    String toDeletePath = root + File.separator + TO_DELETE;
                    new File(toDeletePath).mkdirs(); //create folders
                    key.getFile().renameTo(new File(toDeletePath + File.separator + key.getFile().getName()));
                    found = true;
                }
            }
        }
        if (!found)
            System.out.println(NO_DUPLICATED_FILES);
    }

    @Override
    public void closePreviews() throws IOException {
        if (osType == OSType.MACOS)
            Runtime.getRuntime().exec(KILL_MACOS);
        else if (osType == OSType.LINUX)
            Runtime.getRuntime().exec(KILL_LINUX + app);
        else {
            for (FileProperties fp : toDelete)
                Runtime.getRuntime().exec(KILL_WIN + fp.getFile().getName() + "*\" /t");
        }
        toDelete.clear();
    }

    @Override
    public boolean analyzeAnswer(String answer) {
        boolean accepted = false;

        if (answer.matches("[0-9 ]+|K|D|k|d")) {
            accepted = true;
            if (answer.equalsIgnoreCase("D")) {
                for (FileProperties fp : toDelete)
                    fp.setToDelete(true);
            } else if (!answer.equalsIgnoreCase("K")) {
                String[] numbers = answer.split(" ");
                for (String s : numbers) {
                    int index = Integer.parseInt(s) - 1;
                    if (index < toDelete.size() && index >= 0)
                        toDelete.get(index).setToDelete(true);
                }
            }
        }
        return accepted;
    }

}
