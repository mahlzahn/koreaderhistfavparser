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
 * A class with static functions to read and write lua files and convert their data to or from JSON
 * objects.
 */
class KOReaderLuaReadWrite {
    /**
     * Reads the given lua file and converts the content to a json object.
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
                    .replaceAll("^--.*\n", "")
                    .replaceFirst("return ", "")
                    .replaceAll("\\[([0-9]+)\\] =", "\"$1\":")
                    .replaceAll("\\[(\"[a-zA-Z_0-9]+\")\\] =", "$1:")
                    .replaceAll("\\\\+\n([^{} ])", ";;;;$1")
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts the given JSON object and writes the content to the lua file with given file path.
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
            content = content.replaceFirst("^[{]+", "{\n")
                    .replaceAll("\n", "\n    ")
                    .replaceAll("}}", "}\n}");
        content = "return " + content
                .replaceAll("\"([0-9]+)\":", "[$1] =")
                .replaceAll("\"([a-zA-Z_0-9]+)\":", "[\"$1\"] =")
                .replaceAll(";;;;", "\\\\\n")
                .replaceAll("\\\\/", "/")
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
