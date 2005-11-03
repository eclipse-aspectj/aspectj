public class pr111481 {

  public static void main(String []argv) {
    new pr111481(new Object[]{"a","b","c"});
    new pr111481("a","b","c");
  }

}
aspect A {
    public pr111481.new(Object... names) {
    	this();
    	System.out.println(names[0]);
    }

}
