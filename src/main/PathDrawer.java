package main;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

import java.awt.*;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javafx.scene.text.TextAlignment;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.*;
import javax.swing.event.MouseInputListener;

/**
 * Class is used to create a pathdrawing object that can be used to draw flight paths on a map
 */
public class PathDrawer{
    private Canvas canvas;
    private StackPane stack;
    private GraphicsContext gc;
    private ArrayList<Coordinate> canvasPoints = new ArrayList<Coordinate>();               // Arraylist of ROI points
    private ArrayList<Coordinate> realPoints = new ArrayList<Coordinate>();                 // Arraylist of ROI points for scripts
    private ArrayList<Coordinate> nfzPoints = new ArrayList<Coordinate>();                  // Arraylist of NFZ points
    private ArrayList<Coordinate> realNfzPoints = new ArrayList<Coordinate>();              // Arraylist of NFZ points for scripts
    private ArrayList<ArrayList<Coordinate>> allNFZPoints = new ArrayList<ArrayList<Coordinate>>();    // Arraylist of points for all NFZs
    private ArrayList<ArrayList<Coordinate>> allRealNFZPoints = new ArrayList<ArrayList<Coordinate>>();// For scripts
    private ArrayList<ArrayList<Coordinate>> passCoords = new ArrayList<ArrayList<Coordinate>>();      // List of the coordinates of passes
    private Coordinate startPoint = null;           // Coordinate of takeoff and land point
    private Coordinate realStartPoint = null;       // Coordinate of takeoff relative to the scripts

    private boolean drawingROI = false;             // Boolean used to track if the ROI is being drawn
    private boolean drawingNFZ = false;             // Boolean used to track if NFZs are being drawn
    private boolean drawingStart = false;           // Boolean used to track if the start location is being drawn

    private boolean complete = false;               // Boolean used to track if the setup is complete
    private double markerRadius = 20;               // Size of the markers for the ROI
    private double nfzMarkerRadius = 10;            // Size of the markers for the NFZs
    private double startMarkerRadius = 24;          // Size of the start position marker

    // Create paints for markers
    private Paint markerPaint = Color.BLACK;
    private Paint nfzMarkerPaint = Color.RED;
    private Paint startMarkerPaint = Color.BLUE;
    private Paint textPaint = Color.WHITE;
    private Paint passPaint = Color.BLUE;

    // Set font for ROI marker
    private Font markerFont = Font.font("Arial",FontPosture.ITALIC,15);

    // Create buttons
    private Button drawROI = new Button("Draw ROI");
    private Button drawNFZ = new Button("Draw NFZ");
    private Button clear = new Button("Clear");
    private Button setStartLoc = new Button("Mark Takeoff and Landing");
    private Button findButton = new Button("Find");

    private TextField chooseLocation = new TextField(); // Textfield for the user to choose location
    private GridPane buttonGridPane = new GridPane();   // New gridpan for the buttons
    private JXMapViewer mapViewer;                      // Interactive map
    private SwingNode sn;                               // Swing node to hold the map

    // Dotenv to load the API key for .env file
    DotenvBuilder dotenvBuilder = Dotenv.configure().directory("src/scripts").filename(".env");
    Dotenv dotenv = dotenvBuilder.load();
    private String API_KEY = dotenv.get("API_KEY");

    // API url
    private String geocodeAPI = "https://maps.googleapis.com/maps/api/geocode/json?address=";

    /**
     * PathDrawer constructor
     * Constructor to create the path drawer object which includes a stackpane
     * that holds a map,a canvas and buttons.
     */
    public PathDrawer(){
        this.canvas = new Canvas();
        this.stack = new StackPane();
        this.gc = canvas.getGraphicsContext2D();
        this.chooseLocation.setPromptText("Jump to location");

        canvas.setFocusTraversable(true);

        sn = addMap();
        sn.setPickOnBounds(false);
        stack.getChildren().addAll(sn,buttonGridPane);
        buttonGridPane.setPickOnBounds(false);

        // Add elements to the button gridpane
        buttonGridPane.add(drawROI,0,0);
        buttonGridPane.add(drawNFZ,1,0);
        buttonGridPane.add(clear,2,0);
        buttonGridPane.add(setStartLoc,3,0);
        buttonGridPane.add(chooseLocation,4,0);
        buttonGridPane.add(findButton,5,0);

        drawROI.setTextAlignment(TextAlignment.CENTER);

        chooseLocation.setMaxHeight(Double.MAX_VALUE);

        // Set width of buttons
        drawROI.setMaxWidth(Double.MAX_VALUE);
        drawNFZ.setMaxWidth(Double.MAX_VALUE);
        clear.setMaxWidth(Double.MAX_VALUE);
        setStartLoc.setMaxWidth(Double.MAX_VALUE);
        chooseLocation.setMaxWidth(Double.MAX_VALUE);
        findButton.setMaxWidth(Double.MAX_VALUE);

        // Set column layouts
        ColumnConstraints col25 = new ColumnConstraints();
        col25.setPercentWidth(25);
        ColumnConstraints col20 = new ColumnConstraints();
        col20.setPercentWidth(20);
        ColumnConstraints col15 = new ColumnConstraints();
        col15.setPercentWidth(15);
        ColumnConstraints col5 = new ColumnConstraints();
        col5.setPercentWidth(5);
        buttonGridPane.getColumnConstraints().addAll(col15,col15,col15,col25,col25,col5);

        stack.setBackground(new Background(new BackgroundFill(Color.WHITE,CornerRadii.EMPTY,Insets.EMPTY)));
        gc.setFont(markerFont);

    }

    /**
     * Creates a path drawer as a stackpane containing the canvas, map and control buttons
     * @return stack - The stackpane of the pathdrawer
     */
    public StackPane createPathDrawer(){
        canvas.widthProperty().bind(stack.widthProperty());
        canvas.heightProperty().bind(stack.heightProperty());

        // When the canvas is interacted with
        canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                MouseButton button = event.getButton();
                if(button.equals(MouseButton.PRIMARY)){ // If left mouse button
                    placePoint(event);                  // Add point of mouse click
                }
            }
        });

        // When draw ROI button is pressed
        drawROI.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // If the ROI and NFZ are not being drawn
                if(!drawingROI && !drawingNFZ){
                    // If the canvas is not in the stack
                    if(!stack.getChildren().contains(canvas)) {
                        stack.getChildren().remove(buttonGridPane);         // Shuffle the order by removing the buttons
                        stack.getChildren().addAll(canvas, buttonGridPane); // Add canvas and buttons to stack
                    }
                    drawROI.setText("Done");    // Change text
                    drawingROI = true;          // Set drawing ROI as true
                    complete = false;
                    if(canvasPoints.size()>0) { // Clear current ROI points
                        canvasPoints.clear();
                        gc.clearRect(0,0,stack.getWidth(),stack.getHeight());   // Clear the canvas

                        // Draw NFZ points
                        for(int i=0;i<nfzPoints.size();i++) {
                            gc.strokeOval(nfzPoints.get(i).getX() - nfzMarkerRadius / 2, nfzPoints.get(i).getY() -
                                    nfzMarkerRadius / 2, nfzMarkerRadius, nfzMarkerRadius);
                            if (i > 0) {
                                gc.strokeLine(nfzPoints.get(i).getX(), nfzPoints.get(i).getY(),
                                        nfzPoints.get(i - 1).getX(), nfzPoints.get(i - 1).getY());
                            }
                        }
                    }
                }
                // If an NFZ is not being drawn and ROI is being drawn
                else if(!drawingNFZ){
                    drawROI.setText("Draw ROI");    // Change text
                    drawingROI = false;
                    complete = true;                // Finished drawing the ROI
                    if(canvasPoints.size()>0) {     // Draw line between first and last point
                        gc.strokeLine(canvasPoints.get(canvasPoints.size() - 1).getX(),
                                canvasPoints.get(canvasPoints.size() - 1).getY(), canvasPoints.get(0).getX(),
                                canvasPoints.get(0).getY());
                        canvasPoints.add(canvasPoints.get(0));  // Add first point to the end of the list
                    }
                }else{
                    System.out.println("You must finish drawing the NFZ first");
                }
            }
        });

        // When the draw NFZ button is pressed
        drawNFZ.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!drawingNFZ && !drawingROI){         //If not drawing the NFZ and not drawing the ROI
                     drawNFZ.setText("Done");
                    drawingNFZ = true;

                }else if(!drawingROI){ //If not drawing the ROI
                    if(!stack.getChildren().contains(canvas)) {
                        stack.getChildren().remove(buttonGridPane);
                        stack.getChildren().addAll(canvas, buttonGridPane);
                    }
                    drawNFZ.setText("Draw NFZ");
                    drawingNFZ = false;
                    if(nfzPoints.size()>0) {
                        gc.strokeLine(nfzPoints.get(nfzPoints.size() - 1).getX(), nfzPoints.get(nfzPoints.size() - 1).getY(), nfzPoints.get(0).getX(), nfzPoints.get(0).getY());
                    }
                    if(nfzPoints.size()>2){ // If enough points have been added to the NFZ
                        ArrayList<Coordinate> points = new ArrayList<>(nfzPoints);
                        ArrayList<Coordinate> realPoints = new ArrayList<>(realNfzPoints);
                        allNFZPoints.add(points);
                        allRealNFZPoints.add(realPoints);
                        nfzPoints.clear();
                        realNfzPoints.clear();
                    }
                }else{
                    System.out.println("You must finish drawing the ROI first");
                }
            }
        });

        // When the clear button is pressed
        clear.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // Clear all variables
                stack.getChildren().remove(canvas);
                canvasPoints.clear();
                realPoints.clear();
                nfzPoints.clear();
                realNfzPoints.clear();
                allNFZPoints.clear();
                allRealNFZPoints.clear();
                passCoords.clear();
                startPoint = null;
                realStartPoint = null;
                complete = false;
                drawingNFZ = false;
                drawingROI = false;
                drawROI.setText("Draw ROI");
                drawNFZ.setText("Draw NFZ");
                gc.clearRect(0,0,stack.getWidth(),stack.getHeight());   // Clear canvas
            }
        });

        // When the button to mark the start location is pressed
        setStartLoc.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(drawingStart){
                    setStartLoc.setText("Mark Takeoff and Landing");
                    drawingStart = !drawingStart;
                }else {
                    drawingStart = true;
                    drawingROI = false;
                    drawingNFZ = false;
                    setStartLoc.setText("Click start location");
                }
            }
        });

        // When the location find button is pressed
        findButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!chooseLocation.getText().isEmpty()){
                    try {
                        // https://stackoverflow.com/questions/1359689/how-to-send-http-request-in-java
                        // https://github.com/cdimascio/dotenv-java
                        // https://jar-download.com/artifact-search/java-dotenv

                        String locationString = chooseLocation.getText().replace(" ","+");
                        URL url = new URL(geocodeAPI + locationString+"&key="+ API_KEY);
                        URLConnection urlConnection = url.openConnection();
                        InputStream inputStream =  urlConnection.getInputStream();
                        BufferedReader bufferedReader =  new BufferedReader(new InputStreamReader(inputStream));
                        String input;
                        String geoCodeString = "";
                        while((input = bufferedReader.readLine())!= null){ geoCodeString += input; }

                        JSONObject geoData = new JSONObject(geoCodeString);
                        JSONArray results = (JSONArray) geoData.get("results");
                        JSONObject result = (JSONObject) results.get(0);
                        JSONObject geometry = (JSONObject) result.get("geometry");
                        JSONObject location = (JSONObject) geometry.get("location");

                        BigDecimal lat = (BigDecimal) location.get("lat");
                        BigDecimal lng = (BigDecimal) location.get("lng");

                        Double latitude = lat.doubleValue();
                        Double longitude = lng.doubleValue();

                        GeoPosition position = new GeoPosition(latitude,longitude);
                        mapViewer.setAddressLocation(position);
                        mapViewer.setZoom(5);

                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        });
        return stack;
    }

    /**
     * Function used to add an interactive map to the stackpane
     * @return sn - Swing node that holds the map
     */
    public SwingNode addMap(){
        // Map setup
        mapViewer = new JXMapViewer();
        JXMapKit mapKit = new JXMapKit();
        JToolTip toolTip = new JToolTip();
        toolTip.setComponent(mapKit.getMainMap());

        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);
        tileFactory.setThreadPoolSize(8);

        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));

        // Set the start location
        GeoPosition whitby = new GeoPosition(54.48860179430841, -0.6231669702867165);
        mapViewer.setZoom(8);
        mapViewer.setAddressLocation(whitby);

        mapKit.setTileFactory(tileFactory);
        mapKit.getMainMap().addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {

            }

            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {

                JXMapViewer map = mapKit.getMainMap();

                // convert to world bitmap
                Point2D worldPos = map.getTileFactory().geoToPixel(whitby, map.getZoom());

                // convert to screen
                Rectangle rect = map.getViewportBounds();
                int sx = (int) worldPos.getX() - rect.x;
                int sy = (int) worldPos.getY() - rect.y;
                Point screenPos = new Point(sx, sy);

                // check if near the mouse
                if (screenPos.distance(e.getPoint()) < 20)
                {
                    screenPos.x -= toolTip.getWidth() / 2;
                    toolTip.setLocation(screenPos);
                    toolTip.setVisible(true);
                }
                else
                {
                    toolTip.setVisible(false);
                }
            }
        });

        SwingNode sn = new SwingNode();
        sn.setContent(mapViewer);
        return sn;
    }

    /**
     * Add point to list of points and draw the point on the canvas
     * @param event - The event interacting with the canvas
     */
    public void placePoint(MouseEvent event){
        Double x =  event.getX();
        Double y = event.getY();
        if(!complete && drawingROI) {
            gc.setStroke(markerPaint);
            gc.setFill(markerPaint);
            gc.setLineWidth(2);
            gc.fillOval(x - markerRadius / 2, y - markerRadius / 2, markerRadius, markerRadius);
            Point2D point2D = new Point((int)Math.round(x),(int)Math.round(y));
            GeoPosition geoPosition = mapViewer.convertPointToGeoPosition(point2D);

            canvasPoints.add(new Coordinate(x, y,geoPosition.getLongitude(),geoPosition.getLatitude()));
            realPoints.add(new Coordinate(x,this.canvas.getHeight()-y,geoPosition.getLongitude(),geoPosition.getLatitude()));
            if(canvasPoints.size()>1){
                gc.strokeLine(canvasPoints.get(canvasPoints.size()-1).getX(),canvasPoints.get(canvasPoints.size()-1).getY(), canvasPoints.get(canvasPoints.size()-2).getX(), canvasPoints.get(canvasPoints.size()-2).getY());
            }
            gc.setFill(textPaint);
            String num = Integer.toString(canvasPoints.size());
            gc.fillText(num, x - num.length()*4, y - -4);
        }
        else if(drawingNFZ){
            gc.setStroke(nfzMarkerPaint);
            gc.setFill(nfzMarkerPaint);
            gc.setLineWidth(2);
            gc.fillOval(x - nfzMarkerRadius/2, y - nfzMarkerRadius/2, nfzMarkerRadius, nfzMarkerRadius);
            nfzPoints.add(new Coordinate(x,y));
            realNfzPoints.add(new Coordinate(x,this.canvas.getHeight()-y));
            //gc.fillText(Integer.toString(nfzPoints.size()), x, y);
            if(nfzPoints.size()>1){
                gc.strokeLine(nfzPoints.get(nfzPoints.size()-1).getX(),nfzPoints.get(nfzPoints.size()-1).getY(), nfzPoints.get(nfzPoints.size()-2).getX(), nfzPoints.get(nfzPoints.size()-2).getY());
            }
        }else if(drawingStart){
            startPoint = new Coordinate(x,y);
            realStartPoint = new Coordinate(x,this.canvas.getHeight()-y);
            gc.setFill(startMarkerPaint);
            gc.fillOval(x-startMarkerRadius/2,y-startMarkerRadius/2,startMarkerRadius,startMarkerRadius);
            gc.setFill(textPaint);
            gc.fillText("S/L",x-10,y+4);
            setStartLoc.setText("Mark Takeoff and Landing");
            drawingStart = false;
        }
        else{
            System.out.println("Press the reset button as the path has been finalised!");
        }
    }

    /**
     *
     * @param coord1
     * @param coord2
     */
    public void addPassCoords(Coordinate coord1, Coordinate coord2){
        ArrayList<Coordinate> pass = new ArrayList<Coordinate>();
        pass.add(coord1);
        pass.add(coord2);
        this.passCoords.add(pass);
    }

    /**
     *
     */
    public void drawPasses(){
        gc.setStroke(passPaint);
        for(int i=0;i<passCoords.size();i++) {
            ArrayList<Coordinate> coords = passCoords.get(i);
            gc.strokeLine(coords.get(0).x,coords.get(0).y,coords.get(1).x,coords.get(1).y);
        }
    }

    /**
     *
     * @param dubinsObject
     */
    public void drawDubins(JSONObject dubinsObject){
        JSONArray dubinsArray = (JSONArray) dubinsObject.get("dubins");
        JSONArray spiralsArray = (JSONArray) dubinsObject.get("spirals");
        gc.setFill(Color.RED);
        for(int i = 0; i<dubinsArray.length();i++){
            JSONObject dubinsPoints = (JSONObject) dubinsArray.get(i);
            JSONArray pointArray = (JSONArray) dubinsPoints.get("points");
            for(int j = 0;j<pointArray.length();j++){
                String pointString = pointArray.get(j).toString();
                pointString = pointString.replace("(","");
                pointString = pointString.replace(")","");
                String[] pointContents = pointString.split(",");
                int x = (int) Double.parseDouble(pointContents[0].trim());
                int y = (int)Double.parseDouble(pointContents[1].trim());
                gc.fillOval(x,canvas.getHeight()-y,2,2);
            }
        }
        for(int i = 0; i<spiralsArray.length();i++){
            JSONObject spiralsPoints = (JSONObject) spiralsArray.get(i);
            JSONArray pointArray = (JSONArray) spiralsPoints.get("points");
            for(int j = 0;j<pointArray.length();j++){
                String pointString = pointArray.get(j).toString();
                pointString = pointString.replace("(","");
                pointString = pointString.replace(")","");
                String[] pointContents = pointString.split(",");
                int x = (int) Double.parseDouble(pointContents[0].trim());
                int y = (int)Double.parseDouble(pointContents[1].trim());
                gc.fillOval(x,canvas.getHeight()-y,2,2);
            }
        }
        gc.setFill(passPaint);
    }

    public ArrayList<Coordinate> getCanvasPoints(){
        return this.canvasPoints;
    }

    public ArrayList<Coordinate> getRealPoints(){
        return this.realPoints;
    }

    public Coordinate getStartPoint(){
        return this.startPoint;
    }

    public Coordinate getRealStartPoint(){
        return this.realStartPoint;
    }

    public ArrayList<ArrayList<Coordinate>> getAllNFZs(){
        return this.allNFZPoints;
    }

    public ArrayList<ArrayList<Coordinate>> getAllRealNFZPoints(){
        return this.allRealNFZPoints;
    }

    public SwingNode getMapNode(){
        return sn;
    }

    public Canvas getCanvas(){
        return this.canvas;
    }

    public JXMapViewer getMap(){
        return this.mapViewer;
    }
}
