// modeled on jacks 16.2.7-final-4: A final variable
//        must be definitely unassigned if it
//        is to be assigned inside an if (false) block.
// if false is no protection for evildoers

public class UnderIfFalse {
    static int foo() { return 0; }
    static final int val = foo();
    public static void main(String[] args) {
	if (false) {
	    val = 1;
	}
    }
}
