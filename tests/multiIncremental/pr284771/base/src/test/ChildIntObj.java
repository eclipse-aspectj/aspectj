package test;

public class ChildIntObj implements ChildInt {

	public void doNewTT(String o) {
		// TODO Auto-generated method stub
		
		System.out.println("ChildIntObj.doNewTT");

	}

	public void doNewXX(String o) {
		// TODO Auto-generated method stub
		System.out.println("ChildIntObj.doNewXX");
	}
	
	public static void main(String[] args) {
		new ChildIntObj().doNewTT("");
		
		new ChildIntObj().doNewXX("");
	}

}
