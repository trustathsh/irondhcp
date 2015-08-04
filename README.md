irondhcp
========

This package contains an *experimental* IF-MAP client which can
be used in combination with the [ISC DHCP server][1] to publish
ip-mac metadata on links between ip-address and mac-address
identifiers. It does not represent a standalone DHCP server.

The program was written using Java. For communication purposes
ifmapj is used. Communication between the MAP client and MAP
server is done using [IF-MAP 2.0][2].

Development was started within the IRON project at Hochschule
Hannover (Hannover University of Applied Sciences and Arts). The
implementation is now maintained and extended within the [ESUKOM
research project][3]. More information about the projects can be
found at.


How it works
============

The application reads the dhpc.leases file of the ISC DHCP server.
Based on the information contained in this file it constructs
publish update requests to add ip-mac metadata between the
corresponding ip-address and mac-address identifiers.

If the binding state of one of the entries contained in the
dhcp.leases file changes from active to free, a publish delete request
is sent to the MAP server in order to remove the added ip-mac
metadata. The lifetime of the published metadata is set to 'session'.

Building
========

Simply run 

  mvn package

to build the project. There are three relevant artifacts created: 
  *irondhcp-<version>-bin.jar - Binary with packed dependencies
  *irondhcp-<version>-bundle.zip - Bundle with binary and relevant configuration files
  *irondhcp-<version>-src.zip - Source package
  

Configuration
=============

General configuration can be done through the irondhcp.properties file.
A documented version of this file is backed up as irondhcp.properties.orig. The
default configuration tries to connect to a MAPS at localhost using port 8443
for basic authentication. Furthermore, the client looks for a dhcpd.leases
file in the current working directory.


Server Certificate and Authentication
=====================================

To validate the authenticity of the MAP server, the MAP server's
certificate has to be added in the keystore to be used by irondhcp.
This can be done using the [keytool program provided by Java][4]

Authentication can be either done using basic authentication or
certificate-based authentication. Which of those authentication
methods is used can be configured in the irondhcp.properties file.

The keystore that is provided with this client should work out of the box with
the irond MAPS.


Running
=======
The application was developed using Java 1.6. It is therefore
recommended to test it with Java 1.6. 

The map client can be started using the following command:

	$ java -jar irondhcp.jar


Testing
=======

The provided package contains a simple dhcp.leases file. This file
is used if the configuration is not changed. The contained script
addlease.sh may be used to add entries to the dhcp.leases file while
irondhcp is running.

Feedback
========
If you have any questions, problems or comments, please contact
	trust@f4-i.fh-hannover.de


LICENSE
=======
irondhcp is licensed under the [Apache License, Version 2.0][5].

[1]: http://www.isc.org/software/dhcp
[2]: http://www.trustedcomputinggroup.org/resources/tnc_ifmap_binding_for_soap_specification
[3]: http://trust.f4.hs-hannover.de
[4]: http://download.oracle.com/javase/1.5.0/docs/tooldocs/solaris/keytool.html
[5]: http://www.apache.org/licenses/LICENSE-2.0.html
