package pack;

public class C2 {
	
}

aspect Monitor {
	pointcut pc1() : execution(* *.*(..));
    before() : pc1() {}
}
