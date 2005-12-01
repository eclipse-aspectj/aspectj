import java.util.*;
public interface Attributable {

  void setAttribute(String name, Object attribute);
  Object getAttribute(String name);

  static aspect DefImpl {

    private Map<String,Object> Attributable.attributes =
      new HashMap<String,Object>();

    public void Attributable.setAttribute(String name, Object attribute) {
      this.attributes.put(name, attribute);
    }

    public Object Attributable.getAttribute(String name) {
      return this.attributes.get(name);
    }
  }
}
