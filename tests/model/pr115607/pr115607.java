@interface I {}

class Simple {}

public aspect pr115607 {
  declare @type: Simple : @I;
}
