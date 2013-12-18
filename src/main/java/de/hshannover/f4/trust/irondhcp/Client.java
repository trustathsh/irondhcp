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

package de.hshannover.f4.trust.irondhcp;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.hshannover.f4.trust.irondhcp.parsing.Lease;
import de.hshannover.f4.trust.irondhcp.parsing.LeaseDistributor;
import de.hshannover.f4.trust.irondhcp.parsing.LeaseUpdate;
import de.hshannover.f4.trust.irondhcp.parsing.ParserImpl;
import de.hshannover.f4.trust.irondhcp.service.ConfigService;
import de.hshannover.f4.trust.irondhcp.service.IfmapService;
import de.hshannover.f4.trust.irondhcp.service.LeaseService;
import de.hshannover.f4.trust.irondhcp.util.FileWatcher;
import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.exception.InitializationException;

/**
 * Acts as an IF-MAP client for publishing lease informations from ISC-dhcpd
 * to an IF-MAP capable server.
 * 
 * Following steps are perfomed:
 * 1) Establishing a connection to IF-MAP server
 * 1) Reading the content of a given dhcpd.leases file
 * 2) Parsing and filtering the leases found
 * 3) Determining which leases has to be published or deleted
 * 4) Performing necesserary operations on IF-MAP server
 * 
 */
public class Client implements Observer {

        private final FileWatcher mWatcher;
        private final Thread mWatcherThread;
        private final LeaseDistributor mDistributor;
        private final LeaseService mLeaseService;
        private final IfmapService mIfmapService;
        private final BlockingQueue<LeaseUpdate> mQueue;

        /**
         * Constructor registers this {@link Client} object and a new
         * {@link FileWatcher} object. Further, a shutdown hook is added.
         */
        Client() {
                mIfmapService = IfmapService.getInstance();
                mWatcher = new FileWatcher(ConfigService.getLeaseFile());
                mWatcher.setIntervall(ConfigService.getParserInterval());
                mWatcher.addObserver(this);
                mWatcherThread = new Thread(mWatcher);
                mDistributor = new LeaseDistributor();
                mLeaseService = new LeaseService(new ParserImpl());
                mQueue = new LinkedBlockingQueue<LeaseUpdate>();

                // set the shutdown-hook for send endSession(), this is done on CTRL+C,
                // for example
                Runtime.getRuntime().addShutdownHook(new Thread() {
                        public void run() {
                                try {
                                        mIfmapService.endSession();
                                } catch (IfmapErrorResult ifmaperror) {
                                        System.out
                                                        .println("[irondhcp] IF-MAP Error Result during endSession:");
                                        System.out.println("[irondhcp] " + ifmaperror);
                                        System.exit(1);
                                } catch (IfmapException e) {
                                        System.out.println("[irondhcp] Error during endSession:");
                                        System.out.println("[irondhcp] " + e.getDescription());
                                        System.out.println("[irondhcp] " + e.getMessage());
                                }
                        }
                });
        }

        /**
         * Starts the program, this part is executed by the main-thread
         */
        public void start() {

                mWatcherThread.start();

                /*
                 * If any exception occurs during runtime, irondhcp will most likely
                 * kill himself, he's not a really stable personality...
                 */
                try {
                        System.out.println("[irondhcp] Starting...");
                        mIfmapService.prepareSSRC();
                        System.out.println("[irondhcp] newSession...");
                        mIfmapService.newSession();
                        System.out.println("[irondhcp] purgePublisher for publisher-id=\""
                                        + mIfmapService.getPublisherId() + "\"...");
                        mIfmapService.purgePublisher();

                        while (true) {
                                LeaseUpdate update = null;
                                System.out.println("[irondhcp] Waiting for changed leases...");

                                // blocks if no update is there
                                try {
                                        update = mQueue.take();
                                } catch (InterruptedException e) {
                                        System.out
                                                        .println("[irondhcp] InterruptException, trying again...");
                                        // try harder
                                        continue;
                                }

                                System.out.println("[irondhcp] Doing publish...");

                                if (update.delLeases.size() > 0)
                                        mIfmapService.publishDelete(update.delLeases);

                                if (update.newLeases.size() > 0)
                                        mIfmapService.publishUpdate(update.newLeases);
                        }
                } catch (InitializationException e) {
                        System.err.println("Initialization Error");
                        System.out.println(e.getMessage());
                        System.exit(1);
                } catch (IfmapErrorResult ifmaperror) {
                        System.out.println("[irondhcp] IF-MAP Error Result:");
                        System.out.println("[irondhcp] " + ifmaperror);
                        System.exit(1);
                } catch (IfmapException e) {
                        System.out.println("[irondhcp] " + e.getDescription());
                        System.out.println("[irondhcp] " + e.getMessage());
                        System.exit(1);
                }
        }

        /**
         * When a change is detected, we call the Lease-Distributor with the new
         * list <br/>
         * <br/>
         * <b>NOTE:</b> This method is called from within the {@link FileWatcher}
         * thread using the {@link Observer} / {@link Observable} mechanism. We put
         * the update in form of an {@link LeaseUpdate} object to the main-thread to
         * let it publish the stuff.
         */
        @Override
        public void update(Observable o, Object arg) {

                // do parsing and so on to get the leases...
                List<Lease> fileLeases = mLeaseService.getLeases(mWatcher.getFileAsString());

                if (fileLeases.size() > 0) {
                        // update the mDistributor
                        mDistributor.updateLeases(fileLeases);

                        // get lists for publishUpdate and publishDelete
                        List<Lease> newLeases = mDistributor.getNewLeases();
                        List<Lease> delLeases = mDistributor.getDelLeases();

                        if (newLeases.size() > 0 || delLeases.size() > 0) {
                                LeaseUpdate update = new LeaseUpdate(newLeases, delLeases);
                                do {
                                        try {
                                                mQueue.put(update);
                                                update = null;
                                        } catch (InterruptedException e) { /* try harder */
                                        }
                                } while (update != null);
                        }
                }
        }

        public static void main(String[] args) {
                Client mapdhcp = new Client();
                mapdhcp.start();
        }
}
