package application.components;

import application.models.Label;
import application.services.LabelService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class LabelCard extends HBox {

    private final Label label;
    private final Runnable onDeleteAction;
    private final LabelService labelService;

    public LabelCard(Label label, Runnable onDeleteAction) {
        this.label = label;
        this.onDeleteAction = onDeleteAction;
        labelService = new LabelService();

        this.setAlignment(Pos.CENTER_LEFT);
        this.setSpacing(10);
        this.setPrefWidth(Region.USE_COMPUTED_SIZE);

        HBox labelContainer = new HBox();
        labelContainer.setAlignment(Pos.CENTER);
        labelContainer.setPadding(new javafx.geometry.Insets(8,15,8,15));

        String hexColor = label.getColorCode();

        javafx.scene.control.Label textLbl = new javafx.scene.control.Label(label.getTitle());
        textLbl.setStyle("-fx-text-fill: " + hexColor + "; -fx-font-weight: bold; -fx-font-size: 14px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        labelContainer.getChildren().add(textLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDelete = new Button("✕");
        btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #999; -fx-cursor: hand; -fx-font-size: 12px;");

        btnDelete.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) btnDelete.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #FF5252; -fx-background-radius: 50%;");
            else btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #999;");
        });

        btnDelete.setOnAction(e -> {
            if(labelService.deleteLabel(label.getId())) {
                if(onDeleteAction != null) onDeleteAction.run();
            }
        });

        this.setStyle("-fx-background-color: " + hexColor + "33;; -fx-background-radius: 10; -fx-padding: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 2);");

        this.getChildren().addAll(labelContainer,spacer,btnDelete);
    }

}
