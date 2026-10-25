# Downloads auto-captions for @zerocarb videos using pre-generated ID list.
# Does NOT scrape the channel page — reads directly from docs/zerocarb_video_ids.txt.
#
# Usage (from project root):
#   powershell -ExecutionPolicy Bypass -File download_yt.ps1
#
# Safe to re-run: skips already-downloaded files, resumes where it left off.

$outDir = "yt_transcripts"
$idFile = "docs/zerocarb_video_ids.txt"

New-Item -ItemType Directory -Force -Path $outDir | Out-Null

if (-not (Test-Path $idFile)) {
    Write-Host "ERROR: $idFile not found." -ForegroundColor Red
    exit 1
}

$ids = Get-Content $idFile | Where-Object { $_ -match '\S' }
Write-Host "Total videos in list: $($ids.Count)"

# Count already-downloaded
$existing = (Get-ChildItem "$outDir\*.vtt" -ErrorAction SilentlyContinue).Count
Write-Host "Already downloaded: $existing transcripts"

$downloaded = 0
$failed = 0
$skipped = 0
$batchPause = 50        # pause after N downloads
$batchPauseSec = 60     # seconds to pause
$sleepPerVideo = 1      # seconds between each video
$counter = 0

foreach ($id in $ids) {
    $counter++

    # Skip if already have a .vtt for this ID
    $existingVtt = Get-ChildItem "$outDir\$id*.vtt" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($existingVtt) {
        $skipped++
        continue
    }

    $pct = [Math]::Round(($counter / $ids.Count) * 100, 1)
    Write-Host "[$counter/$($ids.Count)] ($pct%) $id" -ForegroundColor Cyan

    $output = & yt-dlp `
        --cookies C:\Users\jurgen\Downloads\cookies.txt `
        --write-auto-subs `
        --sub-lang en `
        --skip-download `
        --sleep-requests $sleepPerVideo `
        --extractor-retries 2 `
        --retries 2 `
        --no-warnings `
        -o "$outDir\$id.%(ext)s" `
        "https://www.youtube.com/watch?v=$id" 2>&1

    $outputStr = $output -join " "

    if ($LASTEXITCODE -eq 0) {
        $downloaded++
        Write-Host "  OK" -ForegroundColor Green
    } elseif ($outputStr -match "429") {
        $failed++
        Write-Host "  RATE LIMITED (429) — waiting 120s..." -ForegroundColor Yellow
        Start-Sleep -Seconds 120
        # Retry once
        $output2 = & yt-dlp `
            --cookies C:\Users\jurgen\Downloads\cookies.txt `
            --write-auto-subs `
            --sub-lang en `
            --skip-download `
            --sleep-requests $sleepPerVideo `
            --no-warnings `
            -o "$outDir\$id.%(ext)s" `
            "https://www.youtube.com/watch?v=$id" 2>&1
        if ($LASTEXITCODE -eq 0) {
            $downloaded++
            $failed--
            Write-Host "  Retry OK" -ForegroundColor Green
        } else {
            Write-Host "  Retry failed" -ForegroundColor Red
        }
    } elseif ($outputStr -match "no subtitles|Unavailable|private|removed") {
        Write-Host "  No subtitles available" -ForegroundColor DarkGray
    } else {
        $failed++
        Write-Host "  FAILED: $($outputStr.Substring(0, [Math]::Min(120, $outputStr.Length)))" -ForegroundColor Red
    }

    Start-Sleep -Seconds $sleepPerVideo

    # Periodic pause to avoid rate limiting
    if (($downloaded + $failed) % $batchPause -eq 0 -and $downloaded -gt 0) {
        Write-Host ""
        Write-Host "  --- Pause: downloaded=$downloaded failed=$failed skipped=$skipped — waiting ${batchPauseSec}s ---" -ForegroundColor Yellow
        Write-Host ""
        Start-Sleep -Seconds $batchPauseSec
    }
}

Write-Host ""
Write-Host "=== DONE ===" -ForegroundColor Green
Write-Host "Downloaded: $downloaded"
Write-Host "Failed:     $failed"
Write-Host "Skipped:    $skipped (already existed)"
$totalVtt = (Get-ChildItem "$outDir\*.vtt" -ErrorAction SilentlyContinue).Count
Write-Host "Total .vtt files in ${outDir}: $totalVtt"
