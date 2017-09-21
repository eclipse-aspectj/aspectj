package org;

public @interface FetchProfile { 
	String name();
	FetchProfile.FetchOverride[] fetchOverrides();
	
	@interface FetchOverride {
		Class<?> entity();
		String association() default "";
	}
}

//@FetchProfiles({
//@FetchProfile(name = FetchProfileName.LOCATION, fetchOverrides = {
//				@FetchProfile.FetchOverride(entity = Location.class, association = Location.PROPERTY_SYSTEMSTATE),
//				@FetchProfile.FetchOverride(entity = Location.class, association = Location.PROPERTY_KEYVAULT) }),
