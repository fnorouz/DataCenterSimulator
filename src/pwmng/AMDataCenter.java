package pwmng;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AMDataCenter extends AMGeneral{  
    int []SoSCS;
    int []SoSIS;
    int []SoSES;
    int []SLAVioES;int []SLAVioIS;int []SLAVioCS;
    int blockTimer=0;
    Main.strategyEnum strtg;
    boolean SlowDownFromCooler=false;
    @Override
    public void monitor()
    {   
            if(blockTimer>0) 
                blockTimer--;
            DataCenter.AM.SoSCS=new int [Main.CS.size()];
            DataCenter.AM.SoSES=new int [Main.ES.size()];
            DataCenter.AM.SoSIS=new int [Main.IS.size()];
            DataCenter.AM.SLAVioCS=new int [Main.CS.size()];
            DataCenter.AM.SLAVioES=new int [Main.ES.size()];
            DataCenter.AM.SLAVioIS=new int [Main.IS.size()];
        for(int i=0;i<Main.CS.size();i++)
            SLAVioCS[i]=Main.CS.get(i).AM.SLAViolationGen;   
    }
    @Override
    public void analysis(Object vilation)
    {
       //if(strtg==Main.strategyEnum.Green)
                analysisGreen();
       // if(strtg==Main.strategyEnum.SLA)
         //   analysisSLA();
    }
    public void analysisGreen()
    {
        /* update all HPC system heartbeat    : is already done in Monitor     
           For all systems  inside the DC
           begin
            If (SLA is violated)
                Switch strategy to SLA based 
            If (SLA is not violated)
                Switch to green strategy
        end 
         */
        for (int i=0;i< SLAVioCS.length;i++)
        {    if(SLAVioCS[i]>0 && Main.CS.get(i).AM.strtg==Main.strategyEnum.Green)
                {
                    Main.CS.get(i).AM.strtg=Main.strategyEnum.SLA;
                    System.out.println("AM in DC Switch HPC system: "+ i+ " to SLA  @Time=  "+Main.localTime);
                    try {
                        Main.MessageLog.write("ChangePolicyProfile \t"+Main.CS.get(i).name +" to SLA\n");
                     } catch (IOException ex) {
                        Logger.getLogger(ComputeSystem.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
             if(SLAVioCS[i]==0 && Main.CS.get(i).AM.strtg==Main.strategyEnum.SLA)
             {
                 System.out.println("AM in DC Switch HPC system: "+ i+ "  to Green @Time=  "+Main.localTime);
                 Main.CS.get(i).AM.strtg=Main.strategyEnum.Green;
                 try {
                        Main.MessageLog.write("ChangePolicyProfile \t"+Main.CS.get(i).name +" to Green\n");
                     } catch (IOException ex) {
                        Logger.getLogger(ComputeSystem.class.getName()).log(Level.SEVERE, null, ex);
                    }
             }
        }
        /* if Slowdown from cooler        begin 
              block workload with lowest priority
              start a timer: “block timer”
        end
        if available nodes in system allocate one node to the SOS sender
         */
        if(blockTimer==0 && Main.CS.get(0).blocked) //time to unblock hpc system
        {
            Main.CS.get(0).blocked=false;
            Main.CS.get(0).makeSystemaUnBlocked();
            System.out.println("unblocked a system@ time : \t" +Main.localTime);
            try {
                        Main.MessageLog.write("UpdateConfig \t"+Main.CS.get(0).name +"\n");
                     } catch (IOException ex) {
                        Logger.getLogger(ComputeSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        if (SlowDownFromCooler)
        {
            if(!Main.CS.get(0).blocked) 
            {
                Main.CS.get(0).blocked=true;
                blockTimer=120;
                System.out.println("A system is blocked and we have this # "+Main.CS.size()+" of systems @ time= \t"+ Main.localTime);
                //Every system should work in Greeeen
                 try {
                        Main.MessageLog.write("UpdateConfig \t"+Main.CS.get(0).name +"\n");
                         Main.MessageLog.write("ChangePolicyProfile \t"+Main.CS.get(1).name +"\n");
                     } catch (IOException ex) {
                        Logger.getLogger(ComputeSystem.class.getName()).log(Level.SEVERE, null, ex);
             }
            
                Main.CS.get(1).AM.strtg=Main.strategyEnum.Green;
                Main.mesg++; //Extra message for ChangePolicyProfile
            }
            else 
                System.out.println("AM in data center level : HPC system is already blocked nothing can do here @: "+ Main.localTime);
        }
    }
    public void analysisSLA()
    {
    }
    @Override
    public void planning()
    {
        
    }
    @Override
    public void execution()
    {
        
    }
}
