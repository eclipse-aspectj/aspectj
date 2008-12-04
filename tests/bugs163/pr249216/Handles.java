import java.util.*;

public aspect Handles {
  public java.util.List<String> Ship.i(List<String>[][] u) {
	        return null;
  }
  public java.util.List<String> Ship.i(Set<String>[][] u) {
	        return null;
  }
  public java.util.Set<String> i(java.util.Set<String>[][] u) {
                return null;
  }
  public java.util.Set<String> i(java.util.Set<String>[][] u,int i) {
                return null;
  }
  public java.util.Set<String> i2(java.util.Set<? extends Collection<String>>[][] u) {
    return null;
  }
  public java.util.Set<String> i3(java.util.Set<? extends Collection<String[]>>[][] u) {
    return null;
  }
  public java.util.Set<String> i4(java.util.Set<? extends Collection<String>> u) {
    return null;
  }
  public java.util.Set<String> i5(java.util.Set<?> u) {
    return null;
  }
  
  
}

class Ship {}
