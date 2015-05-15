package pwmng;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AMEnterpriseSys extends AMGeneral {
    EnterpriseSystem ES;
    static int kalmanIndex=0;
    double[] percentCompPwr;
    double [] queueLengthApps;
    public int [] allocationVector;
    int lastTime=0;
    int [] accuSLA;
    double wlkIntens=0;
    public AMEnterpriseSys(EnterpriseSystem ES) {
        //super(dtcenter);
       this.ES=ES;
       recForCoop=new int [ES.applicationList.size()];
    }
    @Override
    public void analysis(Object violation)
    {
        //averageWeight();
//       iterativeAlg();
//        utilityBasedPlanning();
        
    }
    void checkItsChildAMForProfilePolicChange(){
         for (int i = 0; i < peerListIamPriviged.size(); i++) 
         {
//             if(peerListIamPriviged.get(i).)
         }
    }
    @Override
    public void planning()
    {
        /////Server Provisioning for each application Bundle///////////
        if(Main.localTime%1200==0)
        {
//                numberOfActiveServ=0;
//                kalmanIndex=Main.localTime/1200;
//                serverProvisioning();
//                kalmanIndex++;
//                int i=ES.applicationList.get(0).occupiedPercentage();
                //System.out.println("occupied\t"+i);
//                if(i>50)
//                  ES.numberOfActiveServ=ES.applicationList.get(0).numberofRunningNode()+1;
//                else
//                  ES.numberOfActiveServ=ES.applicationList.get(0).numberofRunningNode()-1;
        }
        checkItsChildAMForProfilePolicChange();
        
   }
    @Override
    public void execution()
    {
          ES.rc.resourceProvision(ES,allocationVector);
    }
    void workloadIntensity(){
        double avg=0.0;
        final List<AMGeneral> myPeers = getMySubPrevillages();
        for (int i = 0; i < myPeers.size(); i++) {
            final EnterpriseApp app = ((AMApplication) myPeers.get(i)).app;
            avg=avg+(double)app.NumberofBasicNode/app.MaxNumberOfRequest;
        }
        wlkIntens=(double)avg/ES.applicationList.size();
    }
   
    @Override
    public void monitor() {
        final List<AMGeneral> myPeers = getMySubPrevillages();
        percentCompPwr= new double[myPeers.size()];
        allocationVector= new int [myPeers.size()];
        accuSLA= new int [myPeers.size()];
        queueLengthApps=new double [myPeers.size()];
        ES.SLAviolation = 0;
        workloadIntensity();
        for (int i = 0; i < myPeers.size(); i++) {
            final AMApplication AMapp = (AMApplication) myPeers.get(i);
            final EnterpriseApp app = ((AMApplication) AMapp).app;
            if(app.appStartTime> Main.localTime)
                continue;
            ES.SLAviolation =ES.SLAviolation+app.SLAviolation;
            // assume epoch system 2 time epoch application 
            percentCompPwr[i]= AMapp.percnt/((Main.localTime-lastTime)*3*app.ComputeNodeList.size());//(Main.epochSys*/*3*app.ComputeNodeList.size());
            ((AMApplication)(app.AM)).percnt=0;
            accuSLA[i]=AMapp.accumulativeSLA/(Main.localTime-lastTime);//Main.epochSys;
            AMapp.accumulativeSLA=0;
            //for fair allocate/release node needs to know how many jobs are already in each application queue
            queueLengthApps[i]=app.numberOfWaitingJobs();
        }
        SLAViolationGen=ES.SLAviolation;
        if (ES.SLAviolation > 0) {
            try {
                Main.SLALogE.write(ES.name+ "\t"+ Main.localTime + "\t" + ES.SLAviolation + "\n");
            } catch (IOException ex) {
                Logger.getLogger(AMEnterpriseSys.class.getName()).log(Level.SEVERE, null, ex);
            }
            ES.accumolatedViolation++;
        }
        calcSysUtility();
        lastTime=Main.localTime;
    }
    public void calcSysUtility(){
        final List<AMGeneral> myPeers = getMySubPrevillages();

        int localUtil=0, globalUtil;
        for (AMGeneral aMGeneral : myPeers) {
            final AMApplication appAm = (AMApplication) aMGeneral;
            localUtil +=  appAm.util;
        }

        
        localUtil=localUtil/myPeers.size();
        
//        if(ES.applicationList.isEmpty())
//        { super.utility=-1;
//            return;
//        }
//        localUtil=localUtil/ES.applicationList.size();
//        int idlePercent=100*ES.numberofIdleNode/ES.numberofNode;
//        int qos=ES.SLAviolation;
//        globalUtil=idlePercent+localUtil;
//        super.utility=sigmoidsig(globalUtil-100);        
    }
//    void iterativeAlg()
//    {
//        final List<AMGeneral> myPeers = getMySubPrevillages();
//        for(int i=0;i<ES.applicationList.size();i++)
//        {
//            if(app.appStartTime> Main.localTime)
//                continue;
//            app.AM.StrategyWsitch=Main.strategyEnum.Green; //Green Strategy
//            double wkIntensApp;
//            wkIntensApp = (double)app.NumberofBasicNode/app.MaxNumberOfRequest;
//            //if cpmPwr > 50% & violation then allocate a server
//            allocationVector[i]=0;
//            if(percentCompPwr[i]>0.5 && accuSLA[i]>0)
//            {
//
//                    //considering wl intensity of apps for node allocation
//                    //if app has more than average give it more node
//                    int bishtar=0;
//                    if(wkIntensApp>wlkIntens)
//                        bishtar=(int)Math.ceil(Math.abs((wkIntensApp-wlkIntens)/wlkIntens));
//                    else
//                        bishtar=0;
//                    allocationVector[i]=1+bishtar;//+(int)Math.abs((Math.floor((wlkIntens-wkIntensApp)/wlkIntens)));
//                    //System.out.println("Switching Strategy in Application   =" +i +" to SLA ");
//                    app.AM.StrategyWsitch=Main.strategyEnum.SLA;//SLA strategy
//            }
//            //if cpmPwr < 50% & violation is less then release a server
//            if(percentCompPwr[i]<= 0.5 && accuSLA[i]==0){
//                    allocationVector[i]=-1;
//                    System.out.println("Releasing a Server");
//            }
//            //if cpmPwr < 50% & violation is ziyad then nothing no server exchange
//            if(percentCompPwr[i]< 0.5 && accuSLA[i]>0)
//            {
//                allocationVector[i]=1;
//                //System.out.println("Switching Strategy in Application   =" +i +" to SLA ");
//                app.AM.StrategyWsitch=Main.strategyEnum.SLA;   //SLA strategy
//            }
//        }
//        int requestedNd=0;
//        for(int i=0; i< allocationVector.length;i++)
//        {
//            int valNode=app.ComputeNodeList.size()+allocationVector[i];
//            if(app.minProc> valNode
//                    ||app.maxProc< valNode){
////                if(app.minProc> app.ComputeNodeList.size()+allocationVector[i])
////                        System.out.println("error requested less than min in AM system ");
////                if(app.maxProc< app.ComputeNodeList.size()+allocationVector[i])
////                        System.out.println("error requested more than maxxxx in AM system ");
//                  allocationVector[i]=0;
//            }
//            requestedNd=requestedNd+allocationVector[i];
//        }
////        if(requestedNd>ES.numberofIdleNode)
////            System.out.println("IN AM system can not provide server reqested= "+requestedNd);
//    }
//    //determining aloc/release vector and active strategy
//    void averageWeight()
//    {
//       double [] cofficient= new double[ES.applicationList.size()];
//       int [] sugestForAlo= new int [ES.applicationList.size()];
//       double sumCoff=0;
//       //in each app calculate the expected Coefficient which is multiplication SLA violation and queue Length
//       for(int i=0;i<ES.applicationList.size();i++)
//       {
//           if(app.appStartTime> Main.localTime)
//                continue;
//           cofficient[i]=queueLengthApps[i]*accuSLA[i]+accuSLA[i]+queueLengthApps[i];
//           sumCoff=sumCoff+cofficient[i];
//       }
//       int totalNode=ES.ComputeNodeList.size();
//       for(int i=0;i<ES.applicationList.size();i++)
//       {
//           if(app.appStartTime> Main.localTime)
//                continue;
//           sugestForAlo[i]=(int)(cofficient[i]*totalNode/sumCoff);
//           if(sugestForAlo[i]<app.minProc)
//               sugestForAlo[i]=app.minProc;
//           if(sugestForAlo[i]>app.maxProc)
//               sugestForAlo[i]=app.maxProc;
//           allocationVector[i]=sugestForAlo[i]-app.ComputeNodeList.size();
//       }
//        for(int i=0;i<ES.applicationList.size();i++)
//        {
//            if(app.appStartTime> Main.localTime)
//                continue;
//            app.AM.StrategyWsitch=Main.strategyEnum.Green; //Green Strategy
//            if(accuSLA[i]>0)
//            {
//                    //System.out.println("Switching Strategy in Application   =" +i +" to SLA ");
//                    app.AM.StrategyWsitch=Main.strategyEnum.SLA;//SLA strategy
//            }
//        }
//    }
// void serverProvisioning() {
//        int[] numberOfPredictedReq = {251, 246, 229, 229, 223, 225, 231, 241, 265, 265, 271, 276, 273, 273, 268, 258, 255, 257, 242, 241, 233, 228, 231, 261, 274, 302, 343, 375, 404, 405, 469, 562, 1188, 1806, 2150, 2499, 2624, 2793, 2236, 1905, 1706, 1558, 1495, 1448, 1414, 1391, 1430, 1731, 2027, 2170, 2187, 2224, 2363, 1317};
//        if (kalmanIndex >= numberOfPredictedReq.length) {
//            return;
//        }
//        ES.numberOfActiveServ = (int) Math.floor(numberOfPredictedReq[kalmanIndex]*5*ES.applicationList.get(0).NumberofBasicNode/ ES.applicationList.get(0).MaxNumberOfRequest);
//        if (ES.numberOfActiveServ > ES.numberofNode) {
//            System.out.println("In ES : is gonna alocate this number of servers: "+(ES.numberOfActiveServ-ES.numberofNode));
//        }
//    }
// double sigmoid(double i){
//     return (1/(1+Math.exp(-i)));
// }
//void utilityBasedPlanning()
//    {
//        for(int i=0;i<ES.applicationList.size();i++)
//        {
//            app.AM.StrategyWsitch=Main.strategyEnum.Green; //Green Strategy
//            allocationVector[i]=0;
//            if(sigmoid(queueLengthApps[i])>0.5 && accuSLA[i]>0)
//            {
//                 app.AM.StrategyWsitch=Main.strategyEnum.SLA;//SLA strategy
//                 allocationVector[i]=1;
//                 //System.out.println("allocate system!!!!! ");
//            }
//            if(sigmoid(queueLengthApps[i])<0.5 && accuSLA[i]>0)
//            {
//                 app.AM.StrategyWsitch=Main.strategyEnum.SLA;//SLA strategy
//            }
//            if(sigmoid(queueLengthApps[i])<=0.5 && accuSLA[i]==0)
//            {
//                allocationVector[i]=-1;
//                //System.out.println("Resleasing in system!!!!! ");
//            }
//        }
//        int requestedNd=0;
//        for(int i=0; i< allocationVector.length;i++)
//        {
//            int valNode=app.ComputeNodeList.size()+allocationVector[i];
//            if(app.minProc> valNode
//                    ||app.maxProc< valNode){
////                if(app.minProc> app.ComputeNodeList.size()+allocationVector[i])
////                        System.out.println("error requested less than min in AM system ");
////                if(app.maxProc< app.ComputeNodeList.size()+allocationVector[i])
////                        System.out.println("error requested more than maxxxx in AM system ");
//                  allocationVector[i]=0;
//            }
//            requestedNd=requestedNd+allocationVector[i];
//        }
////        if(requestedNd>ES.numberofIdleNode)
////            System.out.println("IN AM system can not provide server reqested= "+requestedNd);
//    }
}