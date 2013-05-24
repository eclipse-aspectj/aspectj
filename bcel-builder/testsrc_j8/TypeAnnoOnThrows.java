public class TypeAnnoOnThrows {
  void m() throws @Anno Exception, @Anno(2) Throwable {}
}
