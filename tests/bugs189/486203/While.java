import java.util.function.Consumer;

class While {
	void m() {
		t(Long.class, value -> {
			int x = 1;
			while (--x >= 0)
				;
		});
	}

	<T> void t(Class<T> clazz, Consumer<T> object) {
	}
}
