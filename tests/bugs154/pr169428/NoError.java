import org.aspectj.lang.annotation.*;

class ClassMissingAspectAnnotation {
	
    @DeclareParents("*")
    public java.io.Serializable s;

}