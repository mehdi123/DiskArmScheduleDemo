/*
 * mainFrame.java
 */

package DiskArmSchedule;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.geom.*;
import java.util.*;


public class DiskFrame extends JFrame {
  JPanel contentPane;
  BorderLayout borderLayout1 = new BorderLayout();
  DiskGraph dg = new DiskGraph(10, 16);
  JPanel legend = new JPanel();
  JPanel control = new JPanel();
  JPanel operation = new JPanel();
  JPanel instruct = new JPanel();
  JPanel queue = new JPanel(new GridLayout(10, 1));
  JPanel dd = new JPanel(new BorderLayout());
  
  JLabel[] instQueue = new JLabel[10];
  JLabel spacer = new JLabel("                                                              ");
  JTextField nowDoing = new JTextField(15);
  JButton start = new JButton("Start");
  JButton stop = new JButton("Stop");
  JButton reset = new JButton("Reset");
  JSlider speed = new JSlider(100, 1000, 400);


  ArrayList cmd = new ArrayList();
  DiskOperator operator;
  DiskFrame f;
  public boolean runFlag = false;
  
  /**Construct the frame*/
  public DiskFrame() {
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      jbInit();
      operator = new DiskOperator(this, 10, 10);
      f = this;
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception  {
    contentPane = (JPanel) this.getContentPane();
    contentPane.setLayout(borderLayout1);
    this.setSize(new Dimension(800, 600));
    this.setResizable(false);
    this.setTitle("Concurrent Project: Disk Arm Scheduling");

    int i;

    operation.setLayout(new BorderLayout());
    contentPane.add(BorderLayout.CENTER, operation);
    control.setBorder(new TitledBorder(new BevelBorder(BevelBorder.RAISED), "Control"));
    contentPane.add(BorderLayout.SOUTH, control);
    instruct.setBorder(new TitledBorder(new BevelBorder(BevelBorder.RAISED), "Instruction"));

    instruct.setLayout(new BorderLayout());
    instruct.add(BorderLayout.CENTER, queue);
    instruct.add(BorderLayout.SOUTH, spacer);
    instruct.add(BorderLayout.NORTH, nowDoing);
    operation.add(BorderLayout.EAST, instruct);
    operation.add(BorderLayout.CENTER, dd);
    queue.setBorder(new TitledBorder("Queue"));
    nowDoing.setEditable(false);
    nowDoing.setBorder(new TitledBorder("Now doing..."));

    for(i = 0 ; i < instQueue.length ; i++){
      instQueue[i] = new JLabel(Integer.toString(i+1)+":");
      queue.add(instQueue[i]);
    }
    dd.setBorder(new TitledBorder("Disk Demo"));
    dd.add(BorderLayout.CENTER, dg);
    dd.add(BorderLayout.EAST, legend);

    control.add(start);
    control.add(stop);
    control.add(reset);
    control.add(speed);
    start.setEnabled(true);
    stop.setEnabled(false);
    start.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            if(operator.isStarted){
                operator.resume();
                operator.resumeThem();
            }
            else{
                runFlag = false;
                operator = new DiskOperator(f, 10, 10);
                operator.start();
                operator.isStarted = false;
            }
            start.setEnabled(false);
            stop.setEnabled(true);
            reset.setEnabled(false);
        }
    });

    stop.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            runFlag = true;
            stop.setEnabled(false);
            start.setEnabled(true);
            reset.setEnabled(true);
        }
    });
    reset.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
            operator = new DiskOperator(f, 10, 10);
        }
    });

    speed.setPaintTicks(true);
    speed.setMajorTickSpacing(100);
    (speed.getAccessibleContext()).setAccessibleName("SliderDemo.majorticks");
    (speed.getAccessibleContext()).setAccessibleDescription("SliderDemo.majorticksdescription");
    speed.putClientProperty("JSlider.isFilled", Boolean.TRUE );
    speed.setBorder(new TitledBorder("Speed"));

  }
  /**Overridden so we can exit when window is closed*/
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      System.exit(0);
    }
  }

  public void writeCommands(ArrayList qu){
          int i;
          for(i = 0 ; i < qu.size() && i < 10 ; i++)
              instQueue[i].setText(Integer.toString(i+1)+": "+qu.get(i));
          for( ; i < 10 ; i++)
                instQueue[i].setText(Integer.toString(i+1)+":");
  }

  public void setCellColor(int t, int s, int red, int green, int blue){
      dg.setCellColor(t, s, red, green, blue);
      dg.repaint();
  }
  
  public void setArmState(int t, int s){
      dg.setArmPos(t, s);
      dg.repaint();
  }
  public void setStateMatrix(boolean[][] mat){
      dg.setMatrix(mat);
  }
  public int getSpeed(){
      return speed.getValue()/3;
  }
  
  public void setNowDoing(String state){
      nowDoing.setText(state);
  }
  static void print(String s){
    System.out.println(s);
  }

}

class DiskGraph extends JPanel{
  private float iHeight;
  private float iWidth;
  private final float angle = 22.5F;
  private Color eColor = new Color(252, 242, 190);
  private int gc = 188, bc= 193;
  private boolean[][] diskMatrix;

  private int tarm = 0, sarm = 0;
  private Color arm = Color.white;
  private Color[][] fcolor;


