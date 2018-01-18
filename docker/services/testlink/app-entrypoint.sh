#!/bin/bash -e

. /opt/bitnami/base/functions
. /opt/bitnami/base/helpers

print_welcome_page
check_for_updates &

if [[ "$1" == "nami" && "$2" == "start" ]] || [[ "$1" == "/init.sh" ]]; then
  nami_initialize apache php testlink
  info "Starting testlink... "
fi

rm -rf /opt/bitnami/testlink/install/

exec tini -- "$@"
