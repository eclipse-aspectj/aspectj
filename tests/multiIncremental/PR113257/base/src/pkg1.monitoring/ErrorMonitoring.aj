package pkg1.monitoring;

public aspect ErrorMonitoring {
	
    pointcut adviceEnabled() : isAdviceEnabled() && scope();	
	pointcut isAdviceEnabled() : if(true);
	
	pointcut scope() : within(DoMonitorErrors+) || !within(pkg1.monitoring..*);

    before(Throwable t) : args(t) && scope() && adviceEnabled() {}
	
////	before(Throwable t) :
////		args(t) && 
////		((scope() && isAdviceEnabled() &&  within(DoMonitorErrors+))
////			||  scope() && isAdviceEnabled() &&  !within(pkg1.monitoring..*)) {
////	}
//	before(Throwable t) :
//		args(t) && 
//		(scope() && ( (isAdviceEnabled() && within(DoMonitorErrors+))
//			|| (isAdviceEnabled() &&  !within(pkg1.monitoring..*)))) {
//	}
}
