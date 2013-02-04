package org.vadel.common.yandexdisk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class YandexDiskApiTest {

	private static final int RANGE_START = 2;

	public static String CLIENT_ID;
	
	public static String LOGIN;
	public static String PASSW;
	
	public static final String TEST_DIR       = "/test-api-dir/";
	public static final String TEST_FILE      = TEST_DIR + "test-api-file";
	public static final String TEST_FILE1     = TEST_DIR + "Atest-api-file1";
	public static final String TEST_FILE2     = TEST_DIR + "Btest-api-file2";
	public static final String TEST_FILE_BODY = "some file body";

	{
		YandexDiskApi.DEBUG = true;		
	}
	
	@Test
	public void yandexApiRangeDownloadTest() throws IOException {
		if (CLIENT_ID == null && LOGIN == null && PASSW == null)
			loadFromResource();
		
		assertNotNull(CLIENT_ID);
		assertNotNull(LOGIN);
		assertNotNull(PASSW);
		YandexDiskApi api = new YandexDiskApi(CLIENT_ID);
		api.setCredentials(LOGIN, PASSW);
		api.createFolder(TEST_DIR);
		api.uploadFile(TEST_FILE, TEST_FILE_BODY);

		String s = YandexDiskApi.getStringFromStream(api.getFileStream(TEST_FILE, RANGE_START));
		System.out.println(s);
		assertEquals(s.length(), TEST_FILE_BODY.length() - RANGE_START);
	 	
		api.delete(TEST_FILE);
		api.delete(TEST_DIR);
	}	
	/*
	@Test
	public void yandexApiTest() throws IOException {
		if (CLIENT_ID == null && LOGIN == null && PASSW == null)
			loadFromResource();
		
		assertNotNull(CLIENT_ID);
		assertNotNull(LOGIN);
		assertNotNull(PASSW);
		YandexDiskApi api = new YandexDiskApi(CLIENT_ID);
		api.setCredentials(LOGIN, PASSW);
		api.createFolder(TEST_DIR);
		api.uploadFile(TEST_FILE, TEST_FILE_BODY);
	 	ArrayList<WebDavFile> files = api.getFiles(TEST_DIR);
	 	assertEquals(files.size(), 2);
	 	assertTrue("not TEST_DIR", files.get(0).path.equals(TEST_DIR));
	 	assertTrue("not TEST_FILE", files.get(1).path.equals(TEST_FILE));
//	 	api.copy(TEST_FILE, TEST_FILE1);
//	 	api.move(TEST_FILE, TEST_FILE2);
		api.delete(TEST_FILE);
		api.delete(TEST_DIR);
	}*/
	
	void loadFromResource() throws IOException {
		InputStream in = this.getClass().getResourceAsStream("/credentials.txt");
		if (in != null) {
			String s = YandexDiskApi.getStringFromStream(in);
			
			for (String line : s.split("\n")) {
				String[] vals = line.split(":");
				if (vals.length == 2) {
					if (vals[0].equals("client_id"))
						CLIENT_ID = vals[1];
					else if (vals[0].equals("login"))
						LOGIN = vals[1];
					else if (vals[0].equals("passw"))
						PASSW = vals[1];
				}
			}			
		}		
	}
}
