package com.datamaio.scd4j.cmd;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.datamaio.junit.IsUbuntu;
import com.datamaio.junit.IsWindows;
import com.datamaio.junit.RunIfRule;
import com.datamaio.junit.RunIfRule.RunIf;

/**
 * <p>This class is responsable for Unit Tests on Ubuntu of the classes {@link Command} and {@link UbuntuCommand}</p>
 * 
 * @author Mateus M. da Costa
 */
public class UbuntuCommandTest {

	@Rule
	public RunIfRule rule = new RunIfRule();
	
	@Test
	@RunIf(IsUbuntu.class)
	public void get() throws Exception {
		Command command = Command.get();
		assertThat(command instanceof UbuntuCommand, is(true));
	}
	
	@Test
	@RunIf(IsUbuntu.class)
	public void distribution() {
		String distribution = Command.get().distribution();
		assertThat(distribution.equals(UbuntuCommand.DIST_NAME),  is(true));
	}
}
