public class LiteralsCf {
    public static void main(String[] args) {
        float f;
        double d;
        f = 3.4028235e+38f; //Why not error?
        f = 1.4023983e-45f; //Why not error?

        f = 1e39f; //ERR: rounds to +INF
        f = 0.0000000000000000000000000000000000000000000000001f; //ERR: rounds to 0
        f = -1234567890123456789012345678901234567890123f; //ERR: rounds to -INF
        d = -1e310; //ERR: rounds to -INF
        d = 1e500; //ERR: rounds to +INF

        int i, i1, i2, i3;
        long l, l1, l2, l3;

        i = 2147483648; //ERR: too big
        i = 0x1ffffffff; //ERR: too big
        i = 01234567012345670; //ERR: too big
        i2 = 0x800000000;
        i3 = 0200000000000;
        i2 = 0x100000000;
        i3 = 040000000000;

        i = -2147483649; //ERR: too small

        l = 9223372036854775808L; //ERR: too big
        l = -9223372036854775809L; //ERR: too small
        l2 = 0x80000000000000000L;
        l3 = 010000000000000000000000L;

        i = 09; //ERR: illegal octal

    }
}
