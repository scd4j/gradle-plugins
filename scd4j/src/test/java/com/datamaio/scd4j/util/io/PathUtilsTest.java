package com.datamaio.scd4j.util.io;

import static com.datamaio.scd4j.util.io.PathUtils.get;
import static com.datamaio.scd4j.util.io.PathUtils.path2str;
import static java.io.File.separator;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;

import org.junit.Test;

public class PathUtilsTest {

	@Test
	public void testget(){
		assertThat(get(Paths.get("/tmp"), "test", "test", "TEST.xls").toString(), is("/tmp/test/test/TEST.xls".replaceAll("/", separator)));
		assertThat(get(Paths.get("c:\\folder"), "myfile.txt").toString(), is("c:/folder/myfile.txt".replaceAll("/", separator)));
		assertThat(get(Paths.get("D:\\folder"), "myfile.txt").toString(), is("D:/folder/myfile.txt".replaceAll("/", separator)));		
		assertThat(get(Paths.get("c:\\folder"), "c:\\myfile.txt").toString(), is("c:/folder/myfile.txt".replaceAll("/", separator)));
		assertThat(get(Paths.get("D:\\folder"), "G:\\myfile.txt").toString(), is("D:/folder/myfile.txt".replaceAll("/", separator)));
	}
	
	@Test
	public void testPath2str(){
		assertThat(path2str("\\test\\test\\TEST.xls"), is("/test/test/TEST.xls".replaceAll("/", separator)));
		assertThat(path2str("c:\\folder\\myfile.txt"), is("/folder/myfile.txt".replaceAll("/", separator)));
		assertThat(path2str("D:\\folder\\myfile.txt"), is("/folder/myfile.txt".replaceAll("/", separator)));
	}
	
}
