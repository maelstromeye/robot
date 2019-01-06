public class RobotBrain {
    private double Kp,Ki,Kd;
    private final double mutationRate=0.2;
    public double getKp() {
        return Kp;
    }

    public void setKp(double kp) {
        Kp = kp;
    }

    public double getKi() {
        return Ki;
    }

    public void setKi(double ki) {
        Ki = ki;
    }

    public double getKd() {
        return Kd;
    }

    public void setKd(double kd) {
        Kd = kd;
    }

    public RobotBrain(){

        Kp = Math.random() * 10;
        Kd = Math.random() * 5;
        Ki = Math.random() * 2;

        //randomowe Kp, Ki, Kd
    }
    public void mutate(){
        double random = Math.random();
        if(random<mutationRate){
            Kp = Math.random()*10;

        }
        random = Math.random();
//        if(random<mutationRate){
//            Kd = Math.random()*5;
//        }
//        random = Math.random();
//        if(random<mutationRate){
//            Ki = Math.random()*2;
//        }

    }
}