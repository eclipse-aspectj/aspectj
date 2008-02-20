import java.io.Serializable;

public interface IPersistable extends Serializable {

    int getId();

    void setId(int id);

}
