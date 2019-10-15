package prioprivacy;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;

public class QIsExplorationController implements Initializable {

    @FXML
    private Slider kSlider;

    @FXML
    private ProgressBar riskBar;

    @FXML
    private Label riskLabel;

    @FXML
    private ComboBox QIsComboBox;

    @FXML
    private StackedBarChart QIsBarChart;

    @FXML
    private CategoryAxis catAxis;

    @FXML
    private NumberAxis numAxis;

    private static Map<String, Integer> rowKAnonymityMap;

    private static Map<String, Integer> safe;

    private static Map<String, Integer> unsafe;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Set<String> QIs = QIsPrioritiesController.getQIs().keySet();

        QIsComboBox.getItems().clear();
        for (String QI : QIs) {
            QIsComboBox.getItems().add(QI);
        }
    }

    @FXML
    private void analyseRisk() {
        int k = (int) kSlider.getValue();

        riskBar.setProgress(-1);
        riskBar.setVisible(true);

        Map<Integer, String> dataset = QIsPrioritiesController.getDataset();

        rowKAnonymityMap = new HashMap<String, Integer>();

        for (int id : dataset.keySet()) {
            String row = dataset.get(id);

            if (rowKAnonymityMap.containsKey(row)) {
                rowKAnonymityMap.put(row, rowKAnonymityMap.get(row) + 1);
            } else {
                rowKAnonymityMap.put(row, 1);
            }
        }

        int unsafe = 0;
        for (String row : rowKAnonymityMap.keySet()) {
            int records = rowKAnonymityMap.get(row);

            if (records < k) {
                unsafe += records;
            }
        }

        double risk = unsafe / (double) dataset.size();

        riskBar.setProgress(risk);

        riskLabel.setText(new DecimalFormat("#0.00").format(risk * 100) + "% of the records are unsafe");
        riskLabel.setVisible(true);
    }

    @FXML
    private void plotQI() {
        if (rowKAnonymityMap == null) {
            analyseRisk();
        }

        QIsBarChart.getData().clear();

        String QI = (String) QIsComboBox.getSelectionModel().getSelectedItem();

        int QIIndex = QIsPrioritiesController.getQIs().get(QI);

        int k = (int) kSlider.getValue();

        safe = new HashMap<String, Integer>();
        unsafe = new HashMap<String, Integer>();
        List<String> values = new ArrayList<String>();

        for (String row : rowKAnonymityMap.keySet()) {
            String value = row.split(";")[QIIndex];

            if (!values.contains(value)) {
                values.add(value);
            }

            int records = rowKAnonymityMap.get(row);

            if (records > k) {
                if (safe.containsKey(value)) {
                    safe.put(value, safe.get(value) + records);
                } else {
                    safe.put(value, records);
                }
            } else {
                if (unsafe.containsKey(value)) {
                    unsafe.put(value, unsafe.get(value) + records);
                } else {
                    unsafe.put(value, records);
                }
            }
        }

        java.util.Collections.sort(values);

        catAxis.setLabel(QI);
        numAxis.setLabel("Count");

        catAxis.getCategories().clear();
        catAxis.setCategories(FXCollections.<String>observableArrayList(values));

        Series<String, Number> safeSeries = new Series<String, Number>();
        safeSeries.setName("safe");

        Series<String, Number> unsafeSeries = new Series<String, Number>();
        unsafeSeries.setName("at risk");

        for (String value : safe.keySet()) {
            safeSeries.getData().add(new Data<String, Number>(value, safe.get(value)));
        }

        for (String value : unsafe.keySet()) {
            unsafeSeries.getData().add(new Data<String, Number>(value, unsafe.get(value)));
        }

        QIsBarChart.getData().addAll(safeSeries, unsafeSeries);
    }

    @FXML
    private void flip() {
        if (catAxis.getSide().equals(Side.BOTTOM)) {
            Series<Number, String> safeSeries = new Series<Number, String>();
            safeSeries.setName("safe");

            Series<Number, String> unsafeSeries = new Series<Number, String>();
            unsafeSeries.setName("at risk");

            for (String value : safe.keySet()) {
                safeSeries.getData().add(new Data<Number, String>(safe.get(value), value));
            }

            for (String value : unsafe.keySet()) {
                unsafeSeries.getData().add(new Data<Number, String>(unsafe.get(value), value));
            }

            QIsBarChart.getData().clear();
            
            catAxis.setSide(Side.LEFT);
            numAxis.setSide(Side.BOTTOM);
            
            QIsBarChart = new StackedBarChart<Number, String>(numAxis, catAxis);

            QIsBarChart.getData().addAll(safeSeries, unsafeSeries);

        } else {
            Series<String, Number> safeSeries = new Series<String, Number>();
            safeSeries.setName("safe");

            Series<String, Number> unsafeSeries = new Series<String, Number>();
            unsafeSeries.setName("at risk");

            for (String value : safe.keySet()) {
                safeSeries.getData().add(new Data<String, Number>(value, safe.get(value)));
            }

            for (String value : unsafe.keySet()) {
                unsafeSeries.getData().add(new Data<String, Number>(value, unsafe.get(value)));
            }

            QIsBarChart.getData().clear();
            
            catAxis.setSide(Side.BOTTOM);
            numAxis.setSide(Side.LEFT);

            QIsBarChart.getData().addAll(safeSeries, unsafeSeries);
        }
    }

}
