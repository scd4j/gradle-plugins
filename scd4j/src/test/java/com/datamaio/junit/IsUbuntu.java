package com.datamaio.junit;

import com.datamaio.junit.RunIfRule.RunIfCondition;
import com.datamaio.scd4j.cmd.Command;
import com.datamaio.scd4j.cmd.linux.debian.UbuntuCommand;

public class IsUbuntu extends IsLinux implements RunIfCondition {
	@Override
	public boolean condition() {
		return super.condition() && UbuntuCommand.DIST_NAME.equals(Command.get().distribution());
	}

}