public class NonStmtInFor {
    void foo(int i, int j) {
	for (i = 0; i < 1; j / i, i++) {}
    }
    public static void main(String[] args) {
    }
}

