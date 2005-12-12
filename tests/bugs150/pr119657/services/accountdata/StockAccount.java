package services.accountdata;

public class StockAccount {
	private String accountNumber;

	private String symbol;

	private int quantity = 9999;

//	public StockAccount (String n) {
//		this.accountNumber  = n;
//	}
	
	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
}
