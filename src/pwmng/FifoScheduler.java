/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pwmng;

import java.util.ArrayList;

/**
 *
 * @author fnorouz
 */
public class FifoScheduler extends Scheduler {
    
    @Override
    public Job nextJob(ArrayList<? extends Job> queue)
    {
        return queue.get(0);
    }
    
}
