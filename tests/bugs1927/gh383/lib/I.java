package lib;

public interface I {
	<T> T create(Class<T> type, Object source);
	<T> T create(Class<T> type);
}
