import org.aspectj.testing.Tester;

public class Ops {
    static public void main(String[] args) {
	// boolean
	Tester.checkEqual(false && false, false, "false && false");
	Tester.checkEqual(true && false, false, "true && false");
	Tester.checkEqual(false && true, false, "false && true");
	Tester.checkEqual(true && true, true, "true && true");

	Tester.checkEqual(false || false, false, "false || false");
	Tester.checkEqual(true || false, true, "true || false");
	Tester.checkEqual(false || true, true, "false || true");
	Tester.checkEqual(true || true, true, "true || true");

	Tester.checkEqual(false & false, false, "false & false");
	Tester.checkEqual(true & false, false, "true & false");
	Tester.checkEqual(false & true, false, "false & true");
	Tester.checkEqual(true & true, true, "true & true");

	Tester.checkEqual(false | false, false, "false | false");
	Tester.checkEqual(true | false, true, "true | false");
	Tester.checkEqual(false | true, true, "false | true");
	Tester.checkEqual(true | true, true, "true | true");

	Tester.checkEqual(false ^ false, false, "false ^ false");
	Tester.checkEqual(true ^ false, true, "true ^ false");
	Tester.checkEqual(false ^ true, true, "false ^ true");
	Tester.checkEqual(true ^ true, false, "true ^ true");

	Tester.checkEqual(! false, true, "! false");
	Tester.checkEqual(! true, false, "! true");

	//int unary
	Tester.checkEqual(+(2), 2, "+(2)");
	Tester.checkEqual(+(1), 1, "+(1)");
	Tester.checkEqual(+(0), 0, "+(0)");
	Tester.checkEqual(+(-1), -1, "+(-1)");	
	Tester.checkEqual(+(-2), -2, "+(-2)");	

	Tester.checkEqual(-(2), -2, "-(2)");
	Tester.checkEqual(-(1), -1, "-(1)");
	Tester.checkEqual(-(0), -0, "-(0)");
	Tester.checkEqual(-(-1), 1, "-(-1)");	
	Tester.checkEqual(-(-2), 2, "-(-2)");	

	Tester.checkEqual(~(2), -3, "~(2)");
	Tester.checkEqual(~(1), -2, "~(1)");
	Tester.checkEqual(~(0), -1, "~(0)");
	Tester.checkEqual(~(-1), 0, "~(-1)");	
	Tester.checkEqual(~(-2), 1, "~(-2)");	

	//int+
	Tester.checkEqual(2 + 2, 4, "2 + 2");
	Tester.checkEqual(2 + 1, 3, "2 + 1");
	Tester.checkEqual(2 + 0, 2, "2 + 0");
	Tester.checkEqual(2 + -1, 1, "2 + -1");
	Tester.checkEqual(2 + -2, 0, "2 + -2");

	Tester.checkEqual(1 + 2, 3, "1 + 2");
	Tester.checkEqual(1 + 1, 2, "1 + 1");
	Tester.checkEqual(1 + 0, 1, "1 + 0");
	Tester.checkEqual(1 + -1, 0, "1 + -1");
	Tester.checkEqual(1 + -2, -1, "1 + -2");

	Tester.checkEqual(0 + 2, 2, "0 + 2");
	Tester.checkEqual(0 + 1, 1, "0 + 1");
	Tester.checkEqual(0 + 0, 0, "0 + 0");
	Tester.checkEqual(0 + -1, -1, "0 + -1");
	Tester.checkEqual(0 + -2, -2, "0 + -2");

	Tester.checkEqual(-1 + 2, 1, "-1 + 2");
	Tester.checkEqual(-1 + 1, 0, "-1 + 1");
	Tester.checkEqual(-1 + 0, -1, "-1 + 0");
	Tester.checkEqual(-1 + -1, -2, "-1 + -1");
	Tester.checkEqual(-1 + -2, -3, "-1 + -2");

	Tester.checkEqual(-2 + 2, 0, "-2 + 2");
	Tester.checkEqual(-2 + 1, -1, "-2 + 1");
	Tester.checkEqual(-2 + 0, -2, "-2 + 0");
	Tester.checkEqual(-2 + -1, -3, "-2 + -1");
	Tester.checkEqual(-2 + -2, -4, "-2 + -2");

	//int-
	Tester.checkEqual(2 - 2,  0, "2 - 2");
	Tester.checkEqual(2 - 1,  1, "2 - 1");
	Tester.checkEqual(2 - 0,  2, "2 - 0");
	Tester.checkEqual(2 - -1,  3, "2 - -1");
	Tester.checkEqual(2 - -2,  4, "2 - -2");

	Tester.checkEqual(1 - 2,  -1, "1 - 2");
	Tester.checkEqual(1 - 1,  0, "1 - 1");
	Tester.checkEqual(1 - 0,  1, "1 - 0");
	Tester.checkEqual(1 - -1,  2, "1 - -1");
	Tester.checkEqual(1 - -2,  3, "1 - -2");

	Tester.checkEqual(0 - 2,  -2, "0 - 2");
	Tester.checkEqual(0 - 1,  -1, "0 - 1");
	Tester.checkEqual(0 - 0,  0, "0 - 0");
	Tester.checkEqual(0 - -1,  1, "0 - -1");
	Tester.checkEqual(0 - -2,  2, "0 - -2");

	Tester.checkEqual(-1 - 2,  -3, "-1 - 2");
	Tester.checkEqual(-1 - 1,  -2, "-1 - 1");
	Tester.checkEqual(-1 - 0,  -1, "-1 - 0");
	Tester.checkEqual(-1 - -1,  0, "-1 - -1");
	Tester.checkEqual(-1 - -2,  1, "-1 - -2");

	Tester.checkEqual(-2 - 2,  -4, "-2 - 2");
	Tester.checkEqual(-2 - 1,  -3, "-2 - 1");
	Tester.checkEqual(-2 - 0,  -2, "-2 - 0");
	Tester.checkEqual(-2 - -1,  -1, "-2 - -1");
	Tester.checkEqual(-2 - -2,  0, "-2 - -2");

	//int*
	Tester.checkEqual(2 * 2,  4, "2 * 2");
	Tester.checkEqual(2 * 1,  2, "2 * 1");
	Tester.checkEqual(2 * 0,  0, "2 * 0");
	Tester.checkEqual(2 * -1,  -2, "2 * -1");
	Tester.checkEqual(2 * -2,  -4, "2 * -2");

	Tester.checkEqual(1 * 2,  2, "1 * 2");
	Tester.checkEqual(1 * 1,  1, "1 * 1");
	Tester.checkEqual(1 * 0,  0, "1 * 0");
	Tester.checkEqual(1 * -1,  -1, "1 * -1");
	Tester.checkEqual(1 * -2,  -2, "1 * -2");

	Tester.checkEqual(0 * 2,  0, "0 * 2");
	Tester.checkEqual(0 * 1,  0, "0 * 1");
	Tester.checkEqual(0 * 0,  0, "0 * 0");
	Tester.checkEqual(0 * -1,  0, "0 * -1");
	Tester.checkEqual(0 * -2,  0, "0 * -2");

	Tester.checkEqual(-1 * 2,  -2, "-1 * 2");
	Tester.checkEqual(-1 * 1,  -1, "-1 * 1");
	Tester.checkEqual(-1 * 0,  0, "-1 * 0");
	Tester.checkEqual(-1 * -1,  1, "-1 * -1");
	Tester.checkEqual(-1 * -2,  2, "-1 * -2");

	Tester.checkEqual(-2 * 2,  -4, "-2 * 2");
	Tester.checkEqual(-2 * 1,  -2, "-2 * 1");
	Tester.checkEqual(-2 * 0,  0, "-2 * 0");
	Tester.checkEqual(-2 * -1,  2, "-2 * -1");
	Tester.checkEqual(-2 * -2,  4, "-2 * -2");

	//int/
	Tester.checkEqual(2 / 2,  1, "2 / 2");
	Tester.checkEqual(2 / 1,  2, "2 / 1");
	//	Tester.checkEqual(2 / 0, 99, "2 / 0");
	Tester.checkEqual(2 / -1,  -2, "2 / -1");
	Tester.checkEqual(2 / -2,  -1, "2 / -2");

	Tester.checkEqual(1 / 2,  0, "1 / 2");
	Tester.checkEqual(1 / 1,  1, "1 / 1");
	//	Tester.checkEqual(1 / 0, 99, "1 / 0");
	Tester.checkEqual(1 / -1,  -1, "1 / -1");
	Tester.checkEqual(1 / -2,  0, "1 / -2");

	Tester.checkEqual(0 / 2,  0, "0 / 2");
	Tester.checkEqual(0 / 1,  0, "0 / 1");
	//	Tester.checkEqual(0 / 0, 99, "0 / 0");
	Tester.checkEqual(0 / -1,  0, "0 / -1");
	Tester.checkEqual(0 / -2,  0, "0 / -2");

	Tester.checkEqual(-1 / 2,  0, "-1 / 2");
	Tester.checkEqual(-1 / 1,  -1, "-1 / 1");
	//Tester.checkEqual(-1 / 0, 99, "-1 / 0");
	Tester.checkEqual(-1 / -1,  1, "-1 / -1");
	Tester.checkEqual(-1 / -2,  0, "-1 / -2");

	Tester.checkEqual(-2 / 2,  -1, "-2 / 2");
	Tester.checkEqual(-2 / 1,  -2, "-2 / 1");
	//Tester.checkEqual(-2 / 0, 99, "-2 / 0");
	Tester.checkEqual(-2 / -1,  2, "-2 / -1");
	Tester.checkEqual(-2 / -2,  1, "-2 / -2");

	//int%
	Tester.checkEqual(2 % 2,  0, "2 % 2");
	Tester.checkEqual(2 % 1,  0, "2 % 1");
	//Tester.checkEqual(2 % 0, 99, "2 % 0");
	Tester.checkEqual(2 % -1,  0, "2 % -1");
	Tester.checkEqual(2 % -2,  0, "2 % -2");

	Tester.checkEqual(1 % 2,  1, "1 % 2");
	Tester.checkEqual(1 % 1,  0, "1 % 1");
	//Tester.checkEqual(1 % 0, 99, "1 % 0");
	Tester.checkEqual(1 % -1,  0, "1 % -1");
	Tester.checkEqual(1 % -2,  1, "1 % -2");

	Tester.checkEqual(0 % 2,  0, "0 % 2");
	Tester.checkEqual(0 % 1,  0, "0 % 1");
	//	Tester.checkEqual(0 % 0, 99, "0 % 0");
	Tester.checkEqual(0 % -1,  0, "0 % -1");
	Tester.checkEqual(0 % -2,  0, "0 % -2");

	Tester.checkEqual(-1 % 2,  -1, "-1 % 2");
	Tester.checkEqual(-1 % 1,  0, "-1 % 1");
	//Tester.checkEqual(-1 % 0, 99, "-1 % 0");
	Tester.checkEqual(-1 % -1,  0, "-1 % -1");
	Tester.checkEqual(-1 % -2,  -1, "-1 % -2");

	Tester.checkEqual(-2 % 2,  0, "-2 % 2");
	Tester.checkEqual(-2 % 1,  0, "-2 % 1");
	//Tester.checkEqual(-2 % 0, 99, "-2 % 0");
	Tester.checkEqual(-2 % -1,  0, "-2 % -1");
	Tester.checkEqual(-2 % -2,  0, "-2 % -2");

	//int&
	Tester.checkEqual(2 & 2,  2, "2 & 2");
	Tester.checkEqual(2 & 1,  0, "2 & 1");
	Tester.checkEqual(2 & 0,  0, "2 & 0");
	Tester.checkEqual(2 & -1,  2, "2 & -1");
	Tester.checkEqual(2 & -2,  2, "2 & -2");

	Tester.checkEqual(1 & 2,  0, "1 & 2");
	Tester.checkEqual(1 & 1,  1, "1 & 1");
	Tester.checkEqual(1 & 0,  0, "1 & 0");
	Tester.checkEqual(1 & -1,  1, "1 & -1");
	Tester.checkEqual(1 & -2,  0, "1 & -2");

	Tester.checkEqual(0 & 2,  0, "0 & 2");
	Tester.checkEqual(0 & 1,  0, "0 & 1");
	Tester.checkEqual(0 & 0,  0, "0 & 0");
	Tester.checkEqual(0 & -1,  0, "0 & -1");
	Tester.checkEqual(0 & -2,  0, "0 & -2");

	Tester.checkEqual(-1 & 2,  2, "-1 & 2");
	Tester.checkEqual(-1 & 1,  1, "-1 & 1");
	Tester.checkEqual(-1 & 0,  0, "-1 & 0");
	Tester.checkEqual(-1 & -1,  -1, "-1 & -1");
	Tester.checkEqual(-1 & -2,  -2, "-1 & -2");

	Tester.checkEqual(-2 & 2,  2, "-2 & 2");
	Tester.checkEqual(-2 & 1,  0, "-2 & 1");
	Tester.checkEqual(-2 & 0,  0, "-2 & 0");
	Tester.checkEqual(-2 & -1,  -2, "-2 & -1");
	Tester.checkEqual(-2 & -2,  -2, "-2 & -2");

	//int|
	Tester.checkEqual(2 | 2,  2, "2 | 2");
	Tester.checkEqual(2 | 1,  3, "2 | 1");
	Tester.checkEqual(2 | 0,  2, "2 | 0");
	Tester.checkEqual(2 | -1,  -1, "2 | -1");
	Tester.checkEqual(2 | -2,  -2, "2 | -2");

	Tester.checkEqual(1 | 2,  3, "1 | 2");
	Tester.checkEqual(1 | 1,  1, "1 | 1");
	Tester.checkEqual(1 | 0,  1, "1 | 0");
	Tester.checkEqual(1 | -1,  -1, "1 | -1");
	Tester.checkEqual(1 | -2,  -1, "1 | -2");

	Tester.checkEqual(0 | 2,  2, "0 | 2");
	Tester.checkEqual(0 | 1,  1, "0 | 1");
	Tester.checkEqual(0 | 0,  0, "0 | 0");
	Tester.checkEqual(0 | -1,  -1, "0 | -1");
	Tester.checkEqual(0 | -2,  -2, "0 | -2");

	Tester.checkEqual(-1 | 2,  -1, "-1 | 2");
	Tester.checkEqual(-1 | 1,  -1, "-1 | 1");
	Tester.checkEqual(-1 | 0,  -1, "-1 | 0");
	Tester.checkEqual(-1 | -1,  -1, "-1 | -1");
	Tester.checkEqual(-1 | -2,  -1, "-1 | -2");

	Tester.checkEqual(-2 | 2,  -2, "-2 | 2");
	Tester.checkEqual(-2 | 1,  -1, "-2 | 1");
	Tester.checkEqual(-2 | 0,  -2, "-2 | 0");
	Tester.checkEqual(-2 | -1,  -1, "-2 | -1");
	Tester.checkEqual(-2 | -2,  -2, "-2 | -2");

	//int^
	Tester.checkEqual(2 ^ 2,  0, "2 ^ 2");
	Tester.checkEqual(2 ^ 1,  3, "2 ^ 1");
	Tester.checkEqual(2 ^ 0,  2, "2 ^ 0");
	Tester.checkEqual(2 ^ -1,  -3, "2 ^ -1");
	Tester.checkEqual(2 ^ -2,  -4, "2 ^ -2");

	Tester.checkEqual(1 ^ 2,  3, "1 ^ 2");
	Tester.checkEqual(1 ^ 1,  0, "1 ^ 1");
	Tester.checkEqual(1 ^ 0,  1, "1 ^ 0");
	Tester.checkEqual(1 ^ -1,  -2, "1 ^ -1");
	Tester.checkEqual(1 ^ -2,  -1, "1 ^ -2");

	Tester.checkEqual(0 ^ 2,  2, "0 ^ 2");
	Tester.checkEqual(0 ^ 1,  1, "0 ^ 1");
	Tester.checkEqual(0 ^ 0,  0, "0 ^ 0");
	Tester.checkEqual(0 ^ -1,  -1, "0 ^ -1");
	Tester.checkEqual(0 ^ -2,  -2, "0 ^ -2");

	Tester.checkEqual(-1 ^ 2,  -3, "-1 ^ 2");
	Tester.checkEqual(-1 ^ 1,  -2, "-1 ^ 1");
	Tester.checkEqual(-1 ^ 0,  -1, "-1 ^ 0");
	Tester.checkEqual(-1 ^ -1,  0, "-1 ^ -1");
	Tester.checkEqual(-1 ^ -2,  1, "-1 ^ -2");

	Tester.checkEqual(-2 ^ 2,  -4, "-2 ^ 2");
	Tester.checkEqual(-2 ^ 1,  -1, "-2 ^ 1");
	Tester.checkEqual(-2 ^ 0,  -2, "-2 ^ 0");
	Tester.checkEqual(-2 ^ -1,  1, "-2 ^ -1");
	Tester.checkEqual(-2 ^ -2,  0, "-2 ^ -2");

	//int<<
	Tester.checkEqual(2 << 2,  8, "2 << 2");
	Tester.checkEqual(2 << 1,  4, "2 << 1");
	Tester.checkEqual(2 << 0,  2, "2 << 0");
	Tester.checkEqual(2 << -1,  0, "2 << -1");
	Tester.checkEqual(2 << -2,  -2147483648, "2 << -2");

	Tester.checkEqual(1 << 2,  4, "1 << 2");
	Tester.checkEqual(1 << 1,  2, "1 << 1");
	Tester.checkEqual(1 << 0,  1, "1 << 0");
	Tester.checkEqual(1 << -1,  -2147483648, "1 << -1");
	Tester.checkEqual(1 << -2,  1073741824, "1 << -2");

	Tester.checkEqual(0 << 2, 0, "0 << 2");
	Tester.checkEqual(0 << 1, 0,  "0 << 1");
	Tester.checkEqual(0 << 0, 0, "0 << 0");
	Tester.checkEqual(0 << -1, 0, "0 << -1");
	Tester.checkEqual(0 << -2, 0, "0 << -2");

	Tester.checkEqual(-1 << 2,  -4, "-1 << 2");
	Tester.checkEqual(-1 << 1,  -2, "-1 << 1");
	Tester.checkEqual(-1 << 0,  -1, "-1 << 0");
	Tester.checkEqual(-1 << -1,  -2147483648, "-1 << -1");
	Tester.checkEqual(-1 << -2,  -1073741824, "-1 << -2");

	Tester.checkEqual(-2 << 2,  -8, "-2 << 2");
	Tester.checkEqual(-2 << 1,  -4, "-2 << 1");
	Tester.checkEqual(-2 << 0,  -2, "-2 << 0");
	Tester.checkEqual(-2 << -1,  0, "-2 << -1");
	Tester.checkEqual(-2 << -2,  -2147483648, "-2 << -2");

	//int>>
	Tester.checkEqual(2 >> 2,  0, "2 >> 2");
	Tester.checkEqual(2 >> 1,  1, "2 >> 1");
	Tester.checkEqual(2 >> 0,  2, "2 >> 0");
	Tester.checkEqual(2 >> -1,  0, "2 >> -1");
	Tester.checkEqual(2 >> -2,  0, "2 >> -2");

	Tester.checkEqual(1 >> 2,  0, "1 >> 2");
	Tester.checkEqual(1 >> 1,  0, "1 >> 1");
	Tester.checkEqual(1 >> 0,  1, "1 >> 0");
	Tester.checkEqual(1 >> -1,  0, "1 >> -1");
	Tester.checkEqual(1 >> -2,  0, "1 >> -2");

	Tester.checkEqual(0 >> 2,  0, "0 >> 2");
	Tester.checkEqual(0 >> 1,  0, "0 >> 1");
	Tester.checkEqual(0 >> 0,  0, "0 >> 0");
	Tester.checkEqual(0 >> -1,  0, "0 >> -1");
	Tester.checkEqual(0 >> -2,  0, "0 >> -2");

	Tester.checkEqual(-1 >> 2,  -1, "-1 >> 2");
	Tester.checkEqual(-1 >> 1,  -1, "-1 >> 1");
	Tester.checkEqual(-1 >> 0,  -1, "-1 >> 0");
	Tester.checkEqual(-1 >> -1,  -1, "-1 >> -1");
	Tester.checkEqual(-1 >> -2,  -1, "-1 >> -2");

	Tester.checkEqual(-2 >> 2,  -1, "-2 >> 2");
	Tester.checkEqual(-2 >> 1,  -1, "-2 >> 1");
	Tester.checkEqual(-2 >> 0,  -2, "-2 >> 0");
	Tester.checkEqual(-2 >> -1,  -1, "-2 >> -1");
	Tester.checkEqual(-2 >> -2,  -1, "-2 >> -2");

	//int>>>
	Tester.checkEqual(2 >>> 2,  0, "2 >>> 2");
	Tester.checkEqual(2 >>> 1,  1, "2 >>> 1");
	Tester.checkEqual(2 >>> 0,  2, "2 >>> 0");
	Tester.checkEqual(2 >>> -1,  0, "2 >>> -1");
	Tester.checkEqual(2 >>> -2,  0, "2 >>> -2");

	Tester.checkEqual(1 >>> 2,  0, "1 >>> 2");
	Tester.checkEqual(1 >>> 1,  0, "1 >>> 1");
	Tester.checkEqual(1 >>> 0,  1, "1 >>> 0");
	Tester.checkEqual(1 >>> -1,  0, "1 >>> -1");
	Tester.checkEqual(1 >>> -2,  0, "1 >>> -2");

	Tester.checkEqual(0 >>> 2,  0, "0 >>> 2");
	Tester.checkEqual(0 >>> 1,  0, "0 >>> 1");
	Tester.checkEqual(0 >>> 0,  0, "0 >>> 0");
	Tester.checkEqual(0 >>> -1,  0, "0 >>> -1");
	Tester.checkEqual(0 >>> -2,  0, "0 >>> -2");

	Tester.checkEqual(-1 >>> 2,  1073741823, "-1 >>> 2");
	Tester.checkEqual(-1 >>> 1,  2147483647, "-1 >>> 1");
	Tester.checkEqual(-1 >>> 0,  -1, "-1 >>> 0");
	Tester.checkEqual(-1 >>> -1,  1, "-1 >>> -1");
	Tester.checkEqual(-1 >>> -2,  3, "-1 >>> -2");

	Tester.checkEqual(-2 >>> 2,  1073741823, "-2 >>> 2");
	Tester.checkEqual(-2 >>> 1,  2147483647, "-2 >>> 1");
	Tester.checkEqual(-2 >>> 0,  -2, "-2 >>> 0");
	Tester.checkEqual(-2 >>> -1,  1, "-2 >>> -1");
	Tester.checkEqual(-2 >>> -2,  3, "-2 >>> -2");

        // ****************

	//long unary
	Tester.checkEqual(+(2L), 2L, "+(2L)");
	Tester.checkEqual(+(1L), 1L, "+(1L)");
	Tester.checkEqual(+(0L), 0L, "+(0L)");
	Tester.checkEqual(+(-1L), -1L, "+(-1L)");	
	Tester.checkEqual(+(-2L), -2L, "+(-2L)");	

	Tester.checkEqual(-(2L), -2L, "-(2L)");
	Tester.checkEqual(-(1L), -1L, "-(1L)");
	Tester.checkEqual(-(0L), -0L, "-(0L)");
	Tester.checkEqual(-(-1L), 1L, "-(-1L)");	
	Tester.checkEqual(-(-2L), 2L, "-(-2L)");	

	Tester.checkEqual(~(2L), -3L, "~(2L)");
	Tester.checkEqual(~(1L), -2L, "~(1L)");
	Tester.checkEqual(~(0L), -1L, "~(0L)");
	Tester.checkEqual(~(-1L), 0L, "~(-1L)");	
	Tester.checkEqual(~(-2L), 1L, "~(-2L)");	

	//long+
	Tester.checkEqual(2L + 2L, 4L, "2 + 2");
	Tester.checkEqual(2L + 1L, 3L, "2 + 1");
	Tester.checkEqual(2L + 0L, 2L, "2 + 0");
	Tester.checkEqual(2L + -1L, 1L, "2 + -1");
	Tester.checkEqual(2L + -2L, 0L, "2 + -2");

	Tester.checkEqual(1L + 2L, 3L, "1 + 2");
	Tester.checkEqual(1L + 1L, 2L, "1 + 1");
	Tester.checkEqual(1L + 0L, 1L, "1 + 0");
	Tester.checkEqual(1L + -1L, 0L, "1 + -1");
	Tester.checkEqual(1L + -2L, -1L, "1 + -2");

	Tester.checkEqual(0L + 2L, 2L, "0 + 2");
	Tester.checkEqual(0L + 1L, 1L, "0 + 1");
	Tester.checkEqual(0L + 0L, 0L, "0 + 0");
	Tester.checkEqual(0L + -1L, -1L, "0 + -1");
	Tester.checkEqual(0L + -2L, -2L, "0 + -2");

	Tester.checkEqual(-1L + 2L, 1L, "-1 + 2");
	Tester.checkEqual(-1L + 1L, 0L, "-1 + 1");
	Tester.checkEqual(-1L + 0L, -1L, "-1 + 0");
	Tester.checkEqual(-1L + -1L, -2L, "-1 + -1");
	Tester.checkEqual(-1L + -2L, -3L, "-1 + -2");

	Tester.checkEqual(-2L + 2L, 0L, "-2 + 2");
	Tester.checkEqual(-2L + 1L, -1L, "-2 + 1");
	Tester.checkEqual(-2L + 0L, -2L, "-2 + 0");
	Tester.checkEqual(-2L + -1L, -3L, "-2 + -1");
	Tester.checkEqual(-2L + -2L, -4L, "-2 + -2");

	//long-
	Tester.checkEqual(2L - 2L,  0L, "2 - 2");
	Tester.checkEqual(2L - 1L,  1L, "2 - 1");
	Tester.checkEqual(2L - 0L,  2L, "2 - 0");
	Tester.checkEqual(2L - -1L,  3L, "2 - -1");
	Tester.checkEqual(2L - -2L,  4L, "2 - -2");

	Tester.checkEqual(1L - 2L,  -1L, "1 - 2");
	Tester.checkEqual(1L - 1L,  0L, "1 - 1");
	Tester.checkEqual(1L - 0L,  1L, "1 - 0");
	Tester.checkEqual(1L - -1L,  2L, "1 - -1");
	Tester.checkEqual(1L - -2L,  3L, "1 - -2");

	Tester.checkEqual(0L - 2L,  -2L, "0 - 2");
	Tester.checkEqual(0L - 1L,  -1L, "0 - 1");
	Tester.checkEqual(0L - 0L,  0L, "0 - 0");
	Tester.checkEqual(0L - -1L,  1L, "0 - -1");
	Tester.checkEqual(0L - -2L,  2L, "0 - -2");

	Tester.checkEqual(-1L - 2L,  -3L, "-1 - 2");
	Tester.checkEqual(-1L - 1L,  -2L, "-1 - 1");
	Tester.checkEqual(-1L - 0L,  -1L, "-1 - 0");
	Tester.checkEqual(-1L - -1L,  0L, "-1 - -1");
	Tester.checkEqual(-1L - -2L,  1L, "-1 - -2");

	Tester.checkEqual(-2L - 2L,  -4L, "-2 - 2");
	Tester.checkEqual(-2L - 1L,  -3L, "-2 - 1");
	Tester.checkEqual(-2L - 0L,  -2L, "-2 - 0");
	Tester.checkEqual(-2L - -1L,  -1L, "-2 - -1");
	Tester.checkEqual(-2L - -2L,  0L, "-2 - -2");

	//long*
	Tester.checkEqual(2L * 2L,  4L, "2 * 2");
	Tester.checkEqual(2L * 1L,  2L, "2 * 1");
	Tester.checkEqual(2L * 0L,  0L, "2 * 0");
	Tester.checkEqual(2L * -1L,  -2L, "2 * -1");
	Tester.checkEqual(2L * -2L,  -4L, "2 * -2");

	Tester.checkEqual(1L * 2L,  2L, "1 * 2");
	Tester.checkEqual(1L * 1L,  1L, "1 * 1");
	Tester.checkEqual(1L * 0L,  0L, "1 * 0");
	Tester.checkEqual(1L * -1L,  -1L, "1 * -1");
	Tester.checkEqual(1L * -2L,  -2L, "1 * -2");

	Tester.checkEqual(0L * 2L,  0L, "0 * 2");
	Tester.checkEqual(0L * 1L,  0L, "0 * 1");
	Tester.checkEqual(0L * 0L,  0L, "0 * 0");
	Tester.checkEqual(0L * -1L,  0L, "0 * -1");
	Tester.checkEqual(0L * -2L,  0L, "0 * -2");

	Tester.checkEqual(-1L * 2L,  -2L, "-1 * 2");
	Tester.checkEqual(-1L * 1L,  -1L, "-1 * 1");
	Tester.checkEqual(-1L * 0L,  0L, "-1 * 0");
	Tester.checkEqual(-1L * -1L,  1L, "-1 * -1");
	Tester.checkEqual(-1L * -2L,  2L, "-1 * -2");

	Tester.checkEqual(-2L * 2L,  -4L, "-2 * 2");
	Tester.checkEqual(-2L * 1L,  -2L, "-2 * 1");
	Tester.checkEqual(-2L * 0L,  0L, "-2 * 0");
	Tester.checkEqual(-2L * -1L,  2L, "-2 * -1");
	Tester.checkEqual(-2L * -2L,  4L, "-2 * -2");

	//long/
	Tester.checkEqual(2L / 2L,  1L, "2 / 2");
	Tester.checkEqual(2L / 1L,  2L, "2 / 1");
	//	Tester.checkEqual(2L / 0L, 99L, "2 / 0");
	Tester.checkEqual(2L / -1L,  -2L, "2 / -1");
	Tester.checkEqual(2L / -2L,  -1L, "2 / -2");

	Tester.checkEqual(1L / 2L,  0L, "1 / 2");
	Tester.checkEqual(1L / 1L,  1L, "1 / 1");
	//	Tester.checkEqual(1L / 0L, 99L, "1 / 0");
	Tester.checkEqual(1L / -1L,  -1L, "1 / -1");
	Tester.checkEqual(1L / -2L,  0L, "1 / -2");

	Tester.checkEqual(0L / 2L,  0L, "0 / 2");
	Tester.checkEqual(0L / 1L,  0L, "0 / 1");
	//	Tester.checkEqual(0L / 0L, 99L, "0 / 0");
	Tester.checkEqual(0L / -1L,  0L, "0 / -1");
	Tester.checkEqual(0L / -2L,  0L, "0 / -2");

	Tester.checkEqual(-1L / 2L,  0L, "-1 / 2");
	Tester.checkEqual(-1L / 1L,  -1L, "-1 / 1");
	//Tester.checkEqual(-1L / 0L, 99L, "-1 / 0");
	Tester.checkEqual(-1L / -1L,  1L, "-1 / -1");
	Tester.checkEqual(-1L / -2L,  0L, "-1 / -2");

	Tester.checkEqual(-2L / 2L,  -1L, "-2 / 2");
	Tester.checkEqual(-2L / 1L,  -2L, "-2 / 1");
	//Tester.checkEqual(-2L / 0L, 99L, "-2 / 0");
	Tester.checkEqual(-2L / -1L,  2L, "-2 / -1");
	Tester.checkEqual(-2L / -2L,  1L, "-2 / -2");

	//long%
	Tester.checkEqual(2L % 2L,  0L, "2 % 2");
	Tester.checkEqual(2L % 1L,  0L, "2 % 1");
	//Tester.checkEqual(2L % 0L, 99L, "2 % 0");
	Tester.checkEqual(2L % -1L,  0L, "2 % -1");
	Tester.checkEqual(2L % -2L,  0L, "2 % -2");

	Tester.checkEqual(1L % 2L,  1L, "1 % 2");
	Tester.checkEqual(1L % 1L,  0L, "1 % 1");
	//Tester.checkEqual(1L % 0L, 99L, "1 % 0");
	Tester.checkEqual(1L % -1L,  0L, "1 % -1");
	Tester.checkEqual(1L % -2L,  1L, "1 % -2");

	Tester.checkEqual(0L % 2L,  0L, "0 % 2");
	Tester.checkEqual(0L % 1L,  0L, "0 % 1");
	//	Tester.checkEqual(0L % 0L, 99L, "0 % 0");
	Tester.checkEqual(0L % -1L,  0L, "0 % -1");
	Tester.checkEqual(0L % -2L,  0L, "0 % -2");

	Tester.checkEqual(-1L % 2L,  -1L, "-1 % 2");
	Tester.checkEqual(-1L % 1L,  0L, "-1 % 1");
	//Tester.checkEqual(-1L % 0L, 99L, "-1 % 0");
	Tester.checkEqual(-1L % -1L,  0L, "-1 % -1");
	Tester.checkEqual(-1L % -2L,  -1L, "-1 % -2");

	Tester.checkEqual(-2L % 2L,  0L, "-2 % 2");
	Tester.checkEqual(-2L % 1L,  0L, "-2 % 1");
	//Tester.checkEqual(-2L % 0L, 99L, "-2 % 0");
	Tester.checkEqual(-2L % -1L,  0L, "-2 % -1");
	Tester.checkEqual(-2L % -2L,  0L, "-2 % -2");

	//long&
	Tester.checkEqual(2L & 2L,  2L, "2 & 2");
	Tester.checkEqual(2L & 1L,  0L, "2 & 1");
	Tester.checkEqual(2L & 0L,  0L, "2 & 0");
	Tester.checkEqual(2L & -1L,  2L, "2 & -1");
	Tester.checkEqual(2L & -2L,  2L, "2 & -2");

	Tester.checkEqual(1L & 2L,  0L, "1 & 2");
	Tester.checkEqual(1L & 1L,  1L, "1 & 1");
	Tester.checkEqual(1L & 0L,  0L, "1 & 0");
	Tester.checkEqual(1L & -1L,  1L, "1 & -1");
	Tester.checkEqual(1L & -2L,  0L, "1 & -2");

	Tester.checkEqual(0L & 2L,  0L, "0 & 2");
	Tester.checkEqual(0L & 1L,  0L, "0 & 1");
	Tester.checkEqual(0L & 0L,  0L, "0 & 0");
	Tester.checkEqual(0L & -1L,  0L, "0 & -1");
	Tester.checkEqual(0L & -2L,  0L, "0 & -2");

	Tester.checkEqual(-1L & 2L,  2L, "-1 & 2");
	Tester.checkEqual(-1L & 1L,  1L, "-1 & 1");
	Tester.checkEqual(-1L & 0L,  0L, "-1 & 0");
	Tester.checkEqual(-1L & -1L,  -1L, "-1 & -1");
	Tester.checkEqual(-1L & -2L,  -2L, "-1 & -2");

	Tester.checkEqual(-2L & 2L,  2L, "-2 & 2");
	Tester.checkEqual(-2L & 1L,  0L, "-2 & 1");
	Tester.checkEqual(-2L & 0L,  0L, "-2 & 0");
	Tester.checkEqual(-2L & -1L,  -2L, "-2 & -1");
	Tester.checkEqual(-2L & -2L,  -2L, "-2 & -2");

	//long|
	Tester.checkEqual(2L | 2,  2, "2 | LL2");
	Tester.checkEqual(2L | 1,  3, "2 | LL1");
	Tester.checkEqual(2L | 0,  2, "2 | LL0");
	Tester.checkEqual(2L | -1,  -1, "2 | -LL1");
	Tester.checkEqual(2L | -2,  -2, "2 | -LL2");

	Tester.checkEqual(1L | 2,  3, "1 | LL2");
	Tester.checkEqual(1L | 1,  1, "1 | LL1");
	Tester.checkEqual(1L | 0,  1, "1 | LL0");
	Tester.checkEqual(1L | -1,  -1, "1 | -LL1");
	Tester.checkEqual(1L | -2,  -1, "1 | -LL2");

	Tester.checkEqual(0L | 2,  2, "0 | LL2");
	Tester.checkEqual(0L | 1,  1, "0 | LL1");
	Tester.checkEqual(0L | 0,  0, "0 | LL0");
	Tester.checkEqual(0L | -1,  -1, "0 | -LL1");
	Tester.checkEqual(0L | -2,  -2, "0 | -LL2");

	Tester.checkEqual(-1L | 2,  -1, "-1 | LL2");
	Tester.checkEqual(-1L | 1,  -1, "-1 | LL1");
	Tester.checkEqual(-1L | 0,  -1, "-1 | LL0");
	Tester.checkEqual(-1L | -1,  -1, "-1 | -LL1");
	Tester.checkEqual(-1L | -2,  -1, "-1 | -LL2");

	Tester.checkEqual(-2L | 2,  -2, "-2 | LL2");
	Tester.checkEqual(-2L | 1,  -1, "-2 | LL1");
	Tester.checkEqual(-2L | 0,  -2, "-2 | LL0");
	Tester.checkEqual(-2L | -1,  -1, "-2 | -LL1");
	Tester.checkEqual(-2L | -2,  -2, "-2 | -LL2");

	//long^
	Tester.checkEqual(2L ^ 2L,  0L, "2 ^ 2");
	Tester.checkEqual(2L ^ 1L,  3L, "2 ^ 1");
	Tester.checkEqual(2L ^ 0L,  2L, "2 ^ 0");
	Tester.checkEqual(2L ^ -1L,  -3L, "2 ^ -1");
	Tester.checkEqual(2L ^ -2L,  -4L, "2 ^ -2");

	Tester.checkEqual(1L ^ 2L,  3L, "1 ^ 2");
	Tester.checkEqual(1L ^ 1L,  0L, "1 ^ 1");
	Tester.checkEqual(1L ^ 0L,  1L, "1 ^ 0");
	Tester.checkEqual(1L ^ -1L,  -2L, "1 ^ -1");
	Tester.checkEqual(1L ^ -2L,  -1L, "1 ^ -2");

	Tester.checkEqual(0L ^ 2L,  2L, "0 ^ 2");
	Tester.checkEqual(0L ^ 1L,  1L, "0 ^ 1");
	Tester.checkEqual(0L ^ 0L,  0L, "0 ^ 0");
	Tester.checkEqual(0L ^ -1L,  -1L, "0 ^ -1");
	Tester.checkEqual(0L ^ -2L,  -2L, "0 ^ -2");

	Tester.checkEqual(-1L ^ 2L,  -3L, "-1 ^ 2");
	Tester.checkEqual(-1L ^ 1L,  -2L, "-1 ^ 1");
	Tester.checkEqual(-1L ^ 0L,  -1L, "-1 ^ 0");
	Tester.checkEqual(-1L ^ -1L,  0L, "-1 ^ -1");
	Tester.checkEqual(-1L ^ -2L,  1L, "-1 ^ -2");

	Tester.checkEqual(-2L ^ 2L,  -4L, "-2 ^ 2");
	Tester.checkEqual(-2L ^ 1L,  -1L, "-2 ^ 1");
	Tester.checkEqual(-2L ^ 0L,  -2L, "-2 ^ 0");
	Tester.checkEqual(-2L ^ -1L,  1L, "-2 ^ -1");
	Tester.checkEqual(-2L ^ -2L,  0L, "-2 ^ -2");

	//long<<
	Tester.checkEqual(2L << 2L,  8L, "2 << 2");
	Tester.checkEqual(2L << 1L,  4L, "2 << 1");
	Tester.checkEqual(2L << 0L,  2L, "2 << 0");
	Tester.checkEqual(2L << -1L,  0L, "2 << -1");
	Tester.checkEqual(2L << -2L,  -9223372036854775808L, "2 << -2");

	Tester.checkEqual(1L << 2L,  4L, "1 << 2");
	Tester.checkEqual(1L << 1L,  2L, "1 << 1");
	Tester.checkEqual(1L << 0L,  1L, "1 << 0");
	Tester.checkEqual(1L << -1L,  -9223372036854775808L, "1 << -1");
	Tester.checkEqual(1L << -2L,  4611686018427387904L, "1 << -2");

	Tester.checkEqual(0L << 2L, 0L, "0 << 2");
	Tester.checkEqual(0L << 1L, 0L,  "0 << 1");
	Tester.checkEqual(0L << 0L, 0L, "0 << 0");
	Tester.checkEqual(0L << -1L, 0L, "0 << -1");
	Tester.checkEqual(0L << -2L, 0L, "0 << -2");

	Tester.checkEqual(-1L << 2L,  -4L, "-1 << 2");
	Tester.checkEqual(-1L << 1L,  -2L, "-1 << 1");
	Tester.checkEqual(-1L << 0L,  -1L, "-1 << 0");
	Tester.checkEqual(-1L << -1L,  -9223372036854775808L, "-1 << -1");
	Tester.checkEqual(-1L << -2L,  -4611686018427387904L, "-1 << -2");

	Tester.checkEqual(-2L << 2L,  -8L, "-2 << 2");
	Tester.checkEqual(-2L << 1L,  -4L, "-2 << 1");
	Tester.checkEqual(-2L << 0L,  -2L, "-2 << 0");
	Tester.checkEqual(-2L << -1L,  0L, "-2 << -1");
	Tester.checkEqual(-2L << -2L,  -9223372036854775808L, "-2 << -2");

	//long>>
	Tester.checkEqual(2L >> 2L,  0L, "2 >> 2");
	Tester.checkEqual(2L >> 1L,  1L, "2 >> 1");
	Tester.checkEqual(2L >> 0L,  2L, "2 >> 0");
	Tester.checkEqual(2L >> -1L,  0L, "2 >> -1");
	Tester.checkEqual(2L >> -2L,  0L, "2 >> -2");

	Tester.checkEqual(1L >> 2L,  0L, "1 >> 2");
	Tester.checkEqual(1L >> 1L,  0L, "1 >> 1");
	Tester.checkEqual(1L >> 0L,  1L, "1 >> 0");
	Tester.checkEqual(1L >> -1L,  0L, "1 >> -1");
	Tester.checkEqual(1L >> -2L,  0L, "1 >> -2");

	Tester.checkEqual(0L >> 2L,  0L, "0 >> 2");
	Tester.checkEqual(0L >> 1L,  0L, "0 >> 1");
	Tester.checkEqual(0L >> 0L,  0L, "0 >> 0");
	Tester.checkEqual(0L >> -1L,  0L, "0 >> -1");
	Tester.checkEqual(0L >> -2L,  0L, "0 >> -2");

	Tester.checkEqual(-1L >> 2L,  -1L, "-1 >> 2");
	Tester.checkEqual(-1L >> 1L,  -1L, "-1 >> 1");
	Tester.checkEqual(-1L >> 0L,  -1L, "-1 >> 0");
	Tester.checkEqual(-1L >> -1L,  -1L, "-1 >> -1");
	Tester.checkEqual(-1L >> -2L,  -1L, "-1 >> -2");

	Tester.checkEqual(-2L >> 2L,  -1L, "-2 >> 2");
	Tester.checkEqual(-2L >> 1L,  -1L, "-2 >> 1");
	Tester.checkEqual(-2L >> 0L,  -2L, "-2 >> 0");
	Tester.checkEqual(-2L >> -1L,  -1L, "-2 >> -1");
	Tester.checkEqual(-2L >> -2L,  -1L, "-2 >> -2");

	//long>>>
	Tester.checkEqual(2L >>> 2L,  0L, "2 >>> 2");
	Tester.checkEqual(2L >>> 1L,  1L, "2 >>> 1");
	Tester.checkEqual(2L >>> 0L,  2L, "2 >>> 0");
	Tester.checkEqual(2L >>> -1L,  0L, "2 >>> -1");
	Tester.checkEqual(2L >>> -2L,  0L, "2 >>> -2");

	Tester.checkEqual(1L >>> 2L,  0L, "1 >>> 2");
	Tester.checkEqual(1L >>> 1L,  0L, "1 >>> 1");
	Tester.checkEqual(1L >>> 0L,  1L, "1 >>> 0");
	Tester.checkEqual(1L >>> -1L,  0L, "1 >>> -1");
	Tester.checkEqual(1L >>> -2L,  0L, "1 >>> -2");

	Tester.checkEqual(0L >>> 2L,  0L, "0 >>> 2");
	Tester.checkEqual(0L >>> 1L,  0L, "0 >>> 1");
	Tester.checkEqual(0L >>> 0L,  0L, "0 >>> 0");
	Tester.checkEqual(0L >>> -1L,  0L, "0 >>> -1");
	Tester.checkEqual(0L >>> -2L,  0L, "0 >>> -2");

	Tester.checkEqual(-1L >>> 2L,  4611686018427387903L, "-1 >>> 2");
	Tester.checkEqual(-1L >>> 1L,  9223372036854775807L, "-1 >>> 1");
	Tester.checkEqual(-1L >>> 0L,  -1L, "-1 >>> 0");
	Tester.checkEqual(-1L >>> -1L,  1L, "-1 >>> -1");
	Tester.checkEqual(-1L >>> -2L,  3L, "-1 >>> -2");

	Tester.checkEqual(-2L >>> 2L,  4611686018427387903L, "-2 >>> 2");
	Tester.checkEqual(-2L >>> 1L,  9223372036854775807L, "-2 >>> 1");
	Tester.checkEqual(-2L >>> 0L,  -2L, "-2 >>> 0");
	Tester.checkEqual(-2L >>> -1L,  1L, "-2 >>> -1");
	Tester.checkEqual(-2L >>> -2L,  3L, "-2 >>> -2");
    }
}
    
