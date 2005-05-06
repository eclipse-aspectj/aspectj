import java.util.*;

public aspect TestBug1 {
    static <T> void addToEnv(Map<String,T> env, String key, T value) {
	env.put(key, value);
    }

    public static void main(String[] argv) {
      Map<String,Integer> msi = new HashMap<String,Integer>();
      addToEnv(msi,"andy",new Integer(42));

      if (msi.get("andy")!=42) throw new RuntimeException("Failed to add");
    }
}

