public class StaticIntroducedReferences {
}

abstract aspect SuperA {
    static void m() {}
    static int i = 0;
}

aspect A extends SuperA {
    public static void StaticIntroducedReferences.main(String[] args) {
	i++;
	m();
    }
}
