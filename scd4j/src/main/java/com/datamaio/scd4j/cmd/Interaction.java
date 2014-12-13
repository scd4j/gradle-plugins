/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2014 scd4j scd4j.tools@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.datamaio.scd4j.cmd;

import java.io.OutputStream;

/**
 * Class used for implementing programmatic interaction with a process
 * 
 * @author Fernando Rubbo
 */
public class Interaction {
	
	/**
	 * Return <code>true</code> if you would like to print the command being executed in the logs
	 */
	boolean shouldPrintCommand() {
		return true;
	}
	
	/**
	 * Return <code>true</code> if you would like to print output of the command being executed in the logs
	 */	
	boolean shouldPrintOutput() {
		return true;
	}
	
	/**
	 * Execute the interaction with the process.<br>
	 * Note: to finish command line you must enter "\n"
	 * <p>
	 * As an example, let say you would like to provide a password for a command
	 * you are going to execute:
	 * 
	 * <pre>
	 * String cmd = ..
	 * String passwd = ..
	 * run(cmd, new Interaction() \{
	 * void interact(OutputStream out) throws Exception {
	 * 	byte[] bytes = (passwd + "\n").getBytes();
	 * 	out.write(bytes);   // write once 
	 * 	out.write(bytes);   // confirm
	 * }
	 * });
	 * </pre>
	 * 
	 * @param out
	 *            where you must write your interaction. Imagine it as a command
	 *            line and you need to type into it (
	 *            {@link OutputStream#write(byte[])}) and then press ENTER
	 *            (write("\n".getBytes()))
	 */	
	void interact(OutputStream out) throws Exception {
		// default: do nothing
	}
	
	/**
	 * You should implement this method if you would like to override the
	 * meaning of a successful execution<br>
	 * 
	 * @param processReturn
	 *            the return of an execution of a process (usually, 0 means
	 *            successful)
	 * @return <code>true</code> if it was a successful execution,
	 *         <code>false</code> otherwise
	 */
	boolean isTheExecutionSuccessful(int processReturn) {
		return 0 == processReturn;
	}
}