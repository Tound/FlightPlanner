package main;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.plaf.synth.SynthEditorPaneUI;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class FlightSettings {
    private ScrollPane sp;

    public final String uav_path = "src/UAVs";
    public final String cam_path = "src/cameras";
    public final String settings_path = "src/flight_settings";
    public final String project_path = "src/projects";

    private TextField uavName = new TextField();
    private TextField uavWeight = new TextField();
    private TextField uavMinRadius = new TextField();
    private TextField uavMaxIncline = new TextField();
    private TextField uavBattery = new TextField();
    private TextField uavBatteryCapacity = new TextField();

    private TextField camName = new TextField();
    private TextField camSensorX = new TextField();
    private TextField camSensorY = new TextField();
    private TextField camFocalLength = new TextField();
    private TextField camResolution = new TextField();
    private TextField camAspectRatio = new TextField();

    private TextField uavSpeed = new TextField();
    private TextField windSpeed = new TextField();
    private TextField windDirection = new TextField();
    private TextField altitude = new TextField();
    private TextField forwardOverlap = new TextField();
    private TextField sideOverlap = new TextField();
    private TextField coverageResolution = new TextField();

    private String defaultUavSpeed = "20";
    private String defaultForwardOverlap = "40";
    private String defaultSideOverlay = "60";
    private String defaultCoverageResolution = "0.02";
    private String defaultAltitude = "120";

    private PathDrawer pathDrawer;

    public FileChooser fileChooser = new FileChooser();

    public FlightSettings(PathDrawer pathDrawer){
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
        this.pathDrawer = pathDrawer;

    }

    public ScrollPane setupFlight(){

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
                fileChooser.setInitialDirectory(new File(uav_path));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("UAV Files (*.uav)","*.uav"));
                File file = fileChooser.showOpenDialog(FlightPlanner.getStage());
                ArrayList<String> output = Parser.load(file);
                uavName.setText(output.get(0));
                uavWeight.setText(output.get(1));
                uavMinRadius.setText(output.get(2));
                uavMaxIncline.setText(output.get(3));
                uavBattery.setText(output.get(4));
                uavBatteryCapacity.setText(output.get(5));
            }
        });

        loadCam.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                fileChooser.setInitialDirectory(new File(cam_path));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Camera Files (*.cam)","*.cam"));
                File file = fileChooser.showOpenDialog(FlightPlanner.getStage());
                ArrayList<String> output = Parser.load(file);
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
                if(createRoute()) { // If route can be created
                    // Screenshot
                    try {
                        File file = new File("src/intermediate/map.png");
                        WritableImage writableImage = pathDrawer.getMapNode().snapshot(new SnapshotParameters(), null);
                        RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                        ImageIO.write(renderedImage, "png", file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Find passes and terraces
                    if(runScript("find_flight_path")){
                        try {
                            File pass_coords = new File("src/intermediate/passes.txt");
                            Scanner scanner = new Scanner(pass_coords);
                            String line = "";
                            String[] content;
                            while (scanner.hasNextLine()) {
                                line = scanner.nextLine();
                                content = line.split(",");
                                Coordinate coord1 = new Coordinate(Double.parseDouble(content[0]),Double.parseDouble(content[1]));
                                line = scanner.nextLine();
                                content = line.split(",");
                                Coordinate coord2 = new Coordinate(Double.parseDouble(content[0]),Double.parseDouble(content[1]));
                                pathDrawer.addPassCoords(coord1,coord2);
                            }
                            pathDrawer.drawPasses();

                        }catch (IOException ioe){
                            ioe.printStackTrace();
                        }
                        if(runScript("create_terraces")) {
                            if (runScript("shortest_path")) {
                                System.out.println("Drawn passes");
                            }
                        }
                    }
                }else{
                    System.out.println("Cannot create route");
                }
            }
        });
        gp.setPadding(new Insets(5,5,5,5));
        //gp.setGridLinesVisible(true);
        sp = new ScrollPane(gp);
        sp.setFitToWidth(true);
        sp.setMaxWidth(Double.MAX_VALUE);
        return sp;
    }

    public boolean runScript(String scriptName){
        System.out.println("Running "+scriptName);
        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("python src/scripts/" + scriptName + ".py");
            BufferedReader bf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            String line = "";
            while ((line = bf.readLine()) != null) {
                if (line == "error") {
                    System.out.println("Script error");
                    return false;
                }
                System.out.println(line);
            }
            System.out.println(stdError.lines());
            if(stdError.ready()) {
                String errorMessage = "";
                while ((line = stdError.readLine()) != null) {
                    errorMessage = errorMessage + line + "\n";
                }
                if(errorMessage != null) {
                    errorMessage = "Script error in " + scriptName + ":\n" + errorMessage;
                    dialog(errorMessage);
                    return false;
                }

            }
            return true;
        }catch (IOException ioe){
            System.out.println("Script error");
            return false;
        }
    }

    public void writeSettings(){


    }

    public void lookupFiles(){
        try {
            File gpsLookup = new File("src/intermediate/gpslookup.txt");
            Scanner reader = new Scanner(gpsLookup);
            FileWriter writer = new FileWriter("src/intermediate/gpsresults.txt");
            while(reader.hasNextLine()){
                String line = reader.nextLine();
                System.out.println(line);
                if(line != "NEW PASS"){
                    //GPS LOOKUP
                    writer.write("GPS");
                }else{
                    writer.write("NEW PASS");
                }
            }
            reader.close();
        } catch(FileNotFoundException fnfe){
            System.out.println("File not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean createRoute(){
        //System.out.println(altitude.getText());
        //Run
        String errorString = "";
        if(altitude.getText().isEmpty() && coverageResolution.getText().isEmpty()){
            System.out.println("Altitude is empty and Coverage resolution is empty");
            errorString = errorString + "Altitude or Coverage Resolution,";
        }
        /*else if(altitude.getText().isEmpty()){
            System.out.println("Altitude is empty and will be calculated from coverage resolution");
        }
        else if (coverageResolution.getText().isEmpty()){
            System.out.println("Coverage resolution is empty");
        }*/
        if(windSpeed.getText().isEmpty()){
            windSpeed.setText("0");
            windDirection.setText("0");
        }if(uavSpeed.getText().isEmpty()){
            errorString = errorString + "UAV speed,";
        }if(uavWeight.getText().isEmpty()){
            errorString = errorString + "UAV Weight,";
        }if(uavMinRadius.getText().isEmpty()){
            errorString = errorString + "Minimum Turn Radius,";
        }if(uavMaxIncline.getText().isEmpty()){
            errorString = errorString + "Max Incline Angle,";
        }if(uavBatteryCapacity.getText().isEmpty()){
            errorString = errorString + "Battery Capacity,";
        }if(camSensorX.getText().isEmpty()){
            errorString = errorString + "Camera sensor width,";
        }if(camSensorY.getText().isEmpty()){
            errorString = errorString + "Camera sensor height,";
        }if(camFocalLength.getText().isEmpty()){
            errorString = errorString + "Camera focal length,";
        }if(camResolution.getText().isEmpty()){
            errorString = errorString + "Camera Resolution,";
        }if(camAspectRatio.getText().isEmpty()){
            errorString = errorString + "Camera Aspect Ratio,";
        }if(forwardOverlap.getText().isEmpty()){
            errorString = errorString + "Forward Overlap,";
        }if(sideOverlap.getText().isEmpty()){
            errorString = errorString + "Side Overlap,";
        }if(FlightPlanner.getPathDrawer().getStartPoint() == null){
            System.out.println("No start location specified");
            errorString = errorString + "Takeoff and Landing location,";
        }if(FlightPlanner.getPathDrawer().getPoints().isEmpty()){
            errorString = errorString + "Area of Interest,";
        }

        if(errorString != ""){
            errorString = errorString.substring(0,errorString.length()-1);
            dialog(errorString + "\n\nAre missing from the flight settings. Make sure all of the settings above are configured.");
            return false;
        }else {
            System.out.println("All inputs are correct");
            //Write intermediate file
            System.out.println("Writing to file");
            try {
                FileWriter writer = new FileWriter("src/intermediate/intermediate.txt");
                //writer.write("====SETTINGS====\n");
                //writer.write("UAV_NAME\t" + uavName.getText());
                writer.write("SCALE\t" + pathDrawer.getMapScale() + "\n");
                writer.write("UAV_WEIGHT\t" + uavWeight.getText() + "\n");
                writer.write("UAV_MIN_RADIUS\t" + uavMinRadius.getText() + "\n");
                writer.write("UAV_MAX_INCLINE\t" + uavMaxIncline.getText() + "\n");
                //writer.write(uavBattery.getText();
                writer.write("BATTERY_CAPACITY\t" + uavBatteryCapacity.getText() + "\n");

                //writer.write("CAMERA_NAME" + camName.getText());
                writer.write("CAM_SENSOR_X\t" + camSensorX.getText() + "\n");
                writer.write("CAM_SENSOR_Y\t" + camSensorY.getText() + "\n");
                writer.write("CAM_FOCAL_LENGTH\t" + camFocalLength.getText() + "\n");
                writer.write("CAM_RESOLUTION\t" + camResolution.getText() + "\n");
                writer.write("CAM_ASPECT_RATIO\t" + camAspectRatio.getText() + "\n");

                writer.write("UAV_SPEED\t" + uavSpeed.getText() + "\n");
                writer.write("WIND_SPEED\t" + windSpeed.getText() + "\n");
                writer.write("WIND_DIRECTION\t" + windDirection.getText() + "\n");
                if(!altitude.getText().isEmpty()) {
                    writer.write("ALTITUDE\t" + altitude.getText() + "\n");
                }
                writer.write("FORWARD_OVERLAP\t" + forwardOverlap.getText() + "\n");
                writer.write("SIDE_OVERLAP\t" + sideOverlap.getText() + "\n");
                writer.write("GSD\t" + coverageResolution.getText() + "\n");
                writer.write("====START====\n");
                Coordinate startLoc = pathDrawer.getStartPoint();
                writer.write("START_LOC: \t" + startLoc.x + "," + startLoc.y + "\n");

                ArrayList<Coordinate> points = pathDrawer.getPoints();
                ArrayList<ArrayList<Coordinate>> allNFZPoints = pathDrawer.getAllNFZs();

                for (int i = 0; i < points.size(); i++) {
                    writer.write(points.get(i).x + "," + points.get(i).y + "\n");
                }
                if (allNFZPoints.size() > 0) {
                    writer.write("====NFZ====\n");
                    for (int i = 0; i < allNFZPoints.size(); i++) {
                        writer.write("NFZ START\n");
                        ArrayList<Coordinate> nfzPoint = allNFZPoints.get(i);
                        for (int j = 0; j < nfzPoint.size(); j++) {
                            writer.write(nfzPoint.get(j).x + "," + nfzPoint.get(j).y + "\n");
                        }
                    }
                }
                writer.write("====END====");
                writer.close();

            } catch (IOException ioe) {
                System.out.println("Cannot write to file");
            }
            //Run scripts
        }
        return true;
    }

    public void dialog(String message){
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Missing Inputs");
        BorderPane bp = new BorderPane();
        Button ok = new Button("OK");


        ok.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                dialogStage.close();
            }
        });
        Label dialogText = new Label(message);
        dialogText.setWrapText(true); //setWrappingWidth(400);
        bp.setCenter(dialogText);
        bp.setBottom(ok);
        bp.setId("border");
        BorderPane.setAlignment(dialogText,Pos.CENTER);
        BorderPane.setAlignment(ok,Pos.CENTER);
        Scene dialogScene = new Scene(bp,400,200);
        dialogScene.setUserAgentStylesheet("style/menus.css");
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }
}
