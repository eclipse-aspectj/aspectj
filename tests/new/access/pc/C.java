package pc;

public class C {
    public String getInternalPackage() {
	return getMyPackage();
    }

    String getMyPackage() { return "pc"; }
}
