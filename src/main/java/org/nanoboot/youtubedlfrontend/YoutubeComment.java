/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.nanoboot.youtubedlfrontend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

/**
 *
 * @author robertvokac
 */
@Data
@NoArgsConstructor
public class YoutubeComment implements Comparable<YoutubeComment> {

    private String id, parentId, text, author;
    private long timestamp;

    YoutubeComment(JSONObject jsonObject) {
        id = jsonObject.getString("id");
        parentId = jsonObject.getString("parent");
        text = jsonObject.getString("text");
        author = jsonObject.getString("author");
        timestamp = jsonObject.getInt("timestamp");
    }
    public static List<YoutubeComment> sort(List<YoutubeComment> list) {
        
        List<YoutubeComment> root = getChildren(list, "root");
        Collections.sort(root);
        return list;
        //return sort(list, new ArrayList<>(), "root");
    }
    private static List<YoutubeComment> getChildren(List<YoutubeComment> all, String parentId) {
        final List<YoutubeComment> children = all.stream().filter(c -> c.getParentId().equals(parentId)).sorted().toList();
        List<YoutubeComment> result = new ArrayList<>();
        children.stream().forEach(c -> {
            result.add(c);
            result.addAll(getChildren(all, c.getId()));
        });
        return result;
    }
    
    @Override
    public int compareTo(YoutubeComment o) {
        //if(this.timestamp != o.timestamp) {
        //            return this.id.compareTo(o.id);
        return Long.valueOf(this.timestamp).compareTo(o.timestamp);
//} 
//        else {
//            return this.id.compareTo(o.id);
//        }
    }

    public int dotCount() {
        int i = 0;
        for (char ch : getId().toCharArray()) {
            if (ch == '.') {
                i++;
            }
        }
        return i;
    }

}
