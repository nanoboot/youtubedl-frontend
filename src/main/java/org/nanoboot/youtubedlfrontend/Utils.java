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

import dev.mccue.guava.hash.Hashing;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author pc00289
 */
public class Utils {

    private static final String UNDERSCORE = "_";

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private Utils() {
        //Not meant to be instantiated.
    }

    public static String replaceUnderscoresBySpaces(String s) {
        if (!s.contains(UNDERSCORE)) {
            //nothing to do
            return s;
        }
        return s.replace(UNDERSCORE, " ");
    }

    public static String makeFirstLetterUppercase(String s) {
        if (Character.isLetter(s.charAt(0)) && Character.isLowerCase(s.charAt(0))) {
            return Character.toUpperCase(s.charAt(0))
                    + (s.length() == 1 ? "" : s.substring(1));
        } else {
            return s;
        }
    }

    public static int getCountOfSlashOccurences(String string) {
        int i = 0;
        for (char ch : string.toCharArray()) {
            if (ch == '/') {
                i++;
            }
        }
        return i++;
    }

    public static List<File> listAllFilesInDir(File dir) {
        return listAllFilesInDir(dir, new ArrayList<>());
    }

    private static List<File> listAllFilesInDir(File dir, List<File> files) {
        files.add(dir);
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                listAllFilesInDir(f, files);
            } else {
                files.add(f);
            }
        }
        return files;
    }

    public static String createDoubleDotSlash(int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= times; i++) {
            sb.append("../");
        }
        String result = sb.toString();
        return result;//.substring(0, result.length() - 1);
    }

    public static void copyFile(File originalFile, File copiedFile) throws YoutubedlFrontendException {
        Path originalPath = originalFile.toPath();
        Path copied = new File(copiedFile, originalFile.getName()).toPath();

        try {
            Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new YoutubedlFrontendException("Copying file failed: " + originalFile.getAbsolutePath());
        }
    }

    public static void writeTextToFile(String text, File file) {
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(file);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new YoutubedlFrontendException("Writing to file failed: " + file.getName(), ex);
        }
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print(text);
        printWriter.close();
    }

    public static String readTextFromFile(File file) {
        if (!file.exists()) {
            return "";
        }
        try {
            return new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        } catch (IOException ex) {
            throw new YoutubedlFrontendException("Reading file failed: " + file.getName(), ex);
        }
    }

    public static String readTextFromResourceFile(String fileName) {
        try {
            Class clazz = Main.class;
            InputStream inputStream = clazz.getResourceAsStream(fileName);
            return readFromInputStream(inputStream);
        } catch (IOException ex) {
            throw new YoutubedlFrontendException("Reading file failed: " + fileName, ex);
        }

    }

    public static String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    public static String calculateSHA512Hash(File file) {
        try {
            return dev.mccue.guava.io.Files.hash(file, Hashing.sha512()).toString();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            throw new YoutubedlFrontendException(ex.getMessage());
        }
    }

}
