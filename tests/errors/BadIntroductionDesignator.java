package errors;

// PR#129

public aspect BadIntroductionDesignator {
	public boolean (M||).m_foo;
}

class M {}
