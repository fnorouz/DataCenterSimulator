/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pwmng.amsetting;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pwmng.AMGeneral;

/**
 *
 * @author fnorouz
 */
public class AMType {

    private String moType;
    private String amJavaClass;
    private List<AMGeneral> instances = new ArrayList<AMGeneral>();
    private List<AMType> subPrevillage = new ArrayList<AMType>();
    private List<AMType> pair = new ArrayList<AMType>();

    public List<AMGeneral> getInstances() {
        return instances;
    }

    

    public void setSubPrevillage(List<AMType> subPrevillage) {
        this.subPrevillage = subPrevillage;
    }

    public void setPair(List<AMType> pair) {
        this.pair = pair;
    }

    public String getAmJavaClass() {
        return amJavaClass;
    }

    public void setAmJavaClass(String amJavaClass) {
        this.amJavaClass = amJavaClass;
    }

    
    void readFromNode(Node node) {
        //if (ComputeNodeList.size()>0) ComputeNodeList.clear();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("MoType")) {
                    moType = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                } else if (childNodes.item(i).getNodeName().equalsIgnoreCase("AmJavaClass")) {
                    final Node item = childNodes.item(i).getChildNodes().item(0);
                    if (item != null) {
                        amJavaClass = item.getNodeValue().trim();
                    }

                }
            }
        }
    }

    public String getMoType() {
        return moType;
    }

    public void setMoType(String moType) {
        this.moType = moType;
    }

    void writeToNode(Document doc, Node node) {
        Element createElement = doc.createElement("MoType");
        node.appendChild(createElement);
        createElement.appendChild(doc.createTextNode(moType));
        createElement = doc.createElement("AmJavaClass");
        node.appendChild(createElement);
        createElement.appendChild(doc.createTextNode(amJavaClass == null ? "" : amJavaClass));
    }

    public List<AMType> getSubPrevillage() {
        return subPrevillage;
    }

    public List<AMType> getPair() {
        return pair;
    }
}
