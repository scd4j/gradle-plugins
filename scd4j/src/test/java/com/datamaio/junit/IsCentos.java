package com.datamaio.junit;

import com.datamaio.scd4j.cmd.CentosCommand;
import com.datamaio.scd4j.cmd.Command;

public class IsCentos extends IsLinux {
	@Override
	public boolean condition() {
		return super.condition() && CentosCommand.DIST_NAME.equals(Command.get().distribution());
	}
}