public class NestedFinally {
    public static void main(String[] args) {
	m(3);
    }
    public static Object m(int key) {
	try {
	    int x = 3;
	    int y = 4; 
	    int i = 5;
	    try {
		return null;
	    } finally {
		i++;
	    }
	} finally {
	    Object x = null;
	    Object y = null;
	    Object i = null;
	    key++;
	}
    }
}
