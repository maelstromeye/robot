import java.util.Random;

public class RobotBrain {
    private double Kp,Ki,Kd;
    private int[] digits = new int[12];
    private final double mutationRate=0.08;
    private final double[] bias = new double[]{0.2,0.4,0.8,1};
    public double getKp() {
        return Kp;
    }

    public int[] getDigits() {
        return digits;
    }

    public void setDigits(int[] digits) {
        this.digits = digits;
    }

    public double getKi() {
        return Ki;
    }


    public double getKd() {
        return Kd;
    }
    public void setPid(int[] digits){
        Kp = digits[0]*10 + digits[1] + digits[2]/10.0 + digits[3]/100.0;
        Ki = digits[4]*10 + digits[5] + digits[6]/10.0 + digits[7]/100.0;
        Kd = digits[8]*10 + digits[9] + digits[10]/10.0 + digits[11]/100.0;
        //print();
    }
    public void print(){
        System.out.println("Kp: " + Kp + ", Kd: " + Kd + ", Ki: " + Ki);
    }





    public RobotBrain(){
        Random random=new Random();
        for(int i=0;i<12;i++){
            digits[i] = random.nextInt(10);
        }
        setPid(digits);

    }

    private RobotBrain(int[] digits){
        this.digits = digits;
        setPid(digits);
        //print();
    }


    public static RobotBrain crossover(RobotBrain b1, RobotBrain b2){
//        System.out.print("gotten brains: ");
//        b1.print();
//        b2.print();
//        System.out.println();
        double random;
        double parentBias = 0.5;//Math.random();
        int[] digits1 = new int[12];
        int[] digits2 = new int[12];
        int[] p1 = b1.getDigits();
        int[] p2 = b2.getDigits();

        int x,y;

        for(int i=0;i<12;i++){
            random = Math.random();
            if(random<parentBias){
                x=p1[i];
                y=p2[i];
            }else{
              y=p1[i];
              x=p2[i];
            }

            digits1[i] = x;
            digits2[i] = y;
        }
        RobotBrain out1 = new RobotBrain(digits1);
        RobotBrain out2 = new RobotBrain(digits2);
        if(Math.random()<0.5) {
            return out1;
        }
        return out2;
    }
    public void mutate(){
        Random random = new Random();
        double rand;
        if(Math.random()<mutationRate){
            rand = Math.random();
            if(rand<bias[0]){
                digits[0] = random.nextInt(10);
            }
            if(rand<bias[1]){
                digits[1] = random.nextInt(10);
            }
            if(rand<bias[2]){
                digits[2] = random.nextInt(10);
            }
            if(rand<bias[3]){
                digits[3] = random.nextInt(10);
            }
        }
        if(Math.random()<mutationRate){
            rand = Math.random();
            if(rand<bias[0]){
                digits[4] = random.nextInt(10);
            }
            if(rand<bias[1]){
                digits[5] = random.nextInt(10);
            }
            if(rand<bias[2]){
                digits[6] = random.nextInt(10);
            }
            if(rand<bias[3]){
                digits[7] = random.nextInt(10);
            }

        }
        if(Math.random()<mutationRate){
            rand = Math.random();
            if(rand<bias[0]){
                digits[8] = random.nextInt(10);
            }
            if(rand<bias[1]){
                digits[9] = random.nextInt(10);
            }
            if(rand<bias[2]){
                digits[10] = random.nextInt(10);
            }
            if(rand<bias[3]){
                digits[11] = random.nextInt(10);
            }

        }
        setPid(digits);
    }
}