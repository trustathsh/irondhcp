/*
 * #%L
 * =====================================================
 * 
 *  |_   _|_ __ _   _ ___| |_  / __ \| | | | ___ | | | |
 *    | | | '__| | | / __| __|/ / _` | |_| |/ __|| |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _  |\__ \|  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_| |_||___/|_| |_|
 *                             \____/
 * 
 * =====================================================
 * 
 * Hochschule Hannover 
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

import java.text.SimpleDateFormat;

/**
 * Simple util class for date transformations
 *
 */
public class DateUtil {

	private static SimpleDateFormat mDateFormatDHCP;
	private static SimpleDateFormat mDateFormatXSD;

	public static SimpleDateFormat getDateFormatDHCP() {
		if (mDateFormatDHCP == null) {
			mDateFormatDHCP = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		}
		return mDateFormatDHCP;
	}

	public static SimpleDateFormat getDateFormatXSD() {
		if (mDateFormatXSD == null) {
			mDateFormatXSD = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		}
		return mDateFormatXSD;
	}

	public static String fixUpTimeZone(String date) {
		int len = date.length();
		return date.substring(0, len  - 2) + ":" + date.substring(len - 2, len);
	}
}
