/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pwmng;

import java.util.Iterator;

/**
 *
 * @author fnorouz
 */
public class array10 {
    class myItFor implements Iterator<Integer> {
        int i=-1;
        int [] dest ;
        public myItFor(int [] tt) {
            dest = tt;
        }
        
        public boolean hasNext() {
            return i <9;
        }

        public Integer next() {
            i++;
            return dest[i];
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
     
    int [] test= {1,2,3,4,5};
    Iterator<Integer> forward(){
        return new myItFor(test);
    }
    void testt(){
        Iterator<Integer> ll=forward();
        ll.hasNext();
    }
}
