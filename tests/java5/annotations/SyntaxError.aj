public aspect SyntaxError {

  after(Object x) : execution((@Alien *) Passenger+.new(..)) && this(x) {
	  System.out.println("after alien...");
  }

}

@interface Alien{}

class Passenger {
	
	@Alien Passenger() {}
	
}

class SubPassenger extends Passenger {
	
	@Alien SubPassenger() { super(); }
	
}