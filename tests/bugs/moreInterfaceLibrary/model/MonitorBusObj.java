package model;

import lib.ExecutionMonitor;

public aspect MonitorBusObj {
	declare parents: BusObj implements ExecutionMonitor.MonitoredItem;
}