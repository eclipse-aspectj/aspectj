import org.aspectj.testing.Tester;

public class OneArmedPrimitiveTests {
    static byte bn = -1; 
    static byte bz = 0; 
    static byte b1 = 1; 

    static short sn = -1; 
    static short sz = 0; 
    static short s1 = 1; 

    static int in = -1; 
    static int iz = 0; 
    static int i1 = 1; 

    static long ln = -1; 
    static long lz = 0; 
    static long l1 = 1; 

    static float fn = -1; 
    static float fz = 0; 
    static float f1 = 1; 
    static float fx = Float.NaN; 

    static double dn = -1; 
    static double dz = 0; 
    static double d1 = 1; 
    static double dx = Double.NaN; 

    static char cz = 0; 
    static char c1 = 1; 

    static boolean neg(boolean b) { return ! b; }
    static boolean pos(boolean b) { return b; }

    public static void main(String[] args) {
	doByte();
	doShort();
	doInt();
	doLong();
	doFloat();
	doDouble();
	doChar();
    }

    static void doByte() {
	Tester.check(neg(0 < bn),  "! 0 < bn");
	Tester.check(neg(0 <= bn), "! 0 <= bn");
	Tester.check(neg(0 == bn), "! 0 == bn");
	Tester.check(pos(0 != bn), "0 != bn");
	Tester.check(pos(0 > bn),  "0 > bn");
	Tester.check(pos(0 >= bn), "0 >= bn");

	Tester.check(pos(bn < 0),  "bn < 0");
	Tester.check(pos(bn <= 0), "bn <= 0");
	Tester.check(neg(bn == 0), "! bn == 0");
	Tester.check(pos(bn != 0), "bn != 0");
	Tester.check(neg(bn > 0),  "! bn > 0");
	Tester.check(neg(bn >= 0), "! bn >= 0");

	Tester.check(neg(0 < bz),  "! 0 < bz");
	Tester.check(pos(0 <= bz), "0 <= bz");
	Tester.check(pos(0 == bz), "0 == bz");
	Tester.check(neg(0 != bz), "! 0 != bz");
	Tester.check(neg(0 > bz),  "! 0 > bz");
	Tester.check(pos(0 >= bz), "0 >= bz");

	Tester.check(neg(bz < 0),  "! bz < 0");
	Tester.check(pos(bz <= 0), "bz <= 0");
	Tester.check(pos(bz == 0), "bz == 0");
	Tester.check(neg(bz != 0), "! bz != 0");
	Tester.check(neg(bz > 0),  "! bz > 0");
	Tester.check(pos(bz >= 0), "bz >= 0");

	Tester.check(pos(0 < b1),  "0 < b1");
	Tester.check(pos(0 <= b1), "0 <= b1");
	Tester.check(neg(0 == b1), "! 0 == b1");
	Tester.check(pos(0 != b1), "0 != b1");
	Tester.check(neg(0 > b1),  "! 0 > b1");
	Tester.check(neg(0 >= b1), "! 0 >= b1");

	Tester.check(neg(b1 < 0),  "! b1 < 0");
	Tester.check(neg(b1 <= 0), "! b1 <= 0");
	Tester.check(neg(b1 == 0), "! b1 == 0");
	Tester.check(pos(b1 != 0), "b1 != 0");
	Tester.check(pos(b1 > 0),  "b1 > 0");
	Tester.check(pos(b1 >= 0), "b1 >= 0");
    }

    static void doShort() {
	Tester.check(neg(0 < sn),  "! 0 < sn");
	Tester.check(neg(0 <= sn), "! 0 <= sn");
	Tester.check(neg(0 == sn), "! 0 == sn");
	Tester.check(pos(0 != sn), "0 != sn");
	Tester.check(pos(0 > sn),  "0 > sn");
	Tester.check(pos(0 >= sn), "0 >= sn");

	Tester.check(pos(sn < 0),  "sn < 0");
	Tester.check(pos(sn <= 0), "sn <= 0");
	Tester.check(neg(sn == 0), "! sn == 0");
	Tester.check(pos(sn != 0), "sn != 0");
	Tester.check(neg(sn > 0),  "! sn > 0");
	Tester.check(neg(sn >= 0), "! sn >= 0");

	Tester.check(neg(0 < sz),  "! 0 < sz");
	Tester.check(pos(0 <= sz), "0 <= sz");
	Tester.check(pos(0 == sz), "0 == sz");
	Tester.check(neg(0 != sz), "! 0 != sz");
	Tester.check(neg(0 > sz),  "! 0 > sz");
	Tester.check(pos(0 >= sz), "0 >= sz");

	Tester.check(neg(sz < 0),  "! sz < 0");
	Tester.check(pos(sz <= 0), "sz <= 0");
	Tester.check(pos(sz == 0), "sz == 0");
	Tester.check(neg(sz != 0), "! sz != 0");
	Tester.check(neg(sz > 0),  "! sz > 0");
	Tester.check(pos(sz >= 0), "sz >= 0");

	Tester.check(pos(0 < s1),  "0 < s1");
	Tester.check(pos(0 <= s1), "0 <= s1");
	Tester.check(neg(0 == s1), "! 0 == s1");
	Tester.check(pos(0 != s1), "0 != s1");
	Tester.check(neg(0 > s1),  "! 0 > s1");
	Tester.check(neg(0 >= s1), "! 0 >= s1");

	Tester.check(neg(s1 < 0),  "! s1 < 0");
	Tester.check(neg(s1 <= 0), "! s1 <= 0");
	Tester.check(neg(s1 == 0), "! s1 == 0");
	Tester.check(pos(s1 != 0), "s1 != 0");
	Tester.check(pos(s1 > 0),  "s1 > 0");
	Tester.check(pos(s1 >= 0), "s1 >= 0");
    }

