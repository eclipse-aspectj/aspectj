public class Fields2 {
	int a = 1;
	String s = "hello";
	double d = 1.0d;
	boolean b = true;

	short ps = (short)1;
	float fs = 1.0f;
	long ls = 1L;
	byte bs = (byte)3;
	char cs = 'a';


	Inner obj = new Inner();
	static int as = 1;
	static String ss = "hello";
	static double ds = 1.0d;
	static Inner objs = new Inner();

	public static void main(String []argv) {
		Fields2 f = new Fields2();
		f.a = 2;
		f.ps = (short)3;
		f.d = 2.0d;
		f.obj = new Inner();
		f.s = "helo";
		f.fs = 4f;
		f.ls = 3L;
		f.bs = (byte)23;
		f.cs = 'a';
	}

	static class Inner {}
}

aspect X {
	before(): within(Fields2) && set(* *) && withincode(* main(..)){
		System.out.println(thisJoinPointStaticPart.getSignature());
	}
}
