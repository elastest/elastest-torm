# Wait for ffmpeg to finish
myfilesize=$(wc -c "./tmp" | awk '{print $1}')
echo "File size: $myfilesize" 
while [[ myfilesize -gt 0 ]]
do
 $myfilesize=$(wc -c "./tmp" | awk '{print $1}')
 sleep 1 
done

