package prioprivacy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static java.util.Map.Entry.comparingByKey;
import static java.util.Map.Entry.comparingByValue;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class QIsPrioritiesController implements Initializable {

    private List<String> fields;

    public static Timer timer;

    private static ObservableList<Attribute> tableData;

    private static Map<String, Integer> QIs;

    private static String separator;

    private static Map<Integer, List<Integer>> priorities;

    private static Map<Integer, String> dataset;

    private static Map<Integer, List<String>> QIsDomainsMap;

    @FXML
    private TableView<Attribute> attributesTable;

    @FXML
    private VBox loadingVBox;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fields = ImportDataController.getFields();

        TableColumn<Attribute, Boolean> column1 = new TableColumn<>("QI?");
        column1.setCellValueFactory(new PropertyValueFactory<>("QI"));
        column1.setSortable(false);
        attributesTable.getColumns().add(column1);

        TableColumn<Attribute, String> column2 = new TableColumn<>("Priority");
        column2.setCellValueFactory(new PropertyValueFactory<>("priority"));
        column2.setSortable(false);
        attributesTable.getColumns().add(column2);

        TableColumn<Attribute, String> column3 = new TableColumn<>("Attribute");
        column3.setCellValueFactory(new PropertyValueFactory<>("name"));
        column3.setSortable(false);
        attributesTable.getColumns().add(column3);

        if (tableData == null || tableData.isEmpty()) {
            ObservableList<String> priorities = FXCollections.observableArrayList();
            priorities.add("-");
            for (int i = 1; i <= fields.size(); i++) {
                priorities.add(i + "");
            }

            ObservableList<Attribute> attributes = FXCollections.observableArrayList();
            for (String field : fields) {
                attributes.add(new Attribute(priorities, field));
            }

            attributesTable.setItems(attributes);

            tableData = attributes;

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sortTable();
                }
            }, 0, 100);
        } else {
            attributesTable.setItems(tableData);
        }
    }

    private void sortTable() {
        Map<Attribute, Integer> prio = new HashMap<Attribute, Integer>();

        for (Attribute att : attributesTable.getItems()) {
            if (att.getPriority().getValue().equals("-")) {
                prio.put(att, 999);
            } else {
                att.setSelected();
                prio.put(att, Integer.parseInt(att.getPriority().getValue().toString()));
            }
        }

        Map<Attribute, Integer> sortedPrio = prio.entrySet().stream().sorted(comparingByValue()).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        ObservableList<Attribute> attributes = FXCollections.observableArrayList();
        for (Map.Entry<Attribute, Integer> entry : sortedPrio.entrySet()) {
            attributes.add(entry.getKey());
        }

        attributesTable.setItems(attributes);

        tableData = attributes;
    }

    @FXML
    private void goBack() {
        try {
            timer.cancel();

            tableData = FXCollections.observableArrayList();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ImportData.fxml"));

            Parent root1 = (Parent) fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("PrioPrivacy - Import Data");
            stage.setScene(new Scene(root1));
            stage.show();

            Stage current = (Stage) attributesTable.getScene().getWindow();
            current.close();
        } catch (IOException ex) {
            Logger.getLogger(QIsPrioritiesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void goNext() {
        timer.cancel();

        loadingVBox.setVisible(true);

        loadData();

        Stage current = (Stage) attributesTable.getScene().getWindow();
        current.close();

    }

    private void loadData() {
        BufferedReader br = null;
        try {
            separator = ImportDataController.getSeparator();
            QIs = new HashMap<String, Integer>();
            dataset = new HashMap<Integer, String>();
            QIsDomainsMap = new HashMap<Integer, List<String>>();
            priorities = new HashMap<Integer, List<Integer>>();

            // get QIs            
            for (Attribute attribute : tableData) {
                if (attribute.getQI().isSelected()) {
                    QIs.put(attribute.getName(), 0);
                }
            }

            br = new BufferedReader(new InputStreamReader(new FileInputStream(ImportDataController.getInput()), "UTF8"));

            // map QIs to integers
            List<Integer> fieldsToRead = new ArrayList<Integer>();
            String header[] = br.readLine().split(separator);
            int index = 0;
            for (int i = 0; i < header.length; i++) {
                if (QIs.containsKey(header[i])) {
                    QIs.put(header[i], index++);
                    fieldsToRead.add(i);
                }
            }

            // create dataset            
            int row_index = 0;
            String line;
            while ((line = br.readLine()) != null) {
                String fields[] = line.split(separator);

                String row = "";
                for (int i = 0; i < fields.length; i++) {
                    if (fieldsToRead.contains(i)) {
                        row += fields[i] + ";";
                    }
                }
                row = row.substring(0, row.length() - 1);

                dataset.put(row_index++, row);
            }

            br.close();

            for (int QI : QIs.values()) {
                QIsDomainsMap.put(QI, new ArrayList<String>());
            }

            for (String row : dataset.values()) {
                String fields[] = row.split(separator);

                for (int i = 0; i < fields.length; i++) {
                    if (!QIsDomainsMap.get(i).contains(fields[i])) {
                        List<String> list = QIsDomainsMap.get(i);
                        list.add(fields[i]);
                        QIsDomainsMap.put(i, list);
                    }
                }
            }

            for (Attribute attribute : tableData) {
                if (QIs.containsKey(attribute.getName())) {
                    List<Integer> prios;

                    int prio = Integer.parseInt((String) attribute.getPriority().getValue());

                    if (priorities.containsKey(prio)) {
                        prios = priorities.get(prio);
                    } else {
                        prios = new ArrayList<Integer>();
                    }

                    prios.add(QIs.get(attribute.getName()));

                    priorities.put(prio, prios);
                }
            }

            priorities = priorities.entrySet().stream().sorted(comparingByKey()).collect(toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2));

            for (int QI : QIsDomainsMap.keySet()) {
                List<String> values = QIsDomainsMap.get(QI);
                java.util.Collections.sort(values);
                QIsDomainsMap.put(QI, values);
            }

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("GeneralisationHierarchies.fxml"));

            Parent root1 = (Parent) fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("PrioPrivacy - Generalisation Hierarchies and Anonymisation");
            stage.setScene(new Scene(root1));
            stage.setX((Screen.getPrimary().getVisualBounds().getWidth() - stage.getWidth()) / 2);
            stage.setY((Screen.getPrimary().getVisualBounds().getHeight() - stage.getHeight()) / 2);
            stage.show();

            fxmlLoader = new FXMLLoader(getClass().getResource("QIsExploration.fxml"));

            root1 = (Parent) fxmlLoader.load();

            stage = new Stage();
            stage.setTitle("PrioPrivacy - QIs Risk Exploration");
            stage.setScene(new Scene(root1));
            stage.show();

//            for (String QI : QIs.keySet()) {
//                System.out.println(QIs.get(QI) + "\t" + QI);
//            }
//
//            for (int QI : QIsDomainsMap.keySet()) {
//                System.out.println(QIsDomainsMap.get(QI));
//            }
//
//            for (int i = 0; i < 10; i++) {
//                System.out.println(dataset.get(i));
//            }
//
//            for (int p : priorities.keySet()) {
//                System.out.println(p + "\t" + priorities.get(p));
//            }           
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GeneralisationHierarchiesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GeneralisationHierarchiesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(GeneralisationHierarchiesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static Map<String, Integer> getQIs() {
        return QIs;
    }

    public static Map<Integer, List<Integer>> getPriorities() {
        return priorities;
    }

    public static Map<Integer, String> getDataset() {
        return dataset;
    }

    public static Map<Integer, List<String>> getQIsDomainsMap() {
        return QIsDomainsMap;
    }

}
