module com.zedo.mcfishing {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.zedo.mcfishing to javafx.fxml;
    exports com.zedo.mcfishing;
}