// PR#457
class RecognizeAspectCE {
	public static void main(String[] ignore) { }
//	pointcut mumble()  
//     : execution(public static void RecognizeAspectCE.main(String[]));
	before(): this(*) { } // ok: get error here: constructor has the wrong name
}
