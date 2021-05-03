package main;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {
    private static Object output;

    /**
     * Parse a UAV file
     * @param nodeList
     * @return
     */
    public static UAV parseUAV(NodeList nodeList){
        UAV uavSettings = new UAV();
        for(int i=0;i<nodeList.getLength();i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName() == "name") {
                    uavSettings.setName(node.getTextContent());
                } else if (node.getNodeName() == "weight") {
                    uavSettings.setWeight(node.getTextContent());
                } else if (node.getNodeName() == "turn_radius") {
                    uavSettings.setTurnRad(node.getTextContent());
                } else if (node.getNodeName() == "max_incline") {
                    uavSettings.setMaxIncline(node.getTextContent());
                } else if (node.getNodeName() == "battery") {
                    uavSettings.setBattery(node.getTextContent());
                } else if (node.getNodeName() == "battery_capacity") {
                    uavSettings.setCapacity(node.getTextContent());
                } else {
                    System.out.println("Unknown node");
                }
            }
        }
        return uavSettings;
    }

    /**
     * Parse a camera file
     * @param nodeList
     * @return
     */
    public static Camera parseCam(NodeList nodeList) {
        Camera camSettings = new Camera();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName() == "name") {
                    camSettings.setName(node.getTextContent());
                } else if (node.getNodeName() == "sensor_x") {
                    camSettings.setSensorX(node.getTextContent());
                } else if (node.getNodeName() == "sensor_y") {
                    camSettings.setSensorY(node.getTextContent());
                } else if (node.getNodeName() == "focal_length") {
                    camSettings.setFocalLength(node.getTextContent());
                } else if (node.getNodeName() == "resolution") {
                    camSettings.setResolution(node.getTextContent());
                } else if (node.getNodeName() == "aspect_ratio") {
                    camSettings.setAspectRatio(node.getTextContent());
                } else {
                    System.out.println("Unknown node");
                }
            }
        }
        return camSettings;
    }

    /**
     * Parse a settings file
     * @param nodeList
     * @return
     */
    public static Settings parseSettings(NodeList nodeList){
        Settings flightSettings = new Settings();
        for(int i=0;i<nodeList.getLength();i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName() == "uav_speed") {
                    flightSettings.setUavSpeed(node.getTextContent());
                } else if (node.getNodeName() == "wind_speed") {
                    flightSettings.setWindSpeed(node.getTextContent());
                } else if (node.getNodeName() == "wind_direction") {
                    flightSettings.setWindDirection(node.getTextContent());
                } else if (node.getNodeName() == "side_overlap") {
                    flightSettings.setSideOverlap(node.getTextContent());
                } else if (node.getNodeName() == "gsd") {
                    flightSettings.setGsd(node.getTextContent());
                } else if (node.getNodeName() == "altitude") {
                    flightSettings.setAltitude(node.getTextContent());
                } else {
                    System.out.println("Unknown node");
                }
            }
        }
        return flightSettings;
    }

    /**
     * Parse a file
     * @param file
     * @return
     */
    public static Object parseFile(File file){
        try{
            // Create an XML document parser
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.normalize();
            doc.getDocumentElement().normalize();
            Element root = doc.getDocumentElement();
            String rootName = root.getNodeName();       // Get name of the root
            NodeList nodeList = root.getChildNodes();   // Get list of nodes

            if(rootName == "UAV"){                      // If the root name matches from
                output = parseUAV(nodeList);
            }else if(rootName == "camera"){
                output = parseCam(nodeList);
            }else if(rootName == "flight_settings"){
                output = parseSettings(nodeList);
            } else{
                System.out.println("Unknown tag");
            }
        }catch (IOException | SAXException | ParserConfigurationException ioe){
            ioe.printStackTrace();
        }
        return output;
    }

}
