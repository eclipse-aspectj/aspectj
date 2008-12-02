package test;

public aspect RunnableAspect {
   public void Executable.run() { execute(); }
   declare parents: Executable implements Runnable;
}

