public abstract class Modifiers {
    public private void foo1() { }
    public protected void foo2() { }
    protected private void foo4() { }

    abstract void foo6() { }
    abstract static void foo7();
    abstract synchronized void foo8();
    abstract private void foo9();

    abstract strictfp void foo10();

    abstract static class A { }
}
