import javax.swing.*;

public class SortingMemories {
    private JPanel rootPanel;
    private JTabbedPane tabbedPane1;
    private JPanel window;
    private JTextArea sortingMemoriesTextArea;

    public static void main(String[] args) {
        JFrame frame = new JFrame("SortingMemories");
        frame.setContentPane(new SortingMemories().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
