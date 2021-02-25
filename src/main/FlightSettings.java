package main;

import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class FlightSettings {
    private static ScrollPane sp;

    private final String uav_path = "src/UAVs";
    private final String cam_path = "src/cameras";
    private final String settings_path = "src/flight_settings";
    private final String project_path = "src/projects";

    private static TextField uavName = new TextField();
    private static TextField uavWeight = new TextField();
    private static TextField uavMinRadius = new TextField();
    private static TextField uavMaxIncline = new TextField();
    private static TextField uavBattery = new TextField();
    private static TextField uavBatteryCapacity = new TextField();

    private static TextField camName = new TextField();
    private static TextField camSensorX = new TextField();
    private static TextField camSensorY = new TextField();
    private static TextField camFocalLength = new TextField();
    private static TextField camResolution = new TextField();
    private static TextField camAspectRatio = new TextField();

    private static TextField uavSpeed = new TextField();
    private static TextField windSpeed = new TextField();
    private static TextField windDirection = new TextField();
    private static TextField altitude = new TextField();
    private static TextField forwardOverlap = new TextField();
    private static TextField sideOverlap = new TextField();
    private static TextField coverageResolution = new TextField();

    private static String defaultUavSpeed = "20";
    private static String defaultForwardOverlap = "40";
    private static String defaultSideOverlay = "60";
    private static String defaultCoverageResolution = "0.02";
    private static String defaultAltitude = "120";

    public FlightSettings(){
        uavName.setPromptText("Name of UAV");
        uavWeight.setPromptText("Weight of UAV (Kg)");
        uavMinRadius.setPromptText("Minimum Radius (m)");
        uavMaxIncline.setPromptText("Max incline angle (degrees)");
        uavBattery.setPromptText("Battery Type");
        uavBatteryCapacity.setPromptText("Battery Capacity (mAh)");

        camName.setPromptText("Name of Cam");
        camSensorX.setPromptText("Sensor Width (mm)");
        camSensorY.setPromptText("Sensor Height (mm)");
        camFocalLength.setPromptText("Focal Length (mm)");
        camResolution.setPromptText("Resolution (MP)");
        camAspectRatio.setPromptText("Aspect Ratio (X:Y)");

        uavSpeed.setPromptText("UAV Speed (Constant m/s)");
        windSpeed.setPromptText("Wind Speed (Constant, Uniform)");
        windDirection.setPromptText("Wind Direction (Bearing)");
        altitude.setPromptText("Uav altitude (Calculated)");
        forwardOverlap.setPromptText("Forward Overlap %");
        sideOverlap.setPromptText("Side Overlap %");
        coverageResolution.setPromptText("Coverage Resolution (metres/pixel)");

    }

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

        GridPane.setColumnSpan(uavName,2);
        GridPane.setColumnSpan(uavWeight,2);
        GridPane.setColumnSpan(uavMinRadius,2);
        GridPane.setColumnSpan(uavMaxIncline,2);
        GridPane.setColumnSpan(uavBattery,2);
        GridPane.setColumnSpan(uavBatteryCapacity,2);

        GridPane.setColumnSpan(camName,2);
        GridPane.setColumnSpan(camSensorX,2);
        GridPane.setColumnSpan(camSensorY,2);
        GridPane.setColumnSpan(camFocalLength,2);
        GridPane.setColumnSpan(camResolution,2);
        GridPane.setColumnSpan(camAspectRatio,2);

        GridPane.setColumnSpan(uavSpeed,2);
        GridPane.setColumnSpan(windSpeed,2);
        GridPane.setColumnSpan(windDirection,2);
        GridPane.setColumnSpan(altitude,2);
        GridPane.setColumnSpan(forwardOverlap,2);
        GridPane.setColumnSpan(sideOverlap,2);
        GridPane.setColumnSpan(coverageResolution,2);

        // BUTTONS
        Button save = new Button("Save Settings");
        Button load = new Button("Load settings");
        Button defaultSettings = new Button("Default");

        Button newUAV = new Button("Create UAV");
        Button editUAV = new Button("Edit UAV");
        Button loadUAV = new Button("Load UAV");

        Button newCam = new Button("Create Camera");
        Button editCam = new Button("Edit Camera");
        Button loadCam = new Button("Load Camera");

        // ALL LABELS
        Label uavNameLabel = new Label("UAV: ");
        Label camNameLabel = new Label("Camera:");

        //UAV Settings - Labels
        Label uavWeightLabel = new Label("Weight:");
        Label uavMinRadLabel = new Label("Turning Radius:");
        Label uavMaxInclineLabel = new Label("Max Incline Angle:");
        Label uavBatteryLabel = new Label("Battery Type");
        Label uavBatteryCapacityLabel = new Label("Battery Capacity:");

        //Camera Settings
        Label camResoLabel = new Label("Resolution (MP):");
        Label camFocalLengthLabel = new Label("Focal Length:");
        Label camSensorXLabel = new Label("Sensor Width:");
        Label camSensorYLabel = new Label("Sensor Height:");
        Label camAspectRatioLabel = new Label("Aspect Ratio (X:Y):");

        //Flight Settings
        Label uavSpeedLabel = new Label("UAV Speed:");
        Label windSpeedLabel = new Label("Wind Speed:");
        Label windDirectionLabel = new Label("Wind Direction:");
        Label uavAltitudeLabel = new Label("Altitude:");

        Label forwardOverlapLabel = new Label("Forward Overlap (%)");
        Label sideOverlapLabel = new Label("Side Overlap (%)");
        Label coverageResolutionLabel = new Label("Coverage resolution");

        //ComboBox uav = new ComboBox();
        //ComboBox camera = new ComboBox();

        gp.setVgap(10);
        gp.setHgap(2);

        //Title
        gp.add(flightSettingsTitle, 0,0);

        //UAV settings
        // Title
        gp.add(uavSettings, 0,1);

        gp.add(uavNameLabel, 0,2);
        gp.add(uavName, 1,2);

        gp.add(uavWeightLabel,0,3);
        gp.add(uavWeight,1,3);

        gp.add(uavMinRadLabel,0,4);
        gp.add(uavMinRadius,1,4);

        gp.add(uavMaxInclineLabel,0,5);
        gp.add(uavMaxIncline,1,5);

        gp.add(uavBatteryLabel,0,6);
        gp.add(uavBattery,1,6);

        gp.add(uavBatteryCapacityLabel,0,7);
        gp.add(uavBatteryCapacity,1,7);

        gp.add(newUAV,0,8);
        gp.add(editUAV,1,8);
        gp.add(loadUAV,2,8);

        //Camera settings
        gp.add(cameraSettings,0,9);

        gp.add(camNameLabel, 0,10);
        gp.add(camName, 1,10);

        gp.add(camResoLabel,0,11);
        gp.add(camResolution,1,11);

        gp.add(camFocalLengthLabel,0,12);
        gp.add(camFocalLength,1,12);

        gp.add(camAspectRatioLabel,0,13);
        gp.add(camAspectRatio,1,13);

        gp.add(camSensorXLabel,0,14);
        gp.add(camSensorX,1,14);

        gp.add(camSensorYLabel,0,15);
        gp.add(camSensorY,1,15);

        gp.add(newCam,0,16);
        gp.add(editCam,1,16);
        gp.add(loadCam,2,16);

        //Flight settings
        gp.add(flightSettings,0,17);

        gp.add(uavSpeedLabel, 0,18);
        gp.add(uavSpeed, 1,18);

        gp.add(windSpeedLabel, 0,19);
        gp.add(windSpeed, 1,19);

        gp.add(windDirectionLabel, 0,20);
        gp.add(windDirection, 1,20);

        gp.add(forwardOverlapLabel, 0,21);
        gp.add(forwardOverlap, 1,21);

        gp.add(sideOverlapLabel,0,22);
        gp.add(sideOverlap,1,22);

        gp.add(coverageResolutionLabel,0,23);
        gp.add(coverageResolution,1,23);

        gp.add(uavAltitudeLabel,0,24);
        gp.add(altitude,1,24);

        //Buttons
        gp.add(save,0,25);
        gp.add(defaultSettings,2,25);
        gp.add(load,1,25);
        gp.add(run,0,26);

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
                String uavString = createUAV.loadUAV();
                uavName.setText(uavString);
                uavWeight.setText(uavString);
                uavMinRadius.setText(uavString);
                uavMaxIncline.setText(uavString);
                uavBattery.setText(uavString);
                uavBatteryCapacity.setText(uavString);
            }
        });

        loadCam.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String camString = createCamera.loadCamera();
                camName.setText(camString);
                camSensorX.setText(camString);
                camSensorY.setText(camString);
                camFocalLength.setText(camString);
                camResolution.setText(camString);
                camAspectRatio.setText(camString);
            }
        });
        save.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

            }
        });
        defaultSettings.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                uavSpeed.setText(defaultUavSpeed);
                forwardOverlap.setText(defaultForwardOverlap);
                sideOverlap.setText(defaultSideOverlay);
                coverageResolution.setText(defaultCoverageResolution);
                altitude.setText(defaultAltitude);
            }
        });
        load.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

            }
        });

        run.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // GET ALL THINGS AND SEND TO SCRIPTS
                createRoute();
            }
        });
        gp.setPadding(new Insets(5,5,5,5));
        //gp.setGridLinesVisible(true);
        sp = new ScrollPane(gp);
        sp.setFitToWidth(true);
        sp.setMaxWidth(Double.MAX_VALUE);
        return sp;
    }

    public static void createRoute(){
        System.out.println(altitude.getText());
        //Run Checks
        if(altitude.getText().isEmpty() && coverageResolution.getText().isEmpty()){
            System.out.println("Altitude is empty");
        }
        else if(altitude.getText().isEmpty()){

        }
        else if (coverageResolution.getText().isEmpty()){


        }
        else{

        }
        //Run scripts


    }
    public void dialog(String message){
        Stage dialogStage = new Stage();
        BorderPane bp = new BorderPane();
        Scene dialogScene = new Scene(bp);

    }

}
