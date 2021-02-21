package main;

import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class FlightSettings {
    private static ScrollPane sp;

    public static ScrollPane setupFlight(){

        CreateUAV createUAV = new CreateUAV();
        CreateCamera createCamera = new CreateCamera();

        GridPane gp = new GridPane();
        //Titles
        Text flightSettingsTitle = new Text("Flight Setup");
        flightSettingsTitle.setId("subtitle");
        Text uavSettings = new Text("UAV Setup");
        Text cameraSettings = new Text("Camera Setup");
        Text flightSettings = new Text("Flight Settings");

        uavSettings.setId("subsubtitle");
        cameraSettings.setId("subsubtitle");
        flightSettings.setId("subsubtitle");

        Button run = new Button("Run!");
        run.setPrefWidth(gp.getMaxWidth());
        run.setAlignment(Pos.CENTER);
        run.setMaxWidth(Double.MAX_VALUE);

        GridPane.setColumnSpan(run,3);
        GridPane.setHalignment(run,HPos.CENTER);
        run.setId("run-button");

        Button save = new Button("Save Settings");
        Button load = new Button("Load settings");
        Button defaultSettings = new Button("Default");

        Button newUAV = new Button("Create UAV");
        Button editUAV = new Button("Edit UAV");
        Button loadUAV = new Button("Load UAV");

        Button newCam = new Button("Create Camera");
        Button editCam = new Button("Edit Camera");
        Button loadCam = new Button("Load Camera");

        Label chosenUav = new Label("UAV: ");
        Label chosenCamera = new Label("Camera:");
        //UAV Settings - Labels
        Label uavWeight = new Label("Weight:");
        Label uavMinRad = new Label("Turning Radius:");
        Label uavBattery = new Label("Battery Type");
        Label uavBatteryCapacity = new Label("Battery Capacity:");

        Label weight = new Label();
        Label minRad = new Label();
        Label batteryType = new Label();
        Label batterySettings = new Label();

        //Camera Settings
        Label camReso = new Label("Resolution:");
        Label camAp = new Label("Aperture:");
        Label camShutterSpeed = new Label("Shutter Speed");
        Label camWeight = new Label("Camera Weight");

        //Flight Settings
        Label uavSpeed = new Label("UAV Speed (Constant):");
        Label uavAltitude = new Label("Altitude:");
        Label uavPasses =  new Label("Passes");

        Label forwardOverlap = new Label("Forward Overlap (%)");
        Label sideOverlap = new Label("Side Overlap (%)");
        Label imageResolution = new Label("Imaging resolution");

        ComboBox uav = new ComboBox();
        ComboBox camera = new ComboBox();

        gp.setVgap(10);
        gp.setHgap(0);
        //Title
        gp.add(flightSettingsTitle, 0,0);
        //UAV settings
        gp.add(uavSettings, 0,1);
        gp.add(chosenUav, 0,2);
        gp.add(uav, 1,2);
        gp.add(uavWeight,0,3);
        gp.add(weight,1,3);
        gp.add(uavMinRad,0,4);
        gp.add(minRad,1,4);
        gp.add(uavBattery,0,5);
        gp.add(batteryType,1,5);
        gp.add(uavBatteryCapacity,0,5);
        gp.add(batterySettings,0,5);
        gp.add(newUAV,0,6);
        gp.add(editUAV,1,6);

        //Camera settings
        gp.add(cameraSettings,0,7);
        gp.add(chosenCamera, 0,8);
        gp.add(camera, 1,9);
        gp.add(camReso,0,10);
        gp.add(camAp,0,11);
        gp.add(camShutterSpeed,0,12);
        gp.add(camWeight,0,13);
        gp.add(newCam,0,14);
        gp.add(editCam,1,14);

        //Flight settings
        gp.add(flightSettings,0,15);
        gp.add(uavSpeed, 0,16);
        gp.add(forwardOverlap, 0,17);
        gp.add(sideOverlap,0,18);


        gp.add(save,0,19);
        gp.add(defaultSettings,1,19);
        gp.add(load,2,19);
        gp.add(run,0,20);

        /*for(int i=0 ;i<gp.getChildren().size();i++){
            GridPane.setHalignment(gp.getChildren().get(i), HPos.CENTER);
        }*/

        newUAV.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                createUAV.newUAV();
            }
        });
        newCam.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                createCamera.newCamera();
            }
        });

        loadUAV.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                createUAV.loadUAV();
            }
        });

        loadCam.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                createCamera.loadCamera();
            }
        });
        gp.setPadding(new Insets(5,5,5,10));
        sp = new ScrollPane(gp);
        sp.setMaxWidth(Double.MAX_VALUE);
        return sp;
    }
}
