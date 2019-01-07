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

    public static final int width = 100, ocular = 40, telescope = 40, length = 0, xstart =1150, ystart = 600, detrad = 8, tracerad = 2, radius = 30;

    public static final double dt = 0.022, p = 100, i = 0, d = 0 , base = 5.0/6.0;

    Population population;

    private volatile boolean running = false;

    Tracer(View view, BufferedImage image)
    {
        this.view = view;
        this.view.repaint();
        track = image;

    }

    public static void main(String args[])
    {
        Tracer tracer;
        BufferedImage image;
        try {
            image = ImageIO.read(new File("resources/track2.png"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        View view = new View(image);
        tracer = new Tracer(view, image);
        tracer.run();
    }

    public void run()
    {
        running = true;
        population = new Population(100);
        while(running){
            if(population.isFinished()){
                System.out.println("genetic");
                //algorytm genetyczny
                population.calculateFitness();
                population.naturalSelection();
                population.mutateBabies();
            }else{
                population.move();
                view.repaint();
            }
            try{
                Thread.sleep(10);
            }catch(InterruptedException e){
                e.printStackTrace();
            }

        }

    }
    private class Population{
        public ArrayList<Robot> population;
        Random random;
        int size;
        int generation =1;
        int bestRobot = 0;
        double allFitness = 0;

        Population(int size){
            this.size = size;
            population = new ArrayList<>(size);
            for(int i=0;i<size;i++){
                population.add(i,new Robot(new RobotBrain()));
            }
        }
        public boolean isFinished(){
            for(int i=0;i<size;i++){
                if(!population.get(i).isFinished()){
                    return false;
                }
            }
            return true;
        }
        public void calculateFitness(){
            for(int i=0;i<size;i++){
                population.get(i).calculateFitness();
            }

        }
        private void calculateFitenssSum(){
            double acc=0;
            for(int i=0;i<size;i++){
                acc+=population.get(i).getFitness();
            }
            allFitness = acc;

        }
        private Robot selectParent(){
            double random = Math.random() * allFitness;
            double runningSum =0;
            Robot robot;
            System.out.println(population.size());
            for(int i=0;i<size;i++){
                runningSum += population.get(i).getFitness();
                if(runningSum > random){
                    return population.get(i);
                }
            }
            robot = new Robot(new RobotBrain());
            return robot;

        }
        public void naturalSelection(){

            setBestRobot();
            calculateFitenssSum();
            ArrayList<Robot> newRobots = new ArrayList<>(size);
            Robot parent1,parent2,child;
            RobotBrain[] brains = new RobotBrain[2];
//            tempRobot = population.get(bestRobot).giveBaby();
//            newRobots.add(0,tempRobot);
//            tempRobot.isBest(); //najlepszy na inny kolor or sth
            for(int i=0;i<size/2;i++){
                parent1 = selectParent();
                parent2 = selectParent();
                brains = RobotBrain.crossover(parent1.getBrain(),parent2.getBrain());
                newRobots.add(new Robot(brains[0]));
                newRobots.add(new Robot(brains[1]));

//                tempRobot = selectParent();
//                tempRobot = tempRobot.giveBaby();
//                newRobots.add(i,tempRobot);
            }
            generation++;
            population = newRobots;
        }
        public void mutateBabies(){
            for(Robot r : population){
                r.myBrain.mutate();
            }
        }
        private void setBestRobot(){
            double max=0;
            int maxIndex=0;
            for(int i=0;i<size;i++){
                if(population.get(i).getFitness()>max){
                    max = population.get(i).getFitness();
                    maxIndex = i;
                }
                bestRobot = maxIndex;
            }

        }
        public void move(){
            Robot robot;
            double angle;
            for(int i=0;i<size;i++){

                robot = population.get(i);
                if(robot.isFinished()){
                    continue;
                }
                robot.steer();
                robot.setAngle(robot.getAngle() + ((double) robot.getLeftEngine().getangvel() - (double) robot.getRightEngine().getangvel()) * radius / width * dt);
                angle = robot.getAngle();
                robot.getTrace().setcrd(robot.getTrace().getx() + Math.cos(angle) * (robot.getLeftEngine().getangvel() + robot.getRightEngine().getangvel()) * dt * 3.14 * radius, robot.getTrace().gety() + Math.sin(angle) * (robot.getLeftEngine().getangvel() + robot.getRightEngine().getangvel()) * dt * 3.14 * radius);
                robot.getLeftDetector().setcrd(robot.getTrace().getx() + Math.cos(angle - robot.getSkew())*  robot.getBias(), robot.getTrace().gety() + Math.sin(angle - robot.getSkew()) * robot.getBias());
                robot.getRightDetector().setcrd(robot.getTrace().getx() + Math.cos(angle + robot.getSkew()) * robot.getBias(), robot.getTrace().gety() + Math.sin(angle + robot.getSkew()) * robot.getBias());
                robot.getLeftEngine().setcrd(robot.getTrace().getx() + Math.cos(angle - 1.5708) * width / 2, robot.getTrace().gety() + Math.sin(angle - 1.5708) * width / 2);
                robot.getRightEngine().setcrd(robot.getTrace().getx() + Math.cos(angle + 1.5708) * width / 2, robot.getTrace().gety() + Math.sin(angle + 1.5708) * width / 2);

                view.load(new Cell(robot.getTrace().getcrd(), robot.getLeftEngine().getcrd(), robot.getRightEngine().getcrd(), robot.getLeftDetector().getcrd(), robot.getRightDetector().getcrd()), i);


            }
        }
    }
}
class Cell {
    private Point engl, engr, left, right, trace;

    Cell(Point t, Point el, Point er, Point dl, Point dr) {
        trace = t;
        engl = el;
        engr = er;
        left = dl;
        right = dr;
    }

    public Point getengl() {
        return engl;
    }

    public Point getengr() {
        return engr;
    }

    public Point getleft() {
        return left;
    }

    public Point getright() {
        return right;
    }

    public Point gettrace() {
        return trace;
    }
}
class View extends JPanel {
    private Vector<Cell> list;
    private Image track;

    View(Image image) {
        track = image;
        JFrame frame;
        frame = new JFrame("whatevs");
        frame.setSize(2000, 2000);
        frame.add(this);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setFocusable(true);
        list = new Vector<>();
    }

    public void load(Cell c, int i) {
        if (i >= list.size()) list.add(c);
        else list.set(i, c);
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(track, 0, 0, this);
        for (Cell c : list) {
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