package sub;

public aspect ExecutionMonitor {
    public interface MonitoredItem {
        int getCount(String eventType, String eventName);
    }

    public int MonitoredItem.getCount(String eventType, String eventName) {
            return 0;
    } 
}         
