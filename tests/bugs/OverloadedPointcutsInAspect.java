class OverloadedPointcutsInAspect {
	public static void main(String[] args) {
		new C().run();
	}
}
class C { 
	public void run() {} 
}

aspect A {
	declare parents: C implements Runnable;
	declare parents: C implements SubRunnable;
	interface SubRunnable extends Runnable {}

	pointcut pc(Runnable r) : target(r) && call(void run());
	pointcut pc(SubRunnable r) : target(r) && call(void run());
	before(Runnable r) : pc(r) { log("pc(Runnable r)"); }
	before(SubRunnable r) : pc(r) { log("pc(SubRunnable r)"); }
	before() : pc(Runnable) { log("pc(Runnable)"); }
	before() : pc(SubRunnable) { log("pc(SubRunnable)"); }
	before() : pc(*) { log("pc(*)"); }
	void log(String s) { System.out.println(s); }
} 
