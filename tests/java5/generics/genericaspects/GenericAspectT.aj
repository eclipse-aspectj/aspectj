import java.util.*;
import java.lang.reflect.*;
import org.aspectj.lang.annotation.*;

aspect ParentChildRelationship {

  interface I<P>{}

  public String I.parent;

  public void I<T>.do(T a) {
    a.parent=null;
  }

}
