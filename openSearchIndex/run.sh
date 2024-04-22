#!/bin/sh

OS_HOST="opensearch-node1:9200"

# Check if the HOST_ENV environment variable is set
if [ -n "$HOST_ENV" ]; then
  OS_HOST="$HOST_ENV"
fi

if [ -z "$USER" ] || [ -z "$PASSWORD" ]; then
  echo "Error: Both USER_ENV and PASSWORD_ENV environment variables must be set."
  exit 1
fi

AUTH_HEADER=$(printf "%s:%s" "$USER" "$PASSWORD" | base64)
JSON_FILE="index.json"

curl --location --insecure --request PUT "https://$OS_HOST/$INDEX_NAME" \
--header 'Content-Type: application/json' \
--header "Authorization: Basic $AUTH_HEADER" \
--data "@$JSON_FILE"