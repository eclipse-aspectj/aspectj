import java.util.*;

aspect TestA_aspect {
  // scary, multiple tvars, one from member, one from target
  public <L extends Number> void TestA_generictype<Z>.m(List<L> ll1, List<Z> lz,List<L> ll2) {} 
}
