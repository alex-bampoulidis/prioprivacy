package prioprivacy;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

public class Attribute {

    private CheckBox QI;
    private ComboBox priority;
    private String name;

    public Attribute(ObservableList<String> items, String name) {
        QI = new CheckBox();

        priority = new ComboBox(items);
        priority.setValue(items.get(0));

        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ComboBox getPriority() {
        return priority;
    }

    public CheckBox getQI() {
        return QI;
    }

    public void setQI(CheckBox QI) {
        this.QI = QI;
    }

    public void setSelected() {
        this.QI.setSelected(true);
    }

}
