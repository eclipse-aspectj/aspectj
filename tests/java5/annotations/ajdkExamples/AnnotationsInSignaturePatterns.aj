import org.xyz.*; import anns.*;
//import org.abc.*;
import java.util.List;

public aspect AnnotationsInSignaturePatterns {
	
	declare warning : set(@SensitiveData * *) : "@SensitiveData * *";
	declare warning : set(@SensitiveData List org.xyz..*.*) : "@SensitiveData List org.xyz..*.*";
	declare warning : set((@SensitiveData *) org.xyz..*.*) : "(@SensitiveData *) org.xyz..*.*";
	declare warning : set(@Foo (@Goo *) (@Hoo *).*) : "@Foo (@Goo *) (@Hoo *).*";
	declare warning : set(@Persisted @Classified * *) : "@Persisted @Classified * *";
	
	declare warning : execution(@Oneway * *(..)) : "@Oneway * *(..)";
	declare warning : execution(@Transaction * (@Persisted org.xyz..*).*(..)) : "@Transaction * (@Persisted org.xyz..*).*(..)";
	declare warning : execution(* *.*(@Immutable *,..)) : "* *.*(@Immutable *,..)";
	
	declare warning : within(@Secure *) : "within(@Secure *)";
	declare warning : staticinitialization(@Persisted *) : "staticinitialization(@Persisted *)";
	declare warning : call(@Oneway * *(..)) : "call(@Oneway * *(..))";
	declare warning : execution(public (@Immutable *) org.xyz..*.*(..)) : "execution(public (@Immutable *) org.xyz..*.*(..))";
	declare warning : set(@Cachable * *) : "set(@Cachable * *)";
	declare warning : handler(!@Catastrophic *) : "handler(!@Catastrophic *)";

}

@interface Foo {}
@interface Goo {}
@interface Hoo {}

class A {
	
	@SensitiveData int x = 1;
	
	@Persisted int y = 2;
	
	@Classified int z = 3;
	
	@Persisted @Classified int a = 99;
}


@Goo class G {
	
	@Oneway void g() {}
	void g2() {}
	
}
@Hoo class H {
	
	 @Foo G g = new G();
	 
	 void doIt(II i) {}
	 void doIt(II i, G g) {}
	 void doIt(II i, G g, H h) {}
	 void doIt(G g, H h) {
	 	g.g();
	 }
} 

@Immutable class II {}

@Secure class S {
	int i = 1;
}

@Persisted class P {}

@Catastrophic class NastyException extends Exception {}

class OKException extends Exception {}

class InError {

	public void ab() {
		try {
			a();
			b();
		} catch (NastyException nEx) {
			; 
		} catch (OKException okEx) {
			;
		}				
	}
	
	private void a() throws NastyException {
		throw new NastyException();
	}
	
	private void b() throws OKException {
		throw new OKException();
	}
}