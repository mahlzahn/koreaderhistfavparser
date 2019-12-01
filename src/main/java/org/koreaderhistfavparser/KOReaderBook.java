package org.koreaderhistfavparser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class KOReaderBook {
    private String filePath;
    private Boolean finished = false;
    private Double percentFinished;
    private Long lastRead = (long) 0;
    private Integer pages;
    private String title;
    private String[] authors;
    private String[] keywords;
    private String language;
    private String[] series;
    private String sdrFilePath;
    private Long sdrFileLastModified = (long) 0;
    private JSONObject sdrJson;

    public KOReaderBook(String filePath) {
        this.filePath = filePath;
        sdrFilePath = sdrFilePath(filePath);
    }

    private  <T> Boolean propertyOutdated(T property) {
        Boolean sdrFileModified = sdrFileModified();
        if (property != null && !sdrFileModified)
            return false;
        if (sdrFileModified)
            readSdr();
        return true;
    }

    public String getFilePath() {
        return filePath;
    }

    public Boolean getFinished() {
        if (propertyOutdated(finished))
            try {
                String finishedString = sdrJson.getJSONObject("summary").getString("status");
                finished = (finishedString.equals("complete"));
            } catch (JSONException e) {}
        return finished;
    }

    public Boolean setFinished(Boolean finished) {
        if (finished)
            return setFinished();
        else
            return setReading();
    }

    public Boolean setFinished() {
        if (finished || sdrJson == null)
            return false;
        try {
            JSONObject summaryJson = sdrJson.getJSONObject("summary");
            summaryJson.put("status", "complete");
            finished = writeSdr();
            return finished;
        } catch (JSONException e) {
            return false;
        }
    }

    public Boolean setReading() {
        if (!finished || sdrJson == null)
            return false;
        try {
            JSONObject summaryJson = sdrJson.getJSONObject("summary");
            summaryJson.put("status", "reading");
            finished = writeSdr();
            return finished;
        } catch (JSONException e) {
            return false;
        }
    }

    public Double getPercentFinished() {
        if (propertyOutdated(percentFinished))
            try {
                percentFinished = sdrJson.getDouble("percent_finished");
            } catch (JSONException e) {}
        return percentFinished;
    }

    public Long getLastRead() {
        return lastRead;
    }

    public void setLastRead(Long lastRead) {
        this.lastRead = lastRead;
    }

    public Integer getPages() {
        if (propertyOutdated(pages))
            try {
                pages = sdrJson.getJSONObject("stats").getInt("pages");
            } catch (JSONException e) {}
        return pages;
    }

    public String getTitle() {
        if (propertyOutdated(title))
            try {
                title = sdrJson.getJSONObject("stats").getString("title");
            } catch (JSONException e) {}
        return title;
    }

    public String[] getAuthors() {
        if (propertyOutdated(authors))
            try {
                String authorsString = sdrJson.getJSONObject("stats").getString("authors");
                authors = authorsString.split(";;;;");
            } catch (JSONException e) {}
        return authors;
    }

    public String[] getKeywords() {
        if (propertyOutdated(keywords))
            try {
                String keywordsString = sdrJson.getJSONObject("stats").getString("keywords");
                keywords = keywordsString.split(";;;;");
            } catch (JSONException e) {}
        return keywords;
    }

    public String getLanguage() {
        if (propertyOutdated(keywords))
            try {
                language = sdrJson.getJSONObject("stats").getString("language");
            } catch (JSONException e) {}
        return language;
    }

    public String[] getSeries() {
        if (propertyOutdated(series))
            try {
                String seriesString = sdrJson.getJSONObject("stats").getString("series");
                series = seriesString.split(";;;;");
            } catch (JSONException e) {}
        return series;
    }

    private String sdrFilePath(String filePath) {
        String filePathWithoutExt = filePath.substring(0, filePath.lastIndexOf("."));
        String filePathExt = filePath.substring(filePath.lastIndexOf("."));
        return filePathWithoutExt + ".sdr/metadata" + filePathExt + ".lua";
    }

    Boolean sdrFileModified() {
        return sdrFileLastModified < new File(sdrFilePath).lastModified();
    }

    Boolean readSdr() {
        sdrFileLastModified = new File(sdrFilePath).lastModified();
        sdrJson = KOReaderLuaReadWrite.readLuaFile(sdrFilePath);
        return (sdrJson != null);
    }

    private Boolean writeSdr() {
        return KOReaderLuaReadWrite.writeLuaFile(sdrFilePath, sdrJson);
    }
}
