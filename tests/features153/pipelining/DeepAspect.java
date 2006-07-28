public class DeepAspect {
  static class Inner {
     static aspect SimpleAspect {
	   before(): staticinitialization(Cl*) {
	   }
     }
  }
}
