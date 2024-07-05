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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import javax.imageio.ImageIO;
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

    public YoutubeVideo(File mediaDirectory) throws InterruptedException, IOException {
        File metadataFile = new File(mediaDirectory, "metadata");
        if (!Main.argAlwaysGenerateMetadata && metadataFile.exists()) {

            YoutubeVideo yv = new YoutubeVideo();
            //new ObjectMapper().readValue(Utils.readTextFromFile(metadataFile), YoutubeVideo.class);

            Properties properties = new Properties();
            properties.load(new FileInputStream(metadataFile));

            id = properties.getProperty("id");

            if (!Main.argVideo.isBlank() && !id.equals(Main.argVideo)) {
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
                if (width < (((double)Main.THUMBNAIL_WIDTH) * 0.8d)) {
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
//            for (String s : ImageIO.getReaderFormatNames()) {
//                System.out.println(s);
//            }
            //if(!miniThumbnailFile.exists()) {

//String miniThumbnailFileAbsolutePath = miniThumbnailFile.getAbsolutePath();
//            String formatName = miniThumbnailFileAbsolutePath.substring(miniThumbnailFileAbsolutePath
//                    .lastIndexOf(".") + 1);
//            BufferedImage inputImage = ImageIO.read(thumbnailFile);
//            if(inputImage == null) {
//
//            }
//            int thumbnailWidth = inputImage.getWidth();
//            int thumbnailHeight = inputImage.getHeight();
//            double heightWidthRatio = ((double) thumbnailHeight) / ((double) thumbnailWidth);
//            int miniThumbnailWidth = Main.THUMBNAIL_WIDTH;
//            int miniThumbnailHeight = (int) (heightWidthRatio * ((double) Main.THUMBNAIL_WIDTH));
//
//            BufferedImage outputImage = new BufferedImage(miniThumbnailWidth,
//                    miniThumbnailHeight, inputImage.getType());
//
//            Graphics2D g2d = outputImage.createGraphics();
//            g2d.drawImage(inputImage, 0, 0, miniThumbnailWidth, miniThumbnailHeight, null);
//            g2d.dispose();
//
//            
//
//            ImageIO.write(outputImage, formatName, new File(miniThumbnailFileAbsolutePath));
            //}
        }
        //
        Optional<File> descriptionFile = files.stream().filter(f -> f.getName().endsWith(".description")).findFirst();

        ext = jsonObject.getString("ext");

        Optional<File> videoFile = files
                .stream()
                .filter(f
                        -> (f.getName().endsWith("." + ext))
                || (f.getName().endsWith(".mp4"))
                || (f.getName().endsWith(".mkv"))
                )
                //                .filter(
                //                        f -> !f.getName().endsWith(".description")
                //                        && !f.getName().endsWith(".json")
                //                        && !f.getName().equals("metadata")
                //                        && !f.getName().endsWith(thumbnail)
                //                )
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
        for(int i = 0;i<result.length();i++) {
            char ch = result.charAt(i);
            if(ch != '?') {
                continue;
            } else {
                questionMarkIndex = i;
            }
        }
        if(questionMarkIndex > 0) {
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

}
