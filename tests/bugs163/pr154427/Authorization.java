public interface Authorization {
  boolean mayPerform(String user, String action);
}

