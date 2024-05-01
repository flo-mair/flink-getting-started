#!/bin/sh

OS_HOST="opensearch-node1:9200"
DASHBOARD_HOST="dashboard:5601"

if [ -n "$HOST_ENV" ]; then
  OS_HOST="$HOST_ENV"
fi

if [ -n "$DASHBOARD_ENV" ]; then
  DASHBOARD_HOST="$DASHBOARD_ENV"
fi

if [ -z "$USER" ] || [ -z "$PASSWORD" ]; then
  echo "Error: Both USER_ENV and PASSWORD_ENV environment variables must be set."
  exit 1
fi

AUTH_HEADER=$(printf "%s:%s" "$USER" "$PASSWORD" | base64)
JSON_FILE="/usr/local/bin/index.json"

curl --location --insecure --request PUT "https://$OS_HOST/$INDEX_NAME" \
--header 'Content-Type: application/json' \
--header "Authorization: Basic $AUTH_HEADER" \
--data "@$JSON_FILE"


curl --location --request POST "http://$DASHBOARD_HOST/api/saved_objects/index-pattern/$INDEX_PATTERN_NAME" \
--header 'Content-Type: application/json' \
--header "Authorization: Basic $AUTH_HEADER" \
--header 'osd-xsrf: true' \
--data '{
    "attributes": {
        "title": "tracking*",
        "timeFieldName": "timestamp"
    }
}'
