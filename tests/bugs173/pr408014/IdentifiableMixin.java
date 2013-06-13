import org.aspectj.lang.annotation.*;

public class IdentifiableMixin implements MyAspect.IIdentifiable {
	
	private String id;
	
	public String getPlatformId() {
		return id;
	}
	
}

@Aspect
class MyAspect {
  
	public interface IIdentifiable {
		String getPlatformId();
	}

    @DeclareMixin("!is(InterfaceType) && !is(EnumType)")
    public static IIdentifiable createIIdentifiable() {
        return new IdentifiableMixin();
    }
}
