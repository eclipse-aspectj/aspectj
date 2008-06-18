import org.aspectj.lang.annotation.*;

@SuppressAjWarnings
public aspect UnrelatedAspect {
	  before(): call(void UnrelatedClas*.unrelatedMethod()) {
	  }
}