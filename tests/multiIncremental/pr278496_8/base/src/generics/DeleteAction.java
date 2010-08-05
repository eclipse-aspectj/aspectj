package generics;
import java.util.List;


public interface DeleteAction<T extends Object>{

    public void delete();
    
    public T getSelected(); 

} 
 