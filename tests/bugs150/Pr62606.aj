import org.aspectj.lang.annotation.*;

public aspect Pr62606 {
	
	// xlint
	public Target.new() {}
	
	// no xlint
	public Target.new(String s) {
		this(1);
	}
	
	// no xlint
	@SuppressAjWarnings
	public Target.new(double d) {}
	
	// no xlint
	@SuppressAjWarnings({"noExplicitConstructorCall"})
	public Target.new(float f) {}
	
	// no xlint
	@SuppressAjWarnings({"adviceDidNotMatch","noExplicitConstructorCall"})
    public Target.new(short s) {}	
}

class Target {
	
	int x = 5;
	int y;
	
	public Target(int z) {
		this.y = z;
	}
	
}