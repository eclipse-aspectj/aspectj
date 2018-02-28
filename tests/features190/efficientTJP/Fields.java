public class Fields {
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
		Fields f = new Fields();
		int a2 = f.a;
		String s2 = f.s;
		double d2 = f.d;
		Inner obj2 = f.obj;

		short ps2 = f.ps;
		float fs2 = f.fs;
		long ls2 = f.ls;
		byte bs2 = f.bs;
		char cs2 = f.cs;

		int a3 = as;
		String s3 = ss;
		double d3 = ds;
		Inner obj3 = objs;
	}

	static class Inner {}
}

aspect X {
	before(): within(Fields) && get(* *) {
		System.out.println(thisJoinPointStaticPart.getSignature());
	}
}
