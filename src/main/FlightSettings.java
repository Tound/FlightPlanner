package main;

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
    private TextField sideOverlap = new TextField();
    private TextField groundSampleDistance = new TextField();

    private String defaultUavSpeed = "20";
    private String defaultSideOverlap = "60";
    private String defaultgroundSampleDistance = "0.02";
    private String defaultAltitude = "120";

    private Stage createStage = new Stage();
    private Scene createScene;
    private TextField settingsName = new TextField();

    private PathDrawer pathDrawer;

    public FileChooser fileChooser = new FileChooser();

    private static Stage dialog = new Stage();
    private static Label dialogMessage = new Label();
    private Double scale;
    private Double minTerraceLength = 3.0;

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
        this.pathDrawer = pathDrawer;


        GridPane gp = new GridPane();
        Text title =  new Text("Name the settings");
        title.setTextAlignment(TextAlignment.CENTER);
        title.setId("title");
        GridPane.setColumnSpan(title,3);
        GridPane.setHalignment(title, HPos.CENTER);

        gp.setId("gridpane");
        gp.getStyleClass().add("grid");

        Label nameLabel =  new Label("Name:");
        settingsName.setPromptText("Name of settings");

        Button save = new Button("Save");
        Button cancel = new Button("Cancel");;

        gp.add(title,0,0);
        //title.setFont(Font.font("Arial", FontPosture.ITALIC, 24));
        gp.add(settingsName,1,1);

        gp.add(nameLabel,0,1);

        gp.add(save,0,2);
        gp.add(cancel,2,2);

        gp.setHgap(10);
        gp.setVgap(5);
        gp.setAlignment(Pos.CENTER);

        //gp.setStyle("-fx-background-color: rgb(42, 45, 48)");

        save.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(settingsName.getText().isEmpty()){
                    System.out.println("Missing name for settings");
                }else {
                    File file = new File("src/flight_settings/" + settingsName.getText() + ".fsettings");
                    if(file.exists()){
                        dialogMessage.setText("The filename "+ settingsName.getText() + ".fsettings already exists in " + settings_path + ".\n Would you like to overwrite?");
                        dialog.show();
                    }else {
                        writeSettings();
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
        createScene.setUserAgentStylesheet("style/menus.css");
        createStage.setScene(createScene);


        // Setup dialog box
        dialog.setTitle("Overwrite File?");
        dialog.initModality(Modality.APPLICATION_MODAL);

        Button yes = new Button("Yes");
        Button no = new Button("No");
        BorderPane bp = new BorderPane();
        bp.setCenter(dialogMessage);
        HBox hb = new HBox(yes, no);

        BorderPane.setAlignment(hb, Pos.CENTER);
        bp.setBottom(hb);

        Scene dialogScene = new Scene(bp,400,100, Color.web("rgb(42, 45, 48)"));
        dialogScene.setUserAgentStylesheet("style/menus.css");
        dialog.setScene(dialogScene);

        yes.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Set overwrite to true");
                writeSettings();
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
        GridPane.setColumnSpan(sideOverlap,2);
        GridPane.setColumnSpan(groundSampleDistance,2);

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
        Label sideOverlapLabel = new Label("Side Overlap (%)");
        Label groundSampleDistanceLabel = new Label("Coverage resolution");

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

        gp.add(sideOverlapLabel,0,21);
        gp.add(sideOverlap,1,21);

        gp.add(groundSampleDistanceLabel,0,22);
        gp.add(groundSampleDistance,1,22);

        gp.add(uavAltitudeLabel,0,23);
        gp.add(altitude,1,23);

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
                FileChooser.ExtensionFilter uavFilter = new FileChooser.ExtensionFilter("UAV Files (*.uav)","*.uav");
                fileChooser.getExtensionFilters().add(uavFilter);
                fileChooser.setSelectedExtensionFilter(uavFilter);
                File file = fileChooser.showOpenDialog(FlightPlanner.getStage());
                UAV uav = (UAV) Parser.parseFile(file);
                uavName.setText(uav.getName());
                uavWeight.setText(uav.getWeight());
                uavMinRadius.setText(uav.getTurnRad());
                uavMaxIncline.setText(uav.getMaxIncline());
                uavBattery.setText(uav.getBattery());
                uavBatteryCapacity.setText(uav.getCapacity());
            }
        });

        loadCam.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                fileChooser.setInitialDirectory(new File(cam_path));
                FileChooser.ExtensionFilter camFilter = new FileChooser.ExtensionFilter("Camera Files (*.cam)","*.cam");
                fileChooser.getExtensionFilters().add(camFilter);
                fileChooser.setSelectedExtensionFilter(camFilter);
                File file = fileChooser.showOpenDialog(FlightPlanner.getStage());
                Camera camera = (Camera) Parser.parseFile(file);
                //String camString = createCamera.loadCamera();
                camName.setText(camera.getName());
                camSensorX.setText(camera.getSensorX());
                camSensorY.setText(camera.getSensorY());
                camFocalLength.setText(camera.getFocalLength());
                camResolution.setText(camera.getResolution());
                camAspectRatio.setText(camera.getAspectRatio());
            }
        });

        save.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                createStage.show();
            }
        });
        defaultSettings.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                uavSpeed.setText(defaultUavSpeed);
                sideOverlap.setText(defaultSideOverlap);
                groundSampleDistance.setText(defaultgroundSampleDistance);
                altitude.setText(defaultAltitude);
            }
        });
        load.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                fileChooser.setInitialDirectory(new File(settings_path));
                FileChooser.ExtensionFilter settingsFilter = new FileChooser.ExtensionFilter("Settings Files (*.cam)","*.fsettings");
                fileChooser.getExtensionFilters().add(settingsFilter);
                fileChooser.setSelectedExtensionFilter(settingsFilter);
                File file = fileChooser.showOpenDialog(FlightPlanner.getStage());
                Settings settings = (Settings) Parser.parseFile(file);
                //String camString = createCamera.loadCamera();
                uavSpeed.setText(settings.getUavSpeed());
                windSpeed.setText(settings.getWindSpeed());
                windDirection.setText(settings.getWindDirection());
                sideOverlap.setText(settings.getSideOverlap());
                groundSampleDistance.setText(settings.getGsd());
                altitude.setText(settings.getAltitude());
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
                            Writer writer = new FileWriter("src/intermediate/altitudeProfile.txt");

                            JSONObject passesJSON = new JSONObject("src/intermediate/passes.json");
                            JSONObject settingsJSON =  new JSONObject("src/intermediate/settings.json");

                            groundSampleDistance.setText(settingsJSON.get("gsd").toString());
                            altitude.setText(settingsJSON.get("altitude").toString());

                            JSONArray jsonArray = (JSONArray) passesJSON.get("passes");

                            for(int i = 0; i<jsonArray.length();i++) {
                                JSONObject pass = (JSONObject) jsonArray.get(i);
                                String start = pass.get("start").toString();
                                String end = pass.get("end").toString();

                                String[] startValues = start.split(",");
                                String[] endValues = end.split(",");

                                Coordinate coord1 = flipCoords(Double.parseDouble(startValues[0]), Double.parseDouble(startValues[1]));
                                Coordinate coord2 = flipCoords(Double.parseDouble(endValues[0]), Double.parseDouble(endValues[1]));
                                pathDrawer.addPassCoords(coord1, coord2);
                                createAltitudeCoords(writer, coord1, coord2);
                            }

                            /*System.out.println("WRITING ALTITUDE");
                            writer.write("SCALE\t"+scale+"\n");
                            writer.write("WIND_ANGLE\t"+(90-Double.parseDouble(windDirection.getText())) + "\n");

                            Scanner scanner = new Scanner(pass_coords);
                            String line = "";
                            String[] content;

                            line = scanner.nextLine();
                            String[] contents = line.split("\t");

                            altitude.setText(contents[1]);
                            Double maxAltitudeDifference = Double.parseDouble(contents[2]);
                            line = scanner.nextLine();
                            contents = line.split("\t");
                            groundSampleDistance.setText(contents[1]);
                            writer.write("ALTITUDE\t" + altitude.getText() + "\t" + maxAltitudeDifference + "\n");
                            writer.write("MIN_TERRACE_LENGTH\t" + minTerraceLength + "\n");
                            writer.write("MIN_TURN_RADIUS\t" + uavMinRadius.getText() + "\n");
                            while (scanner.hasNextLine()) {
                                line = scanner.nextLine();
                                content = line.split(",");
                                Coordinate coord1 = flipCoords(Double.parseDouble(content[0]),Double.parseDouble(content[1]));;
                                line = scanner.nextLine();
                                content = line.split(",");
                                Coordinate coord2 = flipCoords(Double.parseDouble(content[0]),Double.parseDouble(content[1]));
                                pathDrawer.addPassCoords(coord1,coord2);
                                createAltitudeCoords(writer,coord1,coord2);
                            }*/
                            writer.close();
                            //scanner.close();
                            pathDrawer.drawPasses();

                        }catch (IOException ioe){
                            ioe.printStackTrace();
                        }
                        if(runScript("shortest_path")) {
                            System.out.println("Drawn passes");
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

    public void createAltitudeCoords(Writer writer, Coordinate coord1, Coordinate coord2) throws IOException {
        Double dy = coord2.getY() - coord1.getY();
        Double dx = coord2.getX() - coord1.getX();
        Double length = Math.sqrt(dy*dy + dx*dx);
        Double realLength = length/scale;
        int sampleDistance = 30;
        Double samples = realLength / sampleDistance;

        //Double lat = coord1.getLat();
        //Double lon = coord1.getLong_();
        Double x = coord1.getX();
        Double y = coord1.getY();

        JXMapViewer mapViewer = pathDrawer.getMap();

        Double MAX_CALLS = 100.0;

        dx = dx/1;
        dy = dy/1;


        writer.write("NEW_TERRACE\t" + x + "\t" + y + "\t" + length + "\n");
        //System.out.println(length + "," + realLength + "m");

        Point2D point = new Point((int)coord1.getX(),(int)coord1.getY()); // This takes the closest pixel which loses accuracy
        GeoPosition position = mapViewer.convertPointToGeoPosition(point);
        //writer.write(position.getLatitude()+","+ position.getLongitude()+"\n");
        Point2D point2 = new Point((int)coord2.getX(),(int)coord2.getY()); // This takes the closest pixel which loses accuracy
        GeoPosition position2 = mapViewer.convertPointToGeoPosition(point2);
        writer.write(position.getLatitude()+"\t"+ position.getLongitude()+ "\t" + position2.getLatitude() + "\t" + position2.getLongitude() + "\n");
        /*for(int i = 0;i<(int)Math.round(samples);i++){
            Point2D point2D = new Point((int)Math.round(x),(int)Math.round(y)); // This takes the closest pixel which loses accuracy
            GeoPosition position = mapViewer.convertPointToGeoPosition(point2D);
            writer.write(position.getLatitude()+","+ position.getLongitude()+"\n");
            x = x + dx;
            y = y + dy;
        }*/
    }

    public Coordinate flipCoords(Double coord1,Double coord2){
        Double flippedY = this.pathDrawer.getCanvas().getHeight() - coord2;
        Coordinate coordinate = new Coordinate(coord1,flippedY);
        return coordinate;
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
                /*if (line == "error") {
                    System.out.println("Script error");
                    return false;
                }*/
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
        System.out.println("Writing new Settings");
        Document dom;
        Element e = null;

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
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
            //tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"uav.dtd");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","4");

            tr.transform(new DOMSource(dom),new StreamResult(new FileOutputStream("src/flight_settings/"+settingsName.getText()+".fsettings")));

        }catch (IOException | ParserConfigurationException | TransformerConfigurationException ioe){
            ioe.printStackTrace();
        } catch (TransformerException transformerException) {
            transformerException.printStackTrace();
        }

    }

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

        Double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        Double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        Double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        Double d = radiusEarth * c * 1000;

        System.out.println("Distance = " + d);
        scale = d/pixelDistance;    // M/PX -> PX* SCALE = M
        this.scale = scale;
        return scale;
    }

    public Boolean createRoute(){
        //System.out.println(altitude.getText());
        //Run
        String errorString = "";
        if(altitude.getText().isEmpty() && groundSampleDistance.getText().isEmpty()){
            System.out.println("Altitude is empty and Coverage resolution is empty");
            errorString = errorString + "Altitude or Coverage Resolution,";
        }
        /*else if(altitude.getText().isEmpty()){
            System.out.println("Altitude is empty and will be calculated from coverage resolution");
        }
        else if (groundSampleDistance.getText().isEmpty()){
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
        }if(sideOverlap.getText().isEmpty()){
            errorString = errorString + "Side Overlap,";
        }if(FlightPlanner.getPathDrawer().getStartPoint() == null){
            System.out.println("No start location specified");
            errorString = errorString + "Takeoff and Landing location,";
        }if(FlightPlanner.getPathDrawer().getCanvasPoints().isEmpty()){
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
                ArrayList<Coordinate> points = pathDrawer.getRealPoints();
                ArrayList<ArrayList<Coordinate>> allNFZPoints = pathDrawer.getAllRealNFZPoints();
                Double scale = getScale(points);

                JSONObject jsonObject = new JSONObject();

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

                //JSONObject pointsJSON = new JSONObject();
                Coordinate startLoc = pathDrawer.getRealStartPoint();
                jsonObject.put("start_loc",startLoc.getX() +","+startLoc.getY());

                JSONArray pointsArray = new JSONArray();
                for(int i=0;i<points.size();i++){
                    pointsArray.put(points.get(i).getX() + "," + points.get(i).getY());
                }

                jsonObject.put("points",pointsArray);

                if (allNFZPoints.size() > 0) {
                    JSONArray nfzArray = new JSONArray();
                    for (int i = 0; i < allNFZPoints.size(); i++) {
                        JSONArray nfzPointsJSON = new JSONArray();
                        ArrayList<Coordinate> nfzPoint = allNFZPoints.get(i);
                        for (int j = 0; j < nfzPoint.size(); j++) {
                            nfzPointsJSON.put(nfzPoint.get(j).x + "," + nfzPoint.get(j).y);
                            //writer.write(nfzPoint.get(j).x + "," + nfzPoint.get(j).y + "\n");
                        }
                        nfzArray.put(nfzPointsJSON);
                    }
                    jsonObject.put("nfzs",nfzArray);
                }

                FileWriter writer = new FileWriter("src/intermediate/settings.json");
                writer.write(jsonObject.toString());
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
