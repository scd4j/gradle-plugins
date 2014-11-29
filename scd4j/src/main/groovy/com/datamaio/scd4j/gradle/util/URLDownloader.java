/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2014 scd4j scd4j.tools@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.datamaio.scd4j.gradle.util;

import org.gradle.api.Project;
import org.gradle.logging.ProgressLogger;
import org.gradle.logging.ProgressLoggerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class is used to download dependencies
 * <p>
 * Important: this implementation is strongly based on Michel Kraemer's
 * <a href="http://github.com/michel-kraemer/gradle-download-task/">gradle-download-task</a> plugin 
 * 
 * 
 * @author Fernando Rubbo
 */
public class URLDownloader {
	private static final String DEST_DIR = System.getProperty("user.home") + "/.scd4j/cache/url";

	private ProgressLogger pLogger;
	private String size;
	private long processedBytes = 0;
	private long loggedKiloBytes = 0;

	public File download(String u, Project project) throws IOException {
		URL url = new URL(u);
		initProgressLogger(url, project);

		File destFile = getDestination(url);
		long timestamp = getLastModified(destFile);

		URLConnection conn = openConnection(url, timestamp);
		if (conn == null)
			return destFile; // was not modified

		size = getSize(conn);
		InputStream is = conn.getInputStream();
		try {
			startProgress();
			OutputStream os = new FileOutputStream(destFile);

			boolean finished = false;
			try {
				byte[] buf = new byte[1024 * 10];
				int read;
				while ((read = is.read(buf)) >= 0) {
					os.write(buf, 0, read);
					processedBytes += read;
					logProgress();
				}

				os.flush();
				finished = true;
			} finally {
				os.close();
				if (!finished) {
					destFile.delete();
				}
			}
		} finally {
			is.close();
			completeProgress();
		}

		setLastModified(destFile, conn);
		return destFile;
	}

	private File getDestination(URL url) {
		String name = url.toString();
		if (name.endsWith("/")) {
			name = name.substring(0, name.length() - 1);
		}
		name = name.substring(name.lastIndexOf('/') + 1);

		File destFile = new File(DEST_DIR, name);
		if (!destFile.exists()) {
			destFile.getParentFile().mkdirs();
		}
		return destFile;
	}

	private long getLastModified(File destFile) {
		long timestamp = 0;
		if (destFile.exists()) {
			timestamp = destFile.lastModified();
		}
		return timestamp;
	}

	private String getSize(URLConnection conn) {
		long contentLength = parseContentLength(conn);
		if (contentLength >= 0) {
			return toLengthText(contentLength);
		}
		return null;
	}

	private void setLastModified(File destFile, URLConnection conn) {
		long newTimestamp = conn.getLastModified();
		if (newTimestamp > 0) {
			destFile.setLastModified(newTimestamp);
		}
	}

	private void initProgressLogger(URL url, Project project) {
		try {
			Method getServices = project.getClass().getMethod("getServices");
			Object serviceFactory = getServices.invoke(project);
			Method get = serviceFactory.getClass().getMethod("get", Class.class);
			Object progressLoggerFactory = get.invoke(serviceFactory, ProgressLoggerFactory.class);
			Method newOperation = progressLoggerFactory.getClass().getMethod("newOperation", Class.class);
			pLogger = (ProgressLogger) newOperation.invoke(progressLoggerFactory, getClass());
			String desc = "Downloading dependency from url: " + url.toString();
			pLogger.setDescription(desc);
			pLogger.setLoggingHeader(desc);			
		} catch (Exception e) {
			project.getLogger().error("Unable to get progress logger. Download progress will not be displayed.");
		}
	}

	private URLConnection openConnection(URL src, long timestamp) throws IOException {
		URLConnection uc = src.openConnection();
		if (timestamp > 0) {
			uc.setIfModifiedSince(timestamp);
		}
		if (uc instanceof HttpURLConnection) {
			HttpURLConnection httpConnection = (HttpURLConnection) uc;
			httpConnection.setInstanceFollowRedirects(true);
		}

		uc.connect();
		if (uc instanceof HttpURLConnection) {
			HttpURLConnection httpConnection = (HttpURLConnection) uc;
			int responseCode = httpConnection.getResponseCode();
			long lastModified = httpConnection.getLastModified();
			if (notModified(timestamp, responseCode, lastModified)) {
				return null;
			}
		}

		return uc;
	}

	private boolean notModified(long timestamp, int responseCode, long lastModified) {
		return responseCode == HttpURLConnection.HTTP_NOT_MODIFIED || (lastModified != 0 && timestamp >= lastModified);
	}

	private String toLengthText(long bytes) {
		if (bytes < 1024) {
			return bytes + " B";
		} else if (bytes < 1024 * 1024) {
			return (bytes / 1024) + " KB";
		} else if (bytes < 1024 * 1024 * 1024) {
			return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
		} else {
			return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
		}
	}

	private long parseContentLength(URLConnection conn) {
		String value = conn.getHeaderField("Content-Length");
		if (value == null || value.isEmpty()) {
			return -1;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private void startProgress() {
		if (pLogger == null) {
			return;
		}
		pLogger.started();
	}

	private void completeProgress() {
		if (pLogger == null) {
			return;
		}
		pLogger.completed();
	}

	private void logProgress() {
		if (pLogger == null) {
			return;
		}

		long processedKiloBytes = processedBytes / 1024;
		if (processedKiloBytes > loggedKiloBytes) {
			String msg = toLengthText(processedBytes);
			if (size != null) {
				msg += "/" + size;
			}
			msg += " downloaded";
			pLogger.progress(msg);
			loggedKiloBytes = processedKiloBytes;
		}
	}
}