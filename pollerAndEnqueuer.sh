#!/bin/bash

DB_HOST=$MYSQL_HOST
DB_USER=$MYSQL_USER
DB_PASS=$MYSQL_PASS
DB_NAME="NotifyMeDB"
SQS_URL="https://sqs.eu-south-1.amazonaws.com/435703062953/RecurrentDateTime.fifo"

# Recupera le query da eseguire (incluso quelle con next_execution NULL)
queries=$(mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" -D "$DB_NAME" -N -e \
"SELECT id, prompt, cron_params, next_execution, created_at 
 FROM queries 
WHERE is_valid = 1 AND (next_execution <= NOW() OR next_execution IS NULL);" | sed 's/\t/|||/g')

# Funzione per calcolare il prossimo execution timestamp
calculate_next_execution() {
  local cron_expr="$1"
  local base_time="$2"

  python3 -c "
from croniter import croniter
from datetime import datetime
base = datetime.strptime('$base_time', '%Y-%m-%d %H:%M:%S')
it = croniter('$cron_expr', base)
print(it.get_next(datetime).strftime('%Y-%m-%d %H:%M:%S'))
"
}

# Loop riga per riga
while IFS= read -r line; do
    id=$(echo "$line" | cut -d '|' -f1)
    prompt=$(echo "$line" | cut -d '|' -f4)   # prompt è il campo 4 (perché ogni ||| è 3 pipe)
    cron_params=$(echo "$line" | cut -d '|' -f7)
    next_execution=$(echo "$line" | cut -d '|' -f10)
    created_at=$(echo "$line" | cut -d '|' -f13)

    echo "▶️  Eseguo Query ID $id: $prompt"

    # Invia a SQS
    aws sqs send-message \
        --queue-url "$SQS_URL" \
        --message-body "{\"query_id\": $id, \"prompt\": \"$prompt\"}" \
	--message-group-id "`date +%s`"

    # Decidi base_time
    if [[ "$next_execution" == "NULL" || -z "$next_execution" ]]; then
        base_time="$created_at"
    else
        base_time="$next_execution"
    fi

    # Calcola la nuova next_execution a partire da base_time
    next_execution_new=$(calculate_next_execution "$cron_params" "$base_time")
    echo "⏭  Nuova next_execution → $next_execution_new"

    # Aggiorna la nuova next_execution nel DB
    mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" -D "$DB_NAME" -e \
    "UPDATE queries SET next_execution = '$next_execution_new' WHERE id = $id;"
done <<< "$queries"
