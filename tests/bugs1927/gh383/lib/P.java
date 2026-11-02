package lib;

abstract class P implements I {
	public <T> T create(Class<T> type, Object source) { return null; }
	public <T> T create(Class<T> type) { return null; }
}
