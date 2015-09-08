package org.vadel.yandexdisk.webdav;

import java.util.Date;

public class WebDavFile {

	public String name;
	
	public String path;
	
	public boolean isDir;

	public long date;
	
	public long fileLength;
	
	public String contentType;
	
	@Override
	public String toString() {
		return name + ":" + path + ":" + isDir + ":" + (new Date(date)) + ":" + fileLength / 1024 + ":" + contentType;
	}
	
	public String getParentPath() {
		if (path == null || path.length() == 1)
			return null;
		int i = path.lastIndexOf('/');
		if (i < 0)
			return null;
		if (i == path.length() - 1)
			i = path.lastIndexOf('/', i - 1);
		return path.substring(0, i + 1);
	}
}
