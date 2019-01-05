import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
public class Tracer extends Thread
{
    private View view;
    public static BufferedImage track;

    //private Detector left, right, trace;
    //private Engine engl, engr;
    public static final int width=100, ocular=40, telescope=40, length=0, xstart=790, ystart=520, detrad=8, tracerad=2, base=5, radius=4;
    public static final double dt=0.022, p=1, i=0.01, d=0.000001;
    private double angle;



    Tracer(View view, BufferedImage image)
    {
        this.view=view;
        this.view.repaint();
        track=image;
       // left=new Detector(new Point(xstart-ocular/2,  ystart-telescope), detrad);
       // right=new Detector(new Point(xstart+ocular/2, ystart-telescope), detrad);
       // trace=new Detector(new Point(xstart, ystart), tracerad);
        //engl=new Engine(new Point(xstart-width/2, ystart), base);
        //engr=new Engine(new Point(xstart+width/2, ystart), base);
        angle=3.14/2*3;
    }
    public static void main(String args[])
    {
        Tracer tracer;
        BufferedImage image;
        try
        {
            image=ImageIO.read(new File("resources/track.png"));
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return;
        }
        View view=new View(image);
        tracer=new Tracer(view, image);
        tracer.run();
    }
    public void run()
    {
//        double error, steer, lasterror=0, integral=0, derivative, skew, bias;
//        skew=Math.acos((telescope/Math.sqrt(((double)ocular/2)*((double)ocular/2)+((double)telescope)*((double)telescope))));
//        bias=Math.sqrt(((double)ocular/2)*((double)ocular/2)+((double)telescope)*((double)telescope));
//        while(true)
//        {
//            angle+=((double)engl.getangvel()-(double)engr.getangvel())*radius/width*dt;
//            trace.setcrd(trace.getx()+Math.cos(angle)*(engl.getangvel()+engr.getangvel())*dt*3.14*radius,trace.gety()+Math.sin(angle)*(engl.getangvel()+engr.getangvel())*dt*3.14*radius);
//            left.setcrd(trace.getx()+Math.cos(angle-skew)*bias, trace.gety()+Math.sin(angle-skew)*bias);
//            right.setcrd(trace.getx()+Math.cos(angle+skew)*bias, trace.gety()+Math.sin(angle+skew)*bias);
//            engl.setcrd(trace.getx()+Math.cos(angle-1.5708)*width/2, trace.gety()+Math.sin(angle-1.5708)*width/2);
//            engr.setcrd(trace.getx()+Math.cos(angle+1.5708)*width/2, trace.gety()+Math.sin(angle+1.5708)*width/2);
//            error=(right.detect()-left.detect());
//            derivative=(error-lasterror)/dt;
//            integral+=error*dt;
//            steer=error*p+derivative*d+integral*i;
//            engl.setangvel((int) (base-steer));
//            engr.setangvel((int)(base+steer));
//            lasterror=error;
//            view.load(trace.getcrd(), engl.getcrd(), engr.getcrd(), left.getcrd(), right.getcrd());
//            System.out.println(trace.detect());
//            view.repaint();
//            try {
//                sleep(20);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//
//        }
    }
    private class TestLoop implements Runnable{
        Robot robot;
        private boolean running = false;
        public TestLoop(Robot robot){
            this.robot = robot;
            running = true;
        }
        @Override
        public void run(){

            double angle = Math.PI *3.0/2.0;
            while(running){
                robot.steer();
                angle+= ((double)robot.getLeftEngine().getangvel() - (double)robot.getRightEngine().getangvel())*radius/width*dt;
               //ustawienie koordynatow srodka
                robot.getTrace().setcrd(robot.getTrace().getx()+Math.cos(angle)*(robot.getLeftEngine().getangvel()+robot.getRightEngine().getangvel())*dt*3.14*radius,robot.getTrace().gety()+Math.sin(angle)*(robot.getLeftEngine().getangvel()+robot.getRightEngine().getangvel())*dt*3.14*radius);
                robot.getLeftDetector().setcrd(robot.getTrace().getx()+Math.cos(angle-robot.getSkew())*robot.getBias(), robot.getTrace().gety()+Math.sin(angle-robot.getSkew())*robot.getBias());
                robot.getRightDetector().setcrd(robot.getTrace().getx()+Math.cos(angle+robot.getSkew())*robot.getBias(), robot.getTrace().gety()+Math.sin(angle+robot.getSkew())*robot.getBias());
                robot.getLeftEngine().setcrd(robot.getTrace().getx()+Math.cos(angle-1.5708)*width/2, robot.getTrace().gety()+Math.sin(angle-1.5708)*width/2);
                robot.getRightEngine().setcrd(robot.getTrace().getx()+Math.cos(angle+1.5708)*width/2, robot.getTrace().gety()+Math.sin(angle+1.5708)*width/2);
                try {
                    sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

    }


class View extends JPanel
{
    private Vector<Cell> list;
    private Image track;
    View(Image image)
    {
        track=image;
        JFrame frame;
        frame=new JFrame("whatevs");
        frame.setSize(2000,2000);
        frame.add(this);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setFocusable(true);
        list=new Vector<>();
    }
    public void load(Cell c, int i)
    {
        list.add(i, c);
    }
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(track, 0, 0, this);
        for (Cell c : list)
        {
            g.setColor(Color.RED);
            g.fillOval((int) c.getleft().getX() - Tracer.detrad, (int) c.getleft().getY() - Tracer.detrad, Tracer.detrad * 2, Tracer.detrad * 2);
            g.fillOval((int) c.getright().getX() - Tracer.detrad, (int) c.getright().getY() - Tracer.detrad, Tracer.detrad * 2, Tracer.detrad * 2);
            g.fillOval((int) c.gettrace().getX() - Tracer.tracerad, (int) c.gettrace().getY() - Tracer.tracerad, Tracer.tracerad * 2, Tracer.tracerad * 2);
            g.setColor(Color.GRAY);
            g.fillOval((int) c.getengl().getX() - 10, (int) c.getengl().getY() - 10, 20, 20);
            g.fillOval((int) c.getengr().getX() - 10, (int) c.getengr().getY() - 10, 20, 20);

        }
    }
}