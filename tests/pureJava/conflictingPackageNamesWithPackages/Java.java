package conflictingPackageNamesWithPackages;

public class Java {
    public String lang() {
        throw new RuntimeException("String lang(): shouldn't have been called");
    }

    public static class lang {
        public static class String {
            public String() {
                throw new RuntimeException("new String(): shouldn't have been called");
            }
        }
        static String String() { return null; }
    }
}
