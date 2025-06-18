#!/bin/bash

DB_HOST=$MYSQL_HOST
DB_USER=$MYSQL_USER
DB_PASS=$MYSQL_PASS
DB_NAME="NotifyMeDB"
SQS_URL="https://sqs.eu-south-1.amazonaws.com/435703062953/RecurrentDateTime.fifo"

# Recupera le query da eseguire
queries=$(mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" -D "$DB_NAME" -N -e \
"SELECT id, prompt, cron_params FROM queries WHERE is_valid = 1 AND next_execution <= NOW();")

# Funzione per calcolare il next_execution con Python
calculate_next_execution() {
  local cron_expr="$1"
  python3 -c "
from croniter import croniter
from datetime import datetime
base = datetime.now()
it = croniter('$cron_expr', base)
print(it.get_next(datetime).strftime('%Y-%m-%d %H:%M:%S'))
"
}

# Loop sulle query da eseguire
while IFS=$'\t' read -r id prompt cron_params; do
    echo "▶️  Eseguo Query ID $id: $prompt"

    # Invia a SQS
    aws sqs send-message \
        --queue-url "$SQS_URL" \
        --message-body "{\"query_id\": $id, \"prompt\": \"${prompt//\"/\\\"}\"}"

    # Calcola next_execution
    next_execution=$(calculate_next_execution "$cron_params")
    echo "⏭  Prossima esecuzione → $next_execution"

    # Aggiorna in MySQL
    mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" -D "$DB_NAME" -e \
    "UPDATE queries SET next_execution = '$next_execution' WHERE id = $id;"
done <<< "$queries"

