package test;
@MyAnnotation public class MyClass {

  public String _myField;
		 
  public static void main(String[] args) {
    new MyClass().setMyField("test");
  }
		 
  public void setMyField(String nv) {
    this._myField = nv;
  }
}
