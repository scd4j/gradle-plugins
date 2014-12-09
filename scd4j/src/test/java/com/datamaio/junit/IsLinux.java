package com.datamaio.junit;

import com.datamaio.junit.RunIfRule.RunIfCondition;

public class IsLinux implements RunIfCondition {
	@Override
	public boolean condition() {
		String os = System.getProperty("os.name");
		return os.toUpperCase().contains("LINUX");
	}

}