package aspect2;

import aspect1.Base;

aspect Concrete extends Base perthis(where()) {
	protected pointcut where(): call(* *..C.*(..));
}