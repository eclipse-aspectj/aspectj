package figures.support;

import figures.*;

public aspect HistoryUpdating {

	pointcut moves(FigureElement fe):
		this(fe) &&
		execution(void FigureElement+.set*(..));
	
	after(FigureElement fe) returning: moves(fe) {
		Canvas.updateHistory(fe);
	}
	
	declare error:
		set(private * FigureElement+.*) &&
		!(withincode(void FigureElement+.set*(..)) ||
		  withincode(FigureElement+.new(..))): 
		"doh!!!";
	
	before(int newValue): 
		set(int Point.*) && args(newValue) {
		if (newValue < 0) {
			throw new IllegalArgumentException("too small");
		} 
	}
}
