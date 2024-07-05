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
    private static int internalStaticVariableVideoNumberPerRow = 0;
    public static boolean argAlwaysGenerateMetadata = true;
    public static boolean argAlwaysGenerateHtmlFiles = true;
    public static int argVideosPerRow = 4;
    public static int THUMBNAIL_WIDTH = 250;
    public static String argVideo;
    public static String argChannel;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("youtubedlfrontend - HTML generator\n");

        args = "/rv/blupi/archivebox --_video UDpsz1yIwiw --always-generate-metadata 1 --always-generate-html-files 0 --videos-per-row 4".split(" ");
        //args = "/rv/databig/youtube --_video UDpsz1yIwiw --always-generate-metadata 1 --always-generate-html-files 1 --videos-per-row 4".split(" ");

        if (args.length < 1) {
            System.err.println("At least one argument is expected, but the count of arguments is: " + args.length + ".");
            System.exit(1);
        }
        argVideo = "";
        argChannel = "";
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (i == 0 && !arg.startsWith(TWO_DASHES)) {
                    continue;
                }
                if (arg.equals("--video")) {
                    i++;
                    if (i >= args.length) {
                        throw new YoutubedlFrontendException("Fatal error: missing value for --video");
                    }
                    argVideo = args[i];
                }
                if (arg.equals("--channel")) {
                    i++;
                    if (i >= args.length) {
                        throw new YoutubedlFrontendException("Fatal error: missing value for --channel");
                    }
                    argChannel = args[i];
                }

                if (arg.equals("--videos-per-row")) {
                    i++;
                    if (i >= args.length) {
                        throw new YoutubedlFrontendException("Fatal error: missing value for --videos-per-row");
                    }
                    argVideosPerRow = Integer.parseInt(args[i]);
                    if (argVideosPerRow < 2) {
                        argVideosPerRow = 0;
                    }
                }

                if (arg.equals("--always-generate-metadata")) {
                    i++;
                    if (i >= args.length) {
                        throw new YoutubedlFrontendException("Fatal error: missing value for --always-generate-metadata");
                    }
                    String s = args[i];
                    switch (s) {
                        case "1":
                            argAlwaysGenerateMetadata = true;
                            break;
                        case "true":
                            argAlwaysGenerateMetadata = true;
                            break;
                        case "0":
                            argAlwaysGenerateMetadata = false;
                            break;
                        case "false":
                            argAlwaysGenerateMetadata = false;
                            break;
                        default:
                            throw new YoutubedlFrontendException("Invalid value for --always-generate-metadata");
                    };
                }

                if (arg.equals("--always-generate-html-files")) {
                    i++;
                    if (i >= args.length) {
                        throw new YoutubedlFrontendException("Fatal error: missing value for --always-generate-html-files");
                    }
                    String s = args[i];
                    switch (s) {
                        case "1":
                            argAlwaysGenerateHtmlFiles = true;
                            break;
                        case "true":
                            argAlwaysGenerateHtmlFiles = true;
                            break;
                        case "0":
                            argAlwaysGenerateHtmlFiles = false;
                            break;
                        case "false":
                            argAlwaysGenerateHtmlFiles = false;
                            break;
                        default:
                            throw new YoutubedlFrontendException("Invalid value for --always-generate-html-files");
                    };

                }
            }
        }
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
            YoutubeVideo youtubeVideo = new YoutubeVideo(mediaDirectory);
            if (!Main.argVideo.isBlank() && !youtubeVideo.getId().equals(Main.argVideo)) {
                continue;
            }
            if (!argVideo.isBlank() && !youtubeVideo.getId().equals(argVideo)) {
                continue;
            }
            if (!argChannel.isBlank() && !youtubeVideo.getChannelId().equals(argChannel)) {
                continue;
            }
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
            sb.append("<div style=\"max-width:").append((Main.THUMBNAIL_WIDTH + 20) * Main.argVideosPerRow).append("px\"><a href =\"").append(channelUrls.get(c)).append("\">").append(channelUrls.get(c)).append("</a><div class=\"videos\">");
            iii = 0;
            internalStaticVariableVideoNumberPerRow = 0;
            sb.append("<table>\n");
            youtubeVideos.stream().filter(v -> c.equals(v.getChannelName())).forEach(z -> {
                iii++;
                if (internalStaticVariableVideoNumberPerRow == 0) {
                    sb.append("<tr>");
                }
                internalStaticVariableVideoNumberPerRow++;
                sb.append("<td><div class=\"box\"><table style=\"margin:5px;max-width:")
                        .append(THUMBNAIL_WIDTH)
                        .append("px;\">\n<tr><td><a href=\"videos/" + z.getId() + ".html\" target=\"_blank\"><img src=\"archive/");
                sb.append(z.getSnapshot());
                sb
                        .append("/media/mini-thumbnail.")
                        .append(z.getMiniThumbnailFormat())
                        .append("\" width=\"")
                        .append(THUMBNAIL_WIDTH)
                        .append("\"></a></td></tr>\n");
                sb.append("<tr><td><b style=\"font-size:90%;\">").append(z.getTitle()).append("</b></td></tr>\n");
                String uploadDate = z.getUploadDate();
                uploadDate = uploadDate.substring(0, 4) + "-" + uploadDate.substring(4, 6) + "-" + uploadDate.substring(6, 8);
                sb.append("<tr><td style=\"font-size:80%;color:grey;\">").append(uploadDate).append(" •︎ ").append(z.getVideoDuration())
                        .append(" •︎ ")
                        .append("#").append(iii)
                        .append("</td></tr>\n");
                z.setNumber(iii);
                sb.append("</table></div></td>\n");
                if (internalStaticVariableVideoNumberPerRow == argVideosPerRow) {
                    sb.append("<tr>");
                    internalStaticVariableVideoNumberPerRow = 0;
                }
                File videoHtmlFile = new File(videosDirectory, z.getId() + ".html");
                if(!videoHtmlFile.exists() || argAlwaysGenerateHtmlFiles) {
                    

                {
                    StringBuilder sb2 = new StringBuilder("""
                   <!DOCTYPE html>
                  <html>
                  <head>
                  <link rel="icon" type="image/x-icon" href="../favicon.ico" sizes="16x16">
                  <title>"""
                            + z.getTitle()
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
                    String finalUrl = "https://www.youtube.com/watch?v=" + z.getId();

                    sb2.append("<input type=\"text\" id=\"youtube_url\" name=\"youtube_url\" size=\"60\" width=\"60\" style=\"margint-bottom:20px;margin-right:10px;font-size:110%;padding:5px;\" value=\"" + finalUrl + "\"><br>\n<br>\n");
                    sb2.append("<a target=\"_blank\" href=\"").append(finalUrl).append("\">");
                    sb2.append(finalUrl).append("</a>").append("<br>\n");
                    String videoLocalUrl = "";
                    try {
                        videoLocalUrl = "file:///" + archiveBoxRootDirectory.getAbsolutePath() + "/archive/" + z.getSnapshot() + "/media/" + URLEncoder.encode(z.getVideoFileName(), StandardCharsets.UTF_8.toString()).replace("+", "%20");
                    } catch (UnsupportedEncodingException ex) {
                        throw new YoutubedlFrontendException(ex.getMessage());
                    }
                    if(!z.getVideoFileName().endsWith(".mkv")) {
                        try {
                            sb2.append("<video src=\"");
                            
                            sb2.append("../archive/" + z.getSnapshot() + "/media/" + URLEncoder.encode(z.getVideoFileName(), StandardCharsets.UTF_8.toString()).replace("+", "%20"));
                            sb2.append("""
                                                                         " controls width=\"800\">
                                                               Your browser does not support the video tag.
                                                               </video><br>
                                                               """);
                        } catch (UnsupportedEncodingException ex) {
                            throw new YoutubedlFrontendException(ex.getMessage());
                        }
                    } else {
                    sb2.append("<a target=\"_blank\" href=\"").append(videoLocalUrl).append("\">");

                    sb2.append("<img style=\"margin:10px;width:600px;\" src=\"../archive/")
                            .append(z.getSnapshot())
                            .append("/media/thumbnail.")
                            .append(z.getThumbnailFormat())
                            .append("\"></a><br>\n");
                    }
                    sb2.append("<span style=\"font-size:160%;font-weight:bold;\">").append(z.getTitle()).append("</span>");
                    sb2.append("<br>\n<br>\n");
                    sb2.append("#").append(z.getNumber()).append("&nbsp;&nbsp;&nbsp;");
                    if (z.getPreviousVideoId() != null) {
                        sb2.append("<a href=\"./").append(z.getPreviousVideoId()).append(".html\">");
                    }
                    sb2.append("Back");
                    if (z.getPreviousVideoId() != null) {
                        sb2.append("</a>");
                    }
                    sb2.append("&nbsp;&nbsp;&nbsp;");
                    if (z.getNextVideoId() != null) {
                        sb2.append("<a href=\"./").append(z.getNextVideoId()).append(".html\">");
                    }
                    sb2.append("Next");
                    if (z.getNextVideoId() != null) {
                        sb2.append("</a>");
                    }
                    sb2.append(" ");
                    sb2.append("<br>\n");
                    sb2.append("<pre style=\"white-space: pre-wrap; border:1px solid black;max-width:600px;padding:10px;min-height:50px;\">");
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
                                .append(Utils.DATE_FORMAT.format(new Date(co.getTimestamp() * 1000))).append("</span><br>\n");
                        sb2.append("<span style=\"color:grey;font-size:80%;\">")
                                .append(co.getId() + " " + co.getParentId()).append("</span><br>\n");
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
                    String singleVideo = sb2.toString();
                    //singleVideo.replace("<br>\n", "<br>\n\n");
                    Utils.writeTextToFile(singleVideo, videoHtmlFile);
                }
                }
            });
            if (internalStaticVariableVideoNumberPerRow < argVideosPerRow) {
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
    private static final String TWO_DASHES = "--";

}
