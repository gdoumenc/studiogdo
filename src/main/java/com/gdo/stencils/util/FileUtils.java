/**
 * Copyright GDO - 2004
 */
package com.gdo.stencils.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class FileUtils {

	public static final InputStream EMPTY_STREAM = new ByteArrayInputStream(new byte[0]);
	public static final InputStreamReader EMPTY_STREAM_READER = new InputStreamReader(EMPTY_STREAM);

	public static String CR = System.getProperty("line.separator");

	private static int BUF_SIZE = 50000;

	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[BUF_SIZE];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 * Transforms the file's name to a correct URI.
	 * 
	 * @param fileName
	 *          file's name (including path).
	 * @return the correct URI corresponding to the file's location.
	 */
	public static String toURI(String fileName) {
		try {
			String name = "";
			int index = fileName.indexOf("/");

			// no directory path
			if (index == -1)
				return URLEncoder.encode(fileName, "ISO-8859-1").replaceAll("[+]", "%20");

			// do it also on path
			String str = fileName;
			while (index != -1) {
				String first = str.substring(0, index);
				name += URLEncoder.encode(first, "ISO-8859-1").replaceAll("[+]", "%20") + "/";
				str = str.substring(index + 1);
				index = str.indexOf("/");
			}
			name += URLEncoder.encode(str, "ISO-8859-1").replaceAll("[+]", "%20");
			return name;
		} catch (UnsupportedEncodingException e) {
			return fileName;
		}
	}

}