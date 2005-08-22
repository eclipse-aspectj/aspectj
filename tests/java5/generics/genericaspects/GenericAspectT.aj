import java.util.*;
import java.lang.reflect.*;
import org.aspectj.lang.annotation.*;

aspect ParentChildRelationship {

  interface I<P extends I>{}

  public String I.parent;

  public void I<T>.abc(T a) {
    a.parent=null;
  }

}
