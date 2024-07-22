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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Date;
import lombok.Getter;

/**
 *
 * @author robertvokac
 */
public class YoutubeVideoHtml {

    @Getter
    private String singleVideo;

    public YoutubeVideoHtml(YoutubeVideo youtubeVideo, File archiveBoxRootDirectory, File archiveBoxArchiveDirectory) {
          
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
        } else {
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
        videoHtml.append(Utils.TWO_DECIMAL_POINTS_FORMATTER.format(((double) youtubeVideo.getVideoFileSizeInBytes()) / 1024d / 1024d)).append(" MB ");
        if (youtubeVideo.getVideoFileName().endsWith(".mkv")) {
            
            String v = youtubeVideo.getVideoFileName().replaceAll(" ", "\\\\ ");
            v = v.replace("(", "\\(");
            v = v.replace(")", "\\)");
            var vWebm = v.substring(0, v.length() - 3) + "webm";
            
            videoHtml.append("<input type=\"text\" id=\"archiveBoxArchiveDirectory\" name=\"archiveBoxArchiveDirectory\" size=\"100\" width=\"100\" style=\"margin-bottom:20px;margin-right:10px;font-size:110%;padding:5px;\" value=\"");
            videoHtml.append("cd ").append(archiveBoxArchiveDirectory).append("/").append(youtubeVideo.getSnapshot()).append("/media/");
            videoHtml.append(" && ffmpeg -i ").append(v).append(" -preset slow -crf 18 ").append(vWebm);
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
        videoHtml.append("</body></html>");
        singleVideo = videoHtml.toString();
    
    }
    @Override
    public String toString() {
        return this.singleVideo;
    }
    
}
