@interface A {}

aspect Test {
        declare @field : @A int var* : @A;
        declare @field : int var* : @A;

        interface Subject {}

        public int Subject.vara;
        public int Subject.varb;
}

class X implements Test.Subject {
}