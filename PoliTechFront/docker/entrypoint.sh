#!/bin/sh
set -e

: "${BASE_URL:=http://localhost:8080}"

# Generate runtime env file
mkdir -p /usr/share/nginx/html/assets
if [ -f /usr/share/nginx/html/assets/env.template.js ]; then
  sed "s#\${BASE_URL:-http://localhost:8080}#${BASE_URL}#g" \
    /usr/share/nginx/html/assets/env.template.js > /usr/share/nginx/html/assets/env.js
else
  echo "window.__env = window.__env || {}; window.__env.BASE_URL='${BASE_URL}';" > /usr/share/nginx/html/assets/env.js
fi

exec nginx -g 'daemon off;'
