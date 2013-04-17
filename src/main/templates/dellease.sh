#!/bin/bash

# Help...
function usage {
	echo "$0 <leases.file> <ip-addr>"
	exit 1
}


# Delete the entry... This is pretty bad...
# 
# Parameters:
#
# - $1 file
# - $2 ip
function del_lease {
	echo "Removing lease for $2 from file $1"
	BACKUP="$1`date '+%s%N'`"
	mv $1 $BACKUP
	SKIP=0
	FILE="$(cat $BACKUP)"

	while read line; do
		if [ $SKIP -gt 0 ] ; then
			SKIP=$((SKIP-1))
			continue
		fi

		FOUND=`echo "$line" | grep -c "lease $2 {"`
		if [ $FOUND -gt 0 ] ; then
			SKIP=8		# entries have to be of length 7
			continue	# and newline, so skip 8 lines...
		fi
		echo "$line" >> $1
	done < $BACKUP

	rm $BACKUP
}


# Check if this file contains an entry for the given ip
# If the entry does not exist, then we can exit
#
# Parameters:
# $1 file
# $2 ip
function check_ip_exists {
	if [ -f $1 ] ; then
		COUNT=`grep -c "lease $2 {" $1`
		if [ $COUNT -lt 1 ] ; then
			echo "Lease for $2 does not exists in file $1. Aborting."
			exit 0
		fi
	fi
}


# beginning of script
if [ $# -ne 2 ] ; then
	usage
else
	if [ ! -f $1 ] ; then
		echo "File $1 does not exist. Aborting."
		exit 1
	fi

	check_ip_exists $1 $2
	del_lease $1 $2
fi

