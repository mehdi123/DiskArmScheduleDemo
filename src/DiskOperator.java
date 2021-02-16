package DiskArmSchedule;
import java.util.*;
import java.awt.*;

class DiskOperator extends Thread{
    private int qLen, maxGenerator;
    private InstructionQueue queue;
    private FileManager fm;
    private DiskState ds;
    private InstructionGenerator[] generators;
    private String[] cmds = {"read", "delete", "write","write", "write"};
    private DiskFrame frame;
    public boolean isStarted = false;
    public DiskOperator(DiskFrame frame, int queueLen, int maxGen){
        qLen = queueLen;
        this.fm = fm;
        this.frame = frame;
        queue = new InstructionQueue(frame, qLen);
        maxGenerator = maxGen;
        Random r = new Random();
        int red = 0, green = 0, blue = 0;
        boolean [][]a = new boolean[10][16];
        Color[][] c = new Color[10][16];
        for(int i = 0 ; i < 10  ; i++){
            for(int j = 0; j < 16 ; j++){
                if(r.nextInt(4) == 0){
                    a[i][j] = true;
                    red = r.nextInt(256);
                    green = r.nextInt(256);
                    blue = r.nextInt(256);
                    c[i][j] = new Color(252, 242, 190);
                }
                else{
                    frame.setCellColor(i, j, red, green, blue);
                    a[i][j] = false;
                    c[i][j] = new Color(red, green, blue);
                }
            }
        }
        frame.setStateMatrix(a);
        ds = new DiskState(frame, 10, 16, a, c, new Color(252, 242, 190));
        fm = new FileManager(frame, ds);
        generators = new InstructionGenerator[maxGen];
        for(int i = 0 ; i < maxGenerator ; i++)
            generators[i] = new InstructionGenerator(queue, fm, cmds, frame);
    }

    public void run(){
        Command c;
        for(int i = 0 ; i < maxGenerator ; i++)
            generators[i].start();
        while(!frame.runFlag){
            c = queue.get();
            if(c != null){
                frame.setNowDoing(c.toString());
                if(c.cmd.compareTo("write") == 0){
                    fm.writeFile(c.file);
                }else if(c.cmd.compareTo("read") == 0){
                    fm.readFile(c.file);
                }else{
                    fm.deleteFile(c.file);
                }
            }
        }
    }
    public void stopIt(){
        queue.removeAll();
    }
    
    public void resumeThem(){
        for(int i = 0 ; i < generators.length ; i++){
            generators[i] = new InstructionGenerator(queue, fm, cmds, frame);
            generators[i].start();
        }
    }

    public boolean isCellEmpty(int track, int sector){
        return ds.isCellEmpty(track, sector);
    }
}

class InstructionGenerator extends Thread{
    private InstructionQueue inQueue;
    private boolean runFlag = false;
    private String[] commands;
    private FileManager fm;
    private DiskFrame frame;
    private Random rand = new Random();
    private String prevCmd = "";
    public InstructionGenerator(InstructionQueue iq, FileManager fm, String[] cmd, DiskFrame frame){
        inQueue = iq;
        commands = cmd;
        this.fm = fm;
        this.frame = frame;
    }

    public void run(){
        Command c;
        while(!frame.runFlag){
  //          try{
                c = nextInstruction();
                if(c != null){
                    inQueue.append(c);
                }
        }
    }
    public void setFlag(boolean f){
        runFlag = f;
    }
    public Command nextInstruction(){
        int icmd = rand.nextInt(10) % commands.length;
        String command = commands[icmd];
        while(command.compareTo(prevCmd)==0){
            icmd = rand.nextInt(10) % commands.length;
            command = commands[icmd];
        }
        ArrayList files = fm.getAllFiles();
        switch(icmd){
            case 0:
            case 1:
                files = fm.getAllFiles();
                break;
            case 2:
            case 3:
            case 4:
            case 5:
                files = fm.getEmptySpaces();
                break;
        }
        if(files.size() != 0){
            int ifile = rand.nextInt(files.size());
            prevCmd = command; 
            return new Command(command, (DiskFile)files.get(ifile));
        }else{
            return null;
        }
    }
}

class Command{
    String cmd;
    DiskFile file;
    public Command(String cmd, DiskFile f){
        this.cmd = cmd;
        file = f;
    }

    public String toString(){
        return new String(cmd + " " +Integer.toString(file.getTrack()) + " " +
            Integer.toString(file.getSector()) + " "+ Integer.toString(file.getLen()));
    }
}

class Semaphore{
   protected int value = 0;

   protected Semaphore() {value = 0;}

   protected Semaphore(int initial) {
      if (initial < 0) throw new IllegalArgumentException("initial<0");
      value = initial;
   }

   public synchronized void P() {
      value--;
      if (value < 0) {
         while (true) {     // we must be notified not interrupted
            try {
               wait();
               break;       // notify(), so P() succeeds
            } catch (InterruptedException e) {
             System.err.println("Semaphore.P(): InterruptedException, wait again");
               if (value >= 0) break; // race condition fix
               else continue;         // no V() yet
            }
         }
      }
   }

   public synchronized void V() { // this technique prevents
      value++;                    // barging since any caller of
      if (value <= 0) notify();   // P() will wait even if it
   }                              // enters before signaled thread

   public synchronized int value() {
      return value;
   }

   public synchronized String toString() {
      return String.valueOf(value);
   }
}
   
