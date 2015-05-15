package pwmng;
public class BatchJob extends Job{
        public double startTime, exitTime, deadline;
        public int isChangedThisTime=0;
        double reqTime, utilization;
        public double [] remain;
        int numOfNode;
        int[] listOfServer;
        void setRemainParam(double exp, double ut, int node, int deadln) {
            if (ut<1) utilization=1;
            else utilization = ut/100;
            numOfNode=node;
            listOfServer=new int [numOfNode];
            remain=new double [numOfNode];
            for(int i=0;i<numOfNode;i++)
                remain[i] = exp; 
            reqTime = exp;
            deadline=deadln;
        }
        public BatchJob(){
            startTime = 0;
            exitTime = 0;
            reqTime = 0;
            exitTime = 0;
            numOfNode=0;
            deadline=0;
        }
        boolean allDone()
        {
            int i;
            for(i=0;i<numOfNode;i++)
                if(remain[i]>0)
                    return false;
            return true;

        }
        void destroyJobOnAllNodes()
        {
            int i;
            exitTime = Main.localTime;
            double waitTime = (Main.localTime+1) - startTime;//- (int)(reqTime);
            //System.out.println("start= "+startTime+ "\tlocal= "+ Main.localTime+"\t waitTime="+waitTime+"\t ="+waitTime*numOfNode);
            if (waitTime <0)
                 System.out.println("taliiii BatchJob\t" + waitTime);
           DataCenter.theDataCenter.chassisSet.get(whichChasiss(listOfServer[0])).servers.get(whichServer(listOfServer[0])).respTime=waitTime+
                   DataCenter.theDataCenter.chassisSet.get(whichChasiss(listOfServer[0])).servers.get(whichServer(listOfServer[0])).respTime;
           for(i=0;i<numOfNode;i++)
           {
                DataCenter.theDataCenter.chassisSet.get(whichChasiss(listOfServer[i])).servers.get(whichServer(listOfServer[i])).blockedBatchList.remove(this);
           }
           //DataCenter.theDataCenter.chassisSet.get(whichChasiss(listOfServer[0])).servers.get(whichServer(listOfServer[0])).totalFinishedJob=
             //DataCenter.theDataCenter.chassisSet.get(whichChasiss(listOfServer[0])).servers.get(whichServer(listOfServer[0])).totalFinishedJob+numOfNode;
           return;
        }
        int whichServer(int i)
        {
            return i%DataCenter.theDataCenter.chassisSet.get(0).servers.size();
        }
        int whichChasiss (int i)
        {
            return i/DataCenter.theDataCenter.chassisSet.get(0).servers.size();
        }
        int getThisNodeIndex(int serverIndex)
        {
            int ki;
            for(ki=0;ki<remain.length;ki++)
                    if(listOfServer[ki]==serverIndex)
                    {
                        return ki;
                    }
            return -1;
        }

}