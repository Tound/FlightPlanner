package main;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

import java.util.ArrayList;


public class PathDrawer{
    private Canvas canvas;
    private StackPane stack;
    private double polygon;
    private double NFZs;
    private double width;
    private double height;
    private GraphicsContext gc;
    private ArrayList<Coordinate> points = new ArrayList<Coordinate>();
    private ArrayList<Coordinate> nfzPoints = new ArrayList<Coordinate>();
    private ArrayList<Coordinate> allNFZPoints = new ArrayList<Coordinate>();
    private Coordinate takeoff;

    private boolean drawingROI = false;
    private boolean drawingNFZ = false;

    private boolean complete = false;
    private double markerRadius = 20;
    private double nfzMarkerRadius = 10;
    private Paint markerPaint = Color.BLACK;
    private Paint nfzMarkerPaint = Color.RED;
    private Paint textPaint = Color.WHITE;
    private Font markerFont = Font.font("Arial",FontPosture.ITALIC,15);//new Font("Arial", 16);

    private Button drawROI = new Button("Draw ROI");
    private Button drawNFZ = new Button("Draw NFZ");
    private Button clear = new Button("Clear");
    //private Button done = new Button("Done");
    public HBox hBox = new HBox(drawROI,drawNFZ,clear);

    public PathDrawer(){
        this.canvas = new Canvas();
        this.stack = new StackPane();
        this.gc = canvas.getGraphicsContext2D();
        this.width = canvas.getWidth();
        this.height = canvas.getWidth();
        //stack.setMaxHeight(Double.MAX_VALUE);
        stack.getChildren().addAll(canvas,hBox);
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
                    placePoint(event.getX(), event.getY());
                }
            }
        });
        /*canvas.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(points.size() > 0 && drawingROI == true){
                    gc.clearRect(0,0,stack.getWidth(),stack.getHeight());
                    for(int i=0;i<points.size();i++){
                        System.out.println(i);
                        gc.strokeOval(points.get(i).getX() - markerRadius / 2, points.get(i).getY() - markerRadius / 2, markerRadius, markerRadius);
                        gc.fillText(Integer.toString(i+1), points.get(i).getX(), points.get(i).getY());
                        if(i>0){
                            gc.strokeLine(points.get(i).getX(),points.get(i).getY(), points.get(i-1).getX(), points.get(i-1).getY());
                        }
                    }
                    System.out.println("DONE");
                    gc.strokeLine(points.get(0).getX(),points.get(0).getY(), event.getX(), event.getY());
                    gc.strokeLine(points.get(points.size()-1).getX(),points.get(points.size()-1).getY(), event.getX(), event.getY());
                }else{

                }
            }
        });*/
        drawROI.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!drawingROI && !drawingNFZ){
                    drawROI.setText("Done");
                    drawingROI = true;
                    complete = false;
                    if(points.size()>0) {
                        points.clear();
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
                    if(points.size()>0) {
                        gc.strokeLine(points.get(points.size() - 1).getX(), points.get(points.size() - 1).getY(), points.get(0).getX(), points.get(0).getY());
                        points.add(points.get(0));
                    }

                    //joinPath();
                }else{
                    System.out.println("You must finish drawing the NFZ first");
                }
            }
        });
        drawNFZ.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(!drawingNFZ && !drawingROI){
                    drawNFZ.setText("Done");
                    drawingNFZ = true;
                    /*if(nfzPoints.size()>0){
                        nfzPoints.clear();
                    }*/
                }else if(!drawingROI){
                    drawNFZ.setText("Draw NFZ");
                    drawingNFZ = false;
                    if(nfzPoints.size()>0) {
                        gc.strokeLine(nfzPoints.get(nfzPoints.size() - 1).getX(), nfzPoints.get(nfzPoints.size() - 1).getY(), nfzPoints.get(0).getX(), nfzPoints.get(0).getY());
                        nfzPoints.add(nfzPoints.get(0));
                    }
                }else{
                    System.out.println("You must finish drawing the ROI first");
                }
            }
        });
        clear.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                points.clear();
                nfzPoints.clear();
                complete = false;
                drawingNFZ = false;
                drawingROI = false;
                drawROI.setText("Draw ROI");
                drawNFZ.setText("Draw NFZ");
                gc.clearRect(0,0,stack.getWidth(),stack.getHeight());
            }
        });
        return stack;
    }

    public void placePoint(double x, double y){
        if(!complete && drawingROI) {
            gc.setStroke(markerPaint);
            gc.setFill(markerPaint);
            gc.setLineWidth(2);
            gc.fillOval(x - markerRadius / 2, y - markerRadius / 2, markerRadius, markerRadius);
            points.add(new Coordinate((int)x, (int)y));
            if(points.size()>1){
                gc.strokeLine(points.get(points.size()-1).getX(),points.get(points.size()-1).getY(), points.get(points.size()-2).getX(), points.get(points.size()-2).getY());
            }
            gc.setFill(textPaint);
            String num = Integer.toString(points.size());
            System.out.println(num.length());
            gc.fillText(num, x - num.length()*4, y - -4);
            System.out.println("X: " + x + " Y: " + y);
        }
        else if(drawingNFZ){
            gc.setStroke(nfzMarkerPaint);
            gc.setFill(nfzMarkerPaint);
            gc.setLineWidth(2);
            gc.fillOval(x - nfzMarkerRadius/2, y - nfzMarkerRadius/2, nfzMarkerRadius, nfzMarkerRadius);
            nfzPoints.add(new Coordinate((int)x, (int)y));
            //gc.fillText(Integer.toString(nfzPoints.size()), x, y);
            if(nfzPoints.size()>1){
                gc.strokeLine(nfzPoints.get(nfzPoints.size()-1).getX(),nfzPoints.get(nfzPoints.size()-1).getY(), nfzPoints.get(nfzPoints.size()-2).getX(), nfzPoints.get(nfzPoints.size()-2).getY());
            }
        }
        else{
            System.out.println("Press the reset button as the path has been finalised!");
        }
    }
    public ArrayList<Coordinate> getPoints(){
        return points;
    }
    public ArrayList<Coordinate> getNfzPoints(){
        return nfzPoints;
    }
}
