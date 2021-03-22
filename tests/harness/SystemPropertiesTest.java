
public class SystemPropertiesTest {
    public static void main(String[] args) {        
        boolean pass = Boolean.getBoolean("PASS");
        if (!pass) {
            throw new Error("failed to get Boolean \\\"PASS\\\"");
        }
        String value = System.getProperty("name", null);
        if (!"value".equals(value)) {
            throw new Error("failed to get name=\"value\": " + value);
        }
    }
}


