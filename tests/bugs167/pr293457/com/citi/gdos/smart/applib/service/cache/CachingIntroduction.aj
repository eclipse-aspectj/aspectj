 package com.citi.gdos.smart.applib.service.cache;
	
	 import org.springmodules.cache.annotations.Cacheable;
	
	 public aspect CachingIntroduction {
	
	        declare @method: public * *..I*Dao+.set*(..): @Setter; 
	        declare @method: !@Setter public * *..I*Dao+.*(..):
	                @Cacheable(modelId="fooModel");
	
	 }