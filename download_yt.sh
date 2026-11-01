#!/usr/bin/env bash
# Downloads auto-captions for @zerocarb videos using a pre-generated ID list.
# Does NOT scrape the channel page — reads directly from data/zerocarb_video_ids.txt.
#
# Usage (from project root):
#   ./download_yt.sh
#   ./download_yt.sh /path/to/cookies.txt
#
# Safe to re-run: skips already-downloaded files, resumes where it left off.

set -u

out_dir="yt_transcripts"
id_file="data/zerocarb_video_ids.txt"

# Allow an optional cookies file argument; fall back to the usual download location.
cookies_file="${1:-${HOME}/Downloads/cookies.txt}"

mkdir -p "$out_dir"

if [[ ! -f "$id_file" ]]; then
    printf 'ERROR: %s not found.\n' "$id_file" >&2
    exit 1
fi

# Read non-empty lines and strip carriage returns.
mapfile -t ids < <(tr -d '\r' < "$id_file" | sed '/^[[:space:]]*$/d')

total=${#ids[@]}
printf 'Total videos in list: %d\n' "$total"

existing=$(find "$out_dir" -maxdepth 1 -type f -name '*.vtt' | wc -l)
printf 'Already downloaded: %d transcripts\n' "$existing"

downloaded=0
failed=0
skipped=0
batch_pause=50
batch_pause_sec=60
sleep_per_video=1
counter=0

yt_dlp_args=(
    --write-auto-subs
    --sub-lang en
    --skip-download
    --sleep-requests "$sleep_per_video"
    --extractor-retries 2
    --retries 2
    --no-warnings
)

if [[ -f "$cookies_file" ]]; then
    yt_dlp_args+=(--cookies "$cookies_file")
fi

for id in "${ids[@]}"; do
    counter=$((counter + 1))

    # Skip if a .vtt for this ID already exists.
    existing_vtt=$(find "$out_dir" -maxdepth 1 -type f -name "${id}*.vtt" | head -n 1)
    if [[ -n "$existing_vtt" ]]; then
        skipped=$((skipped + 1))
        continue
    fi

    pct=$(awk -v c="$counter" -v t="$total" 'BEGIN { printf "%.1f", (c / t) * 100 }')
    printf '\033[36m[%d/%d] (%s%%) %s\033[0m\n' "$counter" "$total" "$pct" "$id"

    output=$(yt-dlp "${yt_dlp_args[@]}" \
        -o "$out_dir/${id}.%(ext)s" \
        "https://www.youtube.com/watch?v=${id}" 2>&1)
    status=$?

    output_str=$(printf '%s' "$output" | tr '\n' ' ')

    if [[ $status -eq 0 ]]; then
        downloaded=$((downloaded + 1))
        printf '  \033[32mOK\033[0m\n'
    elif [[ "$output_str" == *"429"* ]]; then
        failed=$((failed + 1))
        printf '  \033[33mRATE LIMITED (429) — waiting 120s...\033[0m\n'
        sleep 120
        # Retry once
        output2=$(yt-dlp "${yt_dlp_args[@]}" \
            -o "$out_dir/${id}.%(ext)s" \
            "https://www.youtube.com/watch?v=${id}" 2>&1)
        status2=$?
        if [[ $status2 -eq 0 ]]; then
            downloaded=$((downloaded + 1))
            failed=$((failed - 1))
            printf '  \033[32mRetry OK\033[0m\n'
        else
            printf '  \033[31mRetry failed\033[0m\n'
        fi
    elif [[ "$output_str" =~ no[[:space:]]subtitles|Unavailable|private|removed ]]; then
        printf '  \033[90mNo subtitles available\033[0m\n'
    else
        failed=$((failed + 1))
        short=${output_str:0:120}
        printf '  \033[31mFAILED: %s\033[0m\n' "$short"
    fi

    sleep "$sleep_per_video"

    # Periodic pause to avoid rate limiting
    if (( (downloaded + failed) % batch_pause == 0 && downloaded > 0 )); then
        printf '\n  --- Pause: downloaded=%d failed=%d skipped=%d — waiting %ds ---\n\n' \
            "$downloaded" "$failed" "$skipped" "$batch_pause_sec"
        sleep "$batch_pause_sec"
    fi
done

printf '\n=== DONE ===\n'
printf 'Downloaded: %d\n' "$downloaded"
printf 'Failed:     %d\n' "$failed"
printf 'Skipped:    %d (already existed)\n' "$skipped"
total_vtt=$(find "$out_dir" -maxdepth 1 -type f -name '*.vtt' | wc -l)
printf 'Total .vtt files in %s: %d\n' "$out_dir" "$total_vtt"
