package main;

import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class CreateUAV {
    private static Stage createStage = new Stage();
    private static Scene createScene;
    private static String path = "src/uavs";
    private static TextField name = new TextField();
    private static TextField weight = new TextField();
    private static TextField turnRadius = new TextField();
    private static TextField maxIncline = new TextField();
    private static TextField battery = new TextField();
    private static TextField batteryCapacity = new TextField();

    private static Stage dialog = new Stage();
    private static Label dialogMessage = new Label("The filename "+ name.getText() +".uav already exists in "+path+".\n Would you like to overwrite?");

    public CreateUAV(){
        createStage.setTitle("UAV Setup");
        createStage.initModality(Modality.APPLICATION_MODAL);
        // Setup menu
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
                writeUAV();
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

    public static void newUAV(){
        GridPane gp = new GridPane();
        Text title =  new Text("Create a UAV:\nUAV Settings");
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

        name.setPromptText("Name of craft");
        weight.setPromptText("Weight of craft");
        turnRadius.setPromptText("Min turn radius of craft");
        maxIncline.setPromptText("Max incline/decline angle");
        battery.setPromptText("Battery used in craft (Lipo, NiMH, Li-ion...)");
        batteryCapacity.setPromptText("Battery capacity in mAh");

        Button save = new Button("Save");
        Button cancel = new Button("Cancel");
        Button clear = new Button("Clear");

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
        gp.add(clear,1,7);

        gp.setHgap(10);
        gp.setVgap(20);
        gp.setAlignment(Pos.CENTER);

        //gp.setStyle("-fx-background-color: rgb(42, 45, 48)");

        save.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                if(name.getText().isEmpty()){
                    System.out.println("Missing name for UAV");
                }else {
                    File file = new File("src/uavs/"+name.getText()+".uav");
                    if(file.exists()){
                        dialogMessage.setText("The filename "+ name.getText() + ".uav already exists in "+path+".\n Would you like to overwrite?");
                        dialog.show();
                    }else {
                        writeUAV();
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

        clear.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                name.clear();
                weight.clear();
                turnRadius.clear();
                maxIncline.clear();
                battery.clear();
                batteryCapacity.clear();
            }
        });

        createScene = new Scene(gp,500,700, Color.GRAY);
        createScene.setUserAgentStylesheet("style/menus.css");
        createStage.setScene(createScene);
        createStage.show();
    }

    public static void writeUAV(){
        System.out.println("Writing new UAV");
        Document dom;
        Element e = null;

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            dom = documentBuilder.newDocument();
            Element rootElement = dom.createElement("UAV");

            e = dom.createElement("name");
            e.appendChild(dom.createTextNode(name.getText()));
            rootElement.appendChild(e);

            e = dom.createElement("weight");
            e.appendChild(dom.createTextNode(weight.getText()));
            rootElement.appendChild(e);

            e = dom.createElement("turn_radius");
            e.appendChild(dom.createTextNode(turnRadius.getText()));
            rootElement.appendChild(e);

            e = dom.createElement("max_incline");
            e.appendChild(dom.createTextNode(maxIncline.getText()));
            rootElement.appendChild(e);

            e = dom.createElement("battery");
            e.appendChild(dom.createTextNode(battery.getText()));
            rootElement.appendChild(e);

            e = dom.createElement("battery_capacity");
            e.appendChild(dom.createTextNode(batteryCapacity.getText()));
            rootElement.appendChild(e);

            dom.appendChild(rootElement);

            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT,"yes");
            tr.setOutputProperty(OutputKeys.METHOD,"xml");
            tr.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
            //tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"uav.dtd");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","4");

            tr.transform(new DOMSource(dom),new StreamResult(new FileOutputStream("src/uavs/"+name.getText()+".uav")));

        }catch (IOException | ParserConfigurationException | TransformerConfigurationException ioe){
            ioe.printStackTrace();
        } catch (TransformerException transformerException) {
            transformerException.printStackTrace();
        }
    }

    public String loadUAV(){
        //Parser
    return "UAV";
    }

    public void showPopup(String name){
        dialogMessage.setText("OK");
        dialog.show();
    }

    private static void writeFile(){
        try {
            System.out.println("Writing to "+path+"/"+name.getText()+".uav");
            FileWriter writer = new FileWriter(path + "/" + name.getText() + ".uav");
            System.out.println("Writing file");
            writer.write("name:" + name.getText());
            writer.write("\nweight:" + weight.getText());
            writer.write("\nturnRadius:" + turnRadius.getText());
            writer.write("\nbattery:" + battery.getText());
            writer.close();
        }catch (IOException ioe){
            ioe.printStackTrace();
            System.out.println("Unable to write to file");
        }
    }

}
