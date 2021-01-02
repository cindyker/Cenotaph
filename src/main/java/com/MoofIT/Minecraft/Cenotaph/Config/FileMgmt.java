package com.MoofIT.Minecraft.Cenotaph.Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileMgmt {
	private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private static final Lock readLock = readWriteLock.readLock();
	private static final Lock writeLock = readWriteLock.writeLock();
	
	public static File unpackResourceFile(String filePath, String resource, String defaultRes) {
		try {
			writeLock.lock();
			// open a handle to yml file
			File file = new File(filePath);

			if (file.exists())
				return file;

			String resString;

			/*
			 * create the file as it doesn't exist,
			 * or it's the default file
			 * so refresh just in case.
			 */
			checkOrCreateFile(filePath);

			// Populate a new file
			try {
				resString = convertStreamToString("/" + resource);
				FileMgmt.stringToFile(resString, filePath);

			} catch (IOException e) {
				// No resource file found
				try {
					resString = convertStreamToString("/" + defaultRes);
					FileMgmt.stringToFile(resString, filePath);
				} catch (IOException e1) {
					// Default resource not found
					e1.printStackTrace();
				}
			}

			return file;
			
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Checks a filePath to see if it exists, if it doesn't it will attempt
	 * to create the file at the designated path.
	 *
	 * @param filePath {@link String} containing a path to a file.
	 * @return True if the folder exists or if it was successfully created.
	 */
	public static boolean checkOrCreateFile(String filePath) {
		File file = new File(filePath);
		if (!checkOrCreateFolder(file.getParentFile().getPath())) {
			return false;
		}

		if (file.exists()) {
			return true;
		}

		return newFile(file);
	}
	
	public static String convertStreamToString(String name) throws IOException {
		
		try {
			readLock.lock();
			if (name != null) {
				Writer writer = new StringWriter();
				InputStream is = FileMgmt.class.getResourceAsStream(name);

				char[] buffer = new char[1024];
				try {
					Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
					int n;
					while ((n = reader.read(buffer)) != -1) {
						writer.write(buffer, 0, n);
					}
				} catch (IOException e) {
					System.out.println("Exception ");
				} finally {
					try {
						is.close();
					} catch (NullPointerException e) {
						//Failed to open a stream
						throw new IOException();
					}
				}
				return writer.toString();
			} else {
				return "";
			}
		} finally {
			readLock.unlock();
		}
	}

	public static boolean checkOrCreateFolder(String folderPath) {
		File file = new File(folderPath);
		
		if (file.exists() || file.isDirectory()) {
			return true;
		}
		
		return newDir(file);
	}

	private static boolean newDir(File dir) {
		try {
			writeLock.lock();
			return dir.mkdirs();
		} finally {
			writeLock.unlock();
		}
	}
	
	private static boolean newFile(File file) {
		try {
			writeLock.lock();
			return file.createNewFile();
		} catch (IOException e) {
			return false;
		} finally {
			writeLock.unlock();
		}
	}
	
	//writes a string to a file making all newline codes platform specific
	public static void stringToFile(String source, String FileName) {

		if (source != null) {
			// Save the string to file (*.yml)
			stringToFile(source, new File(FileName));
		}

	}

	/**
	 * Writes the contents of a string to a file.
	 *
	 * @param source String to write.
	 * @param file   File to write to.
	 */
	public static void stringToFile(String source, File file) {
		try {
			writeLock.lock();
			try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
				 BufferedWriter bufferedWriter = new BufferedWriter(osw)) {

				bufferedWriter.write(source);

			} catch (IOException e) {
				System.out.println("Exception ");
			}
		} finally {
			writeLock.unlock();
		}
	}
	
}
