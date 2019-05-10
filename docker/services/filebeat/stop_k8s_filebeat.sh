#!/bin/bash

kubectl --namespace=kube-system delete ds/filebeat --cascade=true
#kubectl -n kube-system delete cm/filebeat-inputs
kubectl -n kube-system delete cm/filebeat-config
