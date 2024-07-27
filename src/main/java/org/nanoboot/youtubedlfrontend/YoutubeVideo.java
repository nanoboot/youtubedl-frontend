/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.nanoboot.youtubedlfrontend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerFormat;
import io.humble.video.Global;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author robertvokac
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private String miniThumbnail;
    private List<YoutubeComment> comments = new ArrayList<>();
    private String previousVideoId = null;
    private String nextVideoId = null;
    private String ext = null;
    private int number;
    //
    public static final List<String> missingYoutubeVideos = new ArrayList<>();

    public YoutubeVideo(File mediaDirectory, boolean argAlwaysGenerateMetadata, String argVideo) throws InterruptedException, IOException {
        File metadataFile = new File(mediaDirectory, "metadata");
        if (!argAlwaysGenerateMetadata && metadataFile.exists()) {

            YoutubeVideo yv = new YoutubeVideo();
            //new ObjectMapper().readValue(Utils.readTextFromFile(metadataFile), YoutubeVideo.class);

            Properties properties = new Properties();
            var input = new FileInputStream(metadataFile);
            properties.load(new InputStreamReader(input, Charset.forName("UTF-8")));

            id = properties.getProperty("id");

            if (!argVideo.isBlank() && !id.equals(argVideo)) {
                return;
            }
            snapshot = properties.getProperty("snapshot");
            title = properties.getProperty("title");
            videoFileName = properties.getProperty("videoFileName");
            videoFileSizeInBytes = Long.valueOf(properties.getProperty("videoFileSizeInBytes"));
            videoFileSha512HashSum = properties.getProperty("videoFileSha512HashSum");
            videoDuration = properties.getProperty("videoDuration");
            channelName = properties.getProperty("channelName");
            channelUrl = properties.getProperty("channelUrl");
            channelId = properties.getProperty("channelId");
            uploadDate = properties.getProperty("uploadDate");
            timestamp = Long.parseLong(properties.getProperty("timestamp"));
            description = properties.getProperty("description");
            thumbnail = properties.getProperty("thumbnail");
            miniThumbnail = properties.getProperty("miniThumbnail");
            comments = new ArrayList<>();
            JSONArray ja = new JSONArray(properties.getProperty("comments"));
            ja.forEach(o -> {
                JSONObject jo = (JSONObject) o;
                try {
                    final String toString = o.toString();
                    System.out.println(toString);
                    comments.add(new ObjectMapper().readValue(toString, YoutubeComment.class));
                } catch (JsonProcessingException ex) {

                    throw new YoutubedlFrontendException(ex.getMessage());
                }
            }
            );
            previousVideoId = properties.getProperty("previousVideoId");
            nextVideoId = properties.getProperty("nextVideoId");
            ext = properties.getProperty("ext");
            number = Integer.valueOf(properties.getProperty("number"));
            return;
        }
        List<File> files = Arrays.asList(mediaDirectory.listFiles());
        Optional<File> jsonFile = files.stream().filter(f -> f.getName().endsWith(".json")).findFirst();
        String json = jsonFile.isPresent() ? Utils.readTextFromFile(jsonFile.get()) : "";
        JSONObject jsonObject = new JSONObject(json);
        id = jsonObject.getString("id");
//        if(!Main.argVideo.isBlank() && !id.equals(Main.argVideo)) {
//            return;
//        }

        thumbnail = jsonObject.getString("thumbnail");
        if (thumbnail == null) {
            thumbnail = "";
        }
        JSONArray thumbnails = jsonObject.getJSONArray("thumbnails");
        for (int i = 0; i < thumbnails.length(); i++) {
            JSONObject o = (JSONObject) thumbnails.get(i);
            if (!o.has("width")) {
                continue;
            } else {
                int width = o.getInt("width");
                if (width < (((double) Main.THUMBNAIL_WIDTH) * 0.8d)) {
                    continue;
                }
                miniThumbnail = o.getString("url");
                break;
            }

        }

        File thumbnailFile = new File(mediaDirectory, "thumbnail." + getThumbnailFormat());
        File miniThumbnailFile = new File(mediaDirectory, "mini-thumbnail." + getMiniThumbnailFormat());

//        new File(mediaDirectory, "thumbnail.jpg").delete();
//        new File(mediaDirectory, "mini-thumbnail.jpg").delete();
//        new File(mediaDirectory, "thumbnail.webp").delete();
//        new File(mediaDirectory, "mini-thumbnail.webp").delete();
        if (thumbnail != null) {
            if (!thumbnailFile.exists()) {
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
            if (!miniThumbnailFile.exists()) {
                try (BufferedInputStream in = new BufferedInputStream(new URL(miniThumbnail).openStream()); FileOutputStream fileOutputStream = new FileOutputStream(miniThumbnailFile.getAbsolutePath())) {
                    byte dataBuffer[] = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }

        }
        //
        Optional<File> descriptionFile = files.stream().filter(f -> f.getName().endsWith(".description")).findFirst();

        ext = jsonObject.getString("ext");

        Optional<File> videoFile = files
                .stream()
                .filter(f
                        -> (f.getName().endsWith("." + ext))
                || (f.getName().endsWith(".mp4"))
                || (f.getName().endsWith(".mkv")) || (f.getName().endsWith(".webm"))
                )

                .findFirst();

        snapshot = mediaDirectory.getParentFile().getName();

        if (videoFile.isEmpty()) {
            missingYoutubeVideos.add(id);
        }
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
        this.comments = YoutubeComment.sort(this.comments);
//
        Properties properties = new Properties();

        properties.put("id", id);
        properties.put("snapshot", snapshot);
        properties.put("title", title);
        properties.put("videoFileName", videoFileName);
        properties.put("videoFileSizeInBytes", String.valueOf(videoFileSizeInBytes));
        properties.put("videoFileSha512HashSum", videoFileSha512HashSum);
        properties.put("videoDuration", videoDuration);
        properties.put("channelName", channelName);
        properties.put("channelUrl", channelUrl);
        properties.put("channelId", channelId);
        properties.put("uploadDate", uploadDate);
        properties.put("timestamp", String.valueOf(timestamp));
        properties.put("description", description);
        properties.put("thumbnail", thumbnail);
        properties.put("miniThumbnail", miniThumbnail);
        properties.put("comments", new JSONArray(comments).toString());
        if (previousVideoId != null) {
            properties.put("previousVideoId", previousVideoId);
        }
        if (nextVideoId != null) {
            properties.put("nextVideoId", nextVideoId);
        }
        properties.put("ext", ext);
        properties.put("number", String.valueOf(number));

        //Utils.writeTextToFile(new JSONObject(this).toString(), metadataFile);
        properties.store(new FileWriter(metadataFile), "store to properties file");
    }

    private static String getVideoFormattedDuration(String arg) throws InterruptedException, IOException {

        final Demuxer demuxer = Demuxer.make();

        demuxer.open(arg, null, false, true, null, null);

        final DemuxerFormat format = demuxer.getFormat();

        final long duration = demuxer.getDuration();
        return formatTimeStamp(duration);

    }

    public String getThumbnailFormat() {
        return getExtensionFromUrl(thumbnail);
    }

    public String getMiniThumbnailFormat() {
        return getExtensionFromUrl(miniThumbnail);
    }

    private String getExtensionFromUrl(String url) {
        String result = url.substring(url
                .lastIndexOf(".") + 1);
        int questionMarkIndex = 0;
        for (int i = 0; i < result.length(); i++) {
            char ch = result.charAt(i);
            if (ch != '?') {
                continue;
            } else {
                questionMarkIndex = i;
            }
        }
        if (questionMarkIndex > 0) {
            result = result.substring(0, questionMarkIndex);
        }
        return result;

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
            if (this.uploadDate.equals(o.uploadDate)) {
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
    public static long totalDurationInMilliseconds = 0l;
    public static List<YoutubeVideo> loadYoutubeVideos(File archiveBoxArchiveDirectory, Args argsInstance) throws IOException, InterruptedException {
        int i = 0;
        List<YoutubeVideo> youtubeVideos = new ArrayList<>();
        for (File snapshotDirectory : archiveBoxArchiveDirectory.listFiles()) {
            //if(i> 40)break;//only for tests
            File mediaDirectory = new File(snapshotDirectory, "media");
            if (!mediaDirectory.exists()) {
                //nothing to do
                continue;
            }
            YoutubeVideo youtubeVideo = new YoutubeVideo(mediaDirectory, argsInstance.getBoolean(ArgType.ALWAYS_GENERATE_METADATA).get(), argsInstance.getString(ArgType.VIDEO).orElse(""));
            if (argsInstance.getString(ArgType.VIDEO).isPresent() && !argsInstance.getString(ArgType.VIDEO).equals(youtubeVideo.getId())) {
                continue;
            }
            if (argsInstance.getString(ArgType.CHANNEL).isPresent() && !argsInstance.getString(ArgType.CHANNEL).equals(youtubeVideo.getChannelId())) {
                continue;
            }

            i++;
            System.out.println("\n\nFound video #" + i);

            for (File f : new File(archiveBoxArchiveDirectory + "/" + youtubeVideo.getSnapshot() + "/media/" + youtubeVideo.getVideoFileName()).getParentFile().listFiles()) {
                if (f.getName().endsWith(".webm")) {
                    //mkv file was manually converted to webm
                    youtubeVideo.setVideoFileName(f.getName());
                    break;
                }

            }
            System.out.println("id = " + youtubeVideo.getId());
            System.out.println("snapshot = " + youtubeVideo.getSnapshot());
            System.out.println("title = " + youtubeVideo.getTitle());
            System.out.println("videoFileName = " + youtubeVideo.getVideoFileName());
            System.out.println("videoFileSizeInBytes = " + youtubeVideo.getVideoFileSizeInBytes());
            System.out.println("videoFileSha512HashSum = " + youtubeVideo.getVideoFileSha512HashSum());
            System.out.println("videoDuration = " + youtubeVideo.getVideoDuration());
            System.out.println("getVideoDurationInMilliseconds = " + youtubeVideo.getVideoDurationInMilliseconds());
            totalDurationInMilliseconds = totalDurationInMilliseconds + youtubeVideo.getVideoDurationInMilliseconds();
            System.out.println("channelName = " + youtubeVideo.getChannelName());
            System.out.println("channelUrl = " + youtubeVideo.getChannelUrl());
            System.out.println("uploadDate = " + youtubeVideo.getUploadDate());
            System.out.println("description = " + youtubeVideo.getDescription());
            System.out.println("thumbnail = " + youtubeVideo.getThumbnail());
            System.out.println("miniThumbnail = " + youtubeVideo.getMiniThumbnail());
            System.out.println("comments = " + youtubeVideo.getComments());
            youtubeVideos.add(youtubeVideo);
        }
        Collections.sort(youtubeVideos);
        YoutubeVideo previousVideo = null;
        YoutubeVideo nextVideo = null;
        YoutubeVideo currentVideo = null;
        for (int j = 0; j < youtubeVideos.size(); j++) {
            previousVideo = currentVideo;
            currentVideo = youtubeVideos.get(j);
            if (j < (youtubeVideos.size() - 1)) {
                nextVideo = youtubeVideos.get(j + 1);
            }
            if (previousVideo != null) {
                currentVideo.setPreviousVideoId(previousVideo.getId());
            }
            if (nextVideo != null) {
                currentVideo.setNextVideoId(nextVideo.getId());
            }
        }
        return youtubeVideos;
    }
    public long getVideoDurationInMilliseconds() {
        String duration = videoDuration;
        String[] array = duration.split(":");
        long ms = Long.valueOf(array[0]) * 60l *60l * 1000l;
        ms = ms + Long.valueOf(array[1]) * 60l * 1000l;
        String[] array2 = array[2].split("\\.");
        ms = ms + Long.valueOf(array2[0]) * 1000l;
        ms = ms + Long.valueOf(array2[1]);
        return ms;
    }

    long getVideoDurationInMinutes() {
        double s = getVideoDurationInMilliseconds();
        return (long) (s / 1000d / 60d);
    }

    long getVideoFileSizeInMegaBytes() {
        double b = getVideoFileSizeInBytes();
        return (long) (b / 1024d / 1024d);
    }

}
