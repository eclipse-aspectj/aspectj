package com.blueprint.util.aspectj5.test;

import java.io.Serializable;

@javaBean()
public class Bean implements Serializable{
		
		private String name;

		public String getName() {
			return name;
		}
		@propertyChanger()
		public void setName( String name ) {
			this.name = name;
		}
}