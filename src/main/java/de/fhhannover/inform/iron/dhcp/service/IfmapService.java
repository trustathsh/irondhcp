package de.fhhannover.inform.iron.dhcp.service;

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
 * This file is part of irondhcp, version 0.3.1, implemented by the Trust@FHH 
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

import java.io.IOException;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.w3c.dom.Document;

import de.fhhannover.inform.iron.dhcp.parsing.Lease;
import de.fhhannover.inform.iron.dhcp.util.DateUtil;
import de.fhhannover.inform.trust.ifmapj.IfmapJ;
import de.fhhannover.inform.trust.ifmapj.IfmapJHelper;
import de.fhhannover.inform.trust.ifmapj.binding.IfmapStrings;
import de.fhhannover.inform.trust.ifmapj.channel.SSRC;
import de.fhhannover.inform.trust.ifmapj.exception.IfmapErrorResult;
import de.fhhannover.inform.trust.ifmapj.exception.IfmapException;
import de.fhhannover.inform.trust.ifmapj.exception.InitializationException;
import de.fhhannover.inform.trust.ifmapj.identifier.Identifiers;
import de.fhhannover.inform.trust.ifmapj.identifier.IpAddress;
import de.fhhannover.inform.trust.ifmapj.identifier.MacAddress;
import de.fhhannover.inform.trust.ifmapj.messages.IdentifierHolder;
import de.fhhannover.inform.trust.ifmapj.messages.MetadataLifetime;
import de.fhhannover.inform.trust.ifmapj.messages.PublishDelete;
import de.fhhannover.inform.trust.ifmapj.messages.PublishRequest;
import de.fhhannover.inform.trust.ifmapj.messages.PublishUpdate;
import de.fhhannover.inform.trust.ifmapj.messages.Requests;
import de.fhhannover.inform.trust.ifmapj.metadata.StandardIfmapMetadataFactory;

/**
 * This Class contains all methods that are necessary for the communication with
 * the IF-MAP Server
 * 
 * @version 0.01 01 Dez 2009
 * @version 0.02 08 Dez 2009
 * @version 0.03 15 Dez 2009
 * @version 0.04 17 Dez 2009
 * @version 0.05 21 Dez 2009
 * @version 0.06 22 Dez 2009
 * @version 0.07 17 Mar 2010
 * @version 0.08 30 Mar 2010
 * @version 0.09 13 Apr 2010
 * @version 0.10 13 Apr 2010
 * @version 0.11 20 Apr 2010
 * @version 0.12 27 Apr 2010
 * @version 0.13 04 May 2010
 * @version 0.14 11 May 2010
 * @version 0.15 18 May 2010
 * @version 0.16 22 Jun 2010
 * @author Sebastian Kobert
 */
public final class IfmapService {

	/**
	 * indicates whether a session is active or not.
	 */
	private volatile boolean mSessionActive;

	/**
	 * represents our SSRC to the MAPS
	 */
	private SSRC mSSRC;

	/**
	 * creates standard IF-MAP metadata, ip-mac in this case
	 */
	private StandardIfmapMetadataFactory mMetadataFactory;

	/**
	 * creates the IF-MAP requests
	 */
	// private RequestFactory mRequestFactory;

	// private IdentifierFactory mIdentifierFactory;

	/**
	 * singleton instance
	 */
	private static IfmapService mInstance;

	/**
	 * This is the private Constructor for the Singleton Pattern
	 */
	private IfmapService() {
		mMetadataFactory = IfmapJ.createStandardMetadataFactory();
		// mRequestFactory = IfmapJ.createRequestFactory();
		// mIdentifierFactory = IfmapJ.createIdentifierFactory();
	}

	/**
	 * static method "getInstance()" delivers the only Instance of this Class it
	 * is synchronized (thread-safe)
	 */
	public synchronized static IfmapService getInstance() {
		if (mInstance == null) {
			mInstance = new IfmapService();
		}
		return mInstance;
	}

	/**
	 * This Method opens a new Connection to the IF-MAP Server
	 * 
	 * @throws InitializationException
	 * @throws BindingException
	 * @throws IfmapErrorResult
	 * @throws IOException
	 * 
	 */
	public void prepareSSRC() throws InitializationException {
		if (mSSRC == null) {
			TrustManager[] tms = IfmapJHelper.getTrustManagers(
					ConfigService.getTrustStoreFile(),
					ConfigService.getTruststorePassword());

			if (ConfigService.getBasicAuthEnabled()) {
				mSSRC = IfmapJ.createSSRC(ConfigService.getServerUrl(),
						ConfigService.getBasicAuthUser(),
						ConfigService.getBasicAuthPassword(), tms);
			} else {
				KeyManager[] kms = IfmapJHelper.getKeyManagers(
						ConfigService.getKeystoreFile(),
						ConfigService.getKeystorePassword());
				mSSRC = IfmapJ.createSSRC(ConfigService.getServerUrl(), kms,
						tms);
			}
		}
	}

