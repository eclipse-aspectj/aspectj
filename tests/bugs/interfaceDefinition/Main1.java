
/** @testcase PR#43972 Use class implementing interface via aspect */
public class Main1 {
	public static void main(String[] args) {
        pack.MyInterface i = new pack.InterfaceDefinition.C();
        i.m();
	}
}
