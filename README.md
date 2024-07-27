# youtubedl-frontend

ffmpeg -i "$video"mkv -preset slow -crf 18 "$video"webm

ffmpeg -i "$video"mkv -preset slow -crf 18 -vf scale="-1:480" "$video"webm
