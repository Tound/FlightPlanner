package main;

import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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

/**
 * Class used to write UAV settings in an XML file
 */
public class CreateUAV {
    private static Stage createStage;
    private static Scene createScene;
    private static String path = "src/uavs";
    private static TextField name = new TextField();
    private static TextField weight = new TextField();
    private static TextField turnRadius = new TextField();
    private static TextField maxIncline = new TextField();
    private static TextField battery = new TextField();
    private static TextField batteryCapacity = new TextField();

    private static Stage dialog = new Stage();
    private static Label dialogMessage = new Label("The filename "+ name.getText() +
            ".uav already exists in "+path+".\n Would you like to overwrite?");

    /**
     *  CreateUAV constructor
     *  Class is used to write and save settings for the UAVs
     */
    public CreateUAV(){
        createStage = new Stage();
        createStage.setTitle("UAV Setup");
        createStage.initModality(Modality.APPLICATION_MODAL);
        setupOverwriteDialog();
    }

    /**
     * Create a dialog box for file overwrites
     */
    private void setupOverwriteDialog(){
        // Setup dialog box
        dialog.setTitle("Overwrite File?");
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Setup buttons
        Button yes = new Button("Yes");
        Button no = new Button("No");

        BorderPane bp = new BorderPane();
        bp.setCenter(dialogMessage);
        bp.setId("border");             // Set CSS style ID
        HBox hb = new HBox(yes, no);

        BorderPane.setAlignment(hb, Pos.CENTER);
        bp.setBottom(hb);

        Scene dialogScene = new Scene(bp,400,100);      // Create the scene for the dialog box
        dialogScene.setUserAgentStylesheet("style/menus.css");      // Set the style for the dialog scene
        dialog.setScene(dialogScene);

        // If overwrite is selected
        yes.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Set overwrite to true");
                writeUAV();             // Write the settings
                dialog.close();
                createStage.close();    // Close the dialog box
            }
        });

        // If overwrite is not selected
        no.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Change the name of the craft and re-save");
                dialog.close();
            }
        });
    }

    /**
     * Menu to create uav settings
     */
    public static void newUAV(){
        GridPane gp = new GridPane();
        Text title =  new Text("Create a UAV:\nUAV Settings");
        title.setTextAlignment(TextAlignment.CENTER);
        title.setId("title");           // Set CSS style for the title
        GridPane.setColumnSpan(title,3);
        GridPane.setHalignment(title, HPos.CENTER);

        gp.setId("gridpane");           // Set CSS style ID for the gridpane
        gp.getStyleClass().add("grid");

        // Create the labels for the menu
        Label nameLabel =  new Label("Name:");
        Label weightLabel =  new Label("Weight (g):");
        Label turnRadiusLabel =  new Label("Turn Radius (m):");
        Label maxInclineLabel =  new Label("Max incline angle (deg):");
        Label batteryLabel =  new Label("Battery Type:");
        Label batteryCapacityLabel =  new Label("Battery Capacity (mAh):");

        name.setPromptText("Name of craft");
        weight.setPromptText("Weight of craft");
        turnRadius.setPromptText("Min turn radius of craft");
        maxIncline.setPromptText("Max incline/decline angle");
        battery.setPromptText("Battery used in craft (Lipo, NiMH, Li-ion...)");
        batteryCapacity.setPromptText("Battery capacity in mAh");

        // Create the buttons for the menu
        Button save = new Button("Save");
        Button cancel = new Button("Cancel");
        Button clear = new Button("Clear");

        // Add all elements to the menu gridpane
        gp.add(title,0,0);

        // Add the components to the menu gridpane
        gp.add(nameLabel,0,1);
        gp.add(weightLabel,0,2);
        gp.add(turnRadiusLabel,0,3);
        gp.add(maxInclineLabel,0,4);
        gp.add(batteryLabel,0,5);
        gp.add(batteryCapacityLabel,0,6);

        gp.add(name,1,1);
        gp.add(weight,1,2);
        gp.add(turnRadius,1,3);
        gp.add(maxIncline,1,4);
        gp.add(battery,1,5);
        gp.add(batteryCapacity,1,6);

        // Add the buttons to the menu
        gp.add(save,0,7);
        gp.add(cancel,2,7);
        gp.add(clear,1,7);

        gp.setHgap(10);
        gp.setVgap(20);
        gp.setAlignment(Pos.CENTER);

        // If the save button is pressed
        save.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // If a name has not been set
                if(name.getText().isEmpty()){
                    System.out.println("Missing name for UAV");
                }else {
                    File file = new File("src/uavs/"+name.getText()+".uav");
                    if(file.exists()){
                        dialogMessage.setText("The filename "+ name.getText() +
                                ".uav already exists in "+path+".\n Would you like to overwrite?");
                        dialog.show();
                    }else {
                        writeUAV();      // Write the UAV settings
                        createStage.close();
                    }
                }
            }
        });

        // Close the menu
        cancel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                createStage.close();
            }
        });

        // Clear all text fields
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

        createScene = new Scene(gp,500,700);
        createScene.setUserAgentStylesheet("style/menus.css");  // Set the style of the menu
        createStage.setScene(createScene);
        createStage.show();
    }

    /**
     * Write UAV settings in an XML file
     */
    public static void writeUAV(){
        System.out.println("Writing new UAV");
        Document dom;
        Element e = null;

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            dom = documentBuilder.newDocument();
            Element rootElement = dom.createElement("UAV");

            // Create the name tag and write the name data
            e = dom.createElement("name");
            e.appendChild(dom.createTextNode(name.getText()));
            rootElement.appendChild(e);

            // Create the weight tag and write the weight data
            e = dom.createElement("weight");
            e.appendChild(dom.createTextNode(weight.getText()));
            rootElement.appendChild(e);

            // Create the turn radius tag and write the turn radius data
            e = dom.createElement("turn_radius");
            e.appendChild(dom.createTextNode(turnRadius.getText()));
            rootElement.appendChild(e);

            // Create the max incline angle tag and write the max incline angle data
            e = dom.createElement("max_incline");
            e.appendChild(dom.createTextNode(maxIncline.getText()));
            rootElement.appendChild(e);

            // Create the battery type tag and write the battery type data
            e = dom.createElement("battery");
            e.appendChild(dom.createTextNode(battery.getText()));
            rootElement.appendChild(e);

            // Create the battery capacity tag and write the battery capacity data
            e = dom.createElement("battery_capacity");
            e.appendChild(dom.createTextNode(batteryCapacity.getText()));
            rootElement.appendChild(e);

            dom.appendChild(rootElement);

            // Set the transforms to make the XML document
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT,"yes");
            tr.setOutputProperty(OutputKeys.METHOD,"xml");
            tr.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
            //tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"uav.dtd");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","4");

            // Write the data to the file
            tr.transform(new DOMSource(dom),new StreamResult(
                    new FileOutputStream("src/uavs/"+name.getText()+".uav")));

        }catch (IOException | ParserConfigurationException | TransformerException ioe){
            ioe.printStackTrace();
            // If error, show dialog box with the error message
            // If error, show dialog box with the error message
            Dialog.showDialog("Unable to write to write camera settings.\n\n" +
                    "Ensure that " + path + " is visible by the application.","Unable to write settings");
        }
    }

}
