package org.vadel.common.yandexdisk;

import org.junit.Test;

import static junit.framework.Assert.*;

public class WebDavFileTest {

	static String DIR0 = "/";
	static String DIR1 = DIR0 + "audiobooks/";
	static String DIR2 = DIR1 + "book1/";
	
	@Test
	public void testGetParetn() {
		WebDavFile dir = new WebDavFile();
		dir.path = DIR2;
		dir.path = dir.getParentPath();
		assertEquals(DIR1, dir.path);
		dir.path = dir.getParentPath();
		assertEquals(DIR0, dir.path);
	}
	
}
