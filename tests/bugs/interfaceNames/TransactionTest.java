import sub.ExecutionMonitor;
import sub.ObserverProtocol;


public class TransactionTest {
	public static void main(String[] args) {
	}
	
    static Transaction theTransaction;

    private void assertCommitted() {
        theTransaction.getCount("method-execution", "commit");
    }

    static aspect MonitorTest {
        declare parents: Transaction implements ExecutionMonitor.MonitoredItem;
    }
}

class Transaction {
}

aspect TransactionControl {
    void begin() {
        CommitObserver.aspectOf().add(this);
    }
    static aspect CommitObserver extends ObserverProtocol {
        declare parents: TransactionControl implements Observer;
    }
}