    static void doInt() {
	Tester.check(neg(0 < in),  "! 0 < in");
	Tester.check(neg(0 <= in), "! 0 <= in");
	Tester.check(neg(0 == in), "! 0 == in");
	Tester.check(pos(0 != in), "0 != in");
	Tester.check(pos(0 > in),  "0 > in");
	Tester.check(pos(0 >= in), "0 >= in");

	Tester.check(pos(in < 0),  "in < 0");
	Tester.check(pos(in <= 0), "in <= 0");
	Tester.check(neg(in == 0), "! in == 0");
	Tester.check(pos(in != 0), "in != 0");
	Tester.check(neg(in > 0),  "! in > 0");
	Tester.check(neg(in >= 0), "! in >= 0");

	Tester.check(neg(0 < iz),  "! 0 < iz");
	Tester.check(pos(0 <= iz), "0 <= iz");
	Tester.check(pos(0 == iz), "0 == iz");
	Tester.check(neg(0 != iz), "! 0 != iz");
	Tester.check(neg(0 > iz),  "! 0 > iz");
	Tester.check(pos(0 >= iz), "0 >= iz");

	Tester.check(neg(iz < 0),  "! iz < 0");
	Tester.check(pos(iz <= 0), "iz <= 0");
	Tester.check(pos(iz == 0), "iz == 0");
	Tester.check(neg(iz != 0), "! iz != 0");
	Tester.check(neg(iz > 0),  "! iz > 0");
	Tester.check(pos(iz >= 0), "iz >= 0");

	Tester.check(pos(0 < i1),  "0 < i1");
	Tester.check(pos(0 <= i1), "0 <= i1");
	Tester.check(neg(0 == i1), "! 0 == i1");
	Tester.check(pos(0 != i1), "0 != i1");
	Tester.check(neg(0 > i1),  "! 0 > i1");
	Tester.check(neg(0 >= i1), "! 0 >= i1");

	Tester.check(neg(i1 < 0),  "! i1 < 0");
	Tester.check(neg(i1 <= 0), "! i1 <= 0");
	Tester.check(neg(i1 == 0), "! i1 == 0");
	Tester.check(pos(i1 != 0), "i1 != 0");
	Tester.check(pos(i1 > 0),  "i1 > 0");
	Tester.check(pos(i1 >= 0), "i1 >= 0");
    }

