//
// class NoByteToInt {
//
//	static final byte DEFAULT_MODE = 0;
//    static final byte RECORDING = 2;
//    
//	static byte mode = DEFAULT_MODE;
//	
//	public static void main (String[] args) {
//		mode = RECORDING;
//	}
//	
//}
//
// privileged aspect LoggingControl {
//	
//	/* ERROR */
//	pointcut startRecording1 (int newMode) :
//		set(byte NoByteToInt.mode) && args(newMode) && if(newMode != 5);
//
//	after () returning : startRecording1 (*) {
//		
//	}
//
////	/* OK */
////	pointcut startRecording2 (byte newMode) :
////		set(byte NoByteToInt.mode) && args(newMode) && if(newMode == 
////NoByteToInt.RECORDING);
////
////	after () returning : startRecording2 (*) {
////		
////	}
//
//	/* OK */
//	pointcut startRecording3 (int newMode) :
//		set(byte NoByteToInt.mode) && args(newMode);
//
//	after (int newMode) returning : startRecording3 (newMode) {
//		if (newMode == NoByteToInt.RECORDING) {
//			
//		}
//	}
//}
//
public class NoByteToInt {
    
	static byte mode;
	
	public static void main (String[] args) {
		setByte();
		setChar();
		setShort();
	}
		
	public static void setByte() {
		mode = 0;
		mode = 2;
		mode = 127;
	}
	
	static char c;
	
	public static void setChar() {
		c = 'A';
		c = 'B';
		c = 'C';
	}

	static short s;
	
	public static void setShort() {
		s = 1;
		s = 32767;
	}
	
}

 privileged aspect LoggingControl {
	
	/* ERROR */
	pointcut startRecording1 (int newMode) :
		set(byte NoByteToInt.mode) && args(newMode)  && if(newMode!=3);

	after (int n) returning : startRecording1 (n) {
		System.err.println("[b"+n+"]");
	}
	
	pointcut startRecording2 (int newMode) :
		set(char NoByteToInt.c) && args(newMode) && if(newMode!=3);

	after (int n) returning : startRecording2 (n) {
		System.err.println("[c"+n+"]");
	}
	
	pointcut startRecording3 (int newMode) :
		set(short NoByteToInt.s) && args(newMode) && if(newMode!=3);

	after (int n) returning : startRecording3 (n) {
		System.err.println("[s"+n+"]");
	}


}