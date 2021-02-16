package DiskArmSchedule;
import java.util.*;
import java.awt.*;
//this class stores and respresents the disk state

class FileManager{
    private DiskState diskState;
    private DiskFrame frame;
    public FileManager(DiskFrame frame, DiskState ds){
        this.frame = frame;
        diskState = ds;
    }

    public synchronized boolean writeFile(DiskFile file){
        if(canWrite(file)){
            diskState.writeFile(file);
            return true;
        }
        return false;
    }

    public synchronized boolean readFile(DiskFile file){
        if(canRead(file)){
            diskState.readFile(file);
            return true;
        }
        return false;
    }

    public synchronized boolean deleteFile(DiskFile file){
        if(file != null && canRead(file)){
            diskState.deleteFile(file);
            return true;
        }
        return false;
    }

    public synchronized ArrayList getAllFiles(){
        return diskState.getFiles();
    }

    public synchronized ArrayList getEmptySpaces(){
        return diskState.getEmptySpaces();
    }
    private synchronized boolean canWrite(DiskFile file){
        ArrayList files = diskState.getFiles();
        Iterator e = files.iterator();
        DiskFile each;
        while(e.hasNext()){
            each = (DiskFile)e.next();
            if(each.hasConflict(file))
                return false;
        }
        return true;
    }
    
    private synchronized boolean canRead(DiskFile file){
        ArrayList files = diskState.getFiles();
        Iterator e = files.iterator();
        DiskFile each;
        while(e.hasNext()){
            each = (DiskFile)e.next();
            if(each.getTrack() == file.getTrack() && each.getSector() == file.getSector() &&
                each.getLen() == file.getLen())
                return true;
        }
        return false;
    }
}

class DiskFile{
    private int length;
    private int sector;
    private int track;
    private int end;
    private int start;
    private int secSize;

    public DiskFile(int len, int sec, int trck, int secSize){
        length = len;
        sector = sec;
        track = trck;
        this.secSize = secSize;
        start = track * secSize + sec;
        end = start + len;
    }
    public int getLen(){
        return length;
    }
    public int getSector(){
        return sector;
    }
    public int getTrack(){
        return track;
    }
    
    public String toString(){
        return Integer.toString(track)+ " " +Integer.toString(sector)+ " " +Integer.toString(length);
    }
    public boolean hasConflict(DiskFile file){
        int sec = file.getSector();
        int trck = file.getTrack();
        int len = file.getLen();
        int startfile = trck * secSize + sec
            , endfile = startfile + len;
        if(start <= startfile)
            return !( start + length < startfile+1 );
        else
            return !( startfile + len < start+1 );
    }
}

class DiskState{
    private boolean[][] diskCell;
    private Color colors[][], eColor;
    private int sectors, tracks;
    private int count = 1;
    private int rc = 35;
    private DiskFrame frame;
    private Random r = new Random();

    public DiskState(DiskFrame df, int tracks, int sectors, boolean[][] initCells, Color[][] c, Color eC){
        frame = df;
        diskCell = initCells;
        this.sectors = sectors;
        this.tracks = tracks;
        colors = c;
        eColor = eC;
    }
    public synchronized void writeFile(DiskFile file){
        int sec = file.getSector();
        int track = file.getTrack();
        int len = file.getLen();
        int i, written = 0;
        int red = r.nextInt(256), green = r.nextInt(256), blue = r.nextInt(256);
        while(written < len){
            for(i = sec ; written < len && i < sectors ; i++, written++){
                diskCell[track][i] = false;
                this.colors[track][i] = new Color(red, green, blue);
                frame.setStateMatrix(diskCell);
                frame.setArmState(track, i);
                frame.setCellColor(track, i, red, green, blue);
                try{
                    Thread.sleep(frame.getSpeed());
                }catch(InterruptedException e){}
            }
            track++;
            sec = 0;
        }
    }

    public synchronized void readFile(DiskFile file){
        int sec = file.getSector();
        int track = file.getTrack();
        int len = file.getLen();
        int i, written = 0;
        while(written < len){
            for(i = sec ; written < len && i < sectors ; i++, written++){
                frame.setArmState(track, i);
                try{
                    Thread.sleep(frame.getSpeed());
                }catch(InterruptedException e){}
            }
            track++;
            sec = 0;
        }
    }
    public synchronized void deleteFile(DiskFile file){
        int sec = file.getSector();
        int track = file.getTrack();
        int len = file.getLen();
        int i, written = 0;
        int red = -1;
        while(written < len){
            for(i = sec ; written < len && i < sectors ; i++, written++){
                    diskCell[track][i] = true;
                    this.colors[track][i] = eColor;
                    frame.setArmState(track, i);
                    frame.setCellColor(track, i, -1, 0, 0);
                    try{
                        Thread.sleep(frame.getSpeed());
                    }catch(InterruptedException e){}
            }
            track++;
            sec = 0;
        }
    }
    public synchronized ArrayList getFiles(){
        boolean state = false, fileState = true;
        int s=0, t=0;
        ArrayList files = new ArrayList();
        int linear = 0;
        int i;
        int sec=0, tr=0, len = 0;
        Color first = colors[0][0];
        while(linear<tracks*sectors){
            for(i = s ; i < sectors ; i++, linear++){
                if( colors[t][i].getRGB() != eColor.getRGB()){
                    if(!state){
                        state = true;
                        fileState = false;
                        len = 1;
                        sec = i;
                        tr = t;
                    }else if(colors[t][i].getRGB() == first.getRGB()){
                        len++;
                    }else{
                        state = false;
                        if(!fileState){
                            files.add(new DiskFile(len, sec, tr, sectors));
                            fileState = true;
                        }
                    }
                }else if(!state && !fileState){
                    state = false;
                    files.add(new DiskFile(len, sec, tr, sectors));
                    fileState = true;
                }
                first = colors[t][i];
            }
            s = 0;
            t++;
        }
        files.add(new DiskFile(len, sec, tr, sectors));
        return files;
    }
    
    public boolean compare(Color s, Color t){
        return (s.getRed() == t.getRed() && s.getGreen() == t.getGreen() && s.getBlue() == t.getBlue());
    }
    public synchronized ArrayList getEmptySpaces(){
        boolean state = false, fileState = true;
        int s=0, t=0;
        ArrayList files = new ArrayList();
        int linear = 0;
        int i;
        int sec=0, tr=0, len = 0;
        while(linear<tracks*sectors){
            for(i = s ; i < sectors ; i++, linear++){
                if( diskCell[t][i] ){
                    if(!state){
                        state = true;
                        fileState = false;
                        len = 1;
                        sec = i;
                        tr = t;
                    }else{
                        len++;
                    }
                }else{
                    state = false;
                    if(!fileState){
                        files.add(new DiskFile(len, sec, tr, sectors));
                        fileState = true;
                    }
                }
            }
            s = 0;
            t++;
        }
        return files;
    }
    public boolean isCellEmpty(int track, int sector){
        return !diskCell[track][sector];
    }
}
