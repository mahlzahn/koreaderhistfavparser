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

import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The class for KOReader history and favorites management.
 */
public class KOReaderHistFav {
    private final static String TAG = "KOReaderHistFav";

    // automatically detected external storage path during construction
    private static String externalStoragePath;
    private final static String EXTERNAL_STORAGE_PATH_DEFAULT = "/storage/emulated/0";

    // something like "/storage/emulated/0/koreader"
    private String koreaderDirectoryPath;
    private final static String HISTORY_FILE_PATH = "history.lua";
    private String historyFilePath;
    private final static String COLLECTION_FILE_PATH = "settings/collection.lua";
    private String collectionFilePath;
    private JSONObject historyJson;
    private JSONObject collectionJson;
    private Long historyLastModified = (long) 0;
    private Long collectionLastModified = (long) 0;
    // Map with filePath as key and Book as value
    private HashMap<String, KOReaderBook> books = new HashMap<>();
    private ArrayList<KOReaderBook> history = new ArrayList<>();
    private ArrayList<KOReaderBook> favorites = new ArrayList<>();

    /**
     * Constructs a new KOReaderHistFav. Searches in external storage and external SD card storage
     * for the settings directory with the name koreader.
     *
     * @throws FileNotFoundException if KOReader settings directory not found
     */
    public KOReaderHistFav() throws FileNotFoundException {
        this(null);
    }

    /**
     * Constructs a new KOReaderHistFav from given settings directory.
     *
     * @param koreaderDirectoryPath the koreader directory path
     * @throws FileNotFoundException if KOReader settings directory not found
     */
    public KOReaderHistFav(String koreaderDirectoryPath) throws FileNotFoundException {
        this.koreaderDirectoryPath = koreaderDirectoryPath(koreaderDirectoryPath);
        historyFilePath = this.koreaderDirectoryPath + "/" + HISTORY_FILE_PATH;
        collectionFilePath = this.koreaderDirectoryPath + "/" + COLLECTION_FILE_PATH;
        Log.d(TAG, "Set up with history file " + historyFilePath
                + " and with collection file " + collectionFilePath);
    }

    /**
     * Returns the external storage path for books. Defaults to "/mnt/sdcard".
     *
     * @return the external storage path
     */
    public static String getExternalStoragePath() {
        if (externalStoragePath == null)
            try {
                externalStoragePath = Environment.getExternalStorageDirectory().getCanonicalPath();
            } catch (IOException | NullPointerException e) {
                externalStoragePath = EXTERNAL_STORAGE_PATH_DEFAULT;
            }
        return externalStoragePath;
    }

    /**
     * Sets the external storage path for books.
     *
     * @param externalStoragePath the external storage path
     * @throws FileNotFoundException if the given file path does not point to valid directory
     */
    public static void setExternalStoragePath(String externalStoragePath)
            throws FileNotFoundException {
        if (new File(externalStoragePath).isDirectory())
            KOReaderHistFav.externalStoragePath = externalStoragePath;
        else
            throw new FileNotFoundException("Could not locate external storage directory "
                    + externalStoragePath);
    }

    /**
     * Returns the KOReader settings' directory path.
     *
     * @return the KOReader settings' directory path
     */
    public String getKoreaderDirectoryPath() {
        return koreaderDirectoryPath;
    }

    /**
     * Returns the KOReader history's file path.
     *
     * @return the kOReader history's file path
     */
    public String getKoreaderHistoryFilePath() {
        return historyFilePath;
    }

    /**
     * Returns the KOReader collection's file path.
     *
     * @return the KOReader collection's file path
     */
    public String getKoreaderCollectionFilePath() {
        return collectionFilePath;
    }

    /**
     * Add book to favorites (and library). If book already in favorites, move book to first
     * position.
     *
     * @param filePath the book's file path
     * @return true if successfully, otherwise false
     */
    public Boolean addBookToFavorites(String filePath) {
        readFavorites();
        filePath = uniqueFilePath(filePath);
        KOReaderBook book = books.get(filePath);
        if (book == null) {
            book = new KOReaderBook(filePath);
            books.put(filePath, book);
        }
        favorites.remove(book);
        favorites.add(0, book);
        return writeFavorites();
    }

    /**
     * Add book to history (and library). If book already in history, move book to first position.
     * Sets book's last reading time to current time.
     *
     * @param filePath the book's file path
     * @return true if successfully, otherwise false
     */
    public Boolean addBookToHistory(String filePath) {
        readHistory();
        filePath = uniqueFilePath(filePath);
        KOReaderBook book = books.get(filePath);
        if (book == null) {
            book = new KOReaderBook(filePath);
            books.put(filePath, book);
        }
        history.remove(book);
        book.setLastRead(new Date().getTime());
        history.add(0, book);
        return writeHistory();
    }

    /**
     * Add book to library.
     *
     * @param filePath the book's file path
     * @return true if successfully, otherwise false
     */
    public Boolean addBookToLibrary(String filePath) {
        readHistory();
        readFavorites();
        filePath = uniqueFilePath(filePath);
        KOReaderBook book = books.get(filePath);
        if (book == null) {
            book = new KOReaderBook(filePath);
            books.put(filePath, book);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Returns the book for given file path.
     *
     * @param filePath the book's file path
     * @return the book or null if book is not in library
     */
    public KOReaderBook getBook(String filePath) {
        readHistory();
        readFavorites();
        return books.get(uniqueFilePath(filePath));
    }

    /**
     * Returns the list of books in favorites, sorted by last added (last added book first).
     *
     * @return the favorites
     */
    public ArrayList<KOReaderBook> getFavorites() {
        readFavorites();
        return favorites;
    }

    /**
     * Returns the list of books in history, sorted by last reading (last read book first).
     *
     * @return the history
     */
    public ArrayList<KOReaderBook> getHistory() {
        readHistory();
        return history;
    }

    /**
     * Returns the library with all books from favorites and history import.
     *
     * @return the library
     */
    public HashMap<String, KOReaderBook> getLibrary() {
        readHistory();
        readFavorites();
        return books;
    }

    /**
     * Remove book from favorites.
     *
     * @param filePath the book's file path
     * @return true if successfully, otherwise false
     */
    public Boolean removeBookFromFavorites(String filePath) {
        readFavorites();
        filePath = uniqueFilePath(filePath);
        KOReaderBook book = books.get(filePath);
        if (book != null && favorites.remove(book))
            return writeFavorites();
        else
            return false;
    }

    /**
     * Remove book from history.
     *
     * @param filePath the book's file path
     * @return true if successfully, otherwise false
     */
    public Boolean removeBookFromHistory(String filePath) {
        readHistory();
        filePath = uniqueFilePath(filePath);
        KOReaderBook book = books.get(filePath);
        if (book != null && history.remove(book))
            return writeHistory();
        else
            return false;
    }

    /**
     * Remove book from library (and favorites and history).
     *
     * @param filePath the book's file path
     * @return true if successfully, otherwise false
     */
    public Boolean removeBookFromLibrary(String filePath) {
        readHistory();
        readFavorites();
        filePath = uniqueFilePath(filePath);
        KOReaderBook book = books.get(filePath);
        if (book != null) {
            Boolean writeHistory = true;
            Boolean writeFavorites = true;
            if (history.remove(book))
                writeHistory =  writeHistory();
            if (favorites.remove(book))
                writeFavorites = writeFavorites();
            if (writeHistory && writeFavorites) {
                books.remove(filePath);
                return true;
            }
            return false;
        }
        else
            return false;
    }

    /**
     * Return a unique file path string for the given file path using canonical path and unification
     * by external storage path (necessary for some devices, see
     * <a href=https://stackoverflow.com/questions/15841380/android-disambiguating-file-paths>
     *     Android disambiguating file path</a>.
     * The external storage path can be set by {@link #setExternalStoragePath} and is returned by
     * {@link #getExternalStoragePath}. It defaults to <code>/storage/emulated/0</code>.
     *
     * @param filePath the file path
     * @return the unique file path
     */
    static String uniqueFilePath(String filePath) {
        try {
            filePath = new File(filePath).getCanonicalPath();
        } catch (IOException e) {}
        // E.g. on device Onyx MC_DARWIN6 the canonical paths of "/storage/emulated/0" and
        // "/storage/emulated/legacy" are not identical, although pointing to same directory.
        String replaceRegexExtStorage = "^/storage/emulated/(legacy|0)|(/mnt|)/sdcard";
        return filePath.replaceFirst(replaceRegexExtStorage, getExternalStoragePath());
    }

    private String koreaderDirectoryPath(String koreaderDirectoryPath)
            throws FileNotFoundException {
        if (koreaderDirectoryPath != null) {
            if (new File(koreaderDirectoryPath).exists())
                return koreaderDirectoryPath;
            throw new FileNotFoundException("Could not locate given koreader directory "
                    + koreaderDirectoryPath);
        } else {
            String[] paths = {getExternalStoragePath(), "/mnt/external_sd", "/mnt/extSdCard"};
            for (String path : paths) {
                if (new File(path + "/koreader").exists())
                    return path + "/koreader";
            }
            throw new FileNotFoundException("Could not locate koreader directory.");
        }
    }

    private Boolean historyFileModified() {
        return (new File(historyFilePath).lastModified() > historyLastModified);
    }

    private Boolean readHistory() {
        if (historyFileModified()) {
            historyJson = KOReaderLuaReadWrite.readLuaFile(historyFilePath);
            historyLastModified = new File(historyFilePath).lastModified();
        } else {
            return false;
        }
        if (historyJson == null)
            return false;
        boolean foundDuplicates = false;
        history.clear();
        Iterator keys = historyJson.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            JSONObject keyjson;
            String filePath;
            Long lastRead;
            try {
                keyjson = historyJson.getJSONObject(key);
                filePath = keyjson.getString("file");
                lastRead = keyjson.getLong("time");
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
            filePath = uniqueFilePath(filePath);
            KOReaderBook book;
            if (books.containsKey(filePath)) {
                book = books.get(filePath);
                int iBookInHistory = history.indexOf(book);
                if (iBookInHistory != -1) {
                    foundDuplicates = true;
                    if (lastRead > history.get(iBookInHistory).getLastRead())
                        history.remove(iBookInHistory);
                    else
                        continue;
                }
            } else {
                book = new KOReaderBook(filePath);
                books.put(filePath, book);
            }
            book.setLastRead(lastRead);
            int historySize = history.size();
            if (historySize == 0) {
                history.add(book);
            } else {
                for (int i = 0; i < historySize; i++) {
                    if (book.getLastRead() > history.get(i).getLastRead()) {
                        history.add(i, book);
                        break;
                    } else if (i == historySize - 1) {
                        history.add(book);
                        break;
                    }
                }
            }
        }
        Log.d(TAG, "--- readBooksFromHistory() successfully. Added "
                + history.size() + " books.");
        if (foundDuplicates && writeHistory())
            Log.d(TAG, "--- readBooksFromHistory(): Found duplicates, wrote history file.");
        return true;
    }

    private Boolean writeHistory() {
        try {
            historyJson = new JSONObject();
            for (int key = 0; key < history.size(); key++) {
                JSONObject keyjson = new JSONObject();
                keyjson.put("file", history.get(key).getFilePath());
                keyjson.put("time", history.get(key).getLastRead());
                historyJson.put(String.valueOf(key + 1), keyjson);
            }
        } catch (JSONException e) {
            return false;
        }
        if (KOReaderLuaReadWrite.writeLuaFile(historyFilePath, historyJson)) {
            historyLastModified = new File(historyFilePath).lastModified();
            return true;
        }
        return false;
    }

    private Boolean collectionFileModified() {
        return (new File(collectionFilePath).lastModified() > collectionLastModified);
    }

    private Boolean readFavorites() {
        if (collectionFileModified()) {
            collectionJson = KOReaderLuaReadWrite.readLuaFile(collectionFilePath);
            collectionLastModified = new File(collectionFilePath).lastModified();
        } else {
            return false;
        }
        if (collectionJson == null)
            return false;
        JSONObject favoritesJson = collectionJson.optJSONObject("favorites");
        if (favoritesJson == null)
            return false;
        boolean foundDuplicates = false;
        favorites.clear();
        ArrayList<Integer> favoritesOrder = new ArrayList<>();
        Iterator keys = favoritesJson.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            JSONObject keyjson;
            String filePath;
            Integer order;
            try {
                keyjson = favoritesJson.getJSONObject(key);
                filePath = keyjson.getString("file");
                order = keyjson.getInt("order");
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
            filePath = uniqueFilePath(filePath);
            KOReaderBook book;
            if (books.containsKey(filePath)) {
                book = books.get(filePath);
                int iBookInFavorites = favorites.indexOf(book);
                if (iBookInFavorites != -1) {
                    foundDuplicates = true;
                    if (order < favoritesOrder.get(iBookInFavorites)) {
                        favorites.remove(iBookInFavorites);
                        favoritesOrder.remove(iBookInFavorites);
                    } else {
                        continue;
                    }
                }
            } else {
                book = new KOReaderBook(filePath);
                books.put(filePath, book);
            }
            int favoritesSize = favorites.size();
            if (favoritesSize == 0) {
                favorites.add(book);
                favoritesOrder.add(order);
            } else {
                for (int i = 0; i < favoritesSize; i++) {
                    if (order < favoritesOrder.get(i)) {
                        favorites.add(i, book);
                        favoritesOrder.add(i, order);
                        break;
                    } else if (i == favoritesSize - 1) {
                        favorites.add(book);
                        favoritesOrder.add(order);
                        break;
                    }
                }
            }
        }
        Log.d(TAG, "--- readBooksFromFavorites() successfully. Added "
                + favorites.size() + " books.");
        if (foundDuplicates && writeFavorites())
            Log.d(TAG, "--- readBooksFromFavorites(): Found duplicates, wrote favorites file.");
        return true;
    }

    private Boolean writeFavorites() {
        try {
            JSONObject favoritesJson = new JSONObject();
            for (int key = 0; key < favorites.size(); key++) {
                JSONObject keyjson = new JSONObject();
                keyjson.put("file", favorites.get(key).getFilePath());
                keyjson.put("order", key + 1);
                favoritesJson.put(String.valueOf(key + 1), keyjson);
            }
            collectionJson.put("favorites", favoritesJson);
        } catch (JSONException e) {
            return false;
        }
        if (KOReaderLuaReadWrite.writeLuaFile(collectionFilePath, collectionJson)) {
            collectionLastModified = new File(collectionFilePath).lastModified();
            return true;
        }
        return false;
    }
}
