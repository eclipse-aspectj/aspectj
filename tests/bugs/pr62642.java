class C {
	  static int x = 13;
	  int y;

	  public C() {
	    y= 14;
	  }

	  public static void main(String args[]) { 
        
        	C m = new C();
        	m.y = 3;
        	System.out.println("hi");
	  }
}

public class pr62642 {
    public static void main(String[] args) {
        try {
         C.main(null);   
        } catch (ExceptionInInitializerError eiie) {
        	System.err.println("EIIE="+eiie.toString());
            //System.err.println("CAUSE="+eiie.getCause().toString());
        }
	}
}


aspect Aspect {

    before () :  within(*) && !within(pr62642) { 
        System.out.println("BEFORE "+ thisJoinPointStaticPart.getKind() +
	                         " at " + thisJoinPointStaticPart.getSourceLocation());
	}

	after ()  : within(*) && !within(pr62642)  { 
        System.out.println("AFTER " + thisJoinPointStaticPart.getKind() +
	                         " at " + thisJoinPointStaticPart.getSourceLocation());
	}
}