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
