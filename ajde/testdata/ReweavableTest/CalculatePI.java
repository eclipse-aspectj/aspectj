import java.util.Random;

public class CalculatePI {
	
	static Random r = new Random();
	static double piApproximation = 1.0f;
	static int repetitions = 500000;
	static int iteration = 0;
	static double inSquare = 0;
	static double inCircle = 0;
		
	public static void main(String[] args) {
	  for (iteration = 0;iteration<repetitions;iteration++) approximate();
	  piApproximation = (inCircle/inSquare)*4.0f;
	  System.out.println("After "+repetitions+" iterations, pi is estimated to be "+piApproximation);
	}
	
	public static void approximate() {
		double x = r.nextDouble();
		double y = r.nextDouble();
		inSquare++;
		if (x*x + y*y < 1) {inCircle++;}
	}

	
}