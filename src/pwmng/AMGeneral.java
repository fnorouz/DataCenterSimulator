package pwmng;

import java.util.ArrayList;
import java.util.List;
import pwmng.amsetting.AMType;

public class AMGeneral {
    double[] compPwrApps= new double[256];
    double[] SlaApps= new double[256];
    public int [] recForCoop;
    int SLAViolationGen;
    Main.strategyEnum strtg;
    boolean Alive;
    AMType amType;
    List<AMGeneral> peerListIamPriviged= new ArrayList<AMGeneral>();
    List<AMGeneral> nonPeerListPriviged= new ArrayList<AMGeneral>();
    public void monitor()
    {   
    }
    public void analysis(Object vilation)
    {
        
    }
    public void planning()
    {
    }
    public void execution()
    {
    }

    public AMType getAmType() {
        return amType;
    }

    public void setAmType(AMType amType) {
        this.amType = amType;
    }

    public List<AMGeneral> getMySubPrevillages(){
        List<AMGeneral> ret = new ArrayList<AMGeneral>();
        for (AMType aMType : amType.getSubPrevillage()) {
            ret.addAll(aMType.getInstances());
        }

        return ret;
    }
    
    public List<AMGeneral> getMyPeers(){
        List<AMGeneral> ret = new ArrayList<AMGeneral>();
        for (AMType aMType : amType.getPair()) {
            ret.addAll(aMType.getInstances());
        }

        return ret;
    }
}
