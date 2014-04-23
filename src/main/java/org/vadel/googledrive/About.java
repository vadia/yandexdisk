package org.vadel.googledrive;

import org.json.JSONException;
import org.json.JSONObject;

public class About {

	public String selfLink;
	public String name;
	public String rootFolderId;
	public String displayName;
	public String pictureUrl;
	
	public About(JSONObject obj) throws JSONException {
		this.selfLink = obj.getString("selfLink");
		this.name = obj.getString("name");
		this.rootFolderId = obj.getString("rootFolderId");
		if (obj.has("user")) {
			JSONObject user = obj.getJSONObject("user");
			this.displayName = user.getString("displayName");
			if (user.has("picture"))
				this.pictureUrl = user.getJSONObject("picture").getString("url");
		}
	}
}
