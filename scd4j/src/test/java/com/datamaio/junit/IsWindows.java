package com.datamaio.junit;

import com.datamaio.junit.RunIfRule.RunIfCondition;

public class IsWindows implements RunIfCondition {
	@Override
	public boolean condition() {
		String os = System.getProperty("os.name");
		return os.toUpperCase().contains("WINDOWS");			
	}	
}