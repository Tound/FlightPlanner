package main;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.RenderedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;

public class FlightSettings {
    private ScrollPane sp;

    public final String uav_path = "src/uavs";                  // Directory to save uav settings
    public final String cam_path = "src/cameras";               // Directory to save camera settings
    public final String settings_path = "src/flight_settings";  // Directory to save flight settings
    public final String project_path = "src/projects";          // To be implemented in V2

    // Create all text fields
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
    private TextField sideOverlap = new TextField();
    private TextField groundSampleDistance = new TextField();

    private TextField populationSize = new TextField();
    private TextField generations = new TextField();

    // Create default values
    private String defaultUavSpeed = "20";
    private String defaultSideOverlap = "60";
    private String defaultgroundSampleDistance = "0.02";
    private String defaultAltitude = "50";

    private Stage createStage = new Stage();
    private Scene createScene;
    private TextField settingsName = new TextField();

    private PathDrawer pathDrawer;

    public FileChooser fileChooser = new FileChooser();

    private static Stage dialog = new Stage();
    private static Label dialogMessage = new Label();
    private Double minTerraceLength = 5.0;
    private Double maxTerraceLength = 5000.0;

    private Double uavMaxSpeed = 25.0;

    /**
     *  Flight settings constructor
     *  The flight settings object is used to set the variables for the flight creation scripts.
     *  UAV, Camera and flight settings can all be set using this class. The object can also be used to run the scripts
     *  required to create an efficient flight path. Settings can be saved for later use in an XML format.
     *
     *  @param pathDrawer - A path drawer object
     */
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
        sideOverlap.setPromptText("Side Overlap %");
        groundSampleDistance.setPromptText("Ground sample distance (metres/pixel)");

        populationSize.setText("50");
        generations.setText("2000");

        this.pathDrawer = pathDrawer;
        createSaveMenu();
        setupOverwriteDialog();
    }

    /**
     * Create save menu for the flight settings
     */
    private void createSaveMenu(){
        GridPane gp = new GridPane();
        Text title =  new Text("Name the settings");
        title.setTextAlignment(TextAlignment.CENTER);
        title.setId("title");               // Set CSS style ID
        GridPane.setColumnSpan(title,3);
        GridPane.setHalignment(title, HPos.CENTER);

        gp.setId("gridpane");               // Set CSS style ID
        gp.getStyleClass().add("grid");

        Label nameLabel =  new Label("Name:");
        settingsName.setPromptText("Name of settings");

        Button saveSettings = new Button("Save");       // Create save settings button
        Button cancel = new Button("Cancel");           // Create cancel button

        // Add elements to gridpane
        gp.add(title,0,0);
        gp.add(settingsName,1,1);

        gp.add(nameLabel,0,1);

        gp.add(saveSettings,0,2);
        gp.add(cancel,2,2);

        // Set gridpane properties
        gp.setHgap(10);
        gp.setVgap(5);
        gp.setAlignment(Pos.CENTER);

        //Event handler for save settings button
        saveSettings.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(settingsName.getText().isEmpty()){
                    System.out.println("Missing name for settings");
                }
                else {
                    File file = new File("src/flight_settings/" + settingsName.getText() + ".fsettings");
                    if(file.exists()){          // Check if file already exists
                        dialogMessage.setText("The filename "+ settingsName.getText() + ".fsettings already exists in "
                                + settings_path + ".\n Would you like to overwrite?");
                        dialog.show();      // Show overwrite dialog box
                    }
                    else {
                        saveSettings();
                        createStage.close();
                    }
                }
            }
        });

        cancel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                createStage.close();
            }
        });

        createScene = new Scene(gp,400,100);
        createScene.setUserAgentStylesheet("style/menus.css"); // Set the style of the menu
        createStage.setScene(createScene);
    }

    /**
     *  Method used to setup the dialog box that is used to overwrite settings save files
     */
    private void setupOverwriteDialog(){
        // Setup dialog box
        dialog.setTitle("Overwrite File?");
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Create buttons
        Button yes = new Button("Yes");
        Button no = new Button("No");
        BorderPane bp = new BorderPane();
        bp.setCenter(dialogMessage);
        HBox hb = new HBox(yes, no);

        BorderPane.setAlignment(hb, Pos.CENTER);
        bp.setBottom(hb);

        Scene dialogScene = new Scene(bp,400,100);
        dialogScene.setUserAgentStylesheet("style/menus.css"); // Setup the style
        dialog.setScene(dialogScene);

        yes.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Set overwrite to true");
                saveSettings();
                dialog.close();
                createStage.close();
            }
        });
        no.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Change the name of the craft and re-save");
                dialog.close();
            }
        });
    }

    /**
     * Setup Flight method.
     * This method is used to create a scrollpane that holds all textfields that can be populated with the required
     * flight settings.
     *
     * @return Scroll -  Scrollpane containing the settings text fields
     */
    public ScrollPane setupFlight(){

        CreateUAV createUAV = new CreateUAV();
        CreateCamera createCamera = new CreateCamera();

        GridPane gp = new GridPane();
        // Titles
        Text flightSettingsTitle = new Text("Flight Setup");
        Text uavSettings = new Text("UAV Setup");
        Text cameraSettings = new Text("Camera Setup");
        Text flightSettings = new Text("Flight Settings");
        Text gaSettings = new Text("GA Settings");

        // Setup styling for all subtitles
        flightSettingsTitle.setId("subtitle");
        uavSettings.setId("subsubtitle");
        cameraSettings.setId("subsubtitle");
        flightSettings.setId("subsubtitle");
        gaSettings.setId("subsubtitle");

        // Create run button
        Button run = new Button("Run!");
        run.setPrefWidth(gp.getMaxWidth());
        run.setAlignment(Pos.CENTER);
        run.setMaxWidth(Double.MAX_VALUE);

        // Rub button properties
        GridPane.setColumnSpan(run,3);
        GridPane.setHalignment(run,HPos.CENTER);
        run.setId("run-button");            // Set run button styling

        // Set up column span for all elements
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
        GridPane.setColumnSpan(sideOverlap,2);
        GridPane.setColumnSpan(groundSampleDistance,2);

        GridPane.setColumnSpan(populationSize,2);
        GridPane.setColumnSpan(generations,2);

        // Create the buttons
        Button saveFlightSettings = new Button("Save Settings");      // Create button to save the flight settings
        Button loadFlightSettings = new Button("Load settings");
        Button defaultSettings = new Button("Default");

        Button newUAV = new Button("Create UAV");
        Button loadUAV = new Button("Load UAV");

        Button newCam = new Button("Create Camera");
        Button loadCam = new Button("Load Camera");

        // Create labels
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
        Label sideOverlapLabel = new Label("Side Overlap (%):");
        Label groundSampleDistanceLabel = new Label("GSD");

        // GA settings
        Label populationSizeLabel = new Label("Population Size:");
        Label generationsLabel = new Label("No. of generations:");

        gp.setVgap(10);
        gp.setHgap(2);

        // Add elements to gridpane
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
        gp.add(loadUAV,1,8);

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
        gp.add(loadCam,1,16);

        //Flight settings
        gp.add(flightSettings,0,17);

        gp.add(uavSpeedLabel, 0,18);
        gp.add(uavSpeed, 1,18);

        gp.add(windSpeedLabel, 0,19);
        gp.add(windSpeed, 1,19);

        gp.add(windDirectionLabel, 0,20);
        gp.add(windDirection, 1,20);

        gp.add(sideOverlapLabel,0,21);
        gp.add(sideOverlap,1,21);

        gp.add(groundSampleDistanceLabel,0,22);
        gp.add(groundSampleDistance,1,22);

        gp.add(uavAltitudeLabel,0,23);
        gp.add(altitude,1,23);

        //Add flight settings buttons
        gp.add(saveFlightSettings,0,24);
        gp.add(defaultSettings,2,24);
        gp.add(loadFlightSettings,1,24);

        // Add GA settings
        gp.add(gaSettings,0,25);
        gp.add(populationSizeLabel,0,26);
        gp.add(populationSize,1,26);

        gp.add(generationsLabel,0,27);
        gp.add(generations,1,27);

        gp.add(run,0,28);

        // When create UAV button is pressed
        newUAV.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                createUAV.newUAV();
            }
        });
        // When create camera button is pressed
        newCam.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                createCamera.newCamera();
            }
        });

        // When load UAV button is pressed
        loadUAV.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                fileChooser.setInitialDirectory(new File(uav_path));    // Create a file chooser window
                FileChooser.ExtensionFilter uavFilter = new FileChooser.ExtensionFilter(
                        "UAV Files (*.uav)","*.uav");
                fileChooser.getExtensionFilters().add(uavFilter);
                fileChooser.setSelectedExtensionFilter(uavFilter);
                File file = fileChooser.showOpenDialog(FlightPlanner.getStage());
                if(file != null) {
                    UAV uav = (UAV) Parser.parseFile(file);     // Parse the UAV file
                    // Get all settings
                    uavName.setText(uav.getName());
                    uavWeight.setText(uav.getWeight());
                    uavMinRadius.setText(uav.getTurnRad());
                    uavMaxIncline.setText(uav.getMaxIncline());
                    uavBattery.setText(uav.getBattery());
                    uavBatteryCapacity.setText(uav.getCapacity());
                }
            }
        });

        // When load Camera button is pressed
        loadCam.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                fileChooser.setInitialDirectory(new File(cam_path));     // Create a file chooser window
                FileChooser.ExtensionFilter camFilter = new FileChooser.ExtensionFilter(
                        "Camera Files (*.cam)","*.cam");
                fileChooser.getExtensionFilters().add(camFilter);
                fileChooser.setSelectedExtensionFilter(camFilter);
                File file = fileChooser.showOpenDialog(FlightPlanner.getStage());
                if(file != null) {
                    Camera camera = (Camera) Parser.parseFile(file);      // Parse the cam file
                    // Get all settings
                    camName.setText(camera.getName());
                    camSensorX.setText(camera.getSensorX());
                    camSensorY.setText(camera.getSensorY());
                    camFocalLength.setText(camera.getFocalLength());
                    camResolution.setText(camera.getResolution());
                    camAspectRatio.setText(camera.getAspectRatio());
                }
            }
        });

        saveFlightSettings.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                createStage.show();
            }
        });

        // Apply defaults
        defaultSettings.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                uavSpeed.setText(defaultUavSpeed);
                sideOverlap.setText(defaultSideOverlap);
                groundSampleDistance.setText(defaultgroundSampleDistance);
                altitude.setText(defaultAltitude);
            }
        });

        loadFlightSettings.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                fileChooser.setInitialDirectory(new File(settings_path));      // Create a file chooser window
                FileChooser.ExtensionFilter settingsFilter = new FileChooser.ExtensionFilter(
                        "Settings Files (*.fsettings)","*.fsettings");
                fileChooser.getExtensionFilters().add(settingsFilter);
                fileChooser.setSelectedExtensionFilter(settingsFilter);
                File file = fileChooser.showOpenDialog(FlightPlanner.getStage());
                if(file != null) {
                    Settings settings = (Settings) Parser.parseFile(file);       // Parse the settings file
                    // Save the settings
                    uavSpeed.setText(settings.getUavSpeed());
                    windSpeed.setText(settings.getWindSpeed());
                    windDirection.setText(settings.getWindDirection());
                    sideOverlap.setText(settings.getSideOverlap());
                    groundSampleDistance.setText(settings.getGsd());
                    altitude.setText(settings.getAltitude());
                }
            }
        });

        run.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(writeSettings()) { // If settings were written successfully
                    // Screenshot used for visualiser in V2 of software
                    try {
                        // Save image and write to intermediate file
                        File file = new File("src/intermediate/map.png");   // Screenshot for 3D representation in V2
                        WritableImage writableImage = pathDrawer.getMapNode().snapshot(new SnapshotParameters(), null);
                        RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                        ImageIO.write(renderedImage, "png", file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Find passes and terraces
                    // Run first script to create passes
                    if(runScript("find_flight_path")){  // If the script ran successfully
                        try {
                            Scanner passes = new Scanner(new File("src/intermediate/passes.json"));
                            Scanner settings = new Scanner(new File("src/intermediate/settings.json"));
                            FileWriter altitudeWriter = new FileWriter("src/intermediate/altitude_profile.json");

                            String passesContent = "";
                            String settingsContent = "";
                            // Read contents of JSON files to a JSON string
                            while(passes.hasNextLine()){ passesContent += passes.nextLine(); }
                            while(settings.hasNextLine()){ settingsContent += settings.nextLine(); }

                            // Obtain JSON objects from the JSON strings
                            JSONObject passesJSON = new JSONObject(passesContent);
                            JSONObject settingsJSON =  new JSONObject(settingsContent);
                            JSONArray passesArray = (JSONArray) passesJSON.get("passes");

                            // Get GSD and altitude from JSON settings files
                            groundSampleDistance.setText(settingsJSON.get("gsd").toString());
                            altitude.setText(settingsJSON.get("altitude").toString());

                            JSONObject altitudeGPS = new JSONObject();
                            JSONArray altitudeArray = new JSONArray();

                            // Cycle through all passes
                            for(int i = 0; i<passesArray.length();i++) {
                                JSONObject pass = (JSONObject) passesArray.get(i);
                                String start = pass.get("start").toString();
                                String end = pass.get("end").toString();

                                // Get start and end coords of passes
                                String[] startValues = start.split(",");
                                String[] endValues = end.split(",");

                                // Flip the coords to get the canvas relative points
                                Coordinate coord1 = flipCoords(Double.parseDouble(startValues[0]), Double.parseDouble(startValues[1]));
                                Coordinate coord2 = flipCoords(Double.parseDouble(endValues[0]), Double.parseDouble(endValues[1]));
                                pathDrawer.addPassCoords(coord1, coord2);

                                // Get altitude coords for the start and end of the passes
                                altitudeArray = createAltitudeCoords(altitudeArray, coord1, coord2);
                            }
                            passes.close();
                            settings.close();

                            altitudeGPS.put("gps",altitudeArray);
                            altitudeWriter.write(altitudeGPS.toString());       // Write altitude data to JSON file
                            altitudeWriter.close();
                            pathDrawer.drawPasses();                            // Draw the passes on the canvas

                        }catch (IOException | JSONException ioe){
                            // If there is an error, show dialog box
                            Dialog.showDialog("Error when reading intermediate files.\n\n" +
                                            "Ensure that the src/intermediate/ directory is visible to the application.",
                                    "Intermediate file error");
                            ioe.printStackTrace();
                        }

                        // Run the script to find the shortest route
                        if(runScript("shortest_path")) {
                            System.out.println("Drawn passes");
                            try {
                                // Read dubins JSON file
                                Scanner dubins = new Scanner(new File("src/intermediate/dubins.json"));
                                String dubinsContent = "";
                                // Read the JSON file and create string of contents
                                while(dubins.hasNextLine()){ dubinsContent += dubins.nextLine(); }

                                JSONObject dubinsObject = new JSONObject(dubinsContent); // Convert to JSON object
                                pathDrawer.drawDubins(dubinsObject);    // Draw the dubins paths
                                dubins.close();

                                // Read output JSON file
                                Scanner output = new Scanner(new File("src/intermediate/output.json"));
                                String outputContent = "";
                                // Read the JSON file and create string of contents
                                while(output.hasNextLine()){ outputContent += output.nextLine(); }

                                JSONObject outputObject = new JSONObject(outputContent); // Convert to JSON object
                                exportFlight(outputObject);    // Export the flight path
                                output.close();
                            } catch (FileNotFoundException e) {
                                // If there is an error, show dialog box
                                Dialog.showDialog("Error with output.json.\n\n" +
                                                "Ensure that the src/intermediate/dubins.json is visible to the application.",
                                        "Read file error");
                                e.printStackTrace();
                            }
                        }
                    }
                }else{
                    // If there is an error, show dialog box
                    Dialog.showDialog("Unable to create a route.\n\n" +
                                    "Ensure that all settings are correct and all directories are visible to the application.",
                            "Cannot create a flight route");
                    System.out.println("Cannot create route");
                }
            }
        });

        // Set the properties of the gridpane
        gp.setPadding(new Insets(5,5,5,5));
        gp.setMaxWidth(Double.MAX_VALUE);

        // Add gridpane of settings to the scrollpane
        sp = new ScrollPane(gp);
        sp.setFitToWidth(true);
        sp.setMaxWidth(Double.MAX_VALUE);
        return sp;
    }

    /**
     * Create Altitude Coords method.
     * Returns a JSONArray object of GPS coordinates for a pass. GPS coordinates are created by looking up the pixel values
     * of the start and end locations of the pass on the embedded map. The output is added to a JSON file which will be
     * used by the Python script to obtain a altitude profile for each pass.
     * @param jsonArray - The json array to be appended to with GPS coordinates
     * @param coord1 - First coordinate of the pass
     * @param coord2 - End coordinate of the pass
     * @return JSONArray - The appended JSON array
     * @throws IOException
     */
    public JSONArray createAltitudeCoords(JSONArray jsonArray, Coordinate coord1, Coordinate coord2) throws IOException {
        JXMapViewer mapViewer = pathDrawer.getMap();
        JSONObject jsonObject = new JSONObject();

        Point2D point = new Point((int)coord1.getX(),(int)coord1.getY());   // This takes the closest pixel which loses accuracy
        GeoPosition position = mapViewer.convertPointToGeoPosition(point);
        Point2D point2 = new Point((int)coord2.getX(),(int)coord2.getY());  // This takes the closest pixel which loses accuracy
        GeoPosition position2 = mapViewer.convertPointToGeoPosition(point2);

        // Add GPS data to JSON file
        jsonObject.put("lat1",position.getLatitude());
        jsonObject.put("long1", position.getLongitude());
        jsonObject.put("lat2", position2.getLatitude());
        jsonObject.put("long2", position2.getLongitude());
        jsonArray.put(jsonObject);
        return jsonArray;
    }

    /**
     * Flip coords method.
     * Used to flip the canvas coordinate system to the same system as the Python scripts
     * @param coord1 - The X coordinate to flip
     * @param coord2 - The Y coordinate to flip
     * @return coordinate - The updated coordinate
     */
    public Coordinate flipCoords(Double coord1,Double coord2){
        Double flippedY = this.pathDrawer.getCanvas().getHeight() - coord2; // Remove the Y coordinate from the canvas height
        Coordinate coordinate = new Coordinate(coord1,flippedY);            // Create new coordinate with updated coordinates
        return coordinate;
    }

    /**
     * Run script method
     * Used to run individual python scripts and print the error stream.
     * @param scriptName - Name of the python script to run.
     * @return boolean - Boolean represents whether the script was ran successfully. True = Success.
     */
    public boolean runScript(String scriptName){
        System.out.println("Running "+scriptName);
        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("python src/scripts/" + scriptName + ".py");

            // Uncomment for python3 command
            //Process pr = rt.exec("python3 src/scripts/" + scriptName + ".py");

            BufferedReader bf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
            String line = "";
            String dialogMessage = "";

            // Get the output of the script
            while ((line = bf.readLine()) != null) {
                System.out.println(line);
                dialogMessage = dialogMessage + line + "\n";
            }
            // If there is an error in the scripts
            if(stdError.ready()) {
                String errorMessage = "";
                // Get error message
                while ((line = stdError.readLine()) != null) {
                    errorMessage = errorMessage + line + "\n";
                }

                // Print error in dialog box
                if(errorMessage != null) {
                    errorMessage = "Script error in " + scriptName + ":\n" + errorMessage;
                    Dialog.showDialog(errorMessage,"Script error");
                    return false;
                }
            }
            // If the shortest path script has been ran, print the flight stats
            if(scriptName == "shortest_path") {
                Dialog.showDialog(dialogMessage, "Script output");
            }
            return true;
        }
        catch (IOException ioe){
            System.out.println("Script error");
            // If error, show dialog box with error
            Dialog.showDialog("Unable to run src/scripts/" + scriptName + ".py.\n\n" +
                            "Ensure that the src/scripts/ directory is visible to the application and the script exists.",
                    "Script Error");
            return false;
        }
    }

    /**
     * Write Settings method.
     * Saves the flight settings in an XML file
     */
    public void saveSettings(){
        System.out.println("Writing new Settings");
        Document dom;
        Element e = null;

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            // Populate the XML file
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            dom = documentBuilder.newDocument();
            Element rootElement = dom.createElement("flight_settings");

            e = dom.createElement("uav_speed");
            e.appendChild(dom.createTextNode(uavSpeed.getText()));
            rootElement.appendChild(e);

            e = dom.createElement("wind_speed");
            e.appendChild(dom.createTextNode(windSpeed.getText()));
            rootElement.appendChild(e);

            e = dom.createElement("wind_direction");
            e.appendChild(dom.createTextNode(windDirection.getText()));
            rootElement.appendChild(e);

            e = dom.createElement("side_overlap");
            e.appendChild(dom.createTextNode(sideOverlap.getText()));
            rootElement.appendChild(e);

            e = dom.createElement("gsd");
            e.appendChild(dom.createTextNode(groundSampleDistance.getText()));
            rootElement.appendChild(e);

            e = dom.createElement("alititude");
            e.appendChild(dom.createTextNode(altitude.getText()));
            rootElement.appendChild(e);

            dom.appendChild(rootElement);

            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT,"yes");
            tr.setOutputProperty(OutputKeys.METHOD,"xml");
            tr.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","4");

            // Write XML data to file
            tr.transform(new DOMSource(dom),new StreamResult(new FileOutputStream(
                    "src/flight_settings/"+settingsName.getText()+".fsettings")));

        }catch (IOException | ParserConfigurationException | TransformerException ioe){
            // If error, show dialog with error message
            Dialog.showDialog("Unable to write to file and save settings.\n\n" +
                            "Ensure that the src/flight_settings/ directory is visible to the application.",
                    "Write settings error");
            ioe.printStackTrace();
        }
    }

    /**
     * Get scale method.
     * Returns the scaling factor between pixel length and length in metres.
     * @param coordinates - An array list containing points of the ROI.
     * @return scale - Scaling factor to relate pixel distance to real distance in metres
     */
    public Double getScale(ArrayList<Coordinate> coordinates){
        //Haversine formula
        Double scale = 1.0;
        Double radiusEarth = 6378.137;
        Double lat1 = coordinates.get(0).getLat();
        Double lon1 = coordinates.get(0).getLong_();
        Double lat2 = coordinates.get(1).getLat();
        Double lon2 = coordinates.get(1).getLong_();

        Double dx = coordinates.get(1).getX() - coordinates.get(0).getX();
        Double dy = coordinates.get(1).getY() - coordinates.get(0).getY();
        Double pixelDistance = Math.sqrt(dx*dx + dy*dy);

        // Get difference in GPS coords
        Double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        Double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        Double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        Double d = radiusEarth * c * 1000;      // Convert to metres

        System.out.println("Distance = " + d);
        scale = d/pixelDistance;    // Calculate scaling factor
        return scale;
    }

    /**
     * Write all settings in a JSON file so they can be used by the python scripts.
     * A dialog box shows if any required settings are missing.
     *
     * @return boolean - Return value depending on whether the settings can be written and therefore
     * a route created. Returns True when all settings can be written
     */
    public Boolean writeSettings(){
        // Write all settings to JSON file
        String errorString = "";
        // Ensure that there is either GSD or Altitude
        if(altitude.getText().isEmpty() && groundSampleDistance.getText().isEmpty()){
            errorString = errorString + "Altitude or Coverage Resolution,";
        }

        // Check all settings for value
        if(windSpeed.getText().isEmpty()){
            // If wind is not set, set to 0
            windSpeed.setText("0");
            windDirection.setText("0");
        }
        if(uavSpeed.getText().isEmpty()){
            errorString = errorString + "UAV speed,";
        }
        if(uavWeight.getText().isEmpty()){
            errorString = errorString + "UAV Weight,";
        }
        if(uavMinRadius.getText().isEmpty()){
            errorString = errorString + "Minimum Turn Radius,";
        }
        if(uavMaxIncline.getText().isEmpty()){
            errorString = errorString + "Max Incline Angle,";
        }
        if(uavBatteryCapacity.getText().isEmpty()){
            errorString = errorString + "Battery Capacity,";
        }
        if(camSensorX.getText().isEmpty()){
            errorString = errorString + "Camera sensor width,";
        }
        if(camSensorY.getText().isEmpty()){
            errorString = errorString + "Camera sensor height,";
        }
        if(camFocalLength.getText().isEmpty()){
            errorString = errorString + "Camera focal length,";
        }
        if(camResolution.getText().isEmpty()){
            errorString = errorString + "Camera Resolution,";
        }
        if(camAspectRatio.getText().isEmpty()){
            errorString = errorString + "Camera Aspect Ratio,";
        }
        if(sideOverlap.getText().isEmpty()){
            errorString = errorString + "Side Overlap,";
        }
        if(populationSize.getText().isEmpty()){
            errorString = errorString + "GA Population size,";
        }
        if(generations.getText().isEmpty()){
            errorString = errorString + "Number of GA generations,";
        }
        if(FlightPlanner.getPathDrawer().getStartPoint() == null){
            errorString = errorString + "Takeoff and Landing location,";
        }
        if(FlightPlanner.getPathDrawer().getCanvasPoints().isEmpty()){
            errorString = errorString + "Area of Interest,";
        }

        // If errors were caught
        if(errorString != ""){
            errorString = errorString.substring(0,errorString.length()-1);  // Remove final comma from string
            // Show dialog with error message
            Dialog.showDialog(errorString + "\n\nAre missing from the flight settings. " +
                    "Make sure all of the settings above are configured.","Missing inputs");
            return false;
        }else {
            //Write intermediate file
            try {
                ArrayList<Coordinate> points = pathDrawer.getRealPoints();  // Get ROI points for the python coord system
                ArrayList<ArrayList<Coordinate>> allNFZPoints = pathDrawer.getAllRealNFZPoints(); // Get points of NFZs
                Double scale = getScale(points);    // Obtain the scaling value required to covert pixels to metres

                JSONObject jsonObject = new JSONObject();

                // Add all settings to JSON file
                jsonObject.put("scale",scale);
                jsonObject.put("uav_weight",uavWeight.getText());
                jsonObject.put("uav_min_radius",uavMinRadius.getText());
                jsonObject.put("uav_max_incline",uavMaxIncline.getText());
                jsonObject.put("battery_capacity",uavBatteryCapacity.getText());
                jsonObject.put("cam_sensor_x",camSensorX.getText());
                jsonObject.put("cam_sensor_y",camSensorY.getText());
                jsonObject.put("cam_focal_length",camFocalLength.getText());
                jsonObject.put("cam_resolution",camResolution.getText());
                jsonObject.put("cam_aspect_ratio",camAspectRatio.getText());
                jsonObject.put("uav_speed",uavSpeed.getText());
                jsonObject.put("wind_speed",windSpeed.getText());
                jsonObject.put("wind_direction",windDirection.getText());
                jsonObject.put("altitude",altitude.getText());
                jsonObject.put("side_overlap",sideOverlap.getText());
                jsonObject.put("gsd",groundSampleDistance.getText());
                jsonObject.put("min_terrace_length",minTerraceLength);
                jsonObject.put("max_pass_length",maxTerraceLength);
                jsonObject.put("population_size",populationSize.getText());
                jsonObject.put("generations",generations.getText());
                jsonObject.put("uav_max_speed",uavMaxSpeed);

                Coordinate startLoc = pathDrawer.getRealStartPoint();
                jsonObject.put("start_loc",startLoc.getX() +","+startLoc.getY());

                Coordinate startPoint = pathDrawer.getStartPoint();
                Point start = new Point((int)startPoint.getX(),(int)startPoint.getY());
                // Convert start location to GPS location
                GeoPosition position = pathDrawer.getMap().convertPointToGeoPosition(start);
                // Add start location gps points to JSON data
                jsonObject.put("start_loc_gps",position.getLatitude() + "," + position.getLongitude());

                JSONArray pointsArray = new JSONArray();

                // Cycle through each point of the ROI
                for(int i=0;i<points.size();i++){
                    pointsArray.put(points.get(i).getX() + "," + points.get(i).getY()); // Add points to JSON file
                }
                jsonObject.put("points",pointsArray);

                // If there are NFZs
                if (allNFZPoints.size() > 0) {
                    JSONArray nfzArray = new JSONArray();
                    // Cycle through NFZs
                    for (int i = 0; i < allNFZPoints.size(); i++) {
                        JSONArray nfzPointsJSON = new JSONArray();
                        ArrayList<Coordinate> nfzPoint = allNFZPoints.get(i);
                        // Cycle through each point on NFZ
                        for (int j = 0; j < nfzPoint.size(); j++) {
                            // Add each point to the JSON file
                            nfzPointsJSON.put(nfzPoint.get(j).x + "," + nfzPoint.get(j).y);
                        }
                        nfzArray.put(nfzPointsJSON);
                    }
                    jsonObject.put("nfzs",nfzArray);    // Write array of NFZ points to JSON data
                }

                // Write data to JSON file
                FileWriter writer = new FileWriter("src/intermediate/settings.json");
                writer.write(jsonObject.toString());
                writer.close();

            } catch (IOException ioe) { // Catch error if file cannot be written to
                // If errors were found, write error message in dialog box
                Dialog.showDialog("Unable to write to file and save settings.\n\n" +
                        "Ensure that the src/intermediate/ directory is visible to the application.",
                        "Write settings error");
                return false;
            }
        }
        return true;    // Return true if no errors were found and settings were written to the file
    }

    /**
     * Export the flight path with GPS coordinates
     * @param output - The JSON object containing all passes in order in px values
     */
    public void exportFlight(JSONObject output){
        try{
            FileWriter writer = new FileWriter("src/flight plan/output.txt");   // Open the output file
            JSONObject startLocation = (JSONObject) output.get("start");                // Find the start location
            String startLocationCoords = startLocation.get("coords").toString();

            // Remove the square brackets and split into 2 coords
            startLocationCoords = startLocationCoords.replace("[","");
            startLocationCoords = startLocationCoords.replace("]","");
            String[] startLocCoordsContents = startLocationCoords.split(",");

            // Create 2D point and flip Y coord
            Point start = new Point((int)Double.parseDouble(startLocCoordsContents[0]),
                    (int)(pathDrawer.getCanvas().getHeight() - Double.parseDouble(startLocCoordsContents[1])));
            // Convert start location to GPS location
            GeoPosition position = pathDrawer.getMap().convertPointToGeoPosition(start);

            // Write the start location GPS coords and altitude to the output file
            writer.write("Start location: [" + position.getLatitude() + "," + position.getLongitude() +
                    "], altitude:" + startLocation.get("Altitude").toString() + " m\n");

            // Get location and altitude for the passes
            JSONArray passes = output.getJSONArray("passes");
            String startPoint = "";
            String endPoint = "";
            String passAltitude = "";

            // Loop through each pass
            for(int i=0;i<passes.length();i++){
                JSONObject pass = (JSONObject) passes.get(i);   // Get the pass JSON object
                startPoint = pass.get("start").toString();      // Get the start location
                endPoint = pass.get("end").toString();          // Get the end location
                passAltitude = pass.get("altitude").toString(); // Get the altitude of the terrace

                // Remove the square brackets and split into 2 coords
                startPoint = startPoint.replace("[","");
                startPoint = startPoint.replace("]","");
                String[] startPointContents = startPoint.split(",");

                // Create 2D point and flip Y coord
                Point passStart = new Point((int)Double.parseDouble(startPointContents[0]),
                        (int)(pathDrawer.getCanvas().getHeight() - Double.parseDouble(startPointContents[1])));
                // Convert start location of pass to GPS location
                GeoPosition passStartGPS = pathDrawer.getMap().convertPointToGeoPosition(passStart);

                // Remove the square brackets and split into 2 coords
                endPoint = endPoint.replace("[","");
                endPoint = endPoint.replace("]","");
                String[] endPointContents = endPoint.split(",");

                // Create 2D point and flip Y coord
                Point passEnd = new Point((int)Double.parseDouble(endPointContents[0]),
                        (int)(pathDrawer.getCanvas().getHeight() - Double.parseDouble(endPointContents[1])));

                // Convert end location of pass to GPS location
                GeoPosition passEndGPS = pathDrawer.getMap().convertPointToGeoPosition(passEnd);

                // Write GPS coords and altitude to file
                writer.write(i+1 + ". " + "Start: [" + passStartGPS.getLatitude() + ", " + passStartGPS.getLongitude() +
                        "], End: [" + passEndGPS.getLatitude() + ", " + passEndGPS.getLongitude() +
                        "], Altitude: " + passAltitude + " m\n");
            }
            writer.close();
        }
        catch (IOException ioe){
            // If an error is found, show the dialog box with the error message
            Dialog.showDialog("Unable to export flight plan.\n\n" +
                            "Ensure that the src/intermediate/ directory is visible to the application.",
                        "Write flight plan error");
        }
    }
}
