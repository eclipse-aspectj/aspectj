import org.xyz.*;

public aspect AnnotationPatternMatching {

	declare warning : execution(@Immutable * *(..)) : "@Immutable";
	
	declare warning : execution(!@Persistent * *(..)) : "!@Persistent";
	
	declare warning : execution(@Foo @Goo * *(..)) : "@Foo @Goo";
	
	declare warning : execution(@(Foo || Goo) * *(..)) : "@(Foo || Goo)";
	
	declare warning : execution(@(org.xyz..*) * *(..)) : "@(org.xyz..*)";
	
}

@interface Immutable {}
@interface Persistent {}
@interface Foo{}
@interface Goo{}


class Annotated {
	
	@Immutable void m1() {}
	
	@Persistent void m2() {}
	
	@Foo @Goo void m3() {}
	
	@Foo void m4() {}
	
	@OrgXYZAnnotation void m5() {}
	
}