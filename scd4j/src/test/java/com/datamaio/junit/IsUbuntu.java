package com.datamaio.junit;

import com.datamaio.junit.RunIfRule.RunIfCondition;
import com.datamaio.scd4j.cmd.Command;
import com.datamaio.scd4j.cmd.UbuntuCommand;

public class IsUbuntu implements RunIfCondition {
	@Override
	public boolean condition() {
		return Command.isLinux() && UbuntuCommand.DIST_NAME.equals(Command.get().distribution());
	}

}