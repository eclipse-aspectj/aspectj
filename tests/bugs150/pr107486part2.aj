public class pr107486part2 {
    public Object f() {
        return new Object() {
            public String toString() {
                return "f";
            }
        };
    }
    public Object g() {
    	return new Object() {
    		public String toString() {
    			return "g";
    		}
    	};
    }
    
    public static void main(String[] args) {
    	pr107486part2 p = new pr107486part2();
		System.out.println(p.f());
		System.out.println(p.g());
	}
}

aspect ToStringDecorator {
	
	Object around() : execution(* toString()) {
		return new String("[advised] " + proceed()); 
	}
	
}