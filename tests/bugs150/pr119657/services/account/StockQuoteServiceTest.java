package services.account;

import services.accountdata.StockAccount;
import services.stockquote.StockQuoteService;
import services.stockquote.StockQuoteServiceImpl;

public class StockQuoteServiceTest {

//	private StockQuoteService stockQuoteService = new StockQuoteServiceImpl();
	private StockQuoteService stockQuoteService;
	
	public static void main (String[] args) {

		new StockQuoteServiceTest().getAccountReport("123456");

	}
	
	public AccountReport getAccountReport(String customerID) {
		StockAccount stockAccount = new StockAccount();
		stockQuoteService = new StockQuoteServiceImpl();
		float balance = (stockQuoteService.getQuote(stockAccount.getSymbol()))*stockAccount.getQuantity();
		return null;
	}


}
