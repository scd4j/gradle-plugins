package com.datamaio.junit;

import com.datamaio.junit.RunIfRule.RunIfCondition;
import com.datamaio.scd4j.cmd.CentosCommand;
import com.datamaio.scd4j.cmd.Command;

public class IsCentos implements RunIfCondition {
	@Override
	public boolean condition() {
		return Command.isLinux() && CentosCommand.DIST_NAME.equals(Command.get().distribution());
	}
}