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

import java.io.File;

/**
 * The class for a book with properties read from KOReader sdr files.
 */
public class KOReaderBook {
    // %t: title, %a: first author, %p: progress in percent, %s: series, %l: language
    private static final String STRING_FORMAT_DEFAULT = "[%a: ]%t[ (%p%)]";
    private static String stringFormat = STRING_FORMAT_DEFAULT;

    private String filePath;
    private Boolean finished = false;
    private Double percentFinished;     // progress in range [0, 1]
    private Long lastRead = (long) 0;   // time in Unix time
    private Integer pages;
    private String title;
    private String[] authors;
    private String[] keywords;
    private String language;
    private String series;
    private String sdrFilePath;
    private Long sdrFileLastModified = (long) 0;
    private JSONObject sdrJson;

    /**
     * Constructs a new KOReaderBook with the specified file path.
     *
     * @param filePath the book's file path
     * @throws IllegalArgumentException if given file path is invalid, e.g. without extension
     */
    public KOReaderBook(String filePath) throws IllegalArgumentException {
        this.filePath = filePath;
        sdrFilePath = sdrFilePath(filePath);
    }

    /**
     * Returns the string format for the string representation with the format classifiers
     * <ul>
     *     <li><code>%t: title</code>,</li>
     *     <li><code>%a: first author</code>,</li>
     *     <li><code>%p: progress in percent</code>,</li>
     *     <li><code>%s: series</code> and </li>
     *     <li><code>%l: language.</code>
     * </ul>
     * Optional classifiers are set by square brackets.
     * Defaults to <code>[%a: ]%t[ (%p%)]</code>.
     *
     * @return the string format
     */
    static public String getStringFormat() {
        return stringFormat;
    }

    /**
     * Sets the string format for the string representation of all books with the format classifiers
     * <ul>
     *     <li><code>%t: title</code>,</li>
     *     <li><code>%a: first author</code>,</li>
     *     <li><code>%p: progress in percent</code>,</li>
     *     <li><code>%s: series</code> and </li>
     *     <li><code>%l: language.</code>
     * </ul>
     * Optional classifiers are set by square brackets.
     * Defaults to <code>[%a: ]%t[ (%p%)]</code>.
     *
     * @param stringFormat the string format
     */
    static public void setStringFormat(String stringFormat) {
        if (stringFormat != null)
            KOReaderBook.stringFormat = stringFormat;
        else
            KOReaderBook.stringFormat = STRING_FORMAT_DEFAULT;
    }

    /**
     * Compares this with another object.
     *
     * @param obj to compare with
     * @return true if objects or if file paths are identical, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || !obj.getClass().equals(getClass()))
            return false;
        KOReaderBook book = (KOReaderBook) obj;
        return filePath.equals(book.filePath);
    }

    /**
     * Returns the hash code of the book's file path string.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return filePath.hashCode();
    }

    /**
     * Returns the formatted string according to the specified string format with the format
     * classifiers
     * <ul>
     *     <li><code>%t: title</code>,</li>
     *     <li><code>%a: first author</code>,</li>
     *     <li><code>%p: progress in percent</code>,</li>
     *     <li><code>%s: series</code> and </li>
     *     <li><code>%l: language.</code>
     * </ul>
     * Optional classifiers are set by square brackets. The string format can be set by
     * {@link #setStringFormat} and is returned by {@link #getStringFormat}. It defaults to
     * <code>[%a: ]%t[ (%p%)]</code>.
     *
     * @return formatted string
     */
    @Override
    public String toString() {
        String output = stringFormat;
        if (output.contains("%t")) {
            getTitle();
            if (title != null && !title.equals(""))
                output = output.replace("%t", title);
        }
        if (output.contains("%a")) {
            getAuthors();
            if (authors != null && authors.length != 0 && authors[0] != null)
                output = output.replace("%a", authors[0]);
        }
        if (output.contains("%p")) {
            getPercentFinished();
            if (percentFinished != null)
                output = output.replace("%p", String.valueOf(Math.round(100 * percentFinished)));
        }
        if (output.contains("%s")) {
            getSeries();
            if (series != null && !series.equals(""))
                output = output.replace("%s", series);
        }
        if (output.contains("%l")) {
            getLanguage();
            if (language != null && !language.equals(""))
                output = output.replace("%l", language);
        }
        String r1 = "\\[[^\\[\\]]*%[tapsl][^\\[\\]]*\\]";   // matches e.g. [%a: ], [ (%p%)]
        String r2 = "\\[([^\\[\\]]*)\\]";                   // e.g. [XY: ], [ (24%)], not [*[*]*]
        while (output.matches(".*" + r2 + ".*")) {
            while (output.matches(".*" + r1 + ".*")) {
                output = output.replaceAll(r1, "");
            }
            output = output.replaceAll(r2, "$1");
        }

        // if not optional by [] and not replaced above, replace by following terms
        output = output.replaceAll("%t", "(no title)");
        output = output.replaceAll("%a", "(no author)");
        output = output.replaceAll("%p", "(no progress)");
        output = output.replaceAll("%s", "(no series)");
        output = output.replaceAll("%l", "(no language)");
        return output;
    }

    /**
     * Returns the file path.
     *
     * @return the book's file path
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Returns true if book is finished, otherwise or if no sdr file is found false.
     *
     * @return true if book is finished, otherwise false
     */
    public Boolean getFinished() {
        if (sdrFileModified())
            readSdr();
        return finished;
    }

    /**
     * Sets the finished flag.
     *
     * @param finished true if book is to be set finished, otherwise false
     * @return true if successfully changed finished state, otherwise false
     */
    public Boolean setFinished(Boolean finished) {
        if (finished)
            return setFinished();
        else
            return setReading();
    }

    /**
     * Sets the finished flag to true.
     *
     * @return true if successfully changed finished state, otherwise false
     */
    public Boolean setFinished() {
        if (finished)
            return false;
        if (sdrJson == null)
            sdrJson = new JSONObject();
        JSONObject summaryJson;
        summaryJson = sdrJson.optJSONObject("summary");
        if (summaryJson == null) {
            summaryJson = new JSONObject();
        }
        try {
            summaryJson.put("status", "complete");
            sdrJson.put("summary", summaryJson);
        } catch (JSONException e) {
            return false;
        }
        finished = writeSdr();
        return finished;
    }

    /**
     * Sets the finished flag to false.
     *
     * @return true if successfully changed finished state, otherwise false
     */
    public Boolean setReading() {
        if (!finished || sdrJson == null)
            return false;
        JSONObject summaryJson = sdrJson.optJSONObject("summary");
        try {
            summaryJson.put("status", "reading");
        } catch (JSONException e) {
            return false;
        }
        finished = !writeSdr();
        return !finished;
    }

    /**
     * Returns the progress in the range [0,1].
     *
     * @return the percent finished; null if not extractable from sdr file
     */
    public Double getPercentFinished() {
        if (sdrFileModified())
            readSdr();
        return percentFinished;
    }

    /**
     * Returns the time of last reading in Unix time format.
     *
     * @return the time of last reading
     */
    public Long getLastRead() {
        return lastRead;
    }

    /**
     * Sets the time of last reading in Unix time format.
     *
     * @param lastRead the time of last reading
     */
    public void setLastRead(Long lastRead) {
        this.lastRead = lastRead;
    }

    /**
     * Returns the number of pages.
     *
     * @return the number of pages; null if not extractable from sdr file
     */
    public Integer getPages() {
        if (sdrFileModified())
            readSdr();
        return pages;
    }

    /**
     * Returns the title.
     *
     * @return the title; null if not extractable from sdr file
     */
    public String getTitle() {
        if (sdrFileModified())
            readSdr();
        return title;
    }

    /**
     * Returns the array of authors.
     *
     * @return the array of authors; null if not extractable from sdr file
     */
    public String[] getAuthors() {
        if (sdrFileModified())
            readSdr();
        return authors;
    }

    /**
     * Returns the array of keywords.
     *
     * @return the array of keywords; null if not extractable from sdr file
     */
    public String[] getKeywords() {
        if (sdrFileModified())
            readSdr();
        return keywords;
    }

    /**
     * Returns the language.
     *
     * @return the language; null if not extractable from sdr file
     */
    public String getLanguage() {
        if (sdrFileModified())
            readSdr();
        return language;
    }

    /**
     * Returns the series.
     *
     * @return the series; null if not extractable from sdr file
     */
    public String getSeries() {
        if (sdrFileModified())
            readSdr();
        return series;
    }

    /**
     * Returns the sdr file path for a given file path.
     *
     * @param filePath the book's file path
     * @return the sdr file path
     * @throws IllegalArgumentException if given file path is invalid, e.g. without extension
     */
    private String sdrFilePath(String filePath) throws IllegalArgumentException {
        try {
            String filePathWithoutExt = filePath.substring(0, filePath.lastIndexOf("."));
            String filePathExt = filePath.substring(filePath.lastIndexOf("."));
            return filePathWithoutExt + ".sdr/metadata" + filePathExt + ".lua";
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Given file path " + filePath
                    + " invalid. Missing extension?", e);
        }
    }

    /**
     * Returns true if sdr file has been modified since last access, otherwise false.
     *
     * @return true if sdr file has been modified, otherwise false
     */
    private Boolean sdrFileModified() {
        return sdrFileLastModified < new File(sdrFilePath).lastModified();
    }

    /**
     * Read the sdr file and convert internally to JSON object.
     *
     * @return true, if reading and conversion successfully, otherwise false
     */
    private Boolean readSdr() {
        sdrFileLastModified = new File(sdrFilePath).lastModified();
        sdrJson = KOReaderLuaReadWrite.readLuaFile(sdrFilePath);
        if (sdrJson != null) {
            JSONObject docPropsJson;
            try {
                String finishedString = sdrJson.getJSONObject("summary").getString("status");
                finished = (finishedString.equals("complete"));
            } catch (JSONException e) {}
            try {
                docPropsJson = sdrJson.getJSONObject("doc_props");
                try {
                    String authorsString = docPropsJson.getString("authors");
                    authors = authorsString.split(";;;;");
                } catch (JSONException e) {}
                try {
                    String keywordsString = docPropsJson.getString("keywords");
                    keywords = keywordsString.split(";;;;");
                } catch (JSONException e) {}
                try {
                    language = docPropsJson.getString("language");
                } catch (JSONException e) {}
                try {
                    series = docPropsJson.getString("series");
                } catch (JSONException e) {}
                try {
                    title = docPropsJson.getString("title");
                } catch (JSONException e) {}
            } catch (JSONException e) {}
            try {
                pages = sdrJson.getJSONObject("stats").getInt("pages");
            } catch (JSONException e) {}
            try {
                percentFinished = sdrJson.getDouble("percent_finished");
            } catch (JSONException e) {}
        }
        return (sdrJson != null);
    }

    /**
     * Converts the internal JSON object and writes the output to the sdr file.
     *
     * @return true, if conversion and writing successfully, otherwise false
     */
    private Boolean writeSdr() {
        return KOReaderLuaReadWrite.writeLuaFile(sdrFilePath, sdrJson);
    }
}
