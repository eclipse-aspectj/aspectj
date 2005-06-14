class Normal { int basicField;}
class Generic<T> { int basicField;}

aspect Injector {
  void Normal.method() {}
  void Generic.method() {}
  int Normal.itdField;
  int Generic.itdField;

  void test() {
    new Normal().method();
    new Generic<Integer>().method(); 
	
    int n1     = new Normal().basicField;
    int normal = new Normal().itdField;

    int a = new Generic<Integer>().basicField; 
    int b = new Generic<Integer>().itdField; 
    int c = new Generic().basicField;
    int d = new Generic().itdField; 
  }
}
