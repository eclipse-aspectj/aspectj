
/** @testcase PR#43972 Use class implementing interface via aspect */
public class Main implements pack.MyInterface {
	public static void main(String[] args) {
		new Main().m();
		new pack.InterfaceDefinition.C().m();
	}
	public void m() { System.out.println("hello"); }
}
