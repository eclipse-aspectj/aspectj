
/** @testcase PR#42574 ZIP and JAR extensions in classpath and extdirs */
public class Main {
    public static void main(String[] args) {
        pack.Util.main(args);
        pack2.Util2.main(args);
    }
}