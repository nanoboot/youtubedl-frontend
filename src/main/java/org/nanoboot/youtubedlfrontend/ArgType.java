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

import lombok.Getter;

/**
 *
 * @author robertvokac
 */
public enum ArgType {
    VIDEO("video", null),
    CHANNEL("channel", null),
    VIDEOS_PER_ROW("videos-per-row", "4"),
    ALWAYS_GENERATE_METADATA("always-generate-metadata", "true"),
    ALWAYS_GENERATE_HTML_FILES("always-generate-html-files", "true"),
    THUMBNAIL_AS_BASE64("thumbnail-as-base64", "false"),
    THUMBNAIL_LINKS_TO_YOUTUBE("thumbnail-links-to-youtube", "false");
    @Getter
    private String name;
    @Getter
    private String defaultValue;
    ArgType(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }
    
}
