import java.util.List;

public aspect TestAspect {
	private pointcut inTest(): within(TestComp);
	
	private pointcut inAdd(BaseModel m): inTest() &&
		execution(public BaseModel+ BaseComp+.add*(BaseModel+)) &&
		args(m);
	
	private pointcut inGetSearchByObj(BaseModel m): inTest() &&
		(execution(public * BaseComp+.get*(BaseModel+)) ||
		execution(public * BaseComp+.search*(BaseModel+))) &&
		args(m);

	private pointcut inGrate():
	(execution(public * BaseComp+.get*(BaseModel+)) ||
	execution(public * BaseComp+.search*(BaseModel+)));
	
	private pointcut inUpdate(BaseModel m): inTest() &&
		execution(public * BaseComp+.*(BaseModel+)) &&
		args(m) && !inAdd(BaseModel) && !inGrate();
	
	before(BaseModel m): inUpdate(m) {	}
}


abstract class BaseComp { }
abstract class BaseModel {}
class TestComp {}


