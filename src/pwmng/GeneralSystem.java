package pwmng;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author fnorouz
 */
public class GeneralSystem {
    protected String name;
    protected String moType;
    protected ResourceAllocation rc;
    protected Scheduler schdler;
    protected int numberofIdleNode = 0, numberofNode; // idle is change in allocation function
    protected ArrayList<Integer> rackId= new ArrayList<Integer> ();
    protected ArrayList<BladeServer> ComputeNodeList;
    protected ArrayList<Integer> ComputeNodeIndex;
    protected BufferedReader bis = null;
    protected int SLAviolation;
    protected boolean sysIsDone=false;
    protected double pwr=0;
    protected AMGeneral AM;
    protected int accumolatedViolation = 0;
    protected int numberOfActiveServ=0;

    public void setMoType(String moType) {
        this.moType = moType;
    }
    
    protected void addComputeNodeToSys(BladeServer b) {
        b.restart();
        ComputeNodeList.add(b);
    }
     void readFromNode(Node node){
     };
     void calculatePwr(){
         for(int i=0;i<ComputeNodeList.size();i++)
         {
             pwr=pwr+ComputeNodeList.get(i).getPower();            
         }
         
     }
     void parseXmlConfig(String config) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(config));
            // normalize text representation
            doc.getDocumentElement().normalize();
            readFromNode(doc.getDocumentElement());
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getMoType() {
        return moType;
    }

}
