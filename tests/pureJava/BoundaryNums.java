import org.aspectj.testing.Tester;

//Boundary base values can be parsed
public class BoundaryNums {
    public static void main(String[] args) {
	byte minByte = -128;
	byte maxByte =  127;
	byte minByteHex = -0x80;
	byte maxByteHex =  0x7F;
        Tester.checkEqual(minByte, Byte.MIN_VALUE);
        Tester.checkEqual(maxByte, Byte.MAX_VALUE);
        Tester.checkEqual(minByteHex, Byte.MIN_VALUE);
        Tester.checkEqual(maxByteHex, Byte.MAX_VALUE);

	short minShort = -32768;
	short maxShort =  32767;
	short minShortHex = -0x8000;
	short maxShortHex =  0x7FFF;
        Tester.checkEqual(minShort, Short.MIN_VALUE);
        Tester.checkEqual(maxShort, Short.MAX_VALUE);
        Tester.checkEqual(minShortHex, Short.MIN_VALUE);
        Tester.checkEqual(maxShortHex, Short.MAX_VALUE);


	char maxChar = 65535;
	char maxCharHex =  0xffff;
	char maxCharChar = '\uffff';
        Tester.checkEqual(maxChar, Character.MAX_VALUE);
        Tester.checkEqual(maxCharHex, Character.MAX_VALUE);
        Tester.checkEqual(maxCharChar, Character.MAX_VALUE);


	int minInt = -2147483648;
	int maxInt =  2147483647;
	int minIntHex = -0x80000000;
	int maxIntHex =  0x7fffffff;
        Tester.checkEqual(minInt, Integer.MIN_VALUE);
        Tester.checkEqual(maxInt, Integer.MAX_VALUE);
        Tester.checkEqual(minIntHex, Integer.MIN_VALUE);
        Tester.checkEqual(maxIntHex, Integer.MAX_VALUE);


	long minLong = -9223372036854775808L;
	long maxLong =  9223372036854775807L;
	long minLongHex = -0x8000000000000000L;
	long maxLongHex =  0x7fffffffffffffffL;
        Tester.checkEqual(minLong, Long.MIN_VALUE);
        Tester.checkEqual(maxLong, Long.MAX_VALUE);
        Tester.checkEqual(minLongHex, Long.MIN_VALUE);
        Tester.checkEqual(maxLongHex, Long.MAX_VALUE);

	float minPosFloat = 1.40239846e-45f;
	float maxPosFloat = 3.40282347e+38f;
        Tester.checkEqual(minPosFloat, Float.MIN_VALUE);
        Tester.checkEqual(maxPosFloat, Float.MAX_VALUE);

	double minPosDouble = 4.94065645841246544e-324;
	double maxPosDouble = 1.79769313486231570e+308;
        Tester.checkEqual(minPosDouble, Double.MIN_VALUE);
        Tester.checkEqual(maxPosDouble, Double.MAX_VALUE);
    }
}
