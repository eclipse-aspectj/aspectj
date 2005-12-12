package accounts.recovery;

import services.stockquote.StockQuoteService;

public aspect RecoveryNotSelf {

	Object around () : call(public * *(..)) && target(StockQuoteService) && !within(RecoveryNotSelf) {
		System.out.println("RecoveryNotSelf.around() " + thisJoinPoint);
		return proceed();
	}
}
