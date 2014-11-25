package com.datamaio.junit;

import com.datamaio.junit.RunIfRule.RunIfCondition;
import com.datamaio.scd4j.cmd.Command;

public class IsWindows implements RunIfCondition {
	@Override
	public boolean condition() {
		return Command.isWindows();			
	}	
}