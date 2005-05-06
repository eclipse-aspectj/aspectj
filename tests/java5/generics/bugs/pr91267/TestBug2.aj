import java.util.*;

public aspect TestBug2 {
    static <T> T lookupEnv(Map<String,T> env, String key) {
	return env.get(key);
    }

    public static void main(String[] argv) {
      Map<String,Integer> msi = new HashMap<String,Integer>();
      msi.put("andy",42);
      if (lookupEnv(msi,"andy")!=42) throw new RuntimeException("Failed to lookup");
    }
}
