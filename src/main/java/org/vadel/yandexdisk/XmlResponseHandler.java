package org.vadel.yandexdisk;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.vadel.yandexdisk.webdav.WebDavFile;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlResponseHandler extends DefaultHandler {

	private static final SimpleDateFormat WEB_DAVE_DATE_FORMAT = 
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	ArrayList<WebDavFile> files;
	StringBuilder str;
	
	WebDavFile currentFile;
	
	public XmlResponseHandler() {
	}
	
	public ArrayList<WebDavFile> getResult() {
		return files;
	}
	
	@Override
	public void startDocument() {
		files = new ArrayList<WebDavFile>();
	}
	
	@Override
	public void endDocument() throws SAXException {
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		str = new StringBuilder();
		qName = qName.toLowerCase();

		if (qName.equals("d:response")) 
			currentFile = new WebDavFile();
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		qName = qName.toLowerCase();
		if (currentFile != null) {
			if (qName.equals("d:href")) {
				currentFile.path = str.toString();
			} else if (qName.equals("d:creationdate")) {
				try {
					currentFile.date = WEB_DAVE_DATE_FORMAT.parse(str.toString()).getTime();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (qName.equals("d:displayname")) {
				currentFile.name = str.toString();
			} else if (qName.equals("d:getcontentlength")) {
				currentFile.fileLength = Long.valueOf(str.toString());
			} else if (qName.equals("d:getcontenttype")) {
				currentFile.contentType = str.toString();
			} else if (qName.equals("d:collection")) {
				currentFile.isDir = true;
			}
		}
		if (qName.equals("d:response")) {
			files.add(currentFile);
			currentFile = null;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		str.append(ch, start, length);
	}
}
