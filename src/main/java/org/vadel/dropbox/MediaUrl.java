package org.vadel.dropbox;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class MediaUrl {

	public String url;
	public Date expires;
	
	public static MediaUrl fromMediaUrl(String s) {
		try {
			return fromMediaUrl(new JSONObject(s));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "{ " + url + ", " + expires + " }";
	}
	
	@SuppressWarnings("deprecation")
	public static MediaUrl fromMediaUrl(JSONObject obj) {
		try {
			MediaUrl media = new MediaUrl();
			media.url = obj.getString("url");
			media.expires = new Date(obj.getString("expires"));
			return media;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
