import java.util.*;
import java.io.*;

interface IVOList<T extends IValueObject> extends List<T>, Externalizable, Serializable {
    void updateList(List<T> newList);
}

interface IValueObject extends Comparable<IValueObject>, Serializable {

}

aspect Foo {
  @SuppressWarnings("rawtypes")
  pointcut IVOListUpdate(IVOList list):  target(list) && call(void updateList(*));
}

