public aspect A {

	before() : call(C+.new(..)) {
	}
	
}
