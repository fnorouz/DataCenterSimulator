package pwmng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import pwmng.amsetting.AmTopoligy;

public class Main {

    public static int localTime = 1;
    public static Map<String, Boolean> isAMNeeded = new HashMap<String, Boolean>();
    public static Map<String, Boolean> whichAMclass = new HashMap<String, Boolean>();
    public static int mesg = 0, mesg2 = 0;
    public static int epochApp = 60, epochSys = 120, epochSideApp = 120;
    public static ArrayList<responseTime> responseArray;
    public static ArrayList<InteractiveSystem> IS = new ArrayList<InteractiveSystem>();
    public static ArrayList<EnterpriseSystem> ES = new ArrayList<EnterpriseSystem>();
    public static ArrayList<ComputeSystem> CS = new ArrayList<ComputeSystem>();
    public static AmTopoligy amTopology;
    public static double[] peakEstimate;
    public static OutputStreamWriter SLALogE = null;
    public static OutputStreamWriter SLALogI = null;
    public static OutputStreamWriter SLALogH = null;
    public static CommunicationWriter MessageLog = null;
    public static int communicationAM = 0;

    public static void simulate() throws IOException {
        //GetStat();
        System.out.println("------------------------------------------");
        System.out.println("Systems start running");
        System.out.println("------------------------------------------");
        //Data Center is green!
//        DataCenter.theDataCenter.AM.strtg = strategyEnum.Green;
//        CS.get(0).AM.strtg = strategyEnum.Green;
//        CS.get(1).AM.strtg = strategyEnum.Green;
        ///////////////////////
        while (anySsyetm() == false) {

            // System.out.println("--"+Main.localTime);
            allSystemRunACycle();
            allSystemCalculatePwr();
            DataCenter.theDataCenter.getPower();
            localTime++;
            ////AM MAPE Loop
            if (Main.localTime % 1 == 0) {

                for (int i = 0; i < CS.size(); i++) {
                    mesg++;
                    mesg++;
                    try {
                        Main.MessageLog.write("RequestForHeartBeat \t" + CS.get(i).name + "\n");
                        Main.MessageLog.write("UpdateHeartbeat \t" + CS.get(i).name + "\n");
                    } catch (IOException ex) {
                        Logger.getLogger(ComputeSystem.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                //   DataCenter.AM.monitor();
                //  DataCenter.AM.analysis(0);
            }
            ///////////////
        }
        System.out.println("------------------------------------------");
        csFinalize();
        //System.out.println("Total JOBs= "+CS.totalJob);
        System.out.println("Total energy Consumption= " + DataCenter.theDataCenter.totalPowerConsumption);
        System.out.println("LocalTime= " + localTime);
        System.out.println("Mean Power Consumption= " + DataCenter.theDataCenter.totalPowerConsumption / localTime);
        System.out.println("Over RED\t " + DataCenter.theDataCenter.overRed + "\t# of Messages DC to sys= " + mesg + "\t# of Messages sys to nodes= " + mesg2);

        try {
            DataCenter.theDataCenter.shutDownDC();
            SLALogE.close();
            SLALogH.close();
            SLALogI.close();
            MessageLog.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static JTextArea comText, initText;

    public Main(JTextArea comText, JTextArea initText) {
        this.comText = comText;
        this.initText = initText;
    }

    public void createAmTopology() {
        Main.amTopology = new AmTopoligy();
        amTopology.parseXmlConfig("amtopology.xml");
    }

    public enum strategyEnum {

        Green, SLA
    };

    public static boolean anySysetm() {
        for (int i = 0; i < ES.size(); i++) {
            if (ES.get(i).sysIsDone == false) {
                return false;
            }
        }
        for (int i = 0; i < IS.size(); i++) {
            if (IS.get(i).sysIsDone == false) {
                return false;
            }
        }
        for (int i = 0; i < CS.size(); i++) {
            if (CS.get(i).sysIsDone == false) {
                return false;   //still we have work to do
            }
        }
        return true;  //there is no job left in all system
    }

    public static void CreatLogicalDC(String config) {
        try {
            SLALogE = new OutputStreamWriter(new FileOutputStream(new File("slaViolLogE.txt")));
            SLALogI = new OutputStreamWriter(new FileOutputStream(new File("slaViolLogI.txt")));
            SLALogH = new OutputStreamWriter(new FileOutputStream(new File("slaViolLogH.txt")));
            MessageLog = new CommunicationWriter(new FileOutputStream(new File("MessageLog.txt")), comText, initText, initText);
        } catch (IOException e) {
            System.out.println("Uh oh, got an IOException error!" + e.getMessage());
        } finally {
        }
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(config));
            // normalize text representation
            doc.getDocumentElement().normalize();
            Node node = doc.getDocumentElement();
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    if (childNodes.item(i).getNodeName().equalsIgnoreCase("layout")) {
                        String DCLayout = childNodes.item(i).getChildNodes().item(0).getNodeValue().trim();
                        DataCenter.theDataCenter = new DataCenter(DCLayout);
                    }
                    if (childNodes.item(i).getNodeName().equalsIgnoreCase("System")) {
                        NodeList nodiLst = childNodes.item(i).getChildNodes();
                        systemConfig(nodiLst);

                    }
                }
            }

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataCenter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void systemConfig(NodeList nodiLst) {
        int whichSystem = -1;
        // whichSystem=1 means Enterprise
        // whichSystem=2 means Interactive
        // whichSystem=3 means HPC
        String name = "";
        String moType = "";
        for (int i = 0; i < nodiLst.getLength(); i++) {
            if (nodiLst.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (nodiLst.item(i).getNodeName().equalsIgnoreCase("moType")) {
                    moType = nodiLst.item(i).getChildNodes().item(0).getNodeValue().trim();
                }

                if (nodiLst.item(i).getNodeName().equalsIgnoreCase("type")) {
                    String systemType = nodiLst.item(i).getChildNodes().item(0).getNodeValue().trim();
                    if (systemType.equalsIgnoreCase("Enterprise")) {
                        whichSystem = 1;
                    } else if (systemType.equalsIgnoreCase("Interactive")) {
                        whichSystem = 2;
                    } else if (systemType.equalsIgnoreCase("HPC")) {
                        whichSystem = 3;
                    }
                }
                if (nodiLst.item(i).getNodeName().equalsIgnoreCase("name")) {
                    name = nodiLst.item(i).getChildNodes().item(0).getNodeValue().trim();
                }
                if (nodiLst.item(i).getNodeName().equalsIgnoreCase("configFile")) {
                    String fileName = nodiLst.item(i).getChildNodes().item(0).getNodeValue().trim();
                    switch (whichSystem) {
                        case 1:
                            System.out.println("------------------------------------------");
                            System.out.println("Initialization of Enterprise System Name=" + name);
                            EnterpriseSystem ES1 = new EnterpriseSystem(fileName, moType);
                            try {
                                MessageLog.writeInit(ES1.AM, " EnterPrise System " + name, localTime);
                            } catch (IOException ex) {
                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            ES1.name = name;
                            ES.add(ES1);
//                            if(checkIfItNeedsAM(name)) 
//                            {
//                                String className=getAMClass();
//                                switch (className){
//                                    case "EntSysAM":
//                                        AMApplication newBornAM=new (ES1, null);
//                                        updatePeerLists("EntAppAM",newBornAM);
//                                        break;
//                                }
//                            }
                            whichSystem = -1;
                            break;
                        case 2:
                            System.out.println("------------------------------------------");
                            System.out.println("Initialization of Interactive System Name=" + name);
                            InteractiveSystem wb1 = new InteractiveSystem(fileName, moType);
                            wb1.name = name;
                            IS.add(wb1);
                            whichSystem = -1;
                            break;
                        case 3:
                            System.out.println("------------------------------------------");
                            System.out.println("Initialization of HPC System Name=" + name);
                            ComputeSystem CP = new ComputeSystem(fileName, moType);
                            CP.name = name;
                            CS.add(CP);
                            whichSystem = -1;
                            break;
                    }
                }
            }
        }
    }

    public static void allSystemRunACycle() throws IOException {
        for (int i = 0; i < ES.size(); i++) {
            if (ES.get(i).sysIsDone == false) {
                ES.get(i).runAcycle();
            }
        }
        for (int i = 0; i < CS.size(); i++) {
            if (CS.get(i).sysIsDone == false) {
                CS.get(i).runAcycle();
            }
        }
        for (int i = 0; i < IS.size(); i++) {
            if (IS.get(i).sysIsDone == false) {
                IS.get(i).runAcycle();
            }
        }
    }
    /////////////////////////////

    public static void allSystemCalculatePwr() throws IOException {
        for (int i = 0; i < ES.size(); i++) {
            ES.get(i).calculatePwr();
        }
        for (int i = 0; i < CS.size(); i++) {
            CS.get(i).calculatePwr();
        }
        for (int i = 0; i < IS.size(); i++) {
            IS.get(i).calculatePwr();
        }
    }
    /////////////////////////////

    public static void GetStat() {
        for (int i = 0; i < 50; i++) {
            DataCenter.theDataCenter.chassisSet.get(i).servers.get(0).ready = -1;
            DataCenter.theDataCenter.chassisSet.get(i).servers.get(0).Mips = 1;// 1.04 1.4;
            DataCenter.theDataCenter.chassisSet.get(i).servers.get(0).currentCPU = 100;
        }
        DataCenter.theDataCenter.getPower();
    }

    public static void formTopology() throws IOException {
        BufferedReader bis;
        try {
            File f = new File("topology.txt");
            bis = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            String s = bis.readLine();
            while (s != null) {
                String[] rec = s.split("\t");

                //Configure Peers for data center class

                //Configure Peers for HPC system class
                //Configure Peers for Enterprise system class
                if (rec[0].equals("EntSys")) {
                    if (rec[2].equals("EntSys")) {
                        for (int i = 0; i < ES.size(); i++) {
//                            ES.get(i).AM.peerList.add(rec[0])
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Uh oh, got an IOException error!" + e.getMessage());

        } finally {
        }

    }

    static void csFinalize() {
        for (int i = 0; i < CS.size(); i++) {
            System.out.println("Total Response Time in CS " + i + "th CS = " + CS.get(i).finalized());
        }
    }

    public static boolean anySsyetm() {
        boolean retValue = true;
        for (int i = 0; i < ES.size(); i++) {
            if (ES.get(i).sysIsDone == false) {
                retValue = false;
            } else {
                System.out.println("--------------------------------------");
                System.out.println("finishing Time EnterSys: " + ES.get(i).name + " at time: " + Main.localTime);
                System.out.println("Computing Power Consumed by  " + ES.get(i).name + " is: " + ES.get(i).pwr);
                //System.out.println("Number of violation: "+ES.get(i).accumolatedViolation);

                ES.remove(i);
                i--;
            }
        }
        for (int i = 0; i < IS.size(); i++) {
            if (IS.get(i).sysIsDone == false) {
                retValue = false;
            } else {
                System.out.println("--------------------------------------");
                System.out.println("finishing Time Interactive sys:  " + IS.get(i).name + " at time: " + Main.localTime);
                System.out.println("Interactive sys: Number of violation: " + IS.get(i).accumolatedViolation);
                System.out.println("Computing Power Consumed by  " + IS.get(i).name + " is: " + IS.get(i).pwr);
                IS.remove(i);
                i--;
                DataCenter.theDataCenter.AM.blockTimer = 0;
            }
        }
        for (int i = 0; i < CS.size(); i++) {
            if (CS.get(i).sysIsDone == false) {
                retValue = false;   //means still we have work to do
            } else {
                System.out.println("--------------------------------------");
                System.out.println("finishing Time HPC_Sys:  " + CS.get(i).name + " at time: " + Main.localTime);
                System.out.println("Total Response Time= " + CS.get(i).finalized());
                System.out.println("Number of violation HPC : " + CS.get(i).accumolatedViolation);
                System.out.println("Computing Power Consumed by  " + CS.get(i).name + " is: " + CS.get(i).pwr);
                CS.remove(i);
                i--;
            }
        }
        return retValue;  //there is no job left in all system
    }
//    static void getPeakEstimate()
//    {
//        File f;
//        peakEstimate=new  double[71];
//        BufferedReader bis = null;
//        try {
//               f = new File("Z:\\PWMNG\\peakEstimation3times.txt");
//               bis = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
//            } catch (IOException e) {
//                   System.out.println("Uh oh, got an IOException error!" + e.getMessage());
//             } finally {
//          }
//         try {
//                String line = bis.readLine();
//                int i=0;
//                while(line!=null)
//                {
//                    String[] numbers= new String[1];
//                    numbers = line.trim().split(" ");
//                    peakEstimate[i++] = Double.parseDouble(numbers[0]);
//                     //System.out.println("Readed inputTime= " + inputTime + " Job Reqested Time=" + j.startTime+" Total job so far="+ total);
//                    line = bis.readLine();
//                }
//             } catch (IOException ex) {
//                System.out.println("readJOB EXC readJOB false ");
//                Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
//             }
//        }
   /*static void coordinator(int times)
    {
    //return every server in ready state then try to make some of them idle
    for(int j=0;j<webSet1.ComputeNodeList.size();j++)
    {
    webSet1.ComputeNodeList.get(j).ready=1;
    webSet1.ComputeNodeList.get(j).currentCPU=0;
    webSet1.ComputeNodeList.get(j).Mips=1;
    }
    ///////////////////////////////////////////////////////////////////////
    int suc=0;
    if(times>=70) return;
    double peak=peakEstimate[times];
    int numberOfidleServer=webSet1.ComputeNodeList.size()-(int)Math.ceil(peak/1000);
    //System.out.println(numberOfidleServer);
    if(numberOfidleServer<0) return;
    for(int j=0;j<numberOfidleServer;j++)
    if(webSet1.ComputeNodeList.get(j).queueLength==0)
    {
    suc++;
    webSet1.ComputeNodeList.get(j).ready=-1;
    webSet1.ComputeNodeList.get(j).currentCPU=0;
    webSet1.ComputeNodeList.get(j).Mips=1;
    }
    else {System.out.println("In Coordinator and else   ");numberOfidleServer++;} //border ra jabeja mikonim
    //if(suc==numberOfidleServer) System.out.println(numberOfidleServer+"\t suc= "+suc);
    }
    public static void addToresponseArray(double num,int time)
    {
    responseTime t= new responseTime();
    t.numberOfJob=num;
    t.responseTime=time;
    responseArray.add(t);
    return;
    }*/
}
