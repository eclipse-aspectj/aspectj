import java.util.*;

// Complex ITDM
public class Parse1 { }

aspect X {
  <T> Parse1.sort(List<T> elements,Comparator<? super T> comparator) {}
}
