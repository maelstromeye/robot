public class RobotBrain {
    private double Kp,Ki,Kd;

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

    public RobotBrain(double Kp, double Ki, double Kd){
        this.Kd =Kd;
        this.Ki = Ki;
        this.Kp = Kp;
    }
}
