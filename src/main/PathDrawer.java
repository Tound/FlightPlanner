package main;

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
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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


public class PathDrawer{
    private Canvas canvas;
    private StackPane stack;
    private double polygon;
    private double NFZs;
    private double width;
    private double height;
    private double scale;
    private GraphicsContext gc;
    private ArrayList<Coordinate> canvasPoints = new ArrayList<Coordinate>();
    private ArrayList<Coordinate> realPoints = new ArrayList<Coordinate>();
    private ArrayList<Coordinate> nfzPoints = new ArrayList<Coordinate>();
    private ArrayList<Coordinate> realNfzPoints = new ArrayList<Coordinate>();
    private ArrayList<ArrayList<Coordinate>> allNFZPoints = new ArrayList<ArrayList<Coordinate>>();
    private ArrayList<ArrayList<Coordinate>> allRealNFZPoints = new ArrayList<ArrayList<Coordinate>>();
    private ArrayList<ArrayList<Coordinate>> pass_coords = new ArrayList<ArrayList<Coordinate>>();
    private Coordinate startPoint = null;
    private Coordinate realStartPoint = null;
    private Coordinate takeoff;

    private boolean drawingROI = false;
    private boolean drawingNFZ = false;
    private boolean drawingStart = false;

    private boolean complete = false;
    private double markerRadius = 20;
    private double nfzMarkerRadius = 10;
    private double startMarkerRadius = 24;
    private Paint markerPaint = Color.BLACK;
    private Paint nfzMarkerPaint = Color.RED;
    private Paint startMarkerPaint = Color.BLUE;
    private Paint textPaint = Color.WHITE;
    private Paint passPaint = Color.BLUE;
    private Font markerFont = Font.font("Arial",FontPosture.ITALIC,15);//new Font("Arial", 16);

    private Button drawROI = new Button("Draw ROI");
    private Button drawNFZ = new Button("Draw NFZ");
    private Button clear = new Button("Clear");
    private Button setStart = new Button("Mark Takeoff and Landing");
    private Button findButton = new Button("Find");
    private TextField chooseLocation = new TextField();
    //private Button done = new Button("Done");
    public HBox hBox = new HBox(drawROI,drawNFZ,clear,setStart,chooseLocation,findButton);
    private JXMapViewer mapViewer;
    private SwingNode sn;

    private String geocodeAPI = "https://maps.googleapis.com/maps/api/geocode/json?address=";

    public PathDrawer(){
        this.canvas = new Canvas();
        this.stack = new StackPane();
        this.gc = canvas.getGraphicsContext2D();
        this.width = canvas.getWidth();
        this.height = canvas.getWidth();
        this.chooseLocation.setPromptText("Jump to location");
        //stack.setMaxHeight(Double.MAX_VALUE);

        canvas.setFocusTraversable(true);

        sn = addMap();
        sn.setPickOnBounds(false);
        stack.getChildren().addAll(sn,hBox);
        //stack.setPickOnBounds(false);
        hBox.setPickOnBounds(false);
        //stack.setFocusTraversable(true);
        //HBox.setHgrow(stack,Priority.ALWAYS);
        stack.setBackground(new Background(new BackgroundFill(Color.WHITE,CornerRadii.EMPTY,Insets.EMPTY)));
        gc.setFont(markerFont);
    }

    public StackPane createPathDrawer(BorderPane bp){
        System.out.println("Added Canvas");
        //gc.clearRect(0,0,width,height);
        canvas.widthProperty().bind(stack.widthProperty());
        canvas.heightProperty().bind(stack.heightProperty());
        System.out.println(stack.getPrefHeight());
        canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                MouseButton button = event.getButton();
                if(button.equals(MouseButton.PRIMARY)){
                    placePoint(event);
                }
            }
        });

        drawROI.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!drawingROI && !drawingNFZ){
                    if(!stack.getChildren().contains(canvas)) {
                        stack.getChildren().remove(hBox);
                        stack.getChildren().addAll(canvas, hBox);
                    }
                    drawROI.setText("Done");
                    drawingROI = true;
                    complete = false;
                    if(canvasPoints.size()>0) {
                        canvasPoints.clear();
                        gc.clearRect(0,0,stack.getWidth(),stack.getHeight());
                        for(int i=0;i<nfzPoints.size();i++) {
                            gc.strokeOval(nfzPoints.get(i).getX() - nfzMarkerRadius / 2, nfzPoints.get(i).getY() - nfzMarkerRadius / 2, nfzMarkerRadius, nfzMarkerRadius);
                            //gc.fillText(Integer.toString(i+1), points.get(i).getX(), points.get(i).getY());
                            if (i > 0) {
                                gc.strokeLine(nfzPoints.get(i).getX(), nfzPoints.get(i).getY(), nfzPoints.get(i - 1).getX(), nfzPoints.get(i - 1).getY());
                            }
                        }
                    }
                }else if(!drawingNFZ){
                    drawROI.setText("Draw ROI");
                    drawingROI = false;
                    complete = true;
                    if(canvasPoints.size()>0) {
                        gc.strokeLine(canvasPoints.get(canvasPoints.size() - 1).getX(), canvasPoints.get(canvasPoints.size() - 1).getY(), canvasPoints.get(0).getX(), canvasPoints.get(0).getY());
                        canvasPoints.add(canvasPoints.get(0));
                    }
                }else{
                    System.out.println("You must finish drawing the NFZ first");
                }
            }
        });
        drawNFZ.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!drawingNFZ && !drawingROI){ //If not drawing the NFZ and not drawing the ROI
                     drawNFZ.setText("Done");
                    drawingNFZ = true;

                }else if(!drawingROI){ //If not drawing the ROI
                    if(!stack.getChildren().contains(canvas)) {
                        stack.getChildren().remove(hBox);
                        stack.getChildren().addAll(canvas, hBox);
                    }
                    drawNFZ.setText("Draw NFZ");
                    drawingNFZ = false;
                    //if(allNFZPoints.get(allNFZPoints.size()-1).add())
                    if(nfzPoints.size()>0) {
                        gc.strokeLine(nfzPoints.get(nfzPoints.size() - 1).getX(), nfzPoints.get(nfzPoints.size() - 1).getY(), nfzPoints.get(0).getX(), nfzPoints.get(0).getY());
                        //nfzPoints.add(nfzPoints.get(0));
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
        clear.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stack.getChildren().remove(canvas);
                canvasPoints.clear();
                realPoints.clear();
                nfzPoints.clear();
                realNfzPoints.clear();
                allNFZPoints.clear();
                allRealNFZPoints.clear();
                pass_coords.clear();
                startPoint = null;
                realStartPoint = null;
                complete = false;
                drawingNFZ = false;
                drawingROI = false;
                drawROI.setText("Draw ROI");
                drawNFZ.setText("Draw NFZ");
                gc.clearRect(0,0,stack.getWidth(),stack.getHeight());
            }
        });
        setStart.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(drawingStart){
                    setStart.setText("Mark Takeoff and Landing");
                    drawingStart = !drawingStart;
                }else {
                    drawingStart = true;
                    drawingROI = false;
                    drawingNFZ = false;
                    setStart.setText("Click start location");
                }
            }
        });
        findButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!chooseLocation.getText().isEmpty()){
                    String geocodeString = geocodeAPI;
                    try {
                        URL url = new URL(geocodeAPI);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    //GeoPosition position = new GeoPosition();
                    //mapViewer.setAddressLocation();
                }
            }
        });
        return stack;
    }

    public SwingNode addMap(){
        //JAVA MAP
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

        GeoPosition whitby = new GeoPosition(54.48860179430841, -0.6231669702867165);
        mapViewer.setZoom(8);
        mapViewer.setAddressLocation(whitby);

        mapViewer.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if(e.getButton() == java.awt.event.MouseEvent.BUTTON3){
                    Point p = e.getPoint();
                    GeoPosition geo = mapViewer.convertPointToGeoPosition(p);
                    System.out.println("x:" + geo.getLatitude()+"Y:"+geo.getLongitude());
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {

            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {

            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {

            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {

            }
        });
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
            System.out.println(num.length());
            gc.fillText(num, x - num.length()*4, y - -4);
            System.out.println("X: " + x + " Y: " + y);
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
            setStart.setText("Mark Takeoff and Landing");
            drawingStart = false;
        }
        else{
            System.out.println("Press the reset button as the path has been finalised!");
        }
    }
    public void addPassCoords(Coordinate coord1, Coordinate coord2){
        ArrayList<Coordinate> pass = new ArrayList<Coordinate>();
        pass.add(coord1);
        pass.add(coord2);
        this.pass_coords.add(pass);
    }
    public void drawPasses(){
        gc.setStroke(passPaint);
        for(int i=0;i<pass_coords.size();i++) {
            ArrayList<Coordinate> coords = pass_coords.get(i);
            gc.strokeLine(coords.get(0).x,coords.get(0).y,coords.get(1).x,coords.get(1).y);
        }
    }

    public void drawDubins(JSONObject dubinsObject){
        JSONArray dubinsArray = (JSONArray) dubinsObject.get("dubins");
        gc.setFill(Color.YELLOW);
        for(int i = 0; i<dubinsArray.length();i++){
            JSONObject dubinsPoints = (JSONObject) dubinsArray.get(i);
            JSONArray pointArray = (JSONArray) dubinsPoints.get("points");
            for(int j = 0;j<pointArray.length();j++){
                String pointString = pointArray.get(j).toString();
                System.out.println(pointString);
                pointString = pointString.replace("(","");
                pointString = pointString.replace(")","");
                System.out.println(pointString);
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
    public ArrayList<Coordinate> getRealPoints(){return this.realPoints; }
    public Coordinate getStartPoint(){ return this.startPoint;}
    public Coordinate getRealStartPoint(){ return this.realStartPoint;}
    public ArrayList<ArrayList<Coordinate>> getAllNFZs(){ return this.allNFZPoints; }
    public ArrayList<ArrayList<Coordinate>> getAllRealNFZPoints(){ return this.allRealNFZPoints; }
    public SwingNode getMapNode(){ return sn; }
    public Canvas getCanvas(){ return this.canvas; }
    public int getMapScale(){ return mapViewer.getZoom(); }
    public JXMapViewer getMap(){ return this.mapViewer; }
}