    static void doLong() {
	Tester.check(neg(0 < ln),  "! 0 < ln");
	Tester.check(neg(0 <= ln), "! 0 <= ln");
	Tester.check(neg(0 == ln), "! 0 == ln");
	Tester.check(pos(0 != ln), "0 != ln");
	Tester.check(pos(0 > ln),  "0 > ln");
	Tester.check(pos(0 >= ln), "0 >= ln");

	Tester.check(pos(ln < 0),  "ln < 0");
	Tester.check(pos(ln <= 0), "ln <= 0");
	Tester.check(neg(ln == 0), "! ln == 0");
	Tester.check(pos(ln != 0), "ln != 0");
	Tester.check(neg(ln > 0),  "! ln > 0");
	Tester.check(neg(ln >= 0), "! ln >= 0");

	Tester.check(neg(0 < lz),  "! 0 < lz");
	Tester.check(pos(0 <= lz), "0 <= lz");
	Tester.check(pos(0 == lz), "0 == lz");
	Tester.check(neg(0 != lz), "! 0 != lz");
	Tester.check(neg(0 > lz),  "! 0 > lz");
	Tester.check(pos(0 >= lz), "0 >= lz");

	Tester.check(neg(lz < 0),  "! lz < 0");
	Tester.check(pos(lz <= 0), "lz <= 0");
	Tester.check(pos(lz == 0), "lz == 0");
	Tester.check(neg(lz != 0), "! lz != 0");
	Tester.check(neg(lz > 0),  "! lz > 0");
	Tester.check(pos(lz >= 0), "lz >= 0");

	Tester.check(pos(0 < l1),  "0 < l1");
	Tester.check(pos(0 <= l1), "0 <= l1");
	Tester.check(neg(0 == l1), "! 0 == l1");
	Tester.check(pos(0 != l1), "0 != l1");
	Tester.check(neg(0 > l1),  "! 0 > l1");
	Tester.check(neg(0 >= l1), "! 0 >= l1");

	Tester.check(neg(l1 < 0),  "! l1 < 0");
	Tester.check(neg(l1 <= 0), "! l1 <= 0");
	Tester.check(neg(l1 == 0), "! l1 == 0");
	Tester.check(pos(l1 != 0), "l1 != 0");
	Tester.check(pos(l1 > 0),  "l1 > 0");
	Tester.check(pos(l1 >= 0), "l1 >= 0");
    }

    static void doFloat() {
	Tester.check(neg(0 < fn),  "! 0 < fn");
	Tester.check(neg(0 <= fn), "! 0 <= fn");
	Tester.check(neg(0 == fn), "! 0 == fn");
	Tester.check(pos(0 != fn), "0 != fn");
	Tester.check(pos(0 > fn),  "0 > fn");
	Tester.check(pos(0 >= fn), "0 >= fn");

	Tester.check(pos(fn < 0),  "fn < 0");
	Tester.check(pos(fn <= 0), "fn <= 0");
	Tester.check(neg(fn == 0), "! fn == 0");
	Tester.check(pos(fn != 0), "fn != 0");
	Tester.check(neg(fn > 0),  "! fn > 0");
	Tester.check(neg(fn >= 0), "! fn >= 0");

	Tester.check(neg(0 < fz),  "! 0 < fz");
	Tester.check(pos(0 <= fz), "0 <= fz");
	Tester.check(pos(0 == fz), "0 == fz");
	Tester.check(neg(0 != fz), "! 0 != fz");
	Tester.check(neg(0 > fz),  "! 0 > fz");
	Tester.check(pos(0 >= fz), "0 >= fz");

	Tester.check(neg(fz < 0),  "! fz < 0");
	Tester.check(pos(fz <= 0), "fz <= 0");
	Tester.check(pos(fz == 0), "fz == 0");
	Tester.check(neg(fz != 0), "! fz != 0");
	Tester.check(neg(fz > 0),  "! fz > 0");
	Tester.check(pos(fz >= 0), "fz >= 0");

	Tester.check(pos(0 < f1),  "0 < f1");
	Tester.check(pos(0 <= f1), "0 <= f1");
	Tester.check(neg(0 == f1), "! 0 == f1");
	Tester.check(pos(0 != f1), "0 != f1");
	Tester.check(neg(0 > f1),  "! 0 > f1");
	Tester.check(neg(0 >= f1), "! 0 >= f1");

	Tester.check(neg(f1 < 0),  "! f1 < 0");
	Tester.check(neg(f1 <= 0), "! f1 <= 0");
	Tester.check(neg(f1 == 0), "! f1 == 0");
	Tester.check(pos(f1 != 0), "f1 != 0");
	Tester.check(pos(f1 > 0),  "f1 > 0");
	Tester.check(pos(f1 >= 0), "f1 >= 0");

	Tester.check(neg(0 < fx),  "! 0 < fx");
	Tester.check(neg(0 <= fx), "! 0 <= fx");
	Tester.check(neg(0 == fx), "! 0 == fx");
	Tester.check(pos(0 != fx), "0 != fx");
	Tester.check(neg(0 > fx),  "! 0 > fx");
	Tester.check(neg(0 >= fx), "! 0 >= fx");

	Tester.check(neg(fx < 0),  "! fx < 0");
	Tester.check(neg(fx <= 0), "! fx <= 0");
	Tester.check(neg(fx == 0), "! fx == 0");
	Tester.check(pos(fx != 0), "fx != 0");
	Tester.check(neg(fx > 0),  "! fx > 0");
	Tester.check(neg(fx >= 0), "! fx >= 0");

    }

