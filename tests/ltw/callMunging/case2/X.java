import java.io.Serializable;

public aspect X {
  declare @type: T: @MarkerAnnotation;

  before(): call(* (@MarkerAnnotation *).m*(..)) {
	 System.out.println("advice running");
  }
}
