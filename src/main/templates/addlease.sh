#!/bin/bash

# Help...
function usage {
	echo "$0 <leases.file> <ip-addr> <mac-addr>"
	exit 1
}


# Adds the lease entry to the file...
# 
# Parameters:
#
# - $1 file
# - $2 ip
# - $3 mac
function add_lease {
	echo "Adding lease for $2 $3 to file $1"

	# DATE is without year!
	DATE=`date +'/%m/%d %H:%M:%S'`
	YEAR=`date +'%Y'`
	# newline
	echo "lease $2 {"				>> $1
	echo "starts 4 ${YEAR}${DATE};"			>> $1
	
	# set endtime to one year in the future
	# avoiding the date stuff...
	YEAR=$((YEAR+1))
	echo "ends 4 ${YEAR}${DATE};"			>> $1
	# i have no idea what cltt stands for
	YEAR=$((YEAR-1))
	echo "cltt 4 ${YEAR}${DATE};"			>> $1
  	echo "binding state active;"			>> $1
  	echo "next binding state free;"			>> $1
	echo "hardware ethernet $3;"			>> $1
	echo "}"					>> $1
	echo ""                                         >> $1
}


# Check if this file contains an entry for the given ip
# If this is the case, we exit
#
# Parameters:
# $1 file
# $2 ip
function check_ip_exists {
	# We can check for the ip only if the file exists
	if [ -f $1 ] ; then
		COUNT=`grep -c "lease $2 {" $1`
		if [ $COUNT -gt 0 ] ; then
			echo "Lease for $2 exists in file $1. Aborting."
			exit 1
		fi
	fi
}


# beginning of script
if [ $# -ne 3 ] ; then
	usage
else
	if [ ! -f $1 ] ; then
		echo "File $1 does not exist, it will be created..."
	fi

	check_ip_exists $1 $2
	add_lease $1 $2 $3
fi

