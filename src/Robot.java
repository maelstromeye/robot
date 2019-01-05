import java.awt.*;

public class Robot {
    private double steering;
    private int center;
    private Detector leftDetector, rightDetector, trace;
    private Engine leftEngine, rightEngine;

    private double integral,derivative,lastError;

    public Detector getLeftDetector() {
        return leftDetector;
    }

    public void setLeftDetector(Detector leftDetector) {
        this.leftDetector = leftDetector;
    }

    public Detector getRightDetector() {
        return rightDetector;
    }

    public void setRightDetector(Detector rightDetector) {
        this.rightDetector = rightDetector;
    }

    public Engine getLeftEngine() {
        return leftEngine;
    }

    public void setLeftEngine(Engine leftEngine) {
        this.leftEngine = leftEngine;
    }

    public Engine getRightEngine() {
        return rightEngine;
    }

    public void setRightEngine(Engine rightEngine) {
        this.rightEngine = rightEngine;
    }

    public static double getSkew() {
        return skew;
    }

    public static double getBias() {
        return bias;
    }

    private static double skew = Math.acos((Tracer.telescope/Math.sqrt(((double) Tracer.ocular/2)*((double) Tracer.ocular/2)+((double) Tracer.telescope)*((double) Tracer.telescope))));
    private static double bias = Math.sqrt(((double) Tracer.ocular/2)*((double) Tracer.ocular/2)+((double) Tracer.telescope)*((double) Tracer.telescope));

    RobotBrain myBrain;

    public Detector getTrace() {
        return trace;
    }

    public void setTrace(Detector trace) {
        this.trace = trace;
    }

    public Robot(RobotBrain brain){
                myBrain = brain;
                trace = new Detector(new Point(Tracer.xstart, Tracer.ystart), Tracer.tracerad);
                leftDetector=new Detector(new Point(Tracer.xstart-Tracer.ocular/2,  Tracer.ystart-Tracer.telescope), Tracer.detrad);
                rightDetector=new Detector(new Point(Tracer.xstart+Tracer.ocular/2, Tracer.ystart-Tracer.telescope), Tracer.detrad);
                leftEngine=new Engine(new Point(Tracer.xstart-Tracer.width/2, Tracer.ystart), Tracer.base);
                rightEngine=new Engine(new Point(Tracer.xstart+Tracer.width/2, Tracer.ystart), Tracer.base);

                integral = 0;
                derivative = 0;
                lastError = 0;
            }

    public void steer() {
                double error = rightDetector.detect() - leftDetector.detect();
                integral+=error*Tracer.dt;
                derivative=(error-lastError)/Tracer.dt;
                steering = myBrain.getKd()*error + myBrain.getKi()*integral + myBrain.getKd()*derivative;


                if(Math.abs(steering)>Tracer.base*4){
                    if (steering>0){
                        steering = Tracer.base*4;
                    }else{
                        steering = -Tracer.base*4;
                    }
                }

                lastError = error;

                leftEngine.setangvel((int)(Tracer.base-steering));
                rightEngine.setangvel((int)(Tracer.base+steering));
    }


}
class Engine
{
    private Point crd;
    private int angvel;
    private double direction;
    Engine(Point point, int i)
    {
        crd=point;
        direction=90;
        angvel=i;
    }
    public double getx(){return crd.getX();}
    public double gety(){return crd.getY();}
    public Point getcrd() {return crd;}
    public void setcrd(double x, double y){crd.setLocation(x,y);}
    public int getangvel(){return angvel;}
    public void setangvel(int i){angvel=i;}
}
class Detector
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
                    color=new Color(Tracer.track.getRGB(x,y));
                    r+=color.getRed();
                    g+=color.getGreen();
                    b+=color.getBlue();
                    count++;
                }
            }
        }

        return (r/count+g/count+b/count)/765*100+(((Math.random()*10)<=1)?(Math.random()*10):0);
    }
    public double getx(){return crd.getX();}
    public double gety(){return crd.getY();}
    public Point getcrd() {return crd;}
    public void setcrd(double x, double y){crd.setLocation(x,y);}
}
