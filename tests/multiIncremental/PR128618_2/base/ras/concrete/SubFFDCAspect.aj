package ras.concrete;

import ras.FFDC;

public aspect SubFFDCAspect extends FFDC {

	protected pointcut ffdcScope() : execution(* somemethod(..));

}
