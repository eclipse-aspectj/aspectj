public class Main {

	  private static class Foo {
	    int x;
	    Foo(int x) { this.x = x; }
	  };

	  private static int foo(int x) { return x+1; }

	  public static void main (String args[])
	    {  Main.foo(1);
	       new Foo(2);
	    }

	}

	 aspect Aspect {
	  // calls to a private method
	  before () : call(* foo(..))
	   { System.out.println("Matches * foo(..)");
	   }

	  before () : call(int foo(int))
	   { System.out.println("Matches int foo(int)");
	   }

	   before () : call(private * foo(..))
	   { System.out.println("Matches private * foo(..)");
	   }

	   before () : call(* foo*(..))
	   { System.out.println("Matches * foo*(..)");
	   }

	   // calls to a constructor that is in a private inner class
	  before () : call(Main.Foo.new(..))  // <- warning from here
	   { System.out.println("Matches Main.Foo.new(..)");
	   }

	  before () : call(Main.Foo*.new(..))
	   { System.out.println("Matches Main.Foo*.new(..)");
	   }

	}