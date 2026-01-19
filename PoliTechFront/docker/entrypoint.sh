#!/bin/sh
set -e

: "${BASE_URL:=}"
: "${KEYCLOAK_URL:=}"
: "${KEYCLOAK_REALM:=}"
: "${KEYCLOAK_CLIENT_ID:=}"

# Generate runtime env file
mkdir -p /usr/share/nginx/html/assets
if [ -f /usr/share/nginx/html/assets/env.template.js ]; then
  sed \
    -e "s#\${BASE_URL:-}#${BASE_URL}#g" \
    -e "s#\${KEYCLOAK_URL:-}#${KEYCLOAK_URL}#g" \
    -e "s#\${KEYCLOAK_REALM:-}#${KEYCLOAK_REALM}#g" \
    -e "s#\${KEYCLOAK_CLIENT_ID:-}#${KEYCLOAK_CLIENT_ID}#g" \
    /usr/share/nginx/html/assets/env.template.js > /usr/share/nginx/html/assets/env.js
else
  echo "window.__env = window.__env || {}; window.__env.BASE_URL='${BASE_URL}'; window.__env.KEYCLOAK_URL='${KEYCLOAK_URL}'; window.__env.KEYCLOAK_REALM='${KEYCLOAK_REALM}'; window.__env.KEYCLOAK_CLIENT_ID='${KEYCLOAK_CLIENT_ID}';" > /usr/share/nginx/html/assets/env.js
fi

exec nginx -g 'daemon off;'
