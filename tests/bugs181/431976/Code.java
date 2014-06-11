import org.aspectj.lang.annotation.SuppressAjWarnings;

privileged aspect BugThisJoinPoint {

	@SuppressAjWarnings("adviceDidNotMatch")
	void around(): execution(boolean forceFocus ()) {
		thisEnclosingJoinPointStaticPart.getSignature();
	}

	@SuppressAjWarnings("adviceDidNotMatch")
	void around(): execution(boolean forceFocus ()) {
		thisJoinPointStaticPart.getSignature();
	}
}

