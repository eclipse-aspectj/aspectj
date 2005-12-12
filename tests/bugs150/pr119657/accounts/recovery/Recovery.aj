package accounts.recovery;

import services.stockquote.StockQuoteService;

public aspect Recovery {

	declare precedence : Recovery, *;
	
	Object around () : call(public * *(..)) && target(StockQuoteService) {
		System.out.println("Recovery.around() " + thisJoinPoint);
		return proceed();
	}
}
