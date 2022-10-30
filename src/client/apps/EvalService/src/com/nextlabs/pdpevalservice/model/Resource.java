package com.nextlabs.pdpevalservice.model;

public class Resource extends Category {

	public Resource(){
		//This is being declared here instead of being overriden as the class level variable due to restrictions in GSON		
		CategoryId = CATEGORYID_RESOURCE;
	}
	
}
