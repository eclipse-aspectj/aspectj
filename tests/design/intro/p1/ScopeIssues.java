package p1;

import org.aspectj.testing.Tester;

import java.math.*;

public aspect ScopeIssues {
    public static void main(String[] args) {
        Tester.checkEqual(C1.bi, BigInteger.ONE);

        Tester.checkEqual(Helper.bi, BigInteger.ONE);
    }

    private static BigInteger C1.bi = BigInteger.ONE;
    private static BigInteger Helper.bi = BigInteger.ONE;
}
