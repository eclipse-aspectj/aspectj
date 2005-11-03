
public class PR82570_1 {
  public static void main(String[] args) {
	new PR82570_1().m();
  }
  
  public void m() {
  	
  }
}

aspect X {
	
	
	public PR82570_1.new(String p) { this(); }
	
	public int PR82570_1.itdField;
	
	public void PR82570_1.itdMethod(String s) {
		
	}
	
	before(): call(* m(..)) {
		System.err.println("m");
	}
	
	void around(): call(* m(..)) {
		
	}
	
}

