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

package com.datamaio.scd4j.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.datamaio.scd4j.util.PathHelper;
import com.datamaio.scd4j.util.io.PathUtils;

/**
 * 
 * @author Fernando Rubbo
 */
public class PathHelperTest {
	Map<String, Object> conf;
	
	@Before
	public void before() {
		conf = new HashMap<>();
		conf.put("all", "VAL_all");
		conf.put("begin", "VAL_begin");
		conf.put("end", "VAL_end");
		conf.put("manydirs", "a/b/c");
	}

	@Test
	public void replacePathVars() throws IOException{		
		PathHelper vpu = new PathHelper(conf, null);
		assertThat(vpu.replaceVars("/opt/jboss/@all@"), is("/opt/jboss/VAL_all"));
		assertThat(vpu.replaceVars("/opt/jboss/@all@/test"), is("/opt/jboss/VAL_all/test"));
		assertThat(vpu.replaceVars("/opt/jboss/@begin@test"), is("/opt/jboss/VAL_begintest"));
		assertThat(vpu.replaceVars("/opt/jboss/test@end@"), is("/opt/jboss/testVAL_end"));
		assertThat(vpu.replaceVars("/opt/@manydirs@/test"), is("/opt/a/b/c/test"));
	}
	
	@Test(expected=IllegalStateException.class)
	public void variableDoNotExists() throws IOException{		
		PathHelper vpu = new PathHelper(conf, null);
		assertThat(vpu.replaceVars("/opt/jboss/@NOTREPLACED@/test"), is("/opt/jboss/@NOTREPLACED@/test"));
	}

	
	@Test
	public void getTarget() throws IOException{
		Path modules = Paths.get("/location/modules/mymodule");		
		Path dir = PathUtils.get(modules, "@manydirs@/test@end@");
				
		PathHelper vpu = new PathHelper(conf, modules);
		Path result = vpu.getTarget(dir);
		assertThat(result, is(PathUtils.get("/a/b/c/testVAL_end")));		
	}
	
	@Test
	public void getTargetWithoutSuffix() throws IOException{
		Path modules = Paths.get("/location/modules/mymodule");		
		Path file = PathUtils.get(modules, "@manydirs@/test.txt.delete");
				
		PathHelper vpu = new PathHelper(conf, modules);
		Path result = vpu.getTargetWithoutSuffix(file, ".delete");
		assertThat(result, is(PathUtils.get("/a/b/c/test.txt")));		
	}
}
