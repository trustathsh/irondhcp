/*
 * #%L
 * =====================================================
 *   _____                _     ____  _   _       _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \| | | | ___ | | | |
 *    | | | '__| | | / __| __|/ / _` | |_| |/ __|| |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _  |\__ \|  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_| |_||___/|_| |_|
 *                             \____/
 * 
 * =====================================================
 * 
 * Fachhochschule Hannover 
 * (University of Applied Sciences and Arts, Hannover)
 * Faculty IV, Dept. of Computer Science
 * Ricklinger Stadtweg 118, 30459 Hannover, Germany
 * 
 * Email: trust@f4-i.fh-hannover.de
 * Website: http://trust.f4.hs-hannover.de/
 * 
 * This file is part of irondhcp, version 0.3.2, implemented by the Trust@HsH 
 * research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2010 - 2013 Trust@HsH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package de.hshannover.f4.trust.irondhcp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Observable;

/**
 * Monitors a file for changes in a given intervall.
 */
public class FileWatcher extends Observable implements Runnable {

	private String mPath;
	private long mMotified = 0;
	private int mIntervall = 1;

	/**
	 * Initializes the FileWatcher
	 * 
	 * @param mPath the file that will be observed
	 */
	public FileWatcher(String path) {
		this.mPath = path;
	}

	/**
	 * Time between checks
	 * 
	 * @return time in seconds
	 */
	public int getIntervall() {
		return mIntervall;
	}

	/**
	 * Sets up the time between ckecks
	 * 
	 * @param the time in seconds
	 */
	public void setIntervall(int sec) {
		mIntervall = sec;
	}

	/**
	 * Watching periodically for file changes. If changes are detected the
	 * observers are notified.
	 */
	@Override
	public void run() {
		long tmp = 0;
		while (true) {
			try {
				do {
					tmp = this.getLastModified(mPath);
					if (mMotified == 0) {
						mMotified = tmp;
						setChanged();
						notifyObservers();
					}
					Thread.sleep(mIntervall * 1000);
				} while (mMotified == tmp);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			setChanged();
			notifyObservers();
			mMotified = tmp;
		}
	}

	/**
	 * Get file content as String
	 * 
	 * @return
	 */
	public String getFileAsString() {
		StringBuilder builder = new StringBuilder(1024);
		BufferedReader reader = getFile();
		if (reader == null) {
			return null;
		}
		char[] buffer = new char[1024];

		try {
			while (reader.read(buffer) != -1) {
				builder.append(buffer);
			}
			reader.close();
			reader = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	/**
	 * Delivers the time the file is last motified
	 * 
	 * @param mPath path to file
	 * @return timestamp the file was last motified
	 */
	private long getLastModified(String path) {
		File f = new File(path);
		long last = f.lastModified();
		f = null;
		return last;
	}

	/**
	 * Creates a BufferedReader for reading the file contents. If something goes
	 * wrong, e.g. file is not there, an exception is thrown. After 10 seconds
	 * the method will be resumed.
	 */
	private BufferedReader getFile() {
		BufferedReader file = null;
		try {
			file = new BufferedReader(new FileReader(this.mPath));
		} catch (Exception e) {
			System.out.println("[irondhcp] Something wrong with reading "
					+ this.mPath + ". Continuing in 10 seconds.");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		return file;
	}
}
