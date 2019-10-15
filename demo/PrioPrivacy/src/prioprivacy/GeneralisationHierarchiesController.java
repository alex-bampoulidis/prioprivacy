package prioprivacy;

import com.sun.javafx.stage.StageHelper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static java.util.Map.Entry.comparingByValue;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toMap;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GeneralisationHierarchiesController implements Initializable {

    @FXML
    private Accordion uniqueValuesAccordion;

    @FXML
    private TabPane generalisationsTPane;

    @FXML
    private Slider kSlider;

    @FXML
    private ProgressBar anonymisationProgressBar;

    @FXML
    private Label anonymisationProgressLabel;

    private static Map<String, Map<Integer, String>> generalisations;

    private static String currentQI;

    private static Map<String, ObservableList<Tab>> QIsTPanes;

    public static Timeline timeline;

    public static Timeline anonymisationProgress;

    private static Map<Integer, String> anonymisedDataset;

    private static File output;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Map<String, Integer> QIs = QIsPrioritiesController.getQIs();
        Map<Integer, List<String>> QIsDomainsMap = QIsPrioritiesController.getQIsDomainsMap();
        generalisations = new HashMap<String, Map<Integer, String>>();
        QIsTPanes = new HashMap<String, ObservableList<Tab>>();

        for (String QI : QIs.keySet()) {
            ScrollPane sPane = new ScrollPane();
            AnchorPane aPaneSPane = new AnchorPane();

            VBox vbox = new VBox();

            for (String value : QIsDomainsMap.get(QIs.get(QI))) {
                vbox.getChildren().add(new Label(value));
            }

            aPaneSPane.getChildren().add(vbox);

            sPane.setContent(aPaneSPane);

            TitledPane tPane = new TitledPane(QI, sPane);

            uniqueValuesAccordion.getPanes().add(tPane);

            generalisations.put(QI, new HashMap<Integer, String>());

            QIsTPanes.put(QI, generalisationsTPane.getTabs());
        }

        uniqueValuesAccordion.setExpandedPane(uniqueValuesAccordion.getPanes().get(0));
        currentQI = uniqueValuesAccordion.getExpandedPane().getText();

        timeline = new Timeline(new KeyFrame(Duration.millis(100), ev -> {
            checkCurrentQI();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    @FXML
    private void addLevel() {
        AnchorPane aPane = new AnchorPane();

        VBox vbox = new VBox();

        Button importButton = new Button("Import Hierarchy...");
        importButton.setOnAction(e -> {
            importHierarchy();
        });

        vbox.getChildren().add(importButton);

        ScrollPane sPane = new ScrollPane();
        AnchorPane aPaneSPane = new AnchorPane();

        TextArea tArea = new TextArea();
        tArea.setPrefWidth(generalisationsTPane.getWidth());
        tArea.setPrefHeight(generalisationsTPane.getHeight());
        tArea.setId(currentQI + "Level" + generalisationsTPane.getTabs().size() + 1);

        aPaneSPane.getChildren().add(tArea);

        sPane.setContent(aPaneSPane);

        vbox.getChildren().add(sPane);

        aPane.getChildren().add(vbox);

        Tab tab = new Tab("Level " + (generalisationsTPane.getTabs().size() + 1), aPane);

        generalisationsTPane.getTabs().add(tab);
        generalisationsTPane.getSelectionModel().selectLast();
    }

    @FXML
    private void removeLevel() {
        generalisationsTPane.getTabs().remove(generalisationsTPane.getSelectionModel().getSelectedItem());

        for (int i = 0; i < generalisationsTPane.getTabs().size(); i++) {
            generalisationsTPane.getTabs().get(i).setText("Level " + (i + 1));
            generalisationsTPane.getTabs().get(i).setId(currentQI + "Level " + (i + 1));
        }
    }

    @FXML
    private void goBack() {
        try {
            timeline.stop();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("QIsPriorities.fxml"));

            Parent root1 = (Parent) fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("PrioPrivacy - QIs and Priorities");
            stage.setScene(new Scene(root1));
            stage.show();

            Stage current = (Stage) generalisationsTPane.getScene().getWindow();
            current.close();

            StageHelper.getStages().get(0).close();
        } catch (IOException ex) {
            Logger.getLogger(GeneralisationHierarchiesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void anonymise() {
        FileChooser fileChooser = new FileChooser();
        output = fileChooser.showSaveDialog(null);

        if (output != null) {
            Map<Integer, List<Integer>> priorities = QIsPrioritiesController.getPriorities();
            Map<Integer, String> dataset = QIsPrioritiesController.getDataset();
            int k = (int) kSlider.getValue();
            Map<Integer, List<String>> QIsDomainsMap = QIsPrioritiesController.getQIsDomainsMap();
            Map<Integer, Map<Integer, List<String>>> levelsQIsRulesMap = new HashMap<Integer, Map<Integer, List<String>>>();

            for (String QI : QIsTPanes.keySet()) {
                for (int i = 0; i < QIsTPanes.get(QI).size(); i++) {
                    Tab tab = QIsTPanes.get(QI).get(i);

                    VBox vbox = (VBox) ((Parent) tab.getContent()).getChildrenUnmodifiable().get(0);
                    ScrollPane sPane = (ScrollPane) vbox.getChildren().get(1);
                    AnchorPane aPane = (AnchorPane) sPane.getContent();
                    TextArea tArea = (TextArea) aPane.getChildren().get(0);

                    int level = i + 1;
                    int QIIndex = QIsPrioritiesController.getQIs().get(QI);

                    List<String> rules = new ArrayList<String>();

                    for (String rule : tArea.getText().split("\n")) {
                        rules.add(QIIndex + ":" + rule);
                    }

                    Map<Integer, List<String>> QIsRulesMap;
                    if (!levelsQIsRulesMap.containsKey(level)) {
                        QIsRulesMap = new HashMap<Integer, List<String>>();
                    } else {
                        QIsRulesMap = levelsQIsRulesMap.get(level);
                    }
                    QIsRulesMap.put(QIIndex, rules);

                    levelsQIsRulesMap.put(level, QIsRulesMap);
                }
            }

            anonymisationProgress = new Timeline(new KeyFrame(Duration.millis(1000), ev -> {
                checkAnonymisationProgress();
            }));
            anonymisationProgress.setCycleCount(Animation.INDEFINITE);
            anonymisationProgress.play();

            try {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            anonymisedDataset = prioprivacy.algorithm.PrioPrivacy.Algorithm(priorities, levelsQIsRulesMap, dataset, k, QIsDomainsMap);
                        } catch (Exception ex) {
                            Logger.getLogger(GeneralisationHierarchiesController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };

                new Thread(runnable).start();
//            try {
//                anonymisedDataset = prioprivacy.algorithm.PrioPrivacy.Algorithm(priorities, levelsQIsRulesMap, dataset, k, QIsDomainsMap);
//        }catch (Exception ex) {
//                Logger.getLogger(GeneralisationHierarchiesController.class.getName()).log(Level.SEVERE, null, ex);
////            }
            } catch (Exception ex) {
                Logger.getLogger(GeneralisationHierarchiesController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private void checkCurrentQI() {
        if (uniqueValuesAccordion.getExpandedPane() != null) {
            if (!uniqueValuesAccordion.getExpandedPane().getText().equals(currentQI)) {
                timeline.pause();

                ObservableList<Tab> tabs = FXCollections.observableArrayList();
                for (Tab tab : generalisationsTPane.getTabs()) {
                    tabs.add(tab);
                }
                QIsTPanes.put(currentQI, tabs);

                generalisationsTPane.getTabs().removeAll(generalisationsTPane.getTabs());

                currentQI = uniqueValuesAccordion.getExpandedPane().getText();

                generalisationsTPane.getTabs().addAll(QIsTPanes.get(currentQI));

                timeline.play();
            }
        }
    }

    private void importHierarchy() {
        FileChooser fileChooser = new FileChooser();
        File input = fileChooser.showOpenDialog(null);

        if (input != null) {
            BufferedReader br = null;
            try {
                String hier = "";

                br = new BufferedReader(new InputStreamReader(new FileInputStream(input), "UTF8"));

                String line;
                while ((line = br.readLine()) != null) {
                    hier += line + "\n";
                }

                br.close();

                AnchorPane aPane = new AnchorPane();

                VBox vbox = new VBox();

                Button importButton = new Button("Import Hierarchy...");
                importButton.setOnAction(e -> {
                    importHierarchy();
                });

                vbox.getChildren().add(importButton);

                ScrollPane sPane = new ScrollPane();
                AnchorPane aPaneSPane = new AnchorPane();

                TextArea tArea = new TextArea();
                tArea.setPrefWidth(generalisationsTPane.getWidth());
                tArea.setPrefHeight(generalisationsTPane.getHeight());
                tArea.setText(hier);

                aPaneSPane.getChildren().add(tArea);

                sPane.setContent(aPaneSPane);

                vbox.getChildren().add(sPane);

                aPane.getChildren().add(vbox);

                generalisationsTPane.getTabs().get(generalisationsTPane.getSelectionModel().getSelectedIndex()).setContent(aPane);

            } catch (FileNotFoundException ex) {
                Logger.getLogger(GeneralisationHierarchiesController.class
                        .getName()).log(Level.SEVERE, null, ex);

            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(GeneralisationHierarchiesController.class
                        .getName()).log(Level.SEVERE, null, ex);

            } catch (IOException ex) {
                Logger.getLogger(GeneralisationHierarchiesController.class
                        .getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    br.close();

                } catch (IOException ex) {
                    Logger.getLogger(GeneralisationHierarchiesController.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void checkAnonymisationProgress() {
        int unsafe = prioprivacy.algorithm.PrioPrivacy.unsafe;
        
        if (unsafe != -1) {
            int total = QIsPrioritiesController.getDataset().size();
            int safeRecords = total - unsafe;

            anonymisationProgressBar.setProgress((double) safeRecords / total);
            anonymisationProgressLabel.setText(safeRecords + " / " + total + " are anonymised");

            anonymisationProgressBar.setVisible(true);
            anonymisationProgressLabel.setVisible(true);

            if (unsafe == 0) {
                BufferedWriter bw = null;
                try {
                    anonymisationProgress.stop();

                    bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), StandardCharsets.UTF_8));

                    String header = "";

                    Map<String, Integer> QIs = QIsPrioritiesController.getQIs();
                    QIs = QIs.entrySet().stream().sorted(comparingByValue()).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

                    for (String QI : QIs.keySet()) {
                        header += QI + ";";
                    }
                    header = header.substring(0, header.length() - 1);

                    bw.write(header + "\n");
                    for (int id : anonymisedDataset.keySet()) {
                        bw.write(anonymisedDataset.get(id) + "\n");
                    }

                    bw.close();

                    anonymisationProgressLabel.setText("Anonymisation completed");

                } catch (FileNotFoundException ex) {
                    Logger.getLogger(GeneralisationHierarchiesController.class
                            .getName()).log(Level.SEVERE, null, ex);

                } catch (IOException ex) {
                    Logger.getLogger(GeneralisationHierarchiesController.class
                            .getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        bw.close();

                    } catch (IOException ex) {
                        Logger.getLogger(GeneralisationHierarchiesController.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
}
