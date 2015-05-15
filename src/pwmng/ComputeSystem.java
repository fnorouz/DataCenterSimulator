package pwmng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.*;

public class ComputeSystem extends GeneralSystem {

    violation SLAViolationType; //different type of violation: ComputeNodeShortage, DEADLINEPASSED
    ArrayList<BatchJob> waitingList;
    int totalJob = 0;//, totalFinishedJob=0;
    int minNode, maxNode;
    double inputTime;
    boolean blocked = false;
    File f;
    int predictNumberofNode;
    int priority;
    boolean bornAMFalg = false;

    ////////////////////////////////////
    public ComputeSystem(String config, String moType) {
        this.moType = moType;
        ComputeNodeList = new ArrayList<BladeServer>();
        waitingList = new ArrayList<BatchJob>();
        ComputeNodeIndex = new ArrayList<Integer>();
        parseXmlConfig(config);
        schdler = new LeastRemainFirst();
        //placement=new jobPlacement(ComputeNodeList);
        rc = new MHR();
        totalJob = 0;
        rc.initialResourceAloc(this);
        AM = Main.amTopology.getAM(moType, new Object []{this});
    }

    boolean runAcycle() {
        SLAviolation = 0;
        int numberOfFinishedJob = 0;
        // if(Main.localTime%1200==0 |Main.localTime%1200==2 )
        //         ASP();
        BatchJob j = new BatchJob();
        //reads all jobs with arrival time less than Localtime
        while (readJob(j)) {
            if (inputTime > Main.localTime) {
                break;
            }
            if (bornAMFalg == false) {
                try {
                    Main.MessageLog.writeInit("InitializeAM \t" + name + "\n");
                } catch (IOException ex) {
                    Logger.getLogger(ComputeSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
                bornAMFalg = true;
            }
            j = new BatchJob();
        }
        if (!blocked) {
            //feeds jobs from waiting list to servers as much as possible
            getFromWaitinglist();
            for (int temp = 0; temp < ComputeNodeList.size(); temp++) {
                ComputeNodeList.get(temp).run(new BatchJob());
            }
            for (int temp = 0; temp < ComputeNodeList.size(); temp++) {
                numberOfFinishedJob = ComputeNodeList.get(temp).totalFinishedJob + numberOfFinishedJob;
            }
            // System.out.println("total "+totalJob+ "\t finished Job= "+numberOfFinishedJob+"\t LocalTime="+Main.localTime);
        }
        //if is blocked and was not belocked before make it blocked
        if (blocked && !allNodesAreBlocked()) {
            makeSystemaBlocked();
        }

        if (!blocked) {
            AM.monitor();
            AM.analysis(0);
        }
        //System.out.println(Main.localTime +"\t"+totalJob+ "\t"+numberOfFinishedJob);
        if (numberOfFinishedJob == totalJob) {
            sysIsDone = true;
            try {
                Main.MessageLog.writeDestroy("DestroyMessage \t" + name + "\n");
            } catch (IOException ex) {
                Logger.getLogger(ComputeSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        } else {
            return false;
        }
    }
    ///returns true if all nodes are blocked

    boolean allNodesAreBlocked() {
        for (int temp = 0; temp < ComputeNodeList.size(); temp++) {
            if (ComputeNodeList.get(temp).ready != -1) {
                return false;
            }
        }
        return true;
    }

    void makeSystemaBlocked() {
        for (int temp = 0; temp < ComputeNodeList.size(); temp++) {
            ComputeNodeList.get(temp).backUpReady = ComputeNodeList.get(temp).ready;
            ComputeNodeList.get(temp).ready = -1;

        }
    }

    void makeSystemaUnBlocked() {
        for (int temp = 0; temp < ComputeNodeList.size(); temp++) {
            ComputeNodeList.get(temp).ready = ComputeNodeList.get(temp).backUpReady;
        }
    }

    int getFromWaitinglist() {
        setSLAviolation(violation.NOTHING);
        if (waitingList.isEmpty()) {
            return 0;
        }
        BatchJob jj = (BatchJob) (schdler.nextJob(waitingList));
        while (jj.startTime <= Main.localTime) {
            int[] indexes = new int[jj.numOfNode]; //number of node the last job wants
            int[] listServer = new int[jj.numOfNode];
            if (rc.nextServerSys(ComputeNodeList, indexes)[0] == -2) {
                setSLAviolation(violation.ComputeNodeShortage);
                //  System.out.println("COMPUTE NODE SHORTAGE in getFromWaitingList");
                return 0; //can not find the bunch of requested node  for the job
            }
            listServer = makeListofServer(indexes);
            for (int i = 0; i < indexes.length; i++) {

                jj.listOfServer = listServer;
                ComputeNodeList.get(indexes[i]).feedWork(jj);// feed also takes care of setting ready :)
                if (indexes.length > 1) {
                    ComputeNodeList.get(indexes[i]).dependency = 1; //means: this server has a process which is dependent on others
                } else {
                    ComputeNodeList.get(indexes[i]).dependency = 0;
                }
            }
            //Check if dealine is missed
            if (Main.localTime - jj.startTime > jj.deadline) {
                setSLAviolation(violation.DEADLINEPASSED);
                // System.out.println("DEADLINE PASSED in getFromWaitingList");
            }
            ////////////////////////////
            waitingList.remove(jj);
            if (waitingList.isEmpty()) {
                return 0;
            }
            jj = (BatchJob) (schdler.nextJob(waitingList));
        }
        return 0; //it is not important
    }

    int[] makeListofServer(int[] list) {
        int[] retList = new int[list.length];
        //int NumOfSerInChas=DataCenter.theDataCenter.chassisSet.get(0).servers.size();
        //map the index in CS compute node list to physical index(chassID , ServerID)
        for (int i = 0; i < list.length; i++) {
            retList[i] = ComputeNodeList.get(list[i]).serverID;//chassisID*NumOfSerInChas+ComputeNodeList.get(list[i]).serverID;
        }
        return retList;
    }

    void setSLAviolation(violation flag) {
        SLAViolationType = violation.NOTHING;
        if (flag == violation.ComputeNodeShortage)//means there is not enough compute nodes for job, this function is called from resourceIsAvailable
        {
            SLAViolationType = violation.ComputeNodeShortage;
            SLAviolation++;
        }
        if (flag == violation.DEADLINEPASSED) {
            SLAViolationType = violation.DEADLINEPASSED;
            SLAviolation++;
        }
        if (SLAViolationType != violation.NOTHING) {
            try {
                Main.SLALogH.write(name + "\t" + Main.localTime + "\t" + SLAViolationType + "\n");
            } catch (IOException ex) {
                Logger.getLogger(ComputeSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
            accumolatedViolation++;
        }
    }

    boolean readJob(BatchJob j) {
        try {
            String line = bis.readLine();
            if (line == null) {
                return false;
            }
            line = line.replace("\t", " ");
            String[] numbers = line.split(" ");
            if (numbers.length < 5) {
                return false;
            }
            // Input log format: (time, requiertime, CPU utilization, number of core, dealine for getting to a server buffer)
            inputTime = Double.parseDouble(numbers[0]);
            j.setRemainParam(Double.parseDouble(numbers[1]), Double.parseDouble(numbers[2]), Integer.parseInt(numbers[3]), Integer.parseInt(numbers[4]));
            j.startTime = inputTime;
            boolean add = waitingList.add(j);
            //number of jobs which are copied on # of requested nodes
            totalJob = totalJob + 1 /*+Integer.parseInt(numbers[3])*/;
            //System.out.println("Readed inputTime= " + inputTime + " Job Reqested Time=" + j.startTime+" Total job so far="+ total);
            return add;
        } catch (IOException ex) {
            System.out.println("readJOB EXC readJOB false ");
            Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    void readFromNode(Node node) {
        //if (ComputeNodeList.size()>0) ComputeNodeList.clear();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("moType")) {
                    moType = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ResourceAllocationAlg")) ;
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Scheduler")) ;
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Workload")) {
                    String fileName = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    try {
                        f = new File(fileName);
                        bis = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                    } catch (IOException e) {
                        System.out.println("Uh oh, got an IOException error!" + e.getMessage());
                    } finally {
                    }
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("ComputeNode")) {
                    numberofNode = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Priority")) {
                    priority = Integer.parseInt(childNodes.item(i).getChildNodes().item(0).getNodeValue().trim());
                }
                if (childNodes.item(i).getNodeName().equalsIgnoreCase("Rack")) {
                    String str = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                    String[] split = str.split(",");
                    for (int j = 0; j < split.length; j++) {
                        rackId.add(Integer.parseInt(split[j]));
                    }
                }
            }
        }
    }

    ArrayList getindexSet() {
        return ComputeNodeIndex;
    }

    int numberofRunningNode() {
        int cnt = 0;
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            if (ComputeNodeList.get(i).ready > -1) {
                cnt++;
            }
        }
        return cnt;
    }

    int numberofIdleNode() {
        int cnt = 0;
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            if (ComputeNodeList.get(i).ready == -1) {
                cnt++;
            }
        }
        return cnt;
    }

    void activeOneNode() {
        int i = 0;
        for (i = 0; i < ComputeNodeList.size(); i++) {
            if (ComputeNodeList.get(i).ready == -1) {
                ComputeNodeList.get(i).restart();
                ComputeNodeList.get(i).ready = 1;
                break;
            }
        }
        System.out.println("activeone node in compuet system MIIIIPPPSSS    " + ComputeNodeList.get(i).Mips);
    }

    double finalized() {
        try {
            bis.close();
        } catch (IOException ex) {
            Logger.getLogger(EnterpriseApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        double totalResponsetime = 0;
        for (int i = 0; i < ComputeNodeList.size(); i++) {
            totalResponsetime = totalResponsetime + ComputeNodeList.get(i).respTime;

        }
        return totalResponsetime;
    }
}
