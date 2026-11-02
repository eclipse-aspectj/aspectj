public aspect Logging {
	before(): execution(* Shape.describe()) {
		System.out.println("before describe");
	}
}
