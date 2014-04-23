package org.vadel.dropbox;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.vadel.common.Helpers;

public class DropFile {

	public String rev;
	public String hash;
	public boolean thumbExists;
	public long bytes;
	public Date modified;
	public Date clientMTime;
	public String path;
	public String name;
	public boolean isDir;
	public boolean isDeleted;
	public String icon;
	public String root;
	public String mimeType;
	public String revision;
	
	public ArrayList<DropFile> contents;
	
	public String getParent() {
		if (root == null || path == null)
			return null;
		
		if (root.equals(path) || "/".equals(path)) 
			return null;
		
		int start = path.indexOf('/');
		int end   = path.lastIndexOf('/');
		int len   = path.length();
		if (start == end)
			return null;
		if (end == len - 1)
			end = path.lastIndexOf('/', end - 1);
		return path.substring(0, end);
	}
	
	@Override
	public String toString() {
		return "{ " + (isDir ? "dir" : "file") + ": " + path + " - " + bytes + " }";
	}
	
	public static DropFile fromJSON(String s) {
		try {
			return fromJSON(new JSONObject(s));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static DropFile fromJSON(JSONObject obj) {
		try {
			DropFile file = new DropFile();
			file.path = obj.getString("path");
			file.name = Helpers.getPageName(file.path);
			file.isDir = obj.getBoolean("is_dir");
			if (obj.has("is_deleted"))
				file.isDeleted = obj.getBoolean("is_deleted");
			file.icon = obj.getString("icon");
			file.root = obj.getString("root");
			if (!file.path.equals("/")) {
				file.thumbExists = obj.getBoolean("thumb_exists");
				file.bytes = obj.getLong("bytes");
				
				file.modified = new Date(obj.getString("modified"));
				if (obj.has("revision"))
					file.revision = obj.getString("revision");
				if (obj.has("rev"))
					file.rev = obj.getString("rev");
				if (obj.has("hash"))
					file.hash = obj.getString("hash");
				if (obj.has("mime_type"))
					file.mimeType = obj.getString("mime_type");
				if (obj.has("client_mtime"))
					file.clientMTime = new Date(obj.getString("client_mtime"));
			}
			if (obj.has("contents")) {
				file.contents = new ArrayList<DropFile>();
				JSONArray items = obj.getJSONArray("contents");
				for (int i = 0; i < items.length(); i++) {
					DropFile f = fromJSON(items.getJSONObject(i));
					if (f != null)
						file.contents.add(f);
				}
			}
			return file;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
