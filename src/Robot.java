
import java.awt.*;

public class Robot {
    private double steering;
    private int center;
    private Detector leftDetector, rightDetector, trace;
    private Engine leftEngine, rightEngine;

    private double integral,derivative,lastError;

    private double accumulatedTrace = 0, maxTrace=0;
    private Point lastgood = new Point(Tracer.xstart, Tracer.ystart);
    private boolean isDead = false;

    private double max_U;

    private boolean  best = false;


    public RobotBrain getBrain(){
        return myBrain;
    }
    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    private double angle =Math.PI * 3.0 / 2.0;

    public double getFitness() {
        return fitness;
    }


    private double fitness = 0;

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
    public void calculateFitness(){
        fitness =  maxTrace/accumulatedTrace;
    }
    public void isBest(){
        best = true;
    }

    public void setTrace(Detector trace) {
        this.trace = trace;
    }

    public Robot giveBaby(){
        return new Robot(this.myBrain);
    }
    public boolean isFinished()
    {
        if(isDead){
            return true;
        }
        int max=100;
        if(Math.hypot(trace.getcrd().getY()-lastgood.getY(), trace.getx()-lastgood.getX())>=max) return true;
        return false;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        isDead = dead;
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
        if(leftDetector.getcrd().getY()+Tracer.detrad>2000 || leftDetector.getcrd().getY()-Tracer.detrad<0 || leftDetector.getcrd().getX()+Tracer.detrad>2000||leftDetector.getcrd().getX()-Tracer.detrad<0 ||
                rightDetector.getcrd().getY()+Tracer.detrad>2000 || rightDetector.getcrd().getY()-Tracer.detrad<0 || rightDetector.getcrd().getX()+Tracer.detrad>2000||rightDetector.getcrd().getX()-Tracer.detrad<0){
            isDead = true;
            return;

        }


        double error = rightDetector.detect() - leftDetector.detect();
        integral+=error*Tracer.dt;
        derivative=(error-lastError)/Tracer.dt;
        steering = myBrain.getKp()*error + myBrain.getKi()*integral + myBrain.getKd()*derivative;


        if(Math.abs(steering)>Tracer.base*4){
            if (steering>0){
                steering = Tracer.base*4;
            }else{
                steering = -Tracer.base*4;
            }
        }

        lastError = error;

        leftEngine.setangvel((Tracer.base-steering));
        rightEngine.setangvel((Tracer.base+steering));

        accumulatedTrace+=(100-leftDetector.track()+rightDetector.track())*(100-leftDetector.track()+rightDetector.track());
        maxTrace+=0;
        if(trace.detect()<=90.0) {
            lastgood = new Point(trace.getcrd());
        }


    }


}
class Engine
{
    private Point crd;
    private double angvel;
    private double direction;
    Engine(Point point, double i)
    {
        crd=point;
        direction=90;
        angvel=i;
    }
    public double getx(){return crd.getX();}
    public double gety(){return crd.getY();}
    public Point getcrd() {return crd;}
    public void setcrd(double x, double y){crd.setLocation(x,y);}
    public double getangvel(){return angvel;}
    public void setangvel(double i){angvel=i;}
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
    public double track()
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

        return 100-(r/count+g/count+b/count)/765*100+(((Math.random()*10)<=1)?(Math.random()*10):0);
    }
    public double getx(){return crd.getX();}
    public double gety(){return crd.getY();}
    public Point getcrd() {return crd;}
    public void setcrd(double x, double y){crd.setLocation(x,y);}
}