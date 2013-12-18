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

package de.hshannover.f4.trust.irondhcp.parsing;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Entity-Class which presents a Lease with possible options
 *
 */
public class Lease {

	private String ip;
	private String mac;
	private String binding_state;
	private Calendar start;
	private Calendar end;
	private ArrayList<Option> optionen = new ArrayList<Option>();

	Lease(String ip) {
		this.ip = ip;
	}

	Lease(ArrayList<Option> opts) {
		this.optionen = opts;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public Calendar getStart() {
		return start;
	}

	public void setStart(Calendar start) {
		this.start = start;
	}

	public Calendar getEnd() {
		return end;
	}

	public void setEnd(Calendar end) {
		this.end = end;
	}

	public ArrayList<Option> getOptions() {
		return optionen;
	}

	public void addOption(Option o) {
		this.optionen.add(o);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getBindingState() {
		return binding_state;
	}

	public void setBindingState(String bindingState) {
		binding_state = bindingState;
	}

	public Lease copy() {
		Lease l = new Lease(getIp());
		l.setMac(getMac());
		l.setStart(getStart());
		l.setEnd(getEnd());
		return l;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("lease{");
		buf.append(ip);
		buf.append(", ");
		buf.append(mac);
		buf.append("}");
		return buf.toString();
	}
}
