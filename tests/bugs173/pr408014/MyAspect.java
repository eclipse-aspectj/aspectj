import org.aspectj.lang.annotation.*;

@Aspect
class MyAspect {
  
	public interface IIdentifiable {
		UUID getPlatformId();
		void setPlatformId(UUID id);
	}

    @DeclareMixin("!is(InterfaceType) && !is(EnumType)")
    public static IIdentifiable createIIdentifiable() {
        return new IdentifiableMixin();
    }
}

class UUID {}
