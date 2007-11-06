public aspect MonitorJmxManagement {
    public interface RequestMonitorMBean extends EagerlyRegisteredManagedBean,
RequestMonitorManagementInterface {}

    declare parents: AbstractMonitor implements RequestMonitorMBean;     

    public Class RequestMonitorMBean.getManagementInterface() {
        return RequestMonitorManagementInterface.class;
    }    

}
