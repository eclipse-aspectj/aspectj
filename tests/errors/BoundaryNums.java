//Over-boundary base values cause compile-time errors
public class BoundaryNums {
    public static void main(String[] args) {
	byte minByte = -129;
	byte maxByte =  128;
	byte minByteHex = -0x81;
	byte maxByteHex =  0x80;

	short minShort = -32769;
	short maxShort =  32768;
	short minShortHex = -0x8001;
	short maxShortHex =  0x8000;

	char maxChar = 65536;
	char maxCharHex =  0x10000;
	char maxCharChar =  '\u10000';

	int minInt = -2147483649;
	int maxInt =  2147483648;
	int minIntHex = -0x80000001;
	int maxIntHex =  0x80000000;

	long minLong = -9223372036854775810L;
	long maxLong =  9223372036854775809L;
	long minLongHex = -0x8000000000000001L;
	long maxLongHex =  0x8000000000000000L;

	float minPosFloat = 1.0e-46f;
	float maxPosFloat = 1.0e+39f;

	double minPosDouble = 1.0e-325;
	double maxPosDouble = 1.0e+309;
    }
}
