import org.aspectj.testing.Tester;

public class OverridingInterfaceObjectMethod {
	private static final int VALUE = 10;
	
	public static void main(String[] args) {
		Identifiable i = new C();
		Tester.checkEqual(i.hashCode(), 42); //System.identityHashCode(i));
		i.setId(new Id(VALUE));
		Tester.checkEqual(i.hashCode(), VALUE);
	}
}

//TODO explore complicated inheritance hierarchies

class C implements Identifiable {}

interface Base { }

interface Identifiable extends Base {
	void setId(Id id);
	Id getId();
}

class Id {
	public Id(int value) {
		this.value = value;
	}
	int value;
}
 
aspect IdentifiableAspect {
	private Id Identifiable.id = null;
	public Id Identifiable.getId() {
       return this.id;
    }
	public void Identifiable.setId(Id id) {
		this.id = id;
	}
 
	public int Identifiable.hashCode() {
		return (this.getId() == null)
			? super.hashCode()
			: this.getId().value;
	}
	
	public int Base.hashCode() {
		return 42;
	}
}
