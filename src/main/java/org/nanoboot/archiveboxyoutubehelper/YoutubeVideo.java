/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.nanoboot.archiveboxyoutubehelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerFormat;
import io.humble.video.Global;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author robertvokac
 */
@Data
@AllArgsConstructor
public class YoutubeVideo implements Comparable<YoutubeVideo> {

    private String id;
    private String snapshot;
    private String title;
    private String videoFileName = "";
    private long videoFileSizeInBytes = 0;
    private String videoFileSha512HashSum = "";
    private String videoDuration = "";
    private String channelName;
    private String channelUrl;
    private String channelId;
    private String uploadDate;
    private long timestamp;
    private String description;
    private String thumbnail;
    private List<YoutubeComment> comments = new ArrayList<>();
    private String previousVideoId = null;
    private String nextVideoId = null;
    private String ext = null;
    private int number;
    public static final List<String> missingYoutubeVideos = new ArrayList<>();
    
    YoutubeVideo(File mediaDirectory) throws InterruptedException, IOException {
        File metadataFile = new File(mediaDirectory, "metadata");
        if (!Main.ALWAYS_COMPUTE_METADATA && metadataFile.exists()) {

            YoutubeVideo yv = new ObjectMapper().readValue(Utils.readTextFromFile(metadataFile), YoutubeVideo.class);

            id = yv.id;
            snapshot = yv.snapshot;
            title = yv.title;
            videoFileName = yv.videoFileName;
            videoFileSizeInBytes = yv.videoFileSizeInBytes;
            videoFileSha512HashSum = yv.videoFileSha512HashSum;
            videoDuration = yv.videoDuration;
            channelName = yv.channelName;
            channelUrl = yv.channelUrl;
            channelId = yv.channelId;
            uploadDate = yv.uploadDate;
            description = yv.description;
            thumbnail = yv.thumbnail;
            comments = yv.comments;
            return;
        }
        List<File> files = Arrays.asList(mediaDirectory.listFiles());
        Optional<File> jsonFile = files.stream().filter(f -> f.getName().endsWith(".json")).findFirst();
        String json = jsonFile.isPresent() ? Utils.readTextFromFile(jsonFile.get()) : "";
        JSONObject jsonObject = new JSONObject(json);
        thumbnail = jsonObject.getString("thumbnail");
        if (thumbnail == null) {
            thumbnail = "";
        }
        File thumbnailFile = new File(mediaDirectory, "thumbnail.jpg");
        if (!thumbnailFile.exists() && thumbnail != null) {
            try (BufferedInputStream in = new BufferedInputStream(new URL(thumbnail).openStream()); FileOutputStream fileOutputStream = new FileOutputStream(thumbnailFile.getAbsolutePath())) {
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        //
        Optional<File> descriptionFile = files.stream().filter(f -> f.getName().endsWith(".description")).findFirst();

        ext = jsonObject.getString("ext");
        
        Optional<File> videoFile = files
                .stream()
                .filter(f -> 
                        (f.getName().endsWith("." + ext)) ||
                                (f.getName().endsWith(".mp4")) ||
                                (f.getName().endsWith(".mkv"))
                )
//                .filter(
//                        f -> !f.getName().endsWith(".description")
//                        && !f.getName().endsWith(".json")
//                        && !f.getName().equals("metadata")
//                        && !f.getName().endsWith(thumbnail)
//                )
                .findFirst();
        
        snapshot = mediaDirectory.getParentFile().getName();
        id = jsonObject.getString("id");
        if(videoFile.isEmpty())missingYoutubeVideos.add(id);
        this.description = descriptionFile.isPresent() ? Utils.readTextFromFile(descriptionFile.get()) : "";

        
        title = jsonObject.getString("title");
        if (videoFile.isPresent() && !videoFile.get().getName().endsWith(".part")) {
            final File videoFileGet = videoFile.get();
            videoFileName = videoFileGet.getName();
            videoFileSizeInBytes = videoFileGet.length();
            videoFileSha512HashSum = Utils.calculateSHA512Hash(videoFileGet);
            videoDuration = getVideoFormattedDuration(videoFileGet.getAbsolutePath());
        }
        channelName = jsonObject.getString("channel");
        channelUrl = jsonObject.getString("channel_url");
        channelId = jsonObject.getString("channel_id");
        uploadDate = jsonObject.getString("upload_date");
        timestamp = jsonObject.getLong("timestamp");
        

        if (jsonObject.has("comments")) {
            final JSONArray jsonArray = jsonObject.getJSONArray("comments");
            for (int i = 0; i < jsonArray.length(); i++) {
                Object o = jsonArray.get(i);
                //System.out.println("instance of=" + o.getClass().getName());
                comments.add(new YoutubeComment((JSONObject) o));
            }
        }
        Collections.sort(this.comments);
        Utils.writeTextToFile(new JSONObject(this).toString(), metadataFile);
    }

    private static String getVideoFormattedDuration(String arg) throws InterruptedException, IOException {
        // In Humble, all objects have special contructors named 'make'.

        // A Demuxer opens up media containers, parses  and de-multiplexes the streams
        // of media data without those containers.
        final Demuxer demuxer = Demuxer.make();

        // We open the demuxer by pointing it at a URL.
        demuxer.open(arg, null, false, true, null, null);

        // Once we've opened a demuxer, Humble can make a guess about the
        // DemuxerFormat. Humble supports over 100+ media container formats.
        final DemuxerFormat format = demuxer.getFormat();

        final long duration = demuxer.getDuration();
        return formatTimeStamp(duration);

    }

    /**
     * Pretty prints a timestamp (in {@link Global.NO_PTS} units) into a string.
     *
     * @param duration A timestamp in {@link Global.NO_PTS} units).
     * @return A string representing the duration.
     */
    private static String formatTimeStamp(long duration) {
        if (duration == Global.NO_PTS) {
            return "00:00:00.00";
        }

        double d = 1.0 * duration / Global.DEFAULT_PTS_PER_SECOND;
        //System.out.println("duration="+ d);
        int hours = (int) (d / (60 * 60));
        int mins = (int) ((d - hours * 60 * 60) / 60);
        int secs = (int) (d - hours * 60 * 60 - mins * 60);
        int subsecs = (int) ((d - (hours * 60 * 60.0 + mins * 60.0 + secs)) * 100.0);
        return String.format("%1$02d:%2$02d:%3$02d.%4$02d", hours, mins, secs, subsecs);
    }

    @Override
    public int compareTo(YoutubeVideo o) {
        if (this.channelName != null && o.channelName != null && this.channelName.contentEquals(o.channelName)) {
            if(this.uploadDate.equals(o.uploadDate)) {
                return Long.valueOf(timestamp).compareTo(o.timestamp);
            } else {
            return this.uploadDate.compareTo(o.uploadDate);
            }
        } else {
            if (this.channelName != null && o.channelName != null) {
                return this.channelName.compareTo(o.channelName);
            } else {
                return 0;
            }
        }
    }

}
