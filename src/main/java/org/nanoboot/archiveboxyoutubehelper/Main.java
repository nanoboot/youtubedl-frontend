///////////////////////////////////////////////////////////////////////////////////////////////
// archivebox-youtube-helper: Tool generating html pages for Archive Box.
// Copyright (C) 2024 the original author or authors.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; version 2
// of the License only.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
///////////////////////////////////////////////////////////////////////////////////////////////
package org.nanoboot.archiveboxyoutubehelper;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 * @since 0.0.0
 */
public class Main {

    private static int iii = 0;
    private static int videoNumberPerRow = 0;
    public static final boolean ALWAYS_COMPUTE_METADATA = true;
    public static final int VIDEOS_PER_ROW = 4;
    private static int THUMBNAIL_WIDTH = 250;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("archiveboxyoutubehelper - HTML generator\n");

        if (args.length == 0) {
            args = new String[]{"/rv/blupi/archivebox"};
        }
        if (args.length != 1) {
            System.err.println("One argument is expected, but the count of arguments is: " + args.length + ".");
            System.exit(1);
        }
        File archiveBoxRootDirectory = new File(args[0]);
        File archiveBoxArchiveDirectory = new File(archiveBoxRootDirectory, "archive");
        int i = 0;
        List<YoutubeVideo> youtubeVideos = new ArrayList<>();
        for (File snapshotDirectory : archiveBoxArchiveDirectory.listFiles()) {
            //if(i> 10)break;
            File mediaDirectory = new File(snapshotDirectory, "media");
            if (!mediaDirectory.exists()) {
                //nothing to do
                continue;
            }
            YoutubeVideo youtubeVideo = new YoutubeVideo(mediaDirectory);
            i++;
            System.out.println("\n\nFound video #" + i);

            System.out.println("id = " + youtubeVideo.getId());
            System.out.println("snapshot = " + youtubeVideo.getSnapshot());
            System.out.println("title = " + youtubeVideo.getTitle());
            System.out.println("videoFileName = " + youtubeVideo.getVideoFileName());
            System.out.println("videoFileSizeInBytes = " + youtubeVideo.getVideoFileSizeInBytes());
            System.out.println("videoFileSha512HashSum = " + youtubeVideo.getVideoFileSha512HashSum());
            System.out.println("videoDuration = " + youtubeVideo.getVideoDuration());
            System.out.println("channelName = " + youtubeVideo.getChannelName());
            System.out.println("channelUrl = " + youtubeVideo.getChannelUrl());
            System.out.println("uploadDate = " + youtubeVideo.getUploadDate());
            System.out.println("description = " + youtubeVideo.getDescription());
            System.out.println("thumbnail = " + youtubeVideo.getThumbnail());
            System.out.println("comments = " + youtubeVideo.getComments());
            youtubeVideos.add(youtubeVideo);
        }
        Collections.sort(youtubeVideos);
        Map<String, String> channelUrls = new HashMap<>();
        List<String> channels = new ArrayList<>();
        youtubeVideos.stream().forEach(c -> {
            final String channelName_ = c.getChannelName();

            if (channelName_ != null && !channelUrls.containsKey(c.getChannelName())) {
                channelUrls.put(channelName_, c.getChannelUrl());
                channels.add(channelName_);
            }
        });

        StringBuilder sb = new StringBuilder();
        File videosHtmlFile = new File(archiveBoxRootDirectory, "videos.html");
        File videosDirectory = new File(archiveBoxRootDirectory, "videos");
        if (!videosDirectory.exists()) {
            videosDirectory.mkdir();
        }
        sb.append("""
                   <!DOCTYPE html>
                  <html>
                  <head>
                  <link rel="icon" type="image/x-icon" href="favicon.ico" sizes="16x16">
                  <title>Youtube videos</title>
                  <style>
                  body {padding:20px;}
                  * {
                    font-family:Arial;
                  }
                  .videos {
                    /*box-sizing: border-box;*/
                  }
                  .box {
                    /*float: left;
                    width: 20.0%;*/
                    padding: 10px;
                  }
                  </style>
                  </head>
                  <body>
                  """);

        channels.forEach(c -> {
            sb.append("<h1>").append(c).append("</h1>\n");
            sb.append("<div style=\"max-width:").append((Main.THUMBNAIL_WIDTH + 20) * Main.VIDEOS_PER_ROW).append("px\"><a href =\"").append(channelUrls.get(c)).append("\">").append(channelUrls.get(c)).append("</a><div class=\"videos\">");
            iii = 0;
            videoNumberPerRow = 0;
            sb.append("<table>\n");
            youtubeVideos.stream().filter(v -> c.equals(v.getChannelName())).forEach(z -> {
                iii++;
                if (videoNumberPerRow == 0) {
                    sb.append("<tr>");
                }
                videoNumberPerRow++;
                sb.append("<td><div class=\"box\"><table style=\"margin:5px;max-width:")
                        .append(THUMBNAIL_WIDTH)
                        .append("px;\">\n<tr><td><a href=\"videos/" + z.getId() + ".html\" target=\"_blank\"><img src=\"archive/");
                sb.append(z.getSnapshot());
                sb.append("/media/thumbnail.jpg\" width=\"")
                        .append(THUMBNAIL_WIDTH)
                        .append("\"></a></td></tr>\n");
                sb.append("<tr><td><b style=\"font-size:90%;\">").append(z.getTitle()).append("</b></td></tr>\n");
                String uploadDate = z.getUploadDate();
                uploadDate = uploadDate.substring(0, 4) + "-" + uploadDate.substring(4, 6) + "-" + uploadDate.substring(6, 8);
                sb.append("<tr><td style=\"font-size:80%;color:grey;\">").append(uploadDate).append(" •︎ ").append(z.getVideoDuration())
                        .append(" •︎ ")
                        .append("#").append(iii)
                        .append("</td></tr>\n");
                sb.append("</table></div></td>\n");
                if (videoNumberPerRow == VIDEOS_PER_ROW) {
                    sb.append("<tr>");
                    videoNumberPerRow = 0;
                }
                File videoHtmlFile = new File(videosDirectory, z.getId() + ".html");
                {
                    StringBuilder sb2 = new StringBuilder("""
                   <!DOCTYPE html>
                  <html>
                  <head>
                  <link rel="icon" type="image/x-icon" href="../favicon.ico" sizes="16x16">
                  <title>"""
                            +z.getTitle() +
                            """
                  </title>
                  <style>
                  body {padding:20px;}
                  * {
                    font-family:Arial;
                  }

                  </style>
                  </head>
                  <body>
                  """
                    );
                    String finalUrl = "https://www.youtube.com/watch?v=" + z.getId();

                    sb2.append("<input type=\"text\" id=\"youtube_url\" name=\"youtube_url\" size=\"60\" width=\"60\" style=\"margint-bottom:20px;margin-right:10px;font-size:110%;padding:5px;\" value=\"" + finalUrl + "\">");
                                        sb2.append("<a target=\"_blank\" href=\"").append(finalUrl).append("\">");
                    sb2.append(finalUrl).append("</a>").append("<br>");
                    String videoLocalUrl = "";
                    try {
                        videoLocalUrl = "file:///" + archiveBoxRootDirectory.getAbsolutePath() + "/archive/" + z.getSnapshot() + "/media/" + URLEncoder.encode(z.getVideoFileName(), StandardCharsets.UTF_8.toString()).replace("+", "%20");
                    } catch (UnsupportedEncodingException ex) {
                        throw new ArchiveBoxYoutubeHelperException(ex.getMessage());
                    }
                    sb2.append("<a target=\"_blank\" href=\"").append(videoLocalUrl).append("\">");
 
                    sb2.append("<img style=\"margin:10px;width:600px;\" src=\"../archive/")
                            .append(z.getSnapshot())
                            .append("/media/thumbnail.jpg\"></a><br>");
                    sb2.append("<span style=\"font-size:160%;font-weight:bold;\">").append(z.getTitle()).append("</span>");
                    sb2.append("<br><br>");
                    sb2.append("<pre style=\"white-space: border:1px solid black;max-width:600px;padding:10px;min-height:50px;\">");
                    sb2.append(z.getDescription().isBlank() ? "No description" : z.getDescription());
                    sb2.append("</pre>");
                    sb2.append("<h2>Comments</h2>");
                    z.getComments().forEach(co -> {
      
//    private String id, parentId, text, author;
//    private int timestamp;
sb2.append("<div style=\"margin-left:")
        .append(co.dotCount() * 50)
        .append("px;\">");
                        sb2.append("<h3>").append(co.getAuthor()).append("</h3>");
                        
                        sb2.append("<span style=\"color:grey;font-size:80%;\">")
                                
                                .append(Utils.DATE_FORMAT.format(new Date(co.getTimestamp() * 1000))).append("</span><br>");
                        sb2.append("<span style=\"color:grey;font-size:80%;\">")
                                .append(co.getId() + " " + co.getParentId()).append("</span><br>");
                        sb2.append("<pre style=\"white-space: pre-wrap;border:1px solid black;max-width:600px;padding:10px;min-height:50px;\">").append(co.getText()).append("</pre>");
                        sb2.append("</div>");
                    });
                    
//                        private String id;
//    
//    private String title;
//    private String videoFileName = "";
//    private long videoFileSizeInBytes = 0;
//    private String videoFileSha512HashSum = "";
//    private String videoDuration = "";
//    private String channelName;
//    private String channelUrl;
//    private String channelId;
//    private String uploadDate;
//    private String description;
//    private String thumbnail;
//    private List<YoutubeComment> comments = new ArrayList<>();
    
    
    
                    sb2.append("</body></html>");
                    Utils.writeTextToFile(sb2.toString(), videoHtmlFile);
                }
                
            });
            if (videoNumberPerRow < VIDEOS_PER_ROW) {
                sb.append("<tr>");
            }
            sb.append("<table>\n");
            sb.append("</div></div>");
        });
        sb.append("""
                  
                  
                  </body>
                  </html> 
                  """);
        Utils.writeTextToFile(sb.toString(), videosHtmlFile);

    }

}
