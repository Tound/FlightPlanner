package main;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CreateCamera {

    private static Stage createStage;
    private static Scene createScene;
    private static String path = "src/UAVs";
    private static TextField name = new TextField();
    private static TextField sensorX = new TextField();
    private static TextField sensorY = new TextField();
    private static TextField focalLocal = new TextField();
    private static TextField resolution = new TextField();
    private static TextField aspectRatio = new TextField();

    public CreateCamera(){
        createStage = new Stage();
        createStage.setTitle("Camera Setup");
        createStage.initModality(Modality.APPLICATION_MODAL);
    }

    public void newCamera(){
        GridPane gp = new GridPane();
        Text title =  new Text("Create a Camera:\nCamera Settings");
        title.setTextAlignment(TextAlignment.CENTER);
        title.setId("title");
        GridPane.setColumnSpan(title,3);
        GridPane.setHalignment(title, HPos.CENTER);

        gp.setId("gridpane");
        gp.getStyleClass().add("grid");

        Label nameLabel = new Label("Name:");
        Label sensorXLabel = new Label("Sensor Width (mm):");
        Label sensorYLabel = new Label("Sensor height (mm):");
        Label focalLengthLabel = new Label("Focal Length:");
        Label resolutionLabel = new Label("Resolution:");
        Label aspectRatioLabel = new Label("Aspect Ratio:");

        name.setPromptText("Name of Camera");
        sensorX.setPromptText("Sensor Width (mm)");
        sensorY.setPromptText("Sensor Height (mm)");
        focalLocal.setPromptText("Focal Length");
        resolution.setPromptText("Resolution (MP)");
        aspectRatio.setPromptText("Aspect Ratio");

        Button save = new Button("Save");
        Button cancel = new Button("Cancel");
        Button def = new Button("Set to default");

        gp.add(title,0,0);

        gp.add(nameLabel,0,1);
        gp.add(sensorXLabel,0,2);
        gp.add(sensorYLabel,0,3);
        gp.add(focalLengthLabel,0,4);
        gp.add(resolutionLabel,0,5);
        gp.add(aspectRatioLabel,0,6);


        gp.add(name,1,1);
        gp.add(sensorX,1,2);
        gp.add(sensorY,1,3);
        gp.add(focalLocal,1,4);
        gp.add(resolution,1,5);
        gp.add(aspectRatio,1,6);

        gp.add(save,0,7);
        gp.add(cancel,2,7);
        gp.add(def,1,7);

        gp.setHgap(10);
        gp.setVgap(20);
        gp.setAlignment(Pos.CENTER);

        //gp.setStyle("-fx-background-color: rgb(42, 45, 48)");

        createScene = new Scene(gp,500,700, Color.GRAY);
        createScene.setUserAgentStylesheet("style/menus.css");
        createStage.setScene(createScene);
        createStage.show();
    }

    public String loadCamera(){
        return "Camera";
    }
}
