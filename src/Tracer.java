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
    private static BufferedImage track;
    private Detector left, right, trace;
    private Engine engl, engr;
    public static final boolean delivery;
    public static final int width, ocular, telescope, xstart, ystart, detrad, tracerad, base, radius, steerlimit;
    public static final double dt, p, i, d;
    static
    {
        delivery=false;
        if(delivery)
        {
            xstart=290;
            ystart=800;
        }
        else
        {
            xstart=1330;
            ystart=600;
        }
        width=100;
        ocular=40;
        telescope=40;
        detrad=8;
        tracerad=2;
        base=5;
        radius=4;
        steerlimit=base*4;
        dt=0.022;
        p=20;
        i=4;
        d=3.5;
    }
    private double angle;
    Tracer(View view, BufferedImage image)
    {
        this.view=view;
        this.view.repaint();
        track=image;
        left=new Detector(new Point(xstart-ocular/2,  ystart-telescope), detrad);
        right=new Detector(new Point(xstart+ocular/2, ystart-telescope), detrad);
        trace=new Detector(new Point(xstart, ystart), tracerad);
        engl=new Engine(new Point(xstart-width/2, ystart), base);
        engr=new Engine(new Point(xstart+width/2, ystart), base);
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
        double error, steer, lasterror=0, integral=0, derivative, skew, bias;
        skew=Math.acos((telescope/Math.sqrt(((double)ocular/2)*((double)ocular/2)+((double)telescope)*((double)telescope))));
        bias=Math.sqrt(((double)ocular/2)*((double)ocular/2)+((double)telescope)*((double)telescope));
        while(true)
        {
            angle+=((double)engl.getangvel()-(double)engr.getangvel())*radius/width*dt;
            trace.setcrd(trace.getx()+Math.cos(angle)*(engl.getangvel()+engr.getangvel())*dt*3.14*radius,trace.gety()+Math.sin(angle)*(engl.getangvel()+engr.getangvel())*dt*3.14*radius);
            left.setcrd(trace.getx()+Math.cos(angle-skew)*bias, trace.gety()+Math.sin(angle-skew)*bias);
            right.setcrd(trace.getx()+Math.cos(angle+skew)*bias, trace.gety()+Math.sin(angle+skew)*bias);
            engl.setcrd(trace.getx()+Math.cos(angle-1.5708)*width/2, trace.gety()+Math.sin(angle-1.5708)*width/2);
            engr.setcrd(trace.getx()+Math.cos(angle+1.5708)*width/2, trace.gety()+Math.sin(angle+1.5708)*width/2);
            error=(right.detect()-left.detect());
            derivative=(error-lasterror)/dt;
            integral+=error*dt;
            steer=error*p+derivative*d+integral*i;
            if(Math.abs(steer)>steerlimit)
            {
                if(steer>0) steer=steerlimit;
                else steer=-steerlimit;
            }
            engl.setangvel((int) (base-steer));
            engr.setangvel((int)(base+steer));
            lasterror=error;
            view.repaint();
            view.load(new Cell(trace.getcrd(), engl.getcrd(), engr.getcrd(), left.getcrd(), right.getcrd()), 0);
            System.out.println(error);
            try {
                sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private class Engine
    {
        private Point crd;
        private int angvel;
        Engine(Point point, int i)
        {
            crd=point;
            angvel=i;
        }
        public double getx(){return crd.getX();}
        public double gety(){return crd.getY();}
        public Point getcrd() {return crd;}
        public void setcrd(double x, double y){crd.setLocation(x,y);}
        public int getangvel(){return angvel;}
        public void setangvel(int i){angvel=i;}
    }
    private static class Detector
    {
        private Point crd;
        private int radius;
        Detector(Point point, int i)
        {
            crd=point;
            radius=i;
        }
        public double detect()
        {
            Color color;
            int count=0;
            double r=0,g=0,b=0;
            for (int x = (int)crd.getX()-radius; x < (int)crd.getX()+radius; x++)
            {
                for (int y = (int)crd.getY()-radius; y < (int)crd.getY()+radius; y++)
                {
                    if ((x - crd.getX())*(x - crd.getX()) + (y - crd.getY())*(y - crd.getY())<= radius*radius)
                    {
                        color=new Color(track.getRGB(x,y));
                        r+=color.getRed();
                        g+=color.getGreen();
                        b+=color.getBlue();
                        count++;
                    }
                }
            }
//            for (int i = (int)crd.getY()-radius; i < (int)crd.getY()+radius; i++)
//            {
//                for (int j = (int)crd.getX(); (j-(int)crd.getX())*(j-(int)crd.getX()) + (i-(int)crd.getY())*(i-(int)crd.getY()) <= radius*radius; j--)
//                {
//                    color=new Color(track.getRGB(i,j));
//                    r+=color.getRed();
//                    g+=color.getGreen();
//                    b+=color.getBlue();
//                    count++;
//                }
//                for (int j = (int)crd.getX()+1; (j-(int)crd.getX())*(j-(int)crd.getX()) + (i-(int)crd.getY())*(i-(int)crd.getY()) <= radius*radius; j++)
//                {
//                    color=new Color(track.getRGB(i,j));
//                    r+=color.getRed();
//                    g+=color.getGreen();
//                    b+=color.getBlue();
//                    count++;
//                }
//            }
            return (r/count+g/count+b/count)/765*100+(((Math.random()*10)<=1)?(Math.random()*10):0);
        }
        public double getx(){return crd.getX();}
        public double gety(){return crd.getY();}
        public Point getcrd() {return crd;}
        public void setcrd(double x, double y){crd.setLocation(x,y);}
    }
}
class Cell
{
    private Point engl, engr, left, right, trace;
    Cell(Point t, Point el, Point er, Point dl, Point dr)
    {
        trace=t;
        engl=el;
        engr=er;
        left=dl;
        right=dr;
    }
    public Point getengl(){return engl;}
    public Point getengr(){return engr;}
    public Point getleft(){return left;}
    public Point getright(){return right;}
    public Point gettrace(){return trace;}
}
class View extends JPanel
{
    private volatile Vector<Cell> list;
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
        if(i>=list.size()) list.add(c);
        else list.set(i,c);
    }
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(track, 0, 0, this);
        for (Cell aList : list)
        {
            g.setColor(Color.RED);
            g.fillOval((int) aList.getleft().getX() - Tracer.detrad, (int) aList.getleft().getY() - Tracer.detrad, Tracer.detrad * 2, Tracer.detrad * 2);
            g.fillOval((int) aList.getright().getX() - Tracer.detrad, (int) aList.getright().getY() - Tracer.detrad, Tracer.detrad * 2, Tracer.detrad * 2);
            g.fillOval((int) aList.gettrace().getX() - Tracer.tracerad, (int) aList.gettrace().getY() - Tracer.tracerad, Tracer.tracerad * 2, Tracer.tracerad * 2);
            g.setColor(Color.GRAY);
            g.fillOval((int) aList.getengl().getX() - 10, (int) aList.getengl().getY() - 10, 20, 20);
            g.fillOval((int) aList.getengr().getX() - 10, (int) aList.getengr().getY() - 10, 20, 20);
        }
    }
}