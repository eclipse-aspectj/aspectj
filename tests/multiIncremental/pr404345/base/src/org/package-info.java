//@FetchProfiles({
  @FetchProfile(name = FetchProfileName.LOCATION, fetchOverrides = {
//				@FetchProfile.FetchOverride(entity = Location.class, association = Location.PROPERTY_SYSTEMSTATE),
				@FetchProfile.FetchOverride(entity = Location.class, association = Location.PROPERTY_KEYVAULT) 
})
//})
package org;


