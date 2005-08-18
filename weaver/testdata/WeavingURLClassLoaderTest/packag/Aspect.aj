package packag;

public aspect Aspect {
	void around() : execution(public static void *.main(String[])) {
		// don't proceed, avoid exception
	}
}
