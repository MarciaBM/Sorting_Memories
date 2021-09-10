package duplicatedFiles;

import file.*;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.img_hash.ImgHashBase;
import org.opencv.img_hash.PHash;
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
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class DuplicatedFiles {
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
    private static final String NO_DIFF = "no differences encountered";
    private static final String KILL_WIN = "taskkill /f /fi \"WINDOWTITLE eq ";
    private static final String KILL_MACOS = "killall Preview";
    private static final String KILL_LINUX = "pkill ";
    private static final String IMAGE_WIN = "rundll32 \"C:\\Program Files (x86)\\Windows Photo Viewer\\PhotoViewer.dll\", ImageView_Fullscreen ";
    private static final String TO_DELETE = "to delete";
    private static final String GET_APPS_SCRIPT = "getApps.sh";
    private static final String DIFF = "diff";
    private static final String DIFF_WIN = "fc";
    private static final String IMAGE = "image";

    private final ImgHashBase ihb;
    private final Map<Float, List<FileProperties>> images;
    private final Map<Long, List<FileProperties>> videos;
    private final File root;
    private final List<Integer> stages;
    private final Map<Integer, CopyOnWriteArrayList<FileProperties>> choosingGroups;
    private OSType osType;
    private boolean isImage;
    private String app;


    public DuplicatedFiles(File root) {
        this.root = root;
        defineOS();
        loadLib();
        app = null;
        ihb = PHash.create();
        videos = new HashMap<>();
        images = new HashMap<>();
        stages = new ArrayList<>();
        choosingGroups = new HashMap<>();
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

    public Iterator<List<FileProperties>> getAllImages() {
        return images.values().iterator();
    }

    public Iterator<List<FileProperties>> getAllVideos() {
        return videos.values().iterator();
    }

    public Iterator<Map.Entry<Integer, CopyOnWriteArrayList<FileProperties>>> getToDelete() {
        return choosingGroups.entrySet().iterator();
    }

    public void createChoosingGroup(int i) {
        choosingGroups.put(i, new CopyOnWriteArrayList<>());
    }

    public void addToChoosingGroup(int i, FileProperties fp) {
        choosingGroups.get(i).add(fp);
    }

    public void removeChoosingGroup(int i){choosingGroups.remove(i);}

    public int getChoosingGroupsSize(){
        return choosingGroups.size();
    }

    public OSType getOSType() {
        return osType;
    }

    public boolean getIsImage() {
        return isImage;
    }

    public void setIsImage(boolean b) {
        isImage = b;
    }

    public void addStage(int stage) {
        stages.add(stage);
    }

    public boolean hasStage(int stage) {
        return stages.contains(stage);
    }

    public String getMimeType(File f) {
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

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public long fileCount(Path dir) {
        try {
            return Files.walk(dir)
                    .parallel()
                    .filter(p -> !p.toFile().isDirectory())
                    .count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int deleteDuplicatedFiles(File file) {
        int sumVideos = 0;
        if(!file.getAbsolutePath().contains(root.getAbsolutePath() + File.separator + "to delete")) {
            if (getMimeType(file).equals(IMAGE)) {
                Dimension d = getImageDimension(file);
                if (d != null) {
                    float proportion = (float) d.height / (float) d.width, valueInMap = AR_OTHER;
                    Set<Float> aspectRatios = images.keySet();

                    for (float aR : aspectRatios)
                        if (Math.abs(proportion - aR) <= PROPORTION_MARGIN)
                            valueInMap = aR;
                    ImageProperties ip = new ImagePropertiesClass(file, false, false, proportion, Tools.getExifDate(file), new Mat());
                    synchronized (this) {
                        images.get(valueInMap).add(ip);
                    }
                }
            } else if (!getMimeType(file).equals("")) {
                sumVideos++;
                long size = file.length();
                VideoProperties vp = new VideoPropertiesClass(file, false, false, Tools.getExifDate(file));
                synchronized (this) {
                    if (!videos.containsKey(size))
                        videos.put(size, new ArrayList<>());
                    videos.get(size).add(vp);
                }
            }
        }

        return sumVideos;
    }

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

    public void definingHash(FileProperties fp) {
        if (fp instanceof ImageProperties) {
            ImageProperties ip = (ImageProperties) fp;
            Mat mat;
            mat = Imgcodecs.imread(fp.getFile().getAbsolutePath());
            synchronized (this) {
                ip.setHash(getHash(mat, ip.getHash()));
            }
            mat.release();
        }
    }

    public boolean compareFiles(FileProperties fileP, FileProperties secondFileP,
                                int percentage, int master) {
        boolean found = false;
        if (!secondFileP.getSeen()) {
            boolean cantCompare = false;
            if (fileP.getDate() != null && secondFileP.getDate() != null)
                if (TimeUnit.DAYS.convert(Math.abs(fileP.getDate().getNano() - secondFileP.getDate().getNano()), TimeUnit.NANOSECONDS) > 1)
                    cantCompare = true;
            if (isImage && fileP instanceof ImageProperties && secondFileP instanceof ImageProperties) {
                ImageProperties ip1 = (ImageProperties) fileP;
                ImageProperties ip2 = (ImageProperties) secondFileP;
                if (Math.abs(ip1.getProportion() - ip2.getProportion()) <= 0.02f && !cantCompare) {
                    if (ip2.getHash() != null && ip1.getHash() != null) {
                        if (100.0 - (ihb.compare(ip1.getHash(), ip2.getHash()) * 100.0 / 64.0) >= percentage) {
                            found = true;
                            secondFileP.setSeen(true);
                            choosingGroups.get(master).add(secondFileP);
                        }
                    }
                }
            } else {
                if (!cantCompare)
                    if (isDuplicatedVideo(fileP.getFile(), secondFileP.getFile())) {
                        found = true;
                        choosingGroups.get(master).add(secondFileP);
                        secondFileP.setSeen(true);
                    }
            }
        }
        return found;
    }

    public void permissionApp() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("chmod", "+x", GET_APPS_SCRIPT);
        processBuilder.start();
    }

    private List<String> runProcess(ProcessBuilder processBuilder) throws IOException {
        Process process = processBuilder.start();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<String> result = new ArrayList<>();

        while ((line = reader.readLine()) != null)
            result.add(line);
        return result;
    }

    public Iterator<String> apps() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("./" + GET_APPS_SCRIPT);
        return runProcess(processBuilder).iterator();
    }

    public void showPicture(FileProperties fp) {
        try {
            if (isImage) {
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

    public void deleteFiles() {
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
                }
            }
        }
    }

    public void closePreviews(int master) throws IOException, InterruptedException {
        CopyOnWriteArrayList<FileProperties> list = choosingGroups.get(master);
        if (osType == OSType.MACOS)
            Runtime.getRuntime().exec(KILL_MACOS);
        else if (osType == OSType.LINUX)
            Runtime.getRuntime().exec(KILL_LINUX + app);
        else {
            for (FileProperties fp : list)
                Runtime.getRuntime().exec(KILL_WIN + fp.getFile().getName() + "*\" /t");
        }
        Thread.sleep(500);
    }

    public boolean analyzeAnswer(String answer, int master) {
        boolean accepted = false;
        CopyOnWriteArrayList<FileProperties> list = choosingGroups.get(master);

        if (answer.matches("[0-9 ]+|K|D|k|d")) {
            accepted = true;
            if (answer.equalsIgnoreCase("D")) {
                for (FileProperties fp : list)
                    fp.setToDelete(true);
            } else if (!answer.equalsIgnoreCase("K")) {
                String[] numbers = answer.split(" ");
                for (String s : numbers) {
                    int index = Integer.parseInt(s) - 1;
                    if (index < list.size() && index >= 0)
                        list.get(index).setToDelete(true);
                }
            }
        }
        return accepted;
    }

}
