import org.aspectj.testing.Tester;

public class CastVsParen {
    public static void main(String[] args) {
	int N = 1;
	boolean T = true;

	int i = (N);
	i = ((N));
	
	i = (T) ? N : 2;
    }
}
