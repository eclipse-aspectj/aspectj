// trying to put two annotations onto one method and two on one ctor - should both be errors
public aspect TwoOnOneMember {
  declare @method: public void m1() : @Colored("red");
  declare @method: public void m1() : @Colored("blue");
  declare @constructor: new(int) : @Colored("red");
  declare @constructor: new(int) : @Colored("blue");
}

