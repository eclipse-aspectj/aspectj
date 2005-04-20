// "declare @Type (should be @type)"

@interface myInterface {}

aspect A{
  declare @Type: A : @myInterface;
}
