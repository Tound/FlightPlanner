package main;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CreateUAV {
    private static Stage createStage;
    private static Scene createScene;
    private static String path = "src/UAVs";
    private static TextField name = new TextField();
    private static TextField weight = new TextField();
    private static TextField turnRadius = new TextField();
    private static TextField maxIncline = new TextField();
    private static TextField battery = new TextField();
    private static TextField batteryCapacity = new TextField();

    public CreateUAV(){
        createStage = new Stage();
        createStage.setTitle("UAV Setup");
        createStage.initModality(Modality.APPLICATION_MODAL);
    }

    public static void newUAV(){
        GridPane gp = new GridPane();
        Text title =  new Text("UAV\nSettings");
        title.setTextAlignment(TextAlignment.CENTER);
        title.setId("title");
        GridPane.setColumnSpan(title,3);
        GridPane.setHalignment(title, HPos.CENTER);

        gp.setId("gridpane");
        gp.getStyleClass().add("grid");

        Label nameLabel =  new Label("Name:");
        Label weightLabel =  new Label("Weight (g):");
        Label turnRadiusLabel =  new Label("Turn Radius (m):");
        Label maxInclineLabel =  new Label("Max incline/decline angle (deg):");
        Label batteryLabel =  new Label("Battery Type:");
        Label batteryCapacityLabel =  new Label("Battery Capacity (mAh):");

        name.setText("Name of craft");
        weight.setText("Weight of craft");
        turnRadius.setText("Min turn radius of craft");
        maxIncline.setText("Max incline/decline angle");
        battery.setText("Battery used in craft (Lipo, NiMH, Li-ion...)");
        batteryCapacity.setText("Battery capacity in mAh");

        Button save = new Button("Save");
        Button cancel = new Button("Cancel");
        Button def = new Button("Set to default");

        gp.add(title,0,0);
        //title.setFont(Font.font("Arial", FontPosture.ITALIC, 24));
        gp.add(name,1,1);
        gp.add(weight,1,2);
        gp.add(turnRadius,1,3);
        gp.add(maxIncline,1,4);
        gp.add(battery,1,5);
        gp.add(batteryCapacity,1,6);

        gp.add(nameLabel,0,1);
        gp.add(weightLabel,0,2);
        gp.add(turnRadiusLabel,0,3);
        gp.add(maxInclineLabel,0,4);
        gp.add(batteryLabel,0,5);
        gp.add(batteryCapacityLabel,0,6);

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

    public void loadUAV(){




    }


}
