#!/bin/sh
cd /etm-logstash
sed -i 's/ELASTICHOST/'"$ELASTICHOST"'/g' ./config/output
sed -i 's/RABBITHOST/'"$RABBITHOST"'/g' ./config/output
sed -i 's/RABBITUSER/'"$RABBITUSER"'/g' ./config/output
sed -i 's/RABBITPASS/'"$RABBITPASS"'/g' ./config/output

FILES=./config/*.conf
for f in $FILES
do
	cat ./config/output >> $f
done
exec bin/logstash -f '/etm-logstash/config/*.conf'
