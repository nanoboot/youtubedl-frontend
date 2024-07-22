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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 *
 * @author robertvokac
 */
@Data
@AllArgsConstructor
@ToString
public class Args {

    public static final String TWO_DASHES = "--";
    private final Map<ArgType, Arg> map = new HashMap<>();

    public Args(String[] args) {
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (i == 0 && !arg.startsWith(TWO_DASHES)) {
                    continue;
                }
                Optional<ArgType> argType = Stream.of((ArgType.values()))
                        .filter(a -> arg.equals(TWO_DASHES + a.getName())).findFirst();

                if (argType.isPresent()) {
                    i++;
                    final ArgType argTypeGet = argType.get();
                    if (i >= args.length) {
                        throw new YoutubedlFrontendException("Fatal error: missing value for " + TWO_DASHES + argTypeGet.getName());
                    }
                    String value = args[i];
                    if (argTypeGet == ArgType.VIDEOS_PER_ROW) {
                        int argVideosPerRow = Integer.parseInt(args[i]);
                        if (argVideosPerRow < 2) {
                            value = "0";
                        }
                    }
                    map.put(argTypeGet, new Arg(argTypeGet, value));
                }

            }
        }
    }

    public Optional<String> getString(ArgType argType) {
        if (!map.containsKey(argType)) {

            return (argType.getDefaultValue() == null || argType.getDefaultValue().isEmpty()) ? Optional.empty() : Optional.of(argType.getDefaultValue());
        }
        return Optional.of(map.get(argType).getValue());

    }

    public Optional<Boolean> getBoolean(ArgType argType) {
        Optional<String> o = getString(argType);
        return o.isPresent() ? Optional.of(Utils.convertStringToBoolean(o.get())) : Optional.empty();
    }

    public Optional<Integer> getInteger(ArgType argType) {
        Optional<String> o = getString(argType);
        return o.isPresent() ? Optional.of(Integer.valueOf(o.get())) : Optional.empty();
    }
}
