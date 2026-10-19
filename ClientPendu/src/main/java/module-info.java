module com.pierreremi.morpion.pendu {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;


    opens com.blancharddero.pendu to javafx.fxml;
    exports com.blancharddero.pendu;
    exports com.blancharddero.pendu.Connexion;
    opens com.blancharddero.pendu.Connexion to javafx.fxml;
    exports com.blancharddero.pendu.Controller;
    opens com.blancharddero.pendu.Controller to javafx.fxml;
}