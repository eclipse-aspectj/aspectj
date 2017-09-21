public interface B extends A<String> {
	@Override
	default String getValue() {
		return "B";
	}
}
