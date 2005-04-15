//"@Aspect extending Aspect"

// This ought to be possible, need to see where the 'can not extend' message
// is coming from and see if you can check for attributes at that point.
// not sure what would happen if these pieces were compiled separately - 
// suspect it would be OK if javac is used for class C but not if ajc is used.

import org.aspectj.lang.annotation.*;

abstract aspect B{
  abstract void say();
}

@Aspect
class C extends B{

  void say(){ }

  public static void Main(String[] args){
    C thing = new C();
    thing.say();
  }
}
