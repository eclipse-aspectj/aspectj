package p2;

public class Derived extends p1.Base {
	public static void main(String[] args) {
		Derived d = new Derived();
		Inner i = d.new Inner();
		System.out.println(i.getFullName());		
	}
	
	class Inner {
		public String getFullName() {
			return Derived.this.getName() + ":" + getName() + ":" +
				Derived.this.value + ":" + value;
		}
	}

	static aspect A {
		before(): withincode(* get*Name(..)) { }
	}

}