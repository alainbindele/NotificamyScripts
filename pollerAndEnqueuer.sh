#!/bin/bash
# AUTHOR: Alain Kiesse Bindele
# EMAIL: alain.bindele@gmail.com
# Used AI: Chat GPT 4o
# -----------------------------------------------------------------------------
# This script is the exclusive property of Alain Kiesse Bindele.
# ©2025  Alain. All rights reserved.
#
# Unauthorized copying, reproduction, modification, distribution, or use
# of this script or any part thereof, in any form or by any means, is
# strictly prohibited without the prior written consent of the owner.
#
# This code is confidential and intended solely for internal use by
# the NotifyMe project and its authorized contributors.
#
# Violators will be subject to applicable civil and/or criminal penalties.
# -----------------------------------------------------------------------------


# Load database and queue configuration from environment variables
DB_HOST=$MYSQL_HOST
DB_USER=$MYSQL_USER
DB_PASS=$MYSQL_PASS
DB_NAME="NotificamyDB"
SQS_URL="https://sqs.eu-south-1.amazonaws.com/435703062953/RecurrentDateTime.fifo"
export MYSQL_PWD="$DB_PASS"


# Fetch queries that are due for execution or have no next_execution set
queries=$(mysql -h "$DB_HOST" -u "$DB_USER" -D "$DB_NAME" -N -e \
"SELECT q.id, q.prompt, q.cron_params, q.next_execution, q.created_at, u.email, u.discord_webhook, u.slack_webhook, u.whatsapp_phone
 FROM queries as q, users as u
 WHERE q.is_valid = 1
 AND (q.next_execution <= NOW() OR q.next_execution IS NULL)
 AND q.user_id = u.id
 ;" | sed 's/\t/|||/g')

# Function that computes the next execution time from a cron expression and a base time
calculate_next_execution() {
  local cron_expr="$1"
  local base_time="$2"

  python3 -c "
from croniter import croniter
from datetime import datetime, timezone
base = datetime.strptime('$base_time', '%Y-%m-%d %H:%M:%S').replace(tzinfo=timezone.utc)
it = croniter('$cron_expr', base)
next_execution = it.get_next(datetime)

# Ensure the next execution time is in the future
now = datetime.now(timezone.utc)
while next_execution <= now:
    next_execution = it.get_next(datetime)

print(next_execution.strftime('%Y-%m-%d %H:%M:%S'))
"
}

# Function to build JSON message excluding null/empty fields
build_json_message() {
    local id="$1"
    local user_email="$2"
    local prompt="$3"
    local user_discord_webhook="$4"
    local user_slack_webhook="$5"
    local user_phone="$6"
    
    # Start with required fields
    local json="{\"query_id\": $id, \"user_email\": \"$user_email\", \"prompt\": \"$prompt\""
    
    # Add optional fields only if they are not NULL or empty
    if [[ -n "$user_discord_webhook" && "$user_discord_webhook" != "NULL" ]]; then
        json="$json, \"user_discord_webhook\": \"$user_discord_webhook\""
    fi
    
    if [[ -n "$user_slack_webhook" && "$user_slack_webhook" != "NULL" ]]; then
        json="$json, \"user_slack_webhook\": \"$user_slack_webhook\""
    fi
    
    if [[ -n "$user_phone" && "$user_phone" != "NULL" ]]; then
        json="$json, \"user_phone\": \"$user_phone\""
    fi
    
    # Close JSON object
    json="$json}"
    
    echo "$json"
}

# Iterate through each query result
while IFS= read -r line; do
    # Parse fields based on '|||' separator
    id=$(echo "$line" | cut -d '|' -f1)
    prompt=$(echo "$line" | cut -d '|' -f2)
    cron_params=$(echo "$line" | cut -d '|' -f3)
    next_execution=$(echo "$line" | cut -d '|' -f4)
    created_at=$(echo "$line" | cut -d '|' -f5)
    user_email=$(echo "$line" | cut -d '|' -f6)
    user_discord_webhook=$(echo "$line" | cut -d '|' -f7)
    user_slack_webhook=$(echo "$line" | cut -d '|' -f8)
    user_phone=$(echo "$line" | cut -d '|' -f9)

    # Check that prompt and email are not empty
    if [[ -n "$prompt" && -n "$user_email" ]]; then
        echo "`date` - ▶️  Eseguo Query ID $id: $prompt"

        # Build JSON message excluding null fields
        json_message=$(build_json_message "$id" "$user_email" "$prompt" "$user_discord_webhook" "$user_slack_webhook" "$user_phone")
        
        # Send the prompt to AWS SQS queue
        aws sqs send-message \
            --queue-url "$SQS_URL" \
            --message-body "$json_message" \
            --message-group-id "`date +%s`"

        # Determine the base time for next execution calculation
        if [[ "$next_execution" == "NULL" || -z "$next_execution" ]]; then
            base_time="$created_at"
        else
            base_time="$next_execution"
        fi

        # Compute the next execution time from the cron expression
        next_execution_new=$(calculate_next_execution "$cron_params" "$base_time")
        echo "`date` - ⏭  Nuova next_execution → $next_execution_new"

        # Update the query with the new next_execution timestamp
        mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASS" -D "$DB_NAME" -e \
        "UPDATE queries SET next_execution = '$next_execution_new' WHERE id = $id;"
    fi
done <<< "$queries"