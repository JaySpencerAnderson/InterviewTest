#!/bin/bash
# Script to implement shell script answer to interview question
# By Jay Anderson, 5/19/2017
#
usage(){
	errortext=$1
	echo "Usage:   `basename $0` -s a|n  -f filename.ext"
	echo "Purpose: Produce concordance of words and frequency for filename.ext."
	echo "         Sort results alphanumerically (-s a) or by frequency (-s n)."
	[ "$errortext" ] && echo "Error:   $errortext"
	echo
	exit 1
}
# Two functions to be used after sorting to order columns
exchange(){
	awk '{print $2, $1}'
}
exprint(){
	awk '{print $1, $2}'
}
export FILENAME
export SORTMETHOD
export SORTPARAM

# Read parameters
while getopts "f:s:" opt
do
	case $opt in
	f)	FILENAME=$OPTARG;;
	s)	SORTMETHOD=$OPTARG;;
	*)	usage "Unknown parameter";;
	esac
done
# Validate parameters
[ "$FILENAME" ] || usage "Please specify a filename, e.g.  -f file.ext"
[ -r $FILENAME ] || usage "$FILENAME is not a readable file"
[ -f $FILENAME ] || usage "$FILENAME is not a readable file"
[ "$SORTMETHOD" ] || usage "Please specify a sort method, e.g.  -s a  or  -s n  "
[ "$SORTMETHOD" == "a" ] || [ "$SORTMETHOD" == "n" ] || usage "Please specify either a or n for the sort method"
# Customizations based on sort method
if [ "$SORTMETHOD" == "n" ]
then
	SORTPARAM='-n'
	SORTORDER="exchange"
else
	SORTPARAM=""
	SORTORDER="exprint"
fi

# The actual working section is pretty brief.
# AWK produces the concordance and prints columns in sort order.
# sort does the obvious (-n when ordering by count)
# SORTORDER reorders the columns if necessary
awk -v Sort=$SORTMETHOD '{
	gsub("[^A-Za-z0-9 ]","");
	for(i=1;i<=NF;i++){
		count[tolower($i)]++;
	}
}
END{
	for(i in count){
		if(Sort == "a"){
			print i, count[i];
		}
		else {
			print count[i], i;
		}
	}
}' $FILENAME\
|sort $SORTPARAM | $SORTORDER
