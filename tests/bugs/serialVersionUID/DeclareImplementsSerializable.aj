import java.io.Serializable;

public aspect DeclareImplementsSerializable {

	declare parents : NonSerializableTest implements Serializable;

}
