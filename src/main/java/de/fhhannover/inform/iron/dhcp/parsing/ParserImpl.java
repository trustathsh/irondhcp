package de.fhhannover.inform.iron.dhcp.parsing;

/*
 * #%L
 * ====================================================
 *   _____                _     ____  _____ _   _ _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \|  ___| | | | | | |
 *    | | | '__| | | / __| __|/ / _` | |_  | |_| | |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _| |  _  |  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_|   |_| |_|_| |_|
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
 * Website: http://trust.inform.fh-hannover.de/
 * 
 * This file is part of irondhcp, version 0.3.0, implemented by the Trust@FHH 
 * research group at the Fachhochschule Hannover.
 * %%
 * Copyright (C) 2010 - 2013 Trust@FHH
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


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.fhhannover.inform.iron.dhcp.util.DateUtil;

public class ParserImpl extends Parser {

	private final String mRegexLease = "lease\\s([01]?\\d\\d?|2[0-4]\\d|25[0-5])"
			+ "\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\s\\{(.+?)\\}";

	private ArrayList<Lease> mLeases;

	/**
	 * Parses the </i>dhcpd.leases</i> file and creates lease objects</br></br>
	 * <b>NOTE: </b>At this time only necessesary options for the IF-MAP
	 * ip-mac-binding are parsed. Besides the ip-address, this are</br>
	 * <ul>
	 * <li>starts</li>
	 * <li>ends</li>
	 * <li>hardware ethernet</li>
	 * <li>binding state</li>
	 * </ul>
	 * In future, maybe there will be more options parsed.
	 * 
	 * @param content the <i>dhpd.leases</i> content as {@link ArrayList<Lease>}
	 * @return a {@link Lease} or an empty {@link ArrayList<Lease>} if parsing fails
	 */
	@Override
	public ArrayList<Lease> parseString(String txt) {
		mLeases = new ArrayList<Lease>();
		if (txt != null) {
			Pattern pattern = Pattern.compile(mRegexLease, Pattern.DOTALL);
			Matcher matcher = pattern.matcher(txt);
			while (matcher.find()) {
				Lease lease = parseLeaseContent(matcher.group());
				if (lease != null) {
					mLeases.add(lease);
				}
			}
		}
		return mLeases;
	}

	/**
	 * Parses the options for a given lease
	 * 
	 * @param content
	 * @return a {@link Lease} or null if parsing fails
	 */

	private Lease parseLeaseContent(String content) {
		String ip = null;
		// we split the content after each linebreak
		String[] lines = content.split("\\r|\\n");
		// get the ip
		ip = lines[0].split("\\s")[1];
		if (ip != null) {
			Lease lease = new Lease(ip);
			for (int i = 1; i < lines.length - 1; i++) {				
				String _tmp = lines[i].trim();
				if(_tmp == null || _tmp.equals("")) {
					continue;
				}
				_tmp = _tmp.replaceAll("\\n","");
				_tmp = _tmp.replaceAll("\\r","");
				lines[i] = _tmp.substring(0, _tmp.indexOf(';'));
				String[] tokens = lines[i].trim().split("\\s");
				String singleKey = tokens[0];
				String dualKey = tokens[0] + " " + tokens[1];

				// start and end time
				if (singleKey.equals("starts") || singleKey.equals("ends")) {
					Date date = null;
					if(tokens[1].equalsIgnoreCase("never")) {
						continue;
					}					
					try {
						date = DateUtil.getDateFormatDHCP().parse(
								tokens[2] + " " + tokens[3]);
					} catch (ParseException e) {						
					}
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					if (singleKey.equals("starts")) {
						lease.setStart(cal);
					} else {
						lease.setEnd(cal);
					}
				}
				// mac address
				else if (dualKey.equals("hardware ethernet")) {
					if (tokens[2] != null) {
						lease.setMac(tokens[2]);
					} else
						return null;
				}
				// binding state
				else if (dualKey.equals("binding state")) {
					if (tokens[2] != null) {
						lease.setBindingState(tokens[2]);
					} else
						return null;
				}
			}

			return lease;
		}
		return null;
	}
}
