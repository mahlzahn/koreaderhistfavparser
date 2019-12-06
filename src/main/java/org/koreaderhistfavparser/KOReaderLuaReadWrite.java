/*
 * Copyright (C) 2019 Robert Wolff <https://github.com/mahlzahn>
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library. If not, see <https://www.gnu.org/licenses>.
 *
 * The author(s) of this library permit(s) the redistribution and/or
 * modification of the source code in src/main/ to the author(s) of
 * the Relaunch application <https://github.com/yiselieren/ReLaunch>
 * and any fork of the Relaunch application under the terms of the
 * GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or any later version.
 */

package org.koreaderhistfavparser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * A class with static functions to read and write lua files and convert their data to and from JSON
 * objects.<br>
 * Arrays found in lua files are converted to strings with ";;;;" delimiter. This is
 * because in lua files arrays with one element are not distinguishable from single elements.
 * JSON Arrays are not supported in the conversion to lua script.
 */
class KOReaderLuaReadWrite {
    /**
     * Reads the given lua file and converts the content to a json object.<br>
     * Arrays found in lua files are converted to strings with ";;;;" delimiter.
     *
     * @param filePath the file path of the lua file
     * @return the converted json object, if reading and conversion successful, otherwise null
     */
    static JSONObject readLuaFile(String filePath) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        char[] buffer = new char[10];
        try {
            while (reader.read(buffer) != -1) {
                stringBuilder.append(new String(buffer));
                buffer = new char[10];
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        String content = stringBuilder.toString();
        try {
            return new JSONObject(content
                    // line comments starting with --
                    .replaceAll("^--.*\n", "")
                    // first code line in KOReader lua files is always "return {"
                    .replaceFirst("return ", "")
                    // "[...] = " --> "...: "
                    //.replaceAll("\\[([0-9]+)\\] =", "\"$1\":")
                    .replaceAll("\\[\"?([a-zA-Z_0-9]+)\"?] =", "\"$1\":")
                    // arrays in lua given with "\" and new line --> strings with ";;;;" delimiter
                    .replaceAll("\\\\\n", ";;;;")
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts the given JSON object and writes the content to the lua file with given file path.
     * <br>
     * Strings with ";;;;" delimiter are interpreted as arrays of strings. JSON Arrays are not
     * supported.
     *
     * @param filePath   the file path of the lua file
     * @param jsonObject the json object to be converted
     * @return true if conversion and writing successful, otherwise false
     */
    static Boolean writeLuaFile(String filePath, JSONObject jsonObject) {
        String content;
        try {
            content = jsonObject.toString(4);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        if (content.startsWith("{\""))
            // nice formatting with only "{" in the first line and "}" in the last line
            content = content.replaceFirst("^\\{", "{\n")
                    .replaceAll("\n", "\n    ")
                    .replaceAll("}}$", "}\n}");
        // first code line in KOReader lua files is always "return {"
        content = "return " + content
                // '"1": ' --> '[1] = '
                .replaceAll("\"([0-9]+)\":", "[$1] =")
                // '"xyz": ' --> '["xyz"] = '
                .replaceAll("\"([a-zA-Z_0-9]+)\":", "[\"$1\"] =")
                // strings with ";;;;" delimiter --> arrays in lua given with "\" and new line
                .replaceAll(";;;;", "\\\\\n")
                // nice formatting without one liners like '["a"] = {["b"] = "c"}'
                .replaceAll("( *)(.*)\\{(.*)}", "$1$2{\n$1    $3\n$1}")
                + "\n";
        FileOutputStream fos;
        try {
            new File(filePath).getParentFile().mkdirs();
            fos = new FileOutputStream(filePath);
            fos.write(content.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
