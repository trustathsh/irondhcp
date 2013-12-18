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
package de.hshannover.f4.trust.irondhcp.service;




import de.hshannover.f4.trust.irondhcp.util.PropertiesReader;

public final class ConfigService {

        /* hard coded configuration file name */
        public static final String IRONDHCP_CONFIGFILE = "irondhcp.properties";
        
        /* The name prefixes of the config file entries */
        private static final String PREFIX = "irondhcp.";
        private static final String PARSER = PREFIX + "parser.";
        private static final String SERVER = PREFIX + "server.";
        private static final String KEYSTORE = PREFIX + "keystore.";
        private static final String TRUSTSTORE = PREFIX + "truststore.";
        private static final String BASIC = SERVER + "basicauth.";
        private static final String DHCPD = PREFIX + "dhcpd.";
        
        /* integer in seconds */
        private static final String INTERVAL_KEY = PARSER + "interval";
        /* leases file */
        private static final String LEASES_FILE_KEY = PARSER + "leasesfile";

        /* connection related entries */
        private static final String URL_KEY = SERVER + "url";
        private static final String BASICAUTH_ENABLED_KEY = BASIC + "enabled";
        private static final String BASICAUTH_USER_KEY = BASIC + "user";
        private static final String BASICAUTH_PASS_KEY = BASIC + "password";

        /* keystore and truststore entries */
        private static final String KS_FILE_KEY = KEYSTORE + "file";
        private static final String KS_PW_KEY = KEYSTORE + "password";
        private static final String TS_FILE_KEY = TRUSTSTORE + "file";
        private static final String TS_PW_KEY = TRUSTSTORE + "password";
        
        /* dhcpd ip entry */
        private static final String DHCPD_IP_KEY = DHCPD + "ip";        
        
        
        
        private static PropertiesReader reader = new PropertiesReader(IRONDHCP_CONFIGFILE);
        
        /**
         * singleton or something like that.
         */
        private ConfigService() { }
        
        
        public static int getParserInterval() {
                int ret;
                try {
                        ret = Integer.parseInt(getConfigEntry(INTERVAL_KEY));
                } catch (NumberFormatException e) {
                        System.err.println("[irondhcp] ERROR: Bad value for " + INTERVAL_KEY);
                        throw new RuntimeException();
                }
                return ret;
        }
        
        public static String getLeaseFile() {
                return getConfigEntry(LEASES_FILE_KEY);
        }
        
        public static String getServerUrl() {
                return getConfigEntry(URL_KEY);
        }
        
        public static boolean getBasicAuthEnabled() {
                return Boolean.parseBoolean(getConfigEntry(BASICAUTH_ENABLED_KEY));
        }
        
        public static String getBasicAuthUser() {
                return getConfigEntry(BASICAUTH_USER_KEY);
        }

        public static String getBasicAuthPassword() {
                return getConfigEntry(BASICAUTH_PASS_KEY);
        }
        
        public static String getKeystoreFile() {
                return getConfigEntry(KS_FILE_KEY);
        }

        public static String getKeystorePassword() {
                return getConfigEntry(KS_PW_KEY);
        }
        
        public static String getTrustStoreFile() {
                return getConfigEntry(TS_FILE_KEY);
        }

        public static String getTruststorePassword() {
                return getConfigEntry(TS_PW_KEY);
        }

        public static String getDhcpdIp() {
                return getConfigEntry(DHCPD_IP_KEY);
        }
        
        /**
         * Helper to print an error if we can't find an entry in the config.
         * 
         * @param key
         * @return
         */
        private static String getConfigEntry(String key) {
                String ret = null;
                ret = reader.getProperty(key);
                if (ret == null) {
                        System.err.println("[irondhcp] ERROR: No entry in config for " + key);
                        throw new RuntimeException("No entry in config for " + key);
                }
                return ret;
        }
}
