// PR#457
class RecognizeAspectCE {
	public static void main(String[] ignore) { }
	pointcut mumble()  // would like error here: "pointcuts not allowed in classes - use aspect"
     : execution(public static void RecognizeAspectCE.main(String[]));
	before(): mumble() { } // ok: get error here: constructor has the wrong name
}
