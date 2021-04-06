package javafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AlertDialogController implements Initializable {

    private static String textAlertBox;
    private static Pane mainPane;

    @FXML
    private Label textAlert;

    @FXML
    private Button okButton;

    public static void setup(Pane pane) {
        mainPane = pane;
    }

    public static void print(String text) {
        try {
            mainPane.setDisable(true);
            textAlertBox = text;
            FXMLLoader loader = new FXMLLoader(AlertDialogController.class.getResource("FXML/alert.fxml"));
            Parent parent = null;
            parent = loader.load();
            Scene scene = new Scene(parent);
            Stage stage = new Stage();

            scene.setFill(Color.TRANSPARENT);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    void okAction(ActionEvent event) {
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
        mainPane.setDisable(false);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("second");
        if (textAlert != null)
            textAlert.setText(textAlertBox);
    }
}
