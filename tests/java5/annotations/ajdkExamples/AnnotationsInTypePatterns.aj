import org.xyz.*; import anns.*;
import org.abc.*;
import java.lang.annotation.Inherited;

public aspect AnnotationsInTypePatterns {
	
	declare warning : staticinitialization(@Immutable *) : "(@Immutable *)";
	
	declare warning : staticinitialization(!@Immutable *) : "(!@Immutable *)";
	
	declare warning : staticinitialization(@Immutable (org.xyz.* || org.abc.*)) : "@Immutable (org.xyz.* || org.abc.*)";

	declare warning : staticinitialization((@Immutable Foo+) || Goo) : "((@Immutable Foo+) || Goo)";

	declare warning : staticinitialization(@(Immutable || NonPersistent) org.xyz..*) : "@(Immutable || NonPersistent) org.xyz..*";

	declare warning : staticinitialization(@Immutable @NonPersistent org.xyz..*) : "@Immutable @NonPersistent org.xyz..*";

	declare warning : staticinitialization(@(@Inherited *) org.xyz..*) : "@(@Inherited *) org.xyz..*";
}

@Immutable
class A {}

class B {}

class Goo {}

class Foo {}

@Immutable
class SubFoo extends Foo {}