import com.formdev.flatlaf.FlatLightLaf;
import duplicatedFiles.DuplicatedFiles;
import duplicatedFiles.OSType;
import file.FileProperties;
import file.Tools;
import organizeFiles.Organizer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class SortingMemories {
    private static final int MAX_PROGRESS_BAR = 50;
    private static final String CONFIRMATION_EMPTY_FOLDERS = " empty folders were deleted.";
    private static final String WARNING_ORGANIZING = "WARNING: if you want that some photos or folders to NOT being organized by year,\nput them on a folder named 'not organize' in the root folder. Continue?";
    private static final String ORGANIZING = "Organizing photos...";
    private static final String ORGANIZED = " photos were organized";
    private static final String CHOOSE_APP = "Please choose an application to open pictures\n(if the photos don't show up reboot the program and choose another app):";
    private static final String DESKTOP = "\\.desktop";
    private static final String VIDEOS = "Videos and others:";
    private static final String INSERT_FOLDER = "Please insert a folder's directory";
    private static final String LOADING_FILES = "Loading files...";
    private static final String WARNING_STAGES = "WARNING: We will divide the images whole process into 9 stages, so how this is a long process you can execute them separately.\n" +
            " The files you'll choose to delete will be moved to a folder named 'to delete', this is a security procedure,\n so at the end you will only have to delete the folder." +
            " If you already have a folder with this name please rename it.";
    private static final String WRONG_SELECTION = "Choose just images stages or just videos stages.";
    private static final String WRONG_PERCENTAGE = "Wrong percentage value.";

    private JPanel rootPanel;
    private static JFrame frame;
    private JTextField textDir;
    private JButton searchButton;
    private JButton emptyFolders;
    private JButton duplicatedFiles;
    private JButton organize;
    private JTextArea log;
    private JTextField textPercentage;
    private JPanel panel1;
    private JButton continueButton;
    private JCheckBox checkBox1;
    private JCheckBox checkBox2;
    private JCheckBox checkBox3;
    private JCheckBox checkBox4;
    private JCheckBox checkBox5;
    private JCheckBox checkBox6;
    private JCheckBox checkBox7;
    private JCheckBox checkBox8;
    private JCheckBox checkBox9;
    private JCheckBox checkBox10;
    private JPanel panel2;
    private JButton nextButton;
    private JTable table1;
    private JLabel logo;
    private File folder;
    private List<JCheckBox> checkBoxes;
    private DuplicatedFiles df;
    private Iterator<Map.Entry<Integer, CopyOnWriteArrayList<FileProperties>>> groups;
    private int size;
    private int master;

    public SortingMemories() {
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser f = new JFileChooser();
                f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                f.showSaveDialog(null);
                folder = f.getSelectedFile();
                textDir.setText(folder.getAbsolutePath());
            }
        });
        emptyFolders.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    checkDirectory();
                    panel1.setVisible(false);
                    panel2.setVisible(false);
                    textDir.setEnabled(true);
                    searchButton.setEnabled(true);
                    log.setText("");
                    JOptionPane.showMessageDialog(frame, Tools.deleteEmptyFolders(folder, 0) + CONFIRMATION_EMPTY_FOLDERS);
                } catch (ScriptException scriptException) {
                    scriptException.printStackTrace();
                }
            }
        });
        organize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    checkDirectory();
                    panel1.setVisible(false);
                    panel2.setVisible(false);
                    textDir.setEnabled(true);
                    searchButton.setEnabled(true);
                    log.setText("");
                    int res = JOptionPane.showConfirmDialog(frame, WARNING_ORGANIZING,"Warning about organizing files", JOptionPane.YES_NO_OPTION);
                    if(res == JOptionPane.YES_OPTION) {
                        int counter = 0;
                        log.append("Log:\n");
                        log.append(ORGANIZING + "\n");
                        Iterator<String> it = (new Organizer(folder)).organizeFiles();

                        while (it.hasNext()) {
                            log.append(it.next() + "\n");
                            counter++;
                        }

                        int deleted = Tools.deleteEmptyFolders(folder, 0);
                        JOptionPane.showMessageDialog(frame, counter + ORGANIZED + "\n" + deleted + CONFIRMATION_EMPTY_FOLDERS);
                    }
                } catch (ScriptException scriptException) {
                    scriptException.printStackTrace();
                }
            }
        });
        duplicatedFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    checkDirectory();
                    frame.setSize(800, 600);
                    frame.setLocationRelativeTo(null);
                    textDir.setEnabled(false);
                    searchButton.setEnabled(false);
                    log.setText("");
                    JOptionPane.showMessageDialog(frame, WARNING_STAGES);
                    panel1.setVisible(true);
                    log.append(LOADING_FILES + "\n");
                    df = new DuplicatedFiles(folder);
                    int sumVideos = df.deleteDuplicatedFiles();
                    printStages(sumVideos);


                } catch (ScriptException scriptException) {
                    scriptException.printStackTrace();
                }
            }
        });
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    boolean exists = false;
                    boolean all = true;
                    for (int i = 0; i < checkBoxes.size() - 1; i++) {
                        if (checkBoxes.get(i).isSelected())
                            exists = true;
                        else
                            all = false;
                    }

                    if (checkBox10.isSelected()) {
                        if (exists) {
                            JOptionPane.showMessageDialog(frame, WRONG_SELECTION);
                            throw new ScriptException("");
                        }
                        df.setIsImage(false);

                    } else if (all) {
                        df.setIsImage(true);
                        df.addStage(-1);
                    } else {
                        df.setIsImage(true);
                        for (int i = 0; i < checkBoxes.size() - 1; i++) {
                            if (checkBoxes.get(i).isSelected())
                                df.addStage(i + 1);
                        }
                    }

                    int percentage = getPercentage();
                    panel1.setVisible(false);
                    panel2.setVisible(true);
                    if (df.getIsImage()) {
                        if (df.getOSType() == OSType.MACOS)
                            df.setApp("open");
                        else if (df.getOSType() == OSType.LINUX)
                            chooseImagesApp();
                        iteratingMaps(percentage);
                    } else {
                        iteratingMaps(-1);
                    }

                } catch (ScriptException | IOException | InterruptedException scriptException) {
                    scriptException.printStackTrace();
                }
            }
        });
        rootPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                panel1.setVisible(false);
                panel2.setVisible(false);
                checkBoxes = new ArrayList<>();
                checkBoxes.add(checkBox1);
                checkBoxes.add(checkBox2);
                checkBoxes.add(checkBox3);
                checkBoxes.add(checkBox4);
                checkBoxes.add(checkBox5);
                checkBoxes.add(checkBox6);
                checkBoxes.add(checkBox7);
                checkBoxes.add(checkBox8);
                checkBoxes.add(checkBox9);
                checkBoxes.add(checkBox10);
                nextButton.setBackground(Color.CYAN);
                continueButton.setBackground(Color.CYAN);
                searchButton.setBackground(Color.CYAN);
                duplicatedFiles.setBackground(Color.lightGray);
                organize.setBackground(Color.lightGray);
                emptyFolders.setBackground(Color.lightGray);
            }
        });
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String answer = "";
                for (int i = 0; i < size; i++) {
                    if ((boolean) table1.getValueAt(i, 0))
                        answer += (i + 1) + " ";
                }

                df.analyzeAnswer(answer, master);
                try {
                    df.closePreviews(master);
                } catch (IOException | InterruptedException ioException) {
                    ioException.printStackTrace();
                }
                chooseToDelete();
            }
        });
    }

    private void checkDirectory() throws ScriptException {
        if (folder == null) {
            File aux = new File(textDir.getText());
            if (!aux.isDirectory()) {
                JOptionPane.showMessageDialog(frame, INSERT_FOLDER);
                throw new ScriptException(INSERT_FOLDER);
            } else
                folder = aux;
        }
    }

    private int getPercentage() throws ScriptException {
        String answer = textPercentage.getText();
        if (answer.matches("[0-9]+")) {
            int percentage = Integer.parseInt(answer);
            if (percentage >= 0 && percentage <= 100)
                return percentage;
            JOptionPane.showMessageDialog(frame, WRONG_PERCENTAGE);
            throw new ScriptException("");
        }
        JOptionPane.showMessageDialog(frame, WRONG_PERCENTAGE);
        throw new ScriptException("");
    }

    private void printStages(int sumVideos) {
        Iterator<List<FileProperties>> it = df.getAllImages();

        checkBox1.setText("Stage 1: " + it.next().size() + " images");
        checkBox2.setText("Stage 2: " + it.next().size() + " images");
        checkBox3.setText("Stage 3: " + it.next().size() + " images");
        checkBox4.setText("Stage 4: " + it.next().size() + " images");
        checkBox5.setText("Stage 5: " + it.next().size() + " images");
        checkBox6.setText("Stage 6: " + it.next().size() + " images");
        checkBox7.setText("Stage 7: " + it.next().size() + " images");
        checkBox8.setText("Stage 8: " + it.next().size() + " images");
        checkBox9.setText("Stage 9: " + it.next().size() + " images");
        checkBox10.setText("Stage V - Total videos and others: " + sumVideos + " files");
    }

    private String progressBar(int actual, int maxLength) {
        StringBuilder progressBar = new StringBuilder();

        for (var i = 0; i < MAX_PROGRESS_BAR; i++) // set default values
            if (i == 0)
                progressBar.append("[");
            else if (i == MAX_PROGRESS_BAR - 1)
                progressBar.append("]");
            else
                progressBar.append("-");

        int conversionToScale = (actual * MAX_PROGRESS_BAR) / maxLength;

        progressBar.append("\t").append((conversionToScale * 100) / MAX_PROGRESS_BAR).append(" % \t");

        for (int x = 0; x < conversionToScale; x++)
            if (x > 0)
                progressBar.setCharAt(x, '█');

        return "\r" + progressBar;
    }

    private void chooseImagesApp() throws IOException {
        df.permissionApp();
        Iterator<String> it = df.apps();
        List<String> list = new ArrayList<>();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel(CHOOSE_APP));

        List<JRadioButton> buttons = new ArrayList<>();
        ButtonGroup group = new ButtonGroup();

        int counter = 0;
        while (it.hasNext()) {
            String app = it.next().split(DESKTOP)[0];
            if (!app.equals("")) {
                counter++;
                list.add(app);
                JRadioButton cb = new JRadioButton();
                cb.setText("[" + counter + "]" + " - " + app);
                if (counter == 1)
                    cb.setSelected(true);
                buttons.add(cb);
                group.add(cb);
                panel.add(cb);
            }
        }

        JOptionPane.showMessageDialog(null, panel);

        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).isSelected()) {
                df.setApp(list.get(i));
            }
        }
    }

    private void iteratingMaps(int percentage) throws IOException, InterruptedException {
        int n, stage = 0;
        FileProperties fileP;
        Iterator<List<FileProperties>> it;

        if (df.getIsImage())
            it = df.getAllImages();
        else
            it = df.getAllVideos();

        while (it.hasNext()) {
            stage++;
            n = 0;
            List<FileProperties> files = it.next();
            if (df.hasStage(stage) || df.hasStage(-1) || !df.getIsImage()) {
                if (df.getIsImage()) {
                    log.append("Stage " + stage + ": loading data (this might take a while)\n");

                    for (FileProperties fp : files) {
                        log.append(progressBar(files.indexOf(fp), files.size()) + "\n");
                        df.definingHash(fp);
                    }
                } else
                    log.append(VIDEOS + "\n");

                int processors = Runtime.getRuntime().availableProcessors() * 2;

                for (int i = 0; i < files.size() - 1; i++) {
                    fileP = files.get(i);
                    n++;
                    log.append("\rFile: " + n + "\n");
                    if (!fileP.getSeen() && !fileP.getToDelete()) {
                        df.createChoosingGroup(i);
                        AtomicBoolean found = new AtomicBoolean(false);
                        int portion;
                        if (files.size() - i - 1 <= processors)
                            portion = 1;
                        else
                            portion = (files.size() - i - 1) / processors;
                        List<Thread> threads = new ArrayList<>();

                        int finalI = i;
                        FileProperties finalFileP = fileP;

                        for (int k = 0; k < Math.min(processors, files.size() - i - 1); k++) {
                            int finalK = k;
                            Thread thread = new Thread(() -> {
                                int aux = finalI + 1 + (finalK * portion);
                                for (int j = aux; j < aux + portion; j++) {
                                    if (df.compareFiles(finalFileP, files.get(j), percentage, finalI))
                                        found.set(true);
                                }
                            });
                            threads.add(thread);
                            thread.start();
                        }

                        for (Thread t : threads)
                            t.join();

                        if (found.get())
                            df.addToChoosingGroup(i, fileP);

                        System.gc();
                    }
                }
            }
        }
        groups = df.getToDelete();
        chooseToDelete();
    }

    private void chooseToDelete() {
        if (groups.hasNext()) {
            Map.Entry<Integer, CopyOnWriteArrayList<FileProperties>> entry = groups.next();
            CopyOnWriteArrayList<FileProperties> list = entry.getValue();

            if (!list.isEmpty()) {
                DefaultTableModel model = new DefaultTableModel(new Object[]{"Delete?", "File path"}, 0) {
                    @Override
                    public Class getColumnClass(int columnIndex) {
                        if (columnIndex == 0)
                            return Boolean.class;
                        return String.class;
                    }
                };

                for (int i = 0; i < list.size(); i++) {
                    FileProperties fp = list.get(i);
                    String text = fp.getFile().getAbsolutePath();
                    model.addRow(new Object[]{false, text});
                    df.showPicture(fp);
                }

                table1.setModel(model);
                table1.getColumn(table1.getColumnName(0)).setMaxWidth(60);

                size = list.size();
                master = entry.getKey();
            } else
                chooseToDelete();
        } else {
            df.deleteFiles();
            panel2.setVisible(false);
            log.setText("");
            textDir.setEnabled(true);
            searchButton.setEnabled(true);
            JOptionPane.showMessageDialog(null, "Selected files were deleted!");
        }
    }


    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel( new FlatLightLaf() );
        frame = new JFrame("Sorting Memories");
        frame.setContentPane(new SortingMemories().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(new ImageIcon("logo.png").getImage());
        frame.pack();
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createUIComponents() {
        try {
            BufferedImage img = ImageIO.read(new File("logo.png"));
            logo = new JLabel(new ImageIcon(img.getScaledInstance(50, 50,
                    Image.SCALE_SMOOTH)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
