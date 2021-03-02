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
    private static ArrayList<String> output = new ArrayList<>();
    static String name;
    static String weight;
    static String turnRad;
    static String maxIncline;
    static String battery;
    static String batteryCapacity;
    public static ArrayList<String> load(File file){
        output.clear();
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.normalize();

            if(file.getName().endsWith(".uav")){
                NodeList nodeList = doc.getElementsByTagName("uav");

                Node nNode = nodeList.item(0);
                if(nNode.getNodeType() ==  Node.ELEMENT_NODE){
                    Element element = (Element) nNode;
                    name = element.getElementsByTagName("name").item(0).getTextContent();
                    NodeList subNodes = nNode.getChildNodes();
                    for(int i =0;i<subNodes.getLength();i++) {
                        Node cNode = nodeList.item(i);
                        if(cNode.getNodeType() ==  Node.ELEMENT_NODE) {
                            Element subElement = (Element) cNode;
                            System.out.println(cNode);
                            if(cNode.getNodeName()=="weight") {
                                weight = subElement.getElementsByTagName("weight").item(0).getTextContent();
                            }
                            else if(cNode.getNodeName()=="weight") {
                                turnRad = subElement.getElementsByTagName("turn_rad").item(0).getTextContent();
                            }
                            else if(cNode.getNodeName()=="weight") {
                                maxIncline = subElement.getElementsByTagName("max_incline").item(0).getTextContent();
                            }
                            else if(cNode.getNodeName()=="weight") {
                                battery = subElement.getElementsByTagName("battery").item(0).getTextContent();
                            }
                            else if(cNode.getNodeName()=="weight") {
                                batteryCapacity = subElement.getElementsByTagName("battery_capacity").item(0).getTextContent();
                            }
                        }
                    }
                }
                output.add(name);
                output.add(weight);
                output.add(turnRad);
                output.add(maxIncline);
                output.add(battery);
                output.add(batteryCapacity);
                System.out.println(output);
            }
            else if(file.getName().endsWith(".cam")){
                NodeList nodeList = doc.getElementsByTagName("camera");
                String name;
                String weight;
                String turnRad;
                String maxIncline;
                String battery;
                String batteryCapacity;
                for(int i=0;i<nodeList.getLength();i++){
                    Node nNode = nodeList.item(i);
                    if(nNode.getNodeType() ==  Node.ELEMENT_NODE){
                        Element element = (Element) nNode;
                        name = element.getElementsByTagName("name").item(0).getTextContent();
                        weight = element.getElementsByTagName("weight").item(0).getTextContent();
                        turnRad = element.getElementsByTagName("turn rad").item(0).getTextContent();
                        maxIncline = element.getElementsByTagName("max incline").item(0).getTextContent();
                        battery = element.getElementsByTagName("battery").item(0).getTextContent();
                        batteryCapacity = element.getElementsByTagName("battery capacity").item(0).getTextContent();
                        output.add(name);
                        output.add(weight);
                        output.add(turnRad);
                        output.add(maxIncline);
                        output.add(battery);
                        output.add(batteryCapacity);
                    }
                }
            }
            else if(file.getName().endsWith(".fsettings")){
                NodeList nodeList = doc.getElementsByTagName("flight settings");
                String name;
                String weight;
                String turnRad;
                String maxIncline;
                String battery;
                String batteryCapacity;
                for(int i=0;i<nodeList.getLength();i++){
                    Node nNode = nodeList.item(i);
                    if(nNode.getNodeType() ==  Node.ELEMENT_NODE){
                        Element element = (Element) nNode;
                        name = element.getElementsByTagName("name").item(0).getTextContent();
                        weight = element.getElementsByTagName("weight").item(0).getTextContent();
                        turnRad = element.getElementsByTagName("turn rad").item(0).getTextContent();
                        maxIncline = element.getElementsByTagName("max incline").item(0).getTextContent();
                        battery = element.getElementsByTagName("battery").item(0).getTextContent();
                        batteryCapacity = element.getElementsByTagName("battery capacity").item(0).getTextContent();
                        output.add(name);
                        output.add(weight);
                        output.add(turnRad);
                        output.add(maxIncline);
                        output.add(battery);
                        output.add(batteryCapacity);
                    }
                }
            }
            else if(file.getName().endsWith(".fpproj")){
                NodeList nodeList = doc.getElementsByTagName("project");
                String name;
                String weight;
                String turnRad;
                String maxIncline;
                String battery;
                String batteryCapacity;
                for(int i=0;i<nodeList.getLength();i++){
                    Node nNode = nodeList.item(i);
                    if(nNode.getNodeType() ==  Node.ELEMENT_NODE){
                        Element element = (Element) nNode;
                        name = element.getElementsByTagName("name").item(0).getTextContent();
                        weight = element.getElementsByTagName("weight").item(0).getTextContent();
                        turnRad = element.getElementsByTagName("turn rad").item(0).getTextContent();
                        maxIncline = element.getElementsByTagName("max incline").item(0).getTextContent();
                        battery = element.getElementsByTagName("battery").item(0).getTextContent();
                        batteryCapacity = element.getElementsByTagName("battery capacity").item(0).getTextContent();
                        output.add(name);
                        output.add(weight);
                        output.add(turnRad);
                        output.add(maxIncline);
                        output.add(battery);
                        output.add(batteryCapacity);
                    }
                }
            }
            else{
                return null;
            }
        } catch (ParserConfigurationException | SAXException | IOException pce){
            return null;
        }
        return output;
    }
}
