package lib;

public aspect ExecutionMonitor {
    public interface MonitoredItem {}
    private void MonitoredItem.record(String eventType, String eventName) {}
}   