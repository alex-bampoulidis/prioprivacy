package prioprivacy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ImportDataController implements Initializable {

    private static File input;

    private static List<String> fields;

    private static String separator;

    @FXML
    private Label fileLabel;

    @FXML
    private VBox separatorPreviewVBox;

    @FXML
    private TextField separatorField;

    @FXML
    private TableView previewTable;

    @FXML
    private VBox previewVBox;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (input != null) {
            try {
                fileLabel.setText("File selected: " + input.getName());

                separatorPreviewVBox.setVisible(true);

                separatorField.setText(separator);
                previewData(new ActionEvent());
            } catch (Exception ex) {
                Logger.getLogger(ImportDataController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @FXML
    private void fileChooser(ActionEvent event) throws Exception {
        FileChooser fileChooser = new FileChooser();
        input = fileChooser.showOpenDialog(null);

        if (input != null) {
            fileLabel.setText("File selected: " + input.getName());

            separatorPreviewVBox.setVisible(true);
        } else {
            fileLabel.setText("File selection cancelled.");
        }
    }

    @FXML
    private void previewData(ActionEvent event) throws Exception {
        previewVBox.setVisible(true);

        previewTable.getColumns().clear();

        Map<Integer, String> indexToFieldMap = new HashMap<Integer, String>();

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));

        fields = new ArrayList<String>();
        separator = separatorField.getText();

        String header[] = br.readLine().split(separatorField.getText());
        for (int i = 0; i < header.length; i++) {
            TableColumn<Map<String, String>, String> column = new TableColumn<>(header[i]);

            final String field = header[i];

            column.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get(field)));

            previewTable.getColumns().add(column);

            indexToFieldMap.put(i, header[i]);

            fields.add(header[i]);
        }

        ObservableList<Map<String, String>> data = FXCollections.observableArrayList();
        for (int i = 0; i < 10; i++) {
            String fields[] = br.readLine().split(separatorField.getText());

            Map<String, String> row = new HashMap<String, String>();
            for (int index = 0; index < fields.length; index++) {
                row.put(indexToFieldMap.get(index), fields[index]);
            }

            data.add(row);
        }

        previewTable.setItems(data);

        br.close();
    }

    @FXML
    private void importData(ActionEvent event) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("QIsPriorities.fxml"));

        Parent root1 = (Parent) fxmlLoader.load();

        Stage stage = new Stage();
        stage.setTitle("PrioPrivacy - QIs and Priorities");
        stage.setScene(new Scene(root1));
        stage.show();

        Stage current = (Stage) fileLabel.getScene().getWindow();
        current.close();
    }

    public static List<String> getFields() {
        return fields;
    }
    
    public static String getSeparator() {
        return separator;
    }
    
    public static File getInput() {
        return input;
    }

}
