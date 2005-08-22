import java.util.*;
import java.lang.reflect.*;
import org.aspectj.lang.annotation.*;

aspect ParentChildRelationship {

  interface I<P extends I>{} // scary!

  public String I.parent;

  public void I<T>.abc(T a) {
    a.parent=null;
  }

}
