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
 * This file is part of irondhcp, version 0.3.2,
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
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
import java.util.List;

/**
 * This class adjusts the new leases with the old ones and outreaches them to
 * the SOAP-component
 * 
 */
public class LeaseDistributor {

        private ArrayList<Lease> mOldLeases = new ArrayList<Lease>();
        private ArrayList<Lease> mAddLeases = new ArrayList<Lease>();
        private ArrayList<Lease> mDelLeases = new ArrayList<Lease>();

        /**
         * Compares two arrays with leases
         * 
         * @param l1
         *            first array
         * @param l2
         *            second array
         * @return an adjusted list of leases
         */
        private void compareAndFill(List<Lease> _new, List<Lease> _old) {
                Lease tmpL = null;
                mAddLeases.clear();
                mDelLeases.clear();

                // FIXME
                if (_new == null || _old == null) {
                        System.err.println("compareAndFill parameter is null.");
                        return;
                }

                for (Lease newL : _new) {
                        boolean isOld = false;

                        // is this lease active?
                        if (newL.getBindingState().equals("active")) {
                                for (Lease oldL : _old) {
                                        // did we see this lease before?
                                        if (newL.getIp().equals(oldL.getIp())) {
                                                isOld = true;
                                                tmpL = oldL;
                                                break;
                                        }
                                }
                                // check whether the lease is a updated one with a new leasetime
                                
                                // add to list, if new
                                if (!isOld) {
                                        mAddLeases.add(newL);
                                } else if ((newL.getEnd() == null && tmpL.getEnd() != null)
                                                || (newL.getEnd() != null && (tmpL.getEnd() == null))) {
                                        mAddLeases.add(newL);
                                        mOldLeases.remove(tmpL);
                                        mDelLeases.add(tmpL);
                                }
                                else if (newL.getEnd() != null && tmpL.getEnd() != null
                                                && newL.getEnd().compareTo(tmpL.getEnd()) > 0) {
                                        mAddLeases.add(newL);
                                        mOldLeases.remove(tmpL);
                                        mDelLeases.add(tmpL);
                                } else {
                                        // Ignoring because nothing changed for this lease...
                                }

                        }
                        // looking for expired ip-addresses
                        else if (newL.getBindingState().equals("free")) {
                                for (Lease oldL : _old) {
                                        if (newL.getIp().equals(oldL.getIp())) {
                                                isOld = true;
                                                tmpL = oldL;
                                                break;
                                        }
                                }
                                // add outdated ip-addresses to delete list
                                if (isOld) {
                                        mDelLeases.add(newL);
                                        mOldLeases.remove(tmpL);
                                }
                        }
                }

                mOldLeases.addAll(mAddLeases);
                System.out.println("[irondhcp] mAddLeases (" + mAddLeases.size() + ")");
                print_r(mAddLeases);
                System.out.println("[irondhcp] mDelLeases (" + mDelLeases.size() + ")");
                print_r(mDelLeases);
        }

        /**
         * Filter out lease duplicates. For every update on an ip, a new entry in
         * dhcpd.leases is appended to the end. Old entries will stay there until
         * the dhcpd is restarted. We must ensure only the latest entries are
         * considered so we can do the right thing on publishing or deleting leases.
         * 
         * @param list
         * @return a list of leases without duplicates
         */
        private List<Lease> filterDuplicates(List<Lease> list) {
                if (list == null || list.size() == 0) {
                        return list;
                }

                List<Lease> tmp = new ArrayList<Lease>();

                for (Lease x : list) {

                        // check if tmp includes a lease with the same ip already
                        for (Lease y : tmp) {

                                // if yes, remove it and add the latest one
                                if (x.getIp().equals(y.getIp())) {
                                        tmp.remove(y);
                                        break;
                                }
                        }

                        tmp.add(x);
                }
                return tmp;
        }

        /**
         * 
         * @param leases
         */
        public void updateLeases(List<Lease> leases) {
                List<Lease> no_dups = filterDuplicates(leases);
                compareAndFill(no_dups, mOldLeases);
        }

        public List<Lease> getNewLeases() {
                return mAddLeases;
        }

        public List<Lease> getDelLeases() {
                return mDelLeases;
        }

        private void print_r(List<Lease> list) {
                for (Lease lease : list) {
                        System.out.println("\t" + lease);
                }
        }
}
