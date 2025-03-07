import org.aspectj.lang.annotation.SuppressAjWarnings;

public aspect H pertypewithin(G) {
	@SuppressAjWarnings("adviceDidNotMatch")
	after(): call(* *(..)) {
		System.err.println("advice running");
	}
}
