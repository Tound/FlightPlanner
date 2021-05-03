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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Class used to write camera settings in an XML file
 */
public class CreateCamera {

    private static Stage createStage;
    private static Scene createScene;
    private static String path = "src/cameras";
    private static TextField name = new TextField();
    private static TextField sensorWidth = new TextField();
    private static TextField sensorHeight = new TextField();
    private static TextField focalLocal = new TextField();
    private static TextField resolution = new TextField();
    private static TextField aspectRatio = new TextField();

    private static Stage dialog = new Stage();
    private static Label dialogMessage = new Label("The filename "+ name.getText() +
            ".cam already exists in "+path+".\n Would you like to overwrite?");

    /**
     * Create camera constructor
     * Class is used to write and save settings for the cameras
     */
    public CreateCamera(){
        createStage = new Stage();
        createStage.setTitle("Camera Setup");
        createStage.initModality(Modality.APPLICATION_MODAL);
        setupOverwriteDialog();      // Setup overwrite dialog box
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
                writeCamera();          // Write the settings
                dialog.close();
                createStage.close();    // Close the dialog box
            }
        });

        // If overwrite is not selected
        no.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Change the name of the camera and re-save");
                dialog.close();
            }
        });
    }

    /**
     * Create the menu that is used to set the settings
     */
    public void newCamera(){
        GridPane gp = new GridPane();
        Text title =  new Text("Create a Camera:\nCamera Settings");
        title.setTextAlignment(TextAlignment.CENTER);
        title.setId("title");
        GridPane.setColumnSpan(title,3);
        GridPane.setHalignment(title, HPos.CENTER);

        gp.setId("gridpane");           // Set CSS style ID
        gp.getStyleClass().add("grid");

        // Create the labels for the menu
        Label nameLabel = new Label("Name:");
        Label sensorWidthLabel = new Label("Sensor Width (mm):");
        Label sensorHeightLabel = new Label("Sensor height (mm):");
        Label focalLengthLabel = new Label("Focal Length:");
        Label resolutionLabel = new Label("Resolution:");
        Label aspectRatioLabel = new Label("Aspect Ratio:");

        name.setPromptText("Name of Camera");
        sensorWidth.setPromptText("Sensor Width (mm)");
        sensorHeight.setPromptText("Sensor Height (mm)");
        focalLocal.setPromptText("Focal Length");
        resolution.setPromptText("Resolution (MP)");
        aspectRatio.setPromptText("Aspect Ratio");

        // Create the buttons for the menu
        Button save = new Button("Save");
        Button cancel = new Button("Cancel");
        Button clear = new Button("Set to default");

        // Add all elements to the menu gridpane
        gp.add(title,0,0);

        // Add the components to the menu gridpane
        gp.add(nameLabel,0,1);
        gp.add(sensorWidthLabel,0,2);
        gp.add(sensorHeightLabel,0,3);
        gp.add(focalLengthLabel,0,4);
        gp.add(resolutionLabel,0,5);
        gp.add(aspectRatioLabel,0,6);

        gp.add(name,1,1);
        gp.add(sensorWidth,1,2);
        gp.add(sensorHeight,1,3);
        gp.add(focalLocal,1,4);
        gp.add(resolution,1,5);
        gp.add(aspectRatio,1,6);

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
                    File file = new File(path+name.getText()+".cam");
                    if(file.exists()){      // Show overwrite dialog if file name already exists
                        dialogMessage.setText("The filename "+ name.getText() +
                                ".cam already exists in "+path+".\n Would you like to overwrite?");
                        dialog.show();
                    }else {
                        writeCamera();      // Write the camera settings
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
                sensorWidth.clear();
                sensorHeight.clear();
                focalLocal.clear();
                resolution.clear();
                aspectRatio.clear();
            }
        });

        createScene = new Scene(gp,500,700);
        createScene.setUserAgentStylesheet("style/menus.css");  // Set the style of the menu
        createStage.setScene(createScene);
        createStage.show();
    }

    /**
     * Write camera settings in an XML file
     */
    public void writeCamera(){
        System.out.println("Writing new Camera");
        Document dom;
        Element e = null;

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            dom = documentBuilder.newDocument();
            Element rootElement = dom.createElement("camera");

            // Create the name tag and write the name data
            e = dom.createElement("name");
            e.appendChild(dom.createTextNode(name.getText()));
            rootElement.appendChild(e);

            // Create the sensor width tag and write the sensor width data
            e = dom.createElement("sensor_x");
            e.appendChild(dom.createTextNode(sensorWidth.getText()));
            rootElement.appendChild(e);

            // Create the sensor height tag and write the sensor height data
            e = dom.createElement("sensor_y");
            e.appendChild(dom.createTextNode(sensorHeight.getText()));
            rootElement.appendChild(e);

            // Create the focal length tag and write the focal length data
            e = dom.createElement("focal_length");
            e.appendChild(dom.createTextNode(focalLocal.getText()));
            rootElement.appendChild(e);

            // Create the resolution tag and write the resolution data
            e = dom.createElement("resolution");
            e.appendChild(dom.createTextNode(resolution.getText()));
            rootElement.appendChild(e);

            // Create the aspect ratio tag and write the aspect ratio data
            e = dom.createElement("aspect_ratio");
            e.appendChild(dom.createTextNode(aspectRatio.getText()));
            rootElement.appendChild(e);

            dom.appendChild(rootElement);

            // Set the transforms to make the XML document
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT,"yes");
            tr.setOutputProperty(OutputKeys.METHOD,"xml");
            tr.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","4");

            // Write the data to the file
            tr.transform(new DOMSource(dom),new StreamResult(
                    new FileOutputStream("src/cameras/"+name.getText()+".cam")));

        }catch (IOException | ParserConfigurationException | TransformerException ioe){
            // If error, show dialog box with the error message
            Dialog.showDialog("Unable to write to write camera settings.\n\n" +
                    "Ensure that " + path + " is visible by the application.","Unable to write settings");
        }
    }
}
