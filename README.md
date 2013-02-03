YandexDisk API / Яндекс Диск API
==========

Реализация Яндекс Диск API. Минимум зависимостей, совместим с Android. 
Android compatible.

*Basic Authorization*

	YandexDiskApi api = new YandexDiskApi(CLIENT_ID);
	api.setCredentials(LOGIN, PASSW);
	api.createFolder(TEST_DIR);

*OAuth Authorization for Android*

	YandexDiskApi api = new YandexDiskApi(CLIENT_ID);
	
	...
	String authUri = api.getOAthRequestUrl();
	// Открываем окно браузера для аутентификации пользователя
	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUri));
	intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
	activity.startActivity(intent);

	<!-- Прописываем Activity в AndroidManifest.xml для перехвата CallBack'а с AccessToken -->
	<activity android:name=".CallBackActivity" >
		<intent-filter>
            <data android:scheme="yandex-disk-test-app" />
		    
            <action android:name="android.intent.action.VIEW" />
            <category android:name="android.intent.category.BROWSABLE" />
		    <category android:name="android.intent.category.DEFAULT" />
	    </intent-filter>
    </activity>


	public class CallBackActivity extends Activity {

		static final String YANDEX_REDIRECT_SCHEME = "yandex-disk-test-app";
		static final String YANDEX_REDIRECT_URI = YANDEX_REDIRECT_SCHEME + "://callback";

		@Override
		protected void onResume() {
			super.onResume();
			boolean logined = false;
			Uri uri = this.getIntent().getData();
			if (uri != null && uri.getScheme().equals(YANDEX_REDIRECT_SCHEME)) {
				api.setTokenFromCallBackURI(uri.toString());
			}
		}
	}