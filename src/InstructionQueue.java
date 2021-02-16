/*
 * InstructionQueue.java
 *
 * Created on July 1, 2002, 4:07 PM
 */

package DiskArmSchedule;
import java.util.*;

class InstructionQueue {        

    private ArrayList queue;
    private int maxlen;
    private DiskFrame frame;
    private Semaphore s, emp;
    public InstructionQueue(DiskFrame f, int maxl) {
        maxlen = maxl;
        frame = f;
        queue = new ArrayList();
        s = new Semaphore(maxl);        
        emp = new Semaphore(1);
    }
    public Command get(){
        Command c = null;
        emp.P();
        synchronized(queue){
            if(queue.size() != 0){
                c = (Command)queue.remove(0);
                frame.writeCommands(queue);
                s.V();
            }
        }
        return c;
    }
    public void append(Command c){
        s.P();
        synchronized(queue){
            queue.add(c);
            frame.writeCommands(queue);
            emp.V();
        }
    }

    public int getLength(){
        return queue.size();
    }
    
    public void removeAll(){
        queue = new ArrayList();
    }
}