  public DiskGraph(int t, int s){
      fcolor = new Color[t][s];
      for(int i = 0 ; i < fcolor.length ; i++)
        for(int j = 0 ; j < fcolor[0].length ; j++)
            fcolor[i][j] = eColor;

  }
  public void setCellColor(int t, int s, int red, int green, int blue){
      if(red == -1)
        fcolor[t][s] = eColor;
      else
        fcolor[t][s] = new Color(red, green, blue);
  }
  public void setArmPos(int t, int s){
      tarm = t;
      sarm = s;
  }
  public void setMatrix(boolean [][]matrix){
      diskMatrix = matrix;
  }
  public void paint(Graphics g){
    Graphics2D g2 = (Graphics2D)g;
    float width = getSize().width;
    float height = getSize().height;
    if(width > height)
      width = height;
    else
      height = width;
    iHeight = height;
    iWidth = width;
    g2.clearRect(0, 0, (int)width, (int)height);
    g2.setColor(new Color(100, 120, 170));
    RoundRectangle2D.Float border = new RoundRectangle2D.Float(0F, 0F, width,
     height -5, 15F, 15F);
    g2.draw(border);
    g2.setColor(Color.pink);
    g2.setColor(Color.black);
    int i;
    g2.setColor(new Color(0, 0, 128));
    NumPlotter np = new NumPlotter(g2);
    Circle disk = new Circle(new Point((int)iWidth/2, (int)iHeight/2), (int)iWidth/2-20, g2, np);
    disk.draw();
    i = 0;
    for(float j = 0 ; j < 181 ; j += angle, i++)
       disk.drawDiameter(j, 20, i);
    for(i = 0 ; i < 10 ; i++){
      disk.draw((int)iWidth/2-20-i*20);
    }
    g2.setColor(new Color(128, 0, 0));
    for(i = 0 ; i < 10 ; i++){
      g2.drawString(Integer.toString(i+1), (int)iHeight/2+(int)iWidth/2-20-i*20-20+5, (int)iHeight/2);
    }
    if(diskMatrix != null)
        drawState(disk, (int)iWidth/2-20, 20);
    disk.draw();
    i = 0;
    for(float j = 0 ; j < 181 ; j += angle, i++)
       disk.drawDiameter(j, 20, i);
    for(i = 0 ; i < 10 ; i++){
      disk.draw((int)iWidth/2-20-i*20);
    }
    g2.setColor(new Color(128, 0, 0));
    for(i = 0 ; i < 10 ; i++){
      g2.drawString(Integer.toString(i+1), (int)iHeight/2+(int)iWidth/2-20-i*20-20+5, (int)iHeight/2);
    }
  }
    public void drawState(Circle disk, int rad, int cons){
        for(int i = 0 ; i < diskMatrix.length ; i++){
            for(int j = 0 ; j < diskMatrix[0].length ; j++){
                if(i == tarm && j == sarm){
                    disk.drawArc(rad-i*cons, j*angle, arm);
                }else{
                    if(diskMatrix[i][j]){
                        disk.drawArc(rad-i*cons, j*angle, eColor);
                    }
                    else{
                        disk.drawArc(rad-i*cons, j*angle, fcolor[i][j]);
                    }
                }
            }
        }
  }

}

class Circle{
  private Point cntr;
  private int radius;
  private Graphics2D screen;
  private NumPlotter nump;
  private Color filled = Color.white;//new Color(0, 255, 255);
  private Color arm = new Color(128, 0, 64);

  public Circle(Point center, int r, Graphics2D s, NumPlotter np){
    cntr = center;
    radius = r;
    screen = s;
    nump = np;
  }
  public void draw(){
    screen.drawArc(cntr.x-radius, cntr.y-radius, radius*2, radius*2, 0, 360);
  }
  public void draw(int rad){
    screen.drawArc(cntr.x-rad, cntr.y-rad, rad*2, rad*2, 0, 360);
  }
  public void drawDiameter(float angle, int extra, int which){
    int x, y;
    if(angle <= 90){
      x = (int)Math.abs(radius * Math.cos((Math.PI/180F) * angle));
      y = (int)Math.abs(radius * Math.sin((Math.PI/180F) * angle));
      screen.drawLine(cntr.x, cntr.y, x+radius+extra, radius-y+extra);
      screen.drawLine(cntr.x, cntr.y, radius-x+extra, radius+y+extra);
    }else{
      x = (int)Math.abs(radius * Math.sin((Math.PI/180F) * (angle-90F)));
      y = (int)Math.abs(radius * Math.cos((Math.PI/180F) * (angle-90F)));
      screen.drawLine(cntr.x, cntr.y, radius-x+extra, radius-y+extra);
      screen.drawLine(cntr.x, cntr.y, radius+x+extra, radius+y+extra);
    }
   }
  public void drawArc(int rad, float startAngle,Color c){
    Color tc = screen.getColor();
    screen.setColor(c);
    screen.fillArc(cntr.x-rad, cntr.y-rad, rad*2, rad*2, (int)startAngle, 23);
    screen.setColor(tc);
  }
}

class NumPlotter{
    Graphics2D s;
    public NumPlotter(Graphics2D screen){
        s = screen;
    }

    public void plotNumber(int number, int x, int y){
        s.drawString(Integer.toString(number), x, y);
    }
}
