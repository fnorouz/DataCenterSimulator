package pwmng;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AMEnterpriseSys2 extends AMGeneral {

    EnterpriseSystem ES;
    static int kalmanIndex = 0;
    double[] percentCompPwr;
    double[] queueLengthApps;
    public int[] allocationVector;
    int lastTime = 0;
    int[] accuSLA;
    double wlkIntens = 0;

    public AMEnterpriseSys2(EnterpriseSystem ES) {
        //super(dtcenter);
        this.ES = ES;
        recForCoop = new int[ES.applicationList.size()];
    }

    @Override
    public void analysis(Object violation) {
    }

    void checkItsChildAMForProfilePolicChange() {
        final List<AMGeneral> myPeers = getMySubPrevillages();
          try {
                    Main.MessageLog.write("RequForHeartBeat \t From " + ES.name + "to all of its subAMs \n");
                    Main.MessageLog.write("UpdtaeHeartBeat \t From apps in " + ES.name + " systemto its system \n");
                } catch (IOException ex) {
                    Logger.getLogger(ComputeSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
        for (AMGeneral am : myPeers) {
            if (am.SLAViolationGen > 0) {
                try {
                    Main.MessageLog.write("ChangeProfilePolicy TO SLA based \t From " + ES.name + "system to app: \t"+ am.amType +"\n");
                } catch (IOException ex) {
                    Logger.getLogger(ComputeSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
                am.strtg = Main.strategyEnum.SLA;
            } else {
                  try {
                    Main.MessageLog.write("ChangeProfilePolicy TO Green \t From " + ES.name + "system to app: \t"+ am.amType +"\n");
                } catch (IOException ex) {
                    Logger.getLogger(ComputeSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
                am.strtg = Main.strategyEnum.Green;
            }
           am.SLAViolationGen = 0;
        }
    }

    @Override
    public void planning() {
        ///// Every 2 Miniutes check if any child AM has violation change its strategy
        if (Main.localTime % 120 == 0) {
            checkItsChildAMForProfilePolicChange();
        }

    }

    @Override
    public void execution() {
    }

    @Override
    public void monitor() {
    }
}
