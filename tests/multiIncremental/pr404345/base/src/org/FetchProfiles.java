package org;

public @interface FetchProfiles { 
	FetchProfile[] value();
}

//@FetchProfiles({
//@FetchProfile(name = FetchProfileName.LOCATION, fetchOverrides = {
//				@FetchProfile.FetchOverride(entity = Location.class, association = Location.PROPERTY_SYSTEMSTATE),
//				@FetchProfile.FetchOverride(entity = Location.class, association = Location.PROPERTY_KEYVAULT) }),