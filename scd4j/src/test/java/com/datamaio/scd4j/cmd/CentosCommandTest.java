package com.datamaio.scd4j.cmd;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;

import com.datamaio.junit.IsCentos;
import com.datamaio.junit.IsUbuntu;
import com.datamaio.junit.RunIfRule;
import com.datamaio.junit.RunIfRule.RunIf;


/**
 * <p>This class is responsable for Unit Tests on CentOS of the classes {@link Command} and {@link CentosCommand}</p>
 * 
 * @author Mateus M. da Costa
 */
public class CentosCommandTest {
	
	@Rule
	public RunIfRule rule = new RunIfRule();
	
	@Test
	@RunIf(IsCentos.class)
	public void get() throws Exception {
		Command command = Command.get();
		assertThat(command instanceof CentosCommand, is(true));
	}
}
