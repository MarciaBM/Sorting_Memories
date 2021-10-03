module com.sm.sortingmemories {
   requires javafx.controls;
    requires javafx.fxml;
    requires org.bytedeco.opencv;
    requires java.desktop;
    requires metadata.extractor;

    opens SortingMemoriesFX to javafx.fxml;
    exports SortingMemoriesFX;
}