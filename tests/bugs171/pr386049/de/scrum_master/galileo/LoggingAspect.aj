package de.scrum_master.galileo;

import de.scrum_master.galileo.filter.BasicFilter;

privileged aspect LoggingAspect
{
	void around(BasicFilter filter) : execution(* BasicFilter.run()) && this(filter) {
		filter.getLogMessage(); proceed(filter);
	}

}
