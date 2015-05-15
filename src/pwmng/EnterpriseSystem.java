package pwmng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.*;

public class EnterpriseSystem extends GeneralSystem {

    ArrayList<EnterpriseApp> applicationList;
    
    public List<String> getMoTypes(){
        final ArrayList<String> ret = new ArrayList<String>();
        for (EnterpriseApp enterpriseApp : applicationList) {
            ret.add(enterpriseApp.moType); 
        }
        return ret;
    }
    
    public EnterpriseSystem(String config, String moType) {
        ComputeNodeList = new ArrayList<BladeServer>();
        ComputeNodeIndex = new ArrayList<Integer>();
        applicationList = new ArrayList<EnterpriseApp>();
        this.moType = moType;
        rc = new MHR();
        parseXmlConfig(config);
        SLAviolation = 0;
        schdler = new FifoScheduler();
        AM = Main.amTopology.getAM(moType, new Object []{this});
        AM.Alive = true;
        rc.initialResourceAloc(this);
    }

    public boolean checkForViolation() {
        for (int i = 0; i < applicationList.size(); i++) {
            if (applicationList.get(i).SLAviolation > 0) {
                return true;
            }
        }
//        try {
//                   Main.MessageLog.write("RequForHeartBeat \t From " + name + "to all of its subAMs \n");
//                   Main.MessageLog.write("UpdtaeHeartBeat \t From apps in " + name + " systemto its system \n");
//                } catch (IOException ex) {
//                    Logger.getLogger(ComputeSystem.class.getName()).log(Level.SEVERE, null, ex);
//                }
        return false;
    }

    public boolean isThereFreeNodeforApp() {
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            if (ComputeNodeList.get(i).ready == -2) {
                return true;
            }
        }
        return false;
    }

    public int numberofAvailableNodetoAlocate() {
        int n = 0;
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            if (ComputeNodeList.get(i).ready == -2) {
                n++;
            }
        }
        return n;
    }
    //in every Run A cycle checks for new Arrival Application

    void checkForNewArrivalApplication() {
        for (int i = 0; i < applicationList.size(); i++) {
            if (applicationList.get(i).appStartTime == Main.localTime && Main.localTime>1 ) {
                applicationList.get(i).AM.Alive = true;
                rc.initialResourceAllocationApplication(this, i);

            }
        }
    }

    boolean runAcycle() throws IOException {
        checkForNewArrivalApplication();
       if (applicationList.size() > 0 & checkForViolation())//& Main.localTime%Main.epochSys==0)
       {
            AM.monitor();
//            AM.analysis(SLAviolation);
//            AM.planning();
//            AM.execution();
//            Main.mesg++;
//
        }
        int finishedBundle = 0;
        for (int i = 0; i < applicationList.size(); i++) {
            if (applicationList.get(i).appStartTime > Main.localTime) {
                continue;
            }
            //TODO: if each bundle needs some help should ask and here resourceallocation should run
            if (applicationList.get(i).runAcycle() == false) //return false if bundle set jobs are done, we need to re-resourcealocation
            {
                numberofIdleNode = applicationList.get(i).ComputeNodeList.size() + numberofIdleNode;
                System.out.println("Number of violation in " + applicationList.get(i).id + "th application=  " + applicationList.get(i).NumofViolation +" \t in the system \t"+name );
                //System.out.println("application "+i +"is destroyed and there are: "+(applicationList.size()-1)+"   left");
                applicationList.get(i).destroyApplication();
                applicationList.remove(i);
                finishedBundle++;
            }
        }
        if (finishedBundle > 0) {
            rc.resourceAloc(this); //Nothing for now!
        }
        if (applicationList.isEmpty()) {
            sysIsDone = true;     // all done!
            return true;
        } else {
            return false;
        }
    }

    @Override
    void readFromNode(Node node) {
        ComputeNodeList.clear();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ComputeNode")) {
                    numberofNode = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Rack")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(",");
                    for (int j = 0; j < split.length; j++) {
                        rackId.add(Integer.parseInt(split[j]));
                    }
                }

                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ResourceAllocationAlg"));
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Scheduler"));
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("EnterpriseApplication")) {
                    applicationList.add(new EnterpriseApp(childNodes.item(i), this));
                    applicationList.get(applicationList.size() - 1).parent = this;
                }
            }
        }
    }
}
