// Bug 53012
// Although the declare precedence mentions interfaces (and not aspects explicitly), it does
// mention with a '+' suffix - this should be allowed.

public aspect Priority {
	public interface Highest {}
	public interface Lowest {}
	declare precedence: Highest+, *, Lowest+;
}

aspect Security implements Priority.Highest {}