    static void doDouble() {
	Tester.check(neg(0 < dn),  "! 0 < dn");
	Tester.check(neg(0 <= dn), "! 0 <= dn");
	Tester.check(neg(0 == dn), "! 0 == dn");
	Tester.check(pos(0 != dn), "0 != dn");
	Tester.check(pos(0 > dn),  "0 > dn");
	Tester.check(pos(0 >= dn), "0 >= dn");

	Tester.check(pos(dn < 0),  "dn < 0");
	Tester.check(pos(dn <= 0), "dn <= 0");
	Tester.check(neg(dn == 0), "! dn == 0");
	Tester.check(pos(dn != 0), "dn != 0");
	Tester.check(neg(dn > 0),  "! dn > 0");
	Tester.check(neg(dn >= 0), "! dn >= 0");

	Tester.check(neg(0 < dz),  "! 0 < dz");
	Tester.check(pos(0 <= dz), "0 <= dz");
	Tester.check(pos(0 == dz), "0 == dz");
	Tester.check(neg(0 != dz), "! 0 != dz");
	Tester.check(neg(0 > dz),  "! 0 > dz");
	Tester.check(pos(0 >= dz), "0 >= dz");

	Tester.check(neg(dz < 0),  "! dz < 0");
	Tester.check(pos(dz <= 0), "dz <= 0");
	Tester.check(pos(dz == 0), "dz == 0");
	Tester.check(neg(dz != 0), "! dz != 0");
	Tester.check(neg(dz > 0),  "! dz > 0");
	Tester.check(pos(dz >= 0), "dz >= 0");

	Tester.check(pos(0 < d1),  "0 < d1");
	Tester.check(pos(0 <= d1), "0 <= d1");
	Tester.check(neg(0 == d1), "! 0 == d1");
	Tester.check(pos(0 != d1), "0 != d1");
	Tester.check(neg(0 > d1),  "! 0 > d1");
	Tester.check(neg(0 >= d1), "! 0 >= d1");

	Tester.check(neg(d1 < 0),  "! d1 < 0");
	Tester.check(neg(d1 <= 0), "! d1 <= 0");
	Tester.check(neg(d1 == 0), "! d1 == 0");
	Tester.check(pos(d1 != 0), "d1 != 0");
	Tester.check(pos(d1 > 0),  "d1 > 0");
	Tester.check(pos(d1 >= 0), "d1 >= 0");

	Tester.check(neg(0 < dx),  "! 0 < dx");
	Tester.check(neg(0 <= dx), "! 0 <= dx");
	Tester.check(neg(0 == dx), "! 0 == dx");
	Tester.check(pos(0 != dx), "0 != dx");
	Tester.check(neg(0 > dx),  "! 0 > dx");
	Tester.check(neg(0 >= dx), "! 0 >= dx");

	Tester.check(neg(dx < 0),  "! dx < 0");
	Tester.check(neg(dx <= 0), "! dx <= 0");
	Tester.check(neg(dx == 0), "! dx == 0");
	Tester.check(pos(dx != 0), "dx != 0");
	Tester.check(neg(dx > 0),  "! dx > 0");
	Tester.check(neg(dx >= 0), "! dx >= 0");
    }


    static void doChar() {
	Tester.check(neg(0 < cz),  "! 0 < cz");
	Tester.check(pos(0 <= cz), "0 <= cz");
	Tester.check(pos(0 == cz), "0 == cz");
	Tester.check(neg(0 != cz), "! 0 != cz");
	Tester.check(neg(0 > cz),  "! 0 > cz");
	Tester.check(pos(0 >= cz), "0 >= cz");

	Tester.check(neg(cz < 0),  "! cz < 0");
	Tester.check(pos(cz <= 0), "cz <= 0");
	Tester.check(pos(cz == 0), "cz == 0");
	Tester.check(neg(cz != 0), "! cz != 0");
	Tester.check(neg(cz > 0),  "! cz > 0");
	Tester.check(pos(cz >= 0), "cz >= 0");

	Tester.check(pos(0 < c1),  "0 < c1");
	Tester.check(pos(0 <= c1), "0 <= c1");
	Tester.check(neg(0 == c1), "! 0 == c1");
	Tester.check(pos(0 != c1), "0 != c1");
	Tester.check(neg(0 > c1),  "! 0 > c1");
	Tester.check(neg(0 >= c1), "! 0 >= c1");

	Tester.check(neg(c1 < 0),  "! c1 < 0");
	Tester.check(neg(c1 <= 0), "! c1 <= 0");
	Tester.check(neg(c1 == 0), "! c1 == 0");
	Tester.check(pos(c1 != 0), "c1 != 0");
	Tester.check(pos(c1 > 0),  "c1 > 0");
	Tester.check(pos(c1 >= 0), "c1 >= 0");
    }
}
