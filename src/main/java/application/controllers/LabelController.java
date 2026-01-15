package application.controllers;

import application.components.LabelCard;
import application.models.Label;
import application.services.LabelService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LabelController implements Initializable {

    @FXML private TextField txtLabelName;
    @FXML private ColorPicker colorPicker;
    @FXML private Button btnAddLabel;
    @FXML private FlowPane labelsFlowPane;

    private final LabelService labelService = new LabelService();

    @Override
    public void initialize(URL location, ResourceBundle resource){
        loadLabels();
        btnAddLabel.setOnAction(event -> handleAddLabel());
    }

    private void handleAddLabel(){
        String name = txtLabelName.getText();
        if(name.isEmpty()) return;
        Color c = colorPicker.getValue();
        String hexColor=String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));

        Label newLabel = new Label(name, hexColor);

        if(labelService.addLabel(newLabel)){
            txtLabelName.clear();
            loadLabels();
        }
    }

    private void loadLabels(){
        labelsFlowPane.getChildren().clear();
        List<Label> labels = labelService.getAllLabels();
        for(Label label : labels){
            LabelCard card = new LabelCard(label, this::loadLabels);
            card.setPrefWidth(250);
            labelsFlowPane.getChildren().add(card);
        }
    }

}
