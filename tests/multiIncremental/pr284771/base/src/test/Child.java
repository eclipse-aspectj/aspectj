package test;

public class Child extends Parent {
	
	public static void doTT(String o){
		System.out.println("child");
	}
	
	public static void main(String[] args) {
		new Child().doTT("kkk");
	}

}
