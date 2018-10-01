public class Outer2 {
    private int i = 0;

    public static void main(String []argv) {
	    Outer2 o2 = new Outer2();
	    Inner2 i2 = o2.new Inner2();
	    System.out.println(i2.i());
    }
    
    public class Inner2 {
        public int i() {
            return i;
        }
    }

}

aspect X {
before(): execution(* Outer2.main(..)) { System.out.println("Before main()");}
before(): execution(* Outer2.Inner2.i(..)) { System.out.println("Before i()");}
}