	/**
	 * Closes an Ifmap-Session
	 * 
	 * @throws IfmapErrorResult
	 * @throws IfmapException
	 */
	public void endSession() throws IfmapErrorResult, IfmapException {
		if (!mSessionActive || mSSRC == null) {
			return;
		}
		mSSRC.endSession();
		mSessionActive = false;
	}

	/**
	 * Starts a new Ifmap-Session
	 * 
	 * @throws IfmapErrorResult
	 * @throws IfmapException
	 */
	public void newSession() throws IfmapErrorResult, IfmapException {
		if (!mSessionActive && mSSRC != null) {
			mSSRC.newSession();
			mSessionActive = true;
		}
	}

	/**
	 * Purges all published metadata
	 * 
	 * @throws IfmapErrorResult
	 * @throws IfmapException
	 */
	public void purgePublisher() throws IfmapErrorResult, IfmapException {
		if (mSSRC != null && mSessionActive)
			mSSRC.purgePublisher();
	}

	/**
	 * Deletes metadata of expired leases
	 * 
	 * @param delLeases
	 * @throws IfmapErrorResult
	 * @throws IfmapException
	 */
	public void publishDelete(List<Lease> delLeases) throws IfmapErrorResult,
			IfmapException {
		PublishRequest req = Requests.createPublishReq();
		for (Lease del : delLeases) {
			PublishDelete pdel = Requests.createPublishDelete();
			fillWithIpMacIdentifier(pdel, del);
			// why prfx? because i want to check if namespace declaration
			// functionality in IfmapJ is working ;)
			pdel.setFilter("prfx:ip-mac[@ifmap-publisher-id=\""
					+ mSSRC.getPublisherId() + "\" and dhcp-server=\""
					+ ConfigService.getDhcpdIp() + "\"]");
			// System.out.println(pd.getFilter());
			pdel.addNamespaceDeclaration("prfx",
					IfmapStrings.STD_METADATA_NS_URI);
			req.addPublishElement(pdel);
		}
		mSSRC.publish(req);
	}

	/**
	 * Publishes metadata of new or updated leases
	 * 
	 * @param newLeases
	 * @throws IfmapErrorResult
	 * @throws IfmapException
	 */
	public void publishUpdate(List<Lease> newLeases) throws IfmapErrorResult,
			IfmapException {
		// PublishRequest pr = mRequestFactory.createPublishReq();
		PublishRequest req = Requests.createPublishReq();
		String start = null;
		String end = null;
		String dhcpserver = ConfigService.getDhcpdIp();
		Document ipMac;
		for (Lease lease : newLeases) {
			// pu = mRequestFactory.createPublishUpdate();
			PublishUpdate pub = Requests.createPublishUpdate();
			pub.setLifeTime(MetadataLifetime.session);

			// we need the dates in xml-schema format <xsd:dateTime>:
			// YYYY-MM-DDThh:mm:ss
			if(lease.getStart() != null) {
				start = DateUtil.getDateFormatXSD().format(
						lease.getStart().getTime());
				start = (start != null) ? DateUtil.fixUpTimeZone(start) : null;
			}
			else {
				start = "never";
			}
			
			if(lease.getEnd() != null) {
				end = DateUtil.getDateFormatXSD().format(lease.getEnd().getTime());
				end = (end != null) ? DateUtil.fixUpTimeZone(end) : null;
			}
			else {
				end = "never";
			}
			
			ipMac = mMetadataFactory.createIpMac(start, end, dhcpserver);
			fillWithIpMacIdentifier(pub, lease);
			pub.addMetadata(ipMac);
			req.addPublishElement(pub);
		}
		mSSRC.publish(req);
	}

	/**
	 * Helper to easily create the links to be published
	 * 
	 * FIXME: Refactor the IdentifierFactory, so we shouldn't really need this
	 * 
	 * @param el
	 * @param lease
	 */
	private void fillWithIpMacIdentifier(IdentifierHolder el, Lease lease) {
		if (el == null || lease == null) {
			throw new NullPointerException();
		}
		IpAddress ip = Identifiers.createIp4(lease.getIp());
		MacAddress mac = Identifiers.createMac(lease.getMac());
		el.setIdentifier1(ip);
		el.setIdentifier2(mac);
	}

	public String getPublisherId() {
		if (mSSRC == null || !mSessionActive)
			return "[NOT CONNECTED]";

		return mSSRC.getPublisherId();
	}
}
