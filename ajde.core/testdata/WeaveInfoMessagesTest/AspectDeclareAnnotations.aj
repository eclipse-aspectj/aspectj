// little aspect and class for testing declaration of annotations

@interface anInterface {}

public aspect AspectDeclareAnnotations{

  declare @type : Test : @anInterface;
  declare @constructor : Test.new(String,int) : @anInterface;
  declare @method : int Test.fac(int) : @anInterface;
  declare @field : int Test.a : @anInterface;

}

class Test{

  public Test(String say, int something){
    System.out.println(say + something);
  }

  public int fac(int n){
   return (n == 0)? 1 : n * fac(n-1);
  }

  public int a = 1;
}
