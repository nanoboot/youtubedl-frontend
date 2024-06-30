/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.nanoboot.archiveboxyoutubehelper;

import lombok.Data;
import org.json.JSONObject;

/**
 *
 * @author robertvokac
 */
@Data
public class YoutubeComment implements Comparable<YoutubeComment>{
    private String id, parentId, text, author;
    private long timestamp;

    YoutubeComment(JSONObject jsonObject) {
        id = jsonObject.getString("id");
        parentId = jsonObject.getString("parent");
        text = jsonObject.getString("text");
        author = jsonObject.getString("author");
        timestamp = jsonObject.getInt("timestamp");
    }

    @Override
    public int compareTo(YoutubeComment o) {
        return Long.valueOf(this.timestamp).compareTo(o.timestamp);
    }
    public int dotCount() {
        int i = 0;
        for(char ch:getId().toCharArray()) {
            if(ch == '.') {
                i++;
            }
        }
        return i;
    }
}
