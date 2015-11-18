//import com.flickbay.orientdb.OrientKey;

class OrientKey<T> {
}

class SimpleOrientDBValue extends OrientDBValue {}
class OrientDBValue<T> {}

public aspect OrientDBKeyIO {

    public interface IO<T> {
        OrientDBValue<T> getOrientDBValue();
    }

    declare parents : OrientKey implements IO;

    public SimpleOrientDBValue OrientKey<T>.value = null;

    public OrientDBValue OrientKey<T>.getOrientDBValue() { return this.value; }

}

