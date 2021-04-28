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

    public static ArrayList<String> parseProject(NodeList nodeList){
        ArrayList<String> projectSettings = new ArrayList<String>();
        for(int i=0;i<nodeList.getLength();i++) {
            Node node = nodeList.item(i);
            System.out.println(node.getNodeName());
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                System.out.println(node.getNodeName() + "," + node.getTextContent());
                /*if (node.getNodeName() == "name") {
                    name = node.getTextContent();
                } else if (node.getNodeName() == "weight") {
                    weight = node.getTextContent();
                } else if (node.getTextContent() == "turn_radius") {
                    turnRad = node.getTextContent();
                } else if () {

                } else if () {

                } else if () {

                } else {
                    System.out.println("");
                }*/
            }
        }
        return projectSettings;
    }

    public static Object parseFile(File file){
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.normalize();
            doc.getDocumentElement().normalize();
            Element root = doc.getDocumentElement();
            String rootName = root.getNodeName();
            NodeList nodeList = root.getChildNodes();
            if(rootName == "UAV"){
                System.out.println("Parsing UAVs");
                output = parseUAV(nodeList);
            }else if(rootName == "camera"){
                System.out.println("Parsing Camera");
                output = parseCam(nodeList);
            }else if(rootName == "flight_settings"){
                System.out.println("Parsing flight settings");
                output = parseSettings(nodeList);
            }else if(rootName == "project"){
                System.out.println("Parsing project");
                output = parseProject(nodeList);
            } else{
                System.out.println("Unknown tag");
            }
        }catch (IOException | SAXException | ParserConfigurationException ioe){

        }
        return output;
    }

}
