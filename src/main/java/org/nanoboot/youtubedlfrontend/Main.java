///////////////////////////////////////////////////////////////////////////////////////////////
// youtubedl-frontend: Tool generating html pages for Archive Box.
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
package org.nanoboot.youtubedlfrontend;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.nanoboot.youtubedlfrontend.Args.TWO_DASHES;

/**
 * @author <a href="mailto:robertvokac@nanoboot.org">Robert Vokac</a>
 * @since 0.0.0
 */
public class Main {

    private static int iii = 0;
    private static int internalStaticVariableVideoNumberPerRow = 0;

    public static int THUMBNAIL_WIDTH = 250;


    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("youtubedlfrontend - HTML generator\n");

        if (args.length < 1) {
            //System.err.println("At least one argument is expected, but the count of arguments is: " + args.length + ".");
            String argsS = "/rv/blupi/archivebox --_video UDpsz1yIwiw --always-generate-metadata 0 "
                    + " --always-generate-html-files 0 --videos-per-row 4 --thumbnail-links-to-youtube 1"
                    + " --thumbnail-as-base64 1";
            args = argsS.split(" ");
            //System.exit(1);
        }
        Args argsInstance = new Args(args);
        System.out.println(argsInstance.toString());
        String workingDirectory = args.length > 0 && !args[0].startsWith(TWO_DASHES) ? args[0] : new File(".").getAbsolutePath();

        File archiveBoxRootDirectory = new File(workingDirectory);
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
                  
                  <!-- Generated by: https://code.nanoboot.org/nanoboot/youtubedl-frontend -->
                  
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
        NumberFormat formatter = new DecimalFormat("#0.00");
        channels.forEach(c -> {
            sb.append("<h1>").append(c).append("</h1>\n");
            sb.append("<div style=\"max-width:").append((Main.THUMBNAIL_WIDTH + 20) * argsInstance.getInteger(ArgType.VIDEOS_PER_ROW).get()).append("px\"><a href =\"").append(channelUrls.get(c)).append("\">").append(channelUrls.get(c)).append("</a><div class=\"videos\">");
            iii = 0;
            internalStaticVariableVideoNumberPerRow = 0;
            sb.append("<table>\n");
            youtubeVideos.stream().filter(v -> c.equals(v.getChannelName())).forEach(youtubeVideo -> {
                iii++;
                if (internalStaticVariableVideoNumberPerRow == 0) {
                    sb.append("<tr>");
                }
                internalStaticVariableVideoNumberPerRow++;
                sb.append("<td><div class=\"box\"><table style=\"margin:5px;max-width:")
                        .append(THUMBNAIL_WIDTH)
                        .append("px;\">\n<tr><td><a href=\"");
                if (argsInstance.getBoolean(ArgType.THUMBNAIL_LINKS_TO_YOUTUBE).get()) {
                    sb.append("https://www.youtube.com/watch?v=").append(youtubeVideo.getId());
                } else {
                    sb.append("videos/" + youtubeVideo.getId() + ".html");
                }
                        sb.append("\" target=\"_blank\"><img src=\"");
                        String thumbnailPath = new StringBuilder()
                                .append("archive/")
                    .append(youtubeVideo.getSnapshot())
                    .append("/media/mini-thumbnail.")
                    .append(youtubeVideo.getMiniThumbnailFormat()).toString();
                if (argsInstance.getBoolean(ArgType.THUMBNAIL_AS_BASE64).get()) {
                    try {
                        byte[] bytes = Files.readAllBytes(new File(archiveBoxRootDirectory + "/" + thumbnailPath).toPath());
                        System.out.println("###=" + archiveBoxRootDirectory + "/" + thumbnailPath);
                        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                        try {
                            bytes = Utils.resizeImage(bais, 25, (int) (9d / 16d * 25d), youtubeVideo.getThumbnailFormat());
                        } catch (Exception e) {
                            //bytes = Utils.resizeImage(bais, 125, (int) (9d / 16d * 125d), "webp");
                        }
                        
                        String bytesS = "data:image/jpg;base64, " + org.nanoboot.powerframework.io.bit.base64.Base64Coder.encode(bytes);
                        sb.append(bytesS);
                    } catch (IOException ex) {
                        throw new YoutubedlFrontendException(ex.getMessage());
                    }
                } else {
                    sb.append(thumbnailPath);
                }
                        sb.append("\" width=\"")
                        .append(THUMBNAIL_WIDTH)
                        .append("\"></a></td></tr>\n");
                sb.append("<tr><td><b style=\"font-size:90%;\">").append(youtubeVideo.getTitle()).append("</b></td></tr>\n");
                String uploadDate = youtubeVideo.getUploadDate();
                uploadDate = uploadDate.substring(0, 4) + "-" + uploadDate.substring(4, 6) + "-" + uploadDate.substring(6, 8);
                sb.append("<tr><td style=\"font-size:80%;color:grey;\">").append(uploadDate).append(" •︎ ").append(youtubeVideo.getVideoDuration())
                        .append(" •︎ ")
                        .append("#").append(iii)
                        .append("</td></tr>\n");
                youtubeVideo.setNumber(iii);
                sb.append("</table></div></td>\n");
                if (internalStaticVariableVideoNumberPerRow == argsInstance.getInteger(ArgType.VIDEOS_PER_ROW).get()) {
                    sb.append("<tr>");
                    internalStaticVariableVideoNumberPerRow = 0;
                }
                File videoHtmlFile = new File(videosDirectory, youtubeVideo.getId() + ".html");
                if (!videoHtmlFile.exists() || argsInstance.getBoolean(ArgType.ALWAYS_GENERATE_HTML_FILES).get()) {

                    {
                        StringBuilder videoHtml = new StringBuilder("""
                   <!DOCTYPE html>
                  <html>
                  <head>
                  <meta charset="UTF-8">                                    
                  <link rel="icon" type="image/x-icon" href="../favicon.ico" sizes="16x16">
                  <title>"""
                                + youtubeVideo.getTitle()
                                + """
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
                        String finalUrl = "https://www.youtube.com/watch?v=" + youtubeVideo.getId();

                        videoHtml.append("<input type=\"text\" id=\"youtube_url\" name=\"youtube_url\" size=\"60\" width=\"60\" style=\"margint-bottom:20px;margin-right:10px;font-size:110%;padding:5px;\" value=\"" + finalUrl + "\"><br>\n<br>\n");
                        videoHtml.append("<a target=\"_blank\" href=\"").append(finalUrl).append("\">");
                        videoHtml.append(finalUrl).append("</a>").append("<br>\n");
                        String videoLocalUrl = "";
                        try {
                            videoLocalUrl = "file:///" + archiveBoxRootDirectory.getAbsolutePath() + "/archive/" + youtubeVideo.getSnapshot() + "/media/" + URLEncoder.encode(youtubeVideo.getVideoFileName(), StandardCharsets.UTF_8.toString()).replace("+", "%20");
                        } catch (UnsupportedEncodingException ex) {
                            throw new YoutubedlFrontendException(ex.getMessage());
                        }
                        if (!youtubeVideo.getVideoFileName().endsWith(".mkv")) {
                            //try {
                            videoHtml.append("<video src=\"");

                            videoHtml.append("../archive/").append(youtubeVideo.getSnapshot()).append("/media/").append(//                                    URLEncoder.encode(
                                    youtubeVideo.getVideoFileName());
                            videoHtml.append("""

                                             " controls height=\"500px\">
                                                               Your browser does not support the video tag.
                                                               </video><br>
                                                               """);
//                        } catch (UnsupportedEncodingException ex) {
//                            throw new YoutubedlFrontendException(ex.getMessage());
//                        }
                        } 
                        else 
                        {
                            videoHtml.append("<a target=\"_blank\" href=\"").append(videoLocalUrl).append("\">");

                            videoHtml.append("<img style=\"margin:10px;height:500px;\" src=\"../archive/")
                                    .append(youtubeVideo.getSnapshot())
                                    .append("/media/thumbnail.")
                                    .append(youtubeVideo.getThumbnailFormat())
                                    .append("\"></a><br>\n");
                        }
                        videoHtml.append("<span style=\"font-size:160%;font-weight:bold;\">").append(youtubeVideo.getTitle()).append("</span>");
                        videoHtml.append("<br>\n<br>\n");
                        videoHtml.append("#").append(youtubeVideo.getNumber()).append("&nbsp;&nbsp;&nbsp;");
                        if (youtubeVideo.getPreviousVideoId() != null) {
//                            videoHtml.append("<a href=\"./").append(youtubeVideo.getPreviousVideoId()).append(".html\">");
                            videoHtml.append("<button style=\"font-size:150%;\" onclick=\"window.location ='").append("./").append(youtubeVideo.getPreviousVideoId()).append(".html'\">");
                            //<button class="link" onclick="alert(1)">Click</button>
                        }
                        videoHtml.append("Back");
                        if (youtubeVideo.getPreviousVideoId() != null) {
                            //videoHtml.append("</a>");
                            videoHtml.append("</button>");
                        }
                        videoHtml.append("&nbsp;&nbsp;&nbsp;");
                        if (youtubeVideo.getNextVideoId() != null) {
                            //videoHtml.append("<a href=\"./").append(youtubeVideo.getNextVideoId()).append(".html\">");
                            videoHtml.append("<button style=\"font-size:150%;\" onclick=\"window.location ='").append("./").append(youtubeVideo.getNextVideoId()).append(".html'\">");
                        }
                        videoHtml.append("Next");
                        if (youtubeVideo.getNextVideoId() != null) {
                            //videoHtml.append("</a>");
                            videoHtml.append("</button>");

                        }
                        videoHtml.append(" ");
                        videoHtml
                                .append("<br><br><a href=\"../archive/")
                                .append(youtubeVideo.getSnapshot())
                                .append("/media/")
                                .append(youtubeVideo.getVideoFileName())
                                .append("\">Download</a> ");

                        videoHtml.append(formatter.format(((double) youtubeVideo.getVideoFileSizeInBytes()) / 1024d / 1024d)).append(" MB ");
                        if (youtubeVideo.getVideoFileName().endsWith(".mkv")) {
                            
                            String v = youtubeVideo.getVideoFileName().replaceAll(" ", "\\\\ ");
                            v = v.replace("(", "\\(");
                            v = v.replace(")", "\\)");
                            var vWebm = v.substring(0, v.length() - 3) + "webm";

                            videoHtml.append("<input type=\"text\" id=\"archiveBoxArchiveDirectory\" name=\"archiveBoxArchiveDirectory\" size=\"100\" width=\"100\" style=\"margin-bottom:20px;margin-right:10px;font-size:110%;padding:5px;\" value=\"");
                            videoHtml.append("cd ").append(archiveBoxArchiveDirectory).append("/").append(youtubeVideo.getSnapshot()).append("/media/");
                                    videoHtml.append(" && ffmpeg -i ").append(v).append(" -preset slow -crf 18 ").append(vWebm)                            ;
                                    videoHtml.append("\"><br>");
                                    
                        } else {
                                                        
                            
                            videoHtml.append("<input type=\"text\" id=\"archiveBoxArchiveDirectory\" name=\"archiveBoxArchiveDirectory\" size=\"100\" width=\"100\" style=\"margin-bottom:20px;margin-right:10px;font-size:110%;padding:5px;\" value=\"");
                            videoHtml.append(archiveBoxArchiveDirectory).append("/").append(youtubeVideo.getSnapshot()).append("/media/");
                                    
                                    videoHtml.append("\"><br>");
                        }
                        videoHtml.append("<a target=\"_blank\" href=\"file://").append(archiveBoxArchiveDirectory).append("/").append(youtubeVideo.getSnapshot()).append("/media\">Directory</a>").append("<br>");
                        videoHtml.append("<br>\n<br>\n");

                        videoHtml.append("<br>\n");
                        videoHtml.append("<pre style=\"white-space: pre-wrap; border:1px solid black;max-width:600px;padding:10px;min-height:50px;\">");
                        videoHtml.append(youtubeVideo.getDescription().isBlank() ? "No description" : youtubeVideo.getDescription());
                        videoHtml.append("</pre>");
                        videoHtml.append("<h2>Comments</h2>");
                        youtubeVideo.getComments().forEach(co -> {

//    private String id, parentId, text, author;
//    private int timestamp;
                            videoHtml.append("<div style=\"margin-left:")
                                    .append(co.dotCount() * 50)
                                    .append("px;\">");
                            videoHtml.append("<h3>").append(co.getAuthor()).append("</h3>");

                            videoHtml.append("<span style=\"color:grey;font-size:80%;\">")
                                    .append(Utils.DATE_FORMAT.format(new Date(co.getTimestamp() * 1000))).append("</span><br>\n");
                            videoHtml.append("<span style=\"color:grey;font-size:80%;\">").append(co.getId()).append(" ")
                                    .append(co.getParentId()).append("</span><br>\n");
                            videoHtml.append("<pre style=\"white-space: pre-wrap;border:1px solid black;max-width:600px;padding:10px;min-height:50px;\">").append(co.getText()).append("</pre>");
                            videoHtml.append("</div>");
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
                        videoHtml.append("</body></html>");
                        String singleVideo = videoHtml.toString();
                        //singleVideo.replace("<br>\n", "<br>\n\n");
                        Utils.writeTextToFile(singleVideo, videoHtmlFile);
                    }
                }
            });
            if (internalStaticVariableVideoNumberPerRow < argsInstance.getInteger(ArgType.VIDEOS_PER_ROW).get()) {
                sb.append("<tr>");
            }
            sb.append("</table>\n");
            sb.append("</div></div>");
        });
        sb.append("""
                  
                  
                  </body>
                  </html> 
                  """);
        Utils.writeTextToFile(sb.toString(), videosHtmlFile);

        System.out.println("[Warning] Snapshots without videos:");
        YoutubeVideo.missingYoutubeVideos.forEach(s -> System.out.println(s));
    }

}
