
/** @testcase PR#42574 ZIP and JAR extensions in classpath and extdirs */
public class Main {
    public static void main(String[] args) {
        jar.Util.main(args);
        zip.Util.main(args);
    }
}
