package psub;

import pc.C;

public class SubC extends C {
    public char[] getMyPackage() { return "psub".toCharArray(); }

    public String getRealPackage() {
	return new String(getMyPackage());
    }
}
