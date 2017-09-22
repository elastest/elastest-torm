#!/bin/sh

cd "/usr/sbin"
exec ./tcpdump -i any -w - > /data/capture.pcap
