public class CircularFolding {
    static final int x = Hoo.x * 8;

    public static void main(String[] args) {
	switch (args.length) {
	case x: System.err.println("this");
	case Hoo.x: System.err.println("shouldn't");
	case Goo.x: System.err.println("compile");
	}
    }
}

class Hoo {
    static final int x = Goo.x - 3;
}

class Goo {
    static final int x = 2 + CircularFolding.x;    
}
