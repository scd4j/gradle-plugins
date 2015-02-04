package com.datamaio.junit;

import com.datamaio.scd4j.cmd.Command;
import com.datamaio.scd4j.cmd.linux.redhat.CentosCommand;

public class IsCentos extends IsLinux {
	@Override
	public boolean condition() {
		return super.condition() && CentosCommand.DIST_NAME.equals(Command.get().distribution());
	}
}