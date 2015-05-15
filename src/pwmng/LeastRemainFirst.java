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
public class LeastRemainFirst extends Scheduler {
    
    @Override
    public Job nextJob(ArrayList<? extends Job> queue)
    {
        double rem=((BatchJob)queue.get(0)).reqTime;
        BatchJob jj=new BatchJob();
        int index=0;
        int minIndex=0;
        for(;index<queue.size();index++)
        {
            if(((BatchJob)queue.get(index)).reqTime <rem)
            {
                rem=((BatchJob)queue.get(index)).reqTime;
                minIndex=index;
            }
        }
        
        return queue.get(minIndex);
    }
    
}
