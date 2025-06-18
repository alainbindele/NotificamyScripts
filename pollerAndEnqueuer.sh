#!/bin/bash

DB_HOST="localhost"
DB_USER="root"
DB_PASS="laTuaPassword"
DB_NAME="NotifyMeDB"
SQS_URL="https://sqs.eu-west-1.amazonaws.com/xxx/notifyme-queue"

# Recupera le query da eseguire
queries=$(mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" -D "$DB_NAME" -N -e \
"SELECT id, prompt, cron_params FROM queries WHERE is_valid = 1 AND next_execution <= NOW();")

# Loop riga per riga
while IFS=$'\t' read -r id prompt cron_params; do
    echo "Eseguo Query ID $id: $prompt"

    # Invia su SQS
    aws sqs send-message \
        --queue-url "$SQS_URL" \
        --message-body "{\"query_id\": $id, \"prompt\": \"${prompt//\"/\\\"}\"}"

    # Calcola nuova next_execution (placeholder - usa tool esterno per cron parsing)
    next_execution=$(date -d "+${cron_params:-60} minutes" +"%Y-%m-%d %H:%M:%S")

    echo "Prossima esecuzione: $next_execution"

    # Aggiorna next_execution
    mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" -D "$DB_NAME" -e \
    "UPDATE queries SET next_execution = '$next_execution' WHERE id = $id;"
done <<< "$queries"

