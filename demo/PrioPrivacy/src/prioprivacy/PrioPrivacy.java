package prioprivacy;

import java.util.Map;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import prioprivacy.GeneralisationHierarchiesController;

public class PrioPrivacy extends Application {

    private Map<Integer, String> dataset;

    public Map<Integer, String> getDataset() {
        return dataset;
    }

    public void setDataset(Map<Integer, String> dataset) {
        this.dataset = dataset;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("ImportData.fxml"));

        Scene scene = new Scene(root);

        stage.setTitle("PrioPrivacy - Data Import");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
        
        QIsPrioritiesController.timer.cancel();
        GeneralisationHierarchiesController.timeline.stop();
    }
}
