package org.vadel.common.yandexdisk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.Test;
import org.vadel.yandexdisk.YandexDiskApi;
import org.vadel.yandexdisk.webdav.WebDavFile;

public class YandexDiskApiTest {

	private static final int RANGE_START = 2;

	public static String CLIENT_ID;
	
	/*
	 * 1. Register app with callback uri = https://oauth.yandex.ru/verification_code?dev=true
	 * 2. https://oauth.yandex.ru/authorize?response_type=token&client_id=<your_client_id>
	 * 3. You will redirect to https://oauth.yandex.ru/verification_code?dev=True#access_token=<your_new_access_token>
	 */
	public static String TOKEN;
//	public static String LOGIN;
//	public static String PASSW;
//	
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
		if (CLIENT_ID == null && TOKEN == null)// && LOGIN == null && PASSW == null)
			loadFromResource();
		
		assertNotNull(CLIENT_ID);
		assertNotNull(TOKEN);
		
//		CLIENT_ID = "";
//		TOKEN = "e0756fd1fa0746de9e7859e54a5a3937";
//		String yandexToken =  "e0756fd1fa0746de9e7859e54a5a3937";
//		int sz = YandexDiskApi.getFiles("OAuth " + yandexToken, "/audiobooks").size();
//		String url = YandexDiskApi.getDownloadUrl("OAuth " + yandexToken, "/133336_1600x1200.jpg"); 
//		System.out.println("Download url: " + url + ", " + sz);
//		assertNotNull(LOGIN);
//		assertNotNull(PASSW);
		YandexDiskApi api = new YandexDiskApi(CLIENT_ID);
//		api.setCredentials(LOGIN, PASSW);
		api.setToken(TOKEN);
		
//		ArrayList<WebDavFile> files = YandexDiskApi.getFiles(api.getAuthorization(),
//				"/audiobooks/Chuck Palahniuk - Rant/");
//		Assert.assertTrue(files != null && files.size() > 0);
//		String path = URLEncoder.encode("/audiobooks/Василий Ян – Чингисхан/", "utf-8");
//		files = YandexDiskApi.getFiles(api.getAuthorization(),
//				"/audiobooks/Василий Ян – Чингисхан/");
//		Assert.assertTrue(files != null && files.size() > 0);
		
		
//		assertEquals(api.getUserLogin(), LOGIN);
		api.createFolder(TEST_DIR);
		api.uploadFile(TEST_FILE, TEST_FILE_BODY);

		String downloadUri = api.getDownloadUrl(TEST_FILE);
		assertNotNull(downloadUri);
		System.out.println("Download url: " + downloadUri);
		
		ArrayList<WebDavFile> files = api.getFiles(TEST_FILE);
		System.out.println("files: " + files.size());
		
		String s = api.getFileString(TEST_FILE, RANGE_START);
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
					else if (vals[0].equals("token"))
						TOKEN = vals[1];
//					else if (vals[0].equals("login"))
//						LOGIN = vals[1];
//					else if (vals[0].equals("passw"))
//						PASSW = vals[1];
				}
			}			
		}		
	}
}
