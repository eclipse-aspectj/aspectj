import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;

public class NewSwitch {
  public static void main(String[] args) {
  	new NewSwitch(2);
  }

  protected NewSwitch() {
	this(3);
  }

  protected NewSwitch(int p) {
	switch (p) {
	  case 3: break;
	}
  }
}

aspect FFDC {

	pointcut initializers( ) : staticinitialization( * ) || initialization( *.new(..) );
	pointcut methodsAndConstructors( ) : execution(* *(..)) || execution(new(..) );
	pointcut guarded( ) :  initializers( ) || methodsAndConstructors( );    

	final pointcut nonStaticContext( Object o ) : this( o );

	after(Object o) throwing(Throwable t) : guarded( ) && nonStaticContext( o ) { }

}
