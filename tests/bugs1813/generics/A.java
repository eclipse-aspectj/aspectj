import java.util.List;


public interface A<T extends BaseT, I extends BaseI> {

    public T setInputs(List<I> inputs);

}