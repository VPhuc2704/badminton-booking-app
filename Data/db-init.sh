#!/bin/sh
set -e

echo "Waiting for postgres..."
until pg_isready -h db -p 5432; do
  sleep 1
done

echo "Creating pgcrypto extension (if missing)..."
psql -h db -U postgres -d DB_MAIN -c "CREATE EXTENSION IF NOT EXISTS pgcrypto;"

EXISTS=$(psql -h db -U postgres -d DB_MAIN -tAc "SELECT 1 FROM information_schema.tables WHERE table_name='document_chunks';")
if [ "$EXISTS" != "1" ]; then
  echo "Importing Data/data.sql..."
  psql -h db -U postgres -d DB_MAIN -f /docker-entrypoint-initdb.d/data.sql
else
  echo "document_chunks already exists, skipping import"
fi

echo "db-init finished."
