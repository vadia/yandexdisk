package org.vadel.googledrive;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.vadel.common.Helpers;

public class DriveFile {

	public static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";
	
	public String id;
	public String selfLink;
	public String iconLink;
	public String title;
	public String mimeType;
	public String pictureUrl;
	public String downloadUrl;
	public long fileSize;
	
	public long createdDate;
	public long modifiedDate;
	
	public ArrayList<ParentLink> parents = new ArrayList<DriveFile.ParentLink>();

	public DriveFile(JSONObject obj) throws JSONException {
		this.id = obj.getString("id");
		this.selfLink = obj.getString("selfLink");
		this.iconLink = obj.getString("iconLink");
		this.title = obj.getString("title");
		this.mimeType = obj.getString("mimeType");
		if (obj.has("picture"))
			this.pictureUrl = obj.getJSONObject("picture").getString("url");
		if (obj.has("downloadUrl"))
			this.downloadUrl = obj.getString("downloadUrl");
		if (obj.has("fileSize"))
			this.fileSize = obj.getLong("fileSize");

		this.createdDate = Helpers.parseAtomDate(obj.getString("createdDate"));
		this.modifiedDate = Helpers.parseAtomDate(obj.getString("modifiedDate"));
		if (obj.has("parents")) {
			JSONArray parents = obj.getJSONArray("parents");
			for (int i = 0; i < parents.length(); i++) {
				this.parents.add(new ParentLink(parents.getJSONObject(i)));
			}
		}
	}
	
	public boolean isFolder() {
		return mimeType.equals(MIME_TYPE_FOLDER);
	}
	
	public static class ParentLink {
		public String id;
		public String selfLink;
		public String parentLink;
		public boolean isRoot;
		
		public ParentLink(JSONObject obj) throws JSONException {
			this.id = obj.getString("id");
			this.selfLink = obj.getString("selfLink");
			this.parentLink = obj.getString("parentLink");
			this.isRoot = obj.getBoolean("isRoot");
		}
	}
}