package org.koreaderhistfavparser;

import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

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

    // Returned as part of KOReaderBook.getFilePath
    private final static String EXTERNAL_STORAGE_PATH = "/mnt/sdcard";
    // automatically detected external storage path, defaults to "/storage/emulated/0"
    private String externalStoragePath;

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
        try {
            externalStoragePath = Environment.getExternalStorageDirectory().getCanonicalPath();
        } catch (IOException e) {
            externalStoragePath = "/storage/emulated/0";
        }

        this.koreaderDirectoryPath = koreaderDirectoryPath(koreaderDirectoryPath);
        historyFilePath = this.koreaderDirectoryPath + "/" + HISTORY_FILE_PATH;
        collectionFilePath = this.koreaderDirectoryPath + "/" + COLLECTION_FILE_PATH;
        Log.d(TAG, "Set up with history file " + historyFilePath
                + " and with collection file " + collectionFilePath);

        if (readHistory())
            readBooksFromHistory();
        if (readFavorites())
            readBooksFromFavorites();
    }

    /**
     * Returns the KOReader settings directory path.
     *
     * @return the KOReader settings directory path
     */
    public String getKoreaderDirectoryPath() {
        return koreaderDirectoryPath;
    }

    /**
     * Returns the KOReader history file path.
     *
     * @return the kOReader history file path
     */
    public String getKoreaderHistoryFilePath() {
        return historyFilePath;
    }

    /**
     * Returns the KOReader collection file path.
     *
     * @return the KOReader collection file path
     */
    public String getKoreaderCollectionFilePath() {
        return collectionFilePath;
    }

    /**
     * Returns the library with all books from favorites and history import.
     *
     * @return the library
     */
    public HashMap<String, KOReaderBook> getLibrary() {
        if (historyFileModified() && readHistory())
            readBooksFromHistory();
        if (collectionFileModified() && readFavorites())
            readBooksFromFavorites();
        return books;
    }

    /**
     * Returns the book for given file path.
     *
     * @param filePath the book file path
     * @return the book
     */
    public KOReaderBook getBook(String filePath) {
        if (historyFileModified() && readHistory())
            readBooksFromHistory();
        if (collectionFileModified() && readFavorites())
            readBooksFromFavorites();
        return books.get(uniqueFilePath(filePath));
    }

    /**
     * Returns the list of books in history, sorted by last reading (last read book first).
     *
     * @return the history
     */
    public ArrayList<KOReaderBook> getHistory() {
        if (historyFileModified() && readHistory())
            readBooksFromHistory();
        return history;
    }

    /**
     * Returns the list of books in favorites, sorted by last added (last added book first).
     *
     * @return the favorites
     */
    public ArrayList<KOReaderBook> getFavorites() {
        if (collectionFileModified() && readFavorites())
            readBooksFromFavorites();
        return favorites;
    }

    /**
     * Add book to library.
     *
     * @param filePath the book file path
     * @return true if successfully, otherwise false
     */
    public Boolean addBookToLibrary(String filePath) {
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
     * Remove book from library (and favorites and history).
     *
     * @param filePath the book file path
     * @return true if successfully, otherwise false
     */
    public Boolean removeBookFromLibrary(String filePath) {
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
     * Add book to history (and library). If book already in history, move book to first position.
     * Sets book's last reading time to current time.
     *
     * @param filePath the book file path
     * @return true if successfully, otherwise false
     */
    public Boolean addBookToHistory(String filePath) {
        filePath = uniqueFilePath(filePath);
        KOReaderBook book = books.get(filePath);
        if (book == null) {
            book = new KOReaderBook(filePath);
            books.put(filePath, book);
        }
        if (history.contains(book))
            history.remove(book);
        book.setLastRead(new Date().getTime());
        history.add(0, book);
        return writeHistory();
    }

    /**
     * Remove book from history.
     *
     * @param filePath the book file path
     * @return true if successfully, otherwise false
     */
    public Boolean removeBookFromHistory(String filePath) {
        filePath = uniqueFilePath(filePath);
        KOReaderBook book = books.get(filePath);
        if (book != null && history.remove(book))
            return writeHistory();
        else
            return false;
    }

    /**
     * Add book to favorites (and library). If book already in favorites, move book to first
     * position.
     *
     * @param filePath the book file path
     * @return true if successfully, otherwise false
     */
    public Boolean addBookToFavorites(String filePath) {
        filePath = uniqueFilePath(filePath);
        KOReaderBook book = books.get(filePath);
        if (book == null) {
            book = new KOReaderBook(filePath);
            books.put(filePath, book);
        }
        if (favorites.contains(book))
            favorites.remove(book);
        favorites.add(0, book);
        return writeFavorites();
    }

    /**
     * Remove book from favorites.
     *
     * @param filePath the book file path
     * @return true if successfully, otherwise false
     */
    public Boolean removeBookFromFavorites(String filePath) {
        filePath = uniqueFilePath(filePath);
        KOReaderBook book = books.get(filePath);
        if (book != null && favorites.remove(book))
            return writeFavorites();
        else
            return false;
    }

    private String koreaderDirectoryPath(String koreaderDirectoryPath)
            throws FileNotFoundException {
        if (koreaderDirectoryPath != null) {
            if (new File(koreaderDirectoryPath).exists())
                return koreaderDirectoryPath;
            throw new FileNotFoundException("Could not locate given koreader directory "
                    + koreaderDirectoryPath);
        } else {
            String[] paths = {externalStoragePath, "/mnt/external_sd", "/mnt/extSdCard"};
            for (String path : paths) {
                if (new File(path + "/koreader").exists())
                    return path + "/koreader";
            }
            throw new FileNotFoundException("Could not locate koreader directory.");
        }
    }

    private String uniqueFilePath(String filePath) {
        try {
            filePath = new File(filePath).getCanonicalPath();
        } catch (IOException e) {}
        // E.g. on device Onyx MC_DARWIN6 the canonical paths of "/storage/emulated/0" and
        // "/storage/emulated/legacy" are not identical, although pointing to same directory.
        // See https://stackoverflow.com/questions/15841380/android-disambiguating-file-paths
        String replaceRegexExtStorage = "^" + externalStoragePath
                + "|/storage/emulated/legacy|/storage/emulated/0|/sdcard";
        return filePath.replaceFirst(replaceRegexExtStorage, EXTERNAL_STORAGE_PATH);
    }

    private Boolean historyFileModified() {
        return (new File(historyFilePath).lastModified() > historyLastModified);
    }

    private Boolean readHistory() {
        if (historyFileModified()) {
            historyJson = KOReaderLuaReadWrite.readLuaFile(historyFilePath);
            historyLastModified = new File(historyFilePath).lastModified();
            return (historyJson != null);
        } else {
            return false;
        }
    }

    private Boolean readBooksFromHistory() {
        if (historyJson == null)
            return false;
        history.clear();
        Iterator keys = historyJson.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            JSONObject keyjson = null;
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
        return true;
    }

    private Boolean writeHistory() {
        try {
            historyJson = new JSONObject();
            for (int key = 0; key < history.size(); key++) {
                JSONObject keyjson = new JSONObject();
                keyjson.put("file", history.get(key).getFilePath());
                keyjson.put("time", history.get(key).getLastRead());
                historyJson.put(String.valueOf(key), keyjson);
            }
        } catch (JSONException e) {
            return false;
        }
        return KOReaderLuaReadWrite.writeLuaFile(historyFilePath, historyJson);
    }

    private Boolean collectionFileModified() {
        return (new File(collectionFilePath).lastModified() > collectionLastModified);
    }

    private Boolean readFavorites() {
        if (collectionFileModified()) {
            collectionJson = KOReaderLuaReadWrite.readLuaFile(collectionFilePath);
            collectionLastModified = new File(collectionFilePath).lastModified();
            return (collectionJson != null);
        } else {
            return false;
        }
    }

    private Boolean readBooksFromFavorites() {
        if (collectionJson == null)
            return false;
        JSONObject favoritesJson = collectionJson.optJSONObject("favorites");
        if (favoritesJson == null)
            return false;
        favorites.clear();
        SparseArray<KOReaderBook> booksArray = new SparseArray<>();
        Iterator keys = favoritesJson.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            JSONObject keyjson = null;
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
            } else {
                book = new KOReaderBook(filePath);
                books.put(filePath, book);
            }
            if (booksArray.get(order) == null)
                booksArray.append(order, book);
            else
                booksArray.append(booksArray.keyAt(booksArray.size() - 1) + 100, book);
        }
        for (int i = 0; i < booksArray.size(); i++)
            favorites.add(booksArray.valueAt(i));
        Log.d(TAG, "--- readBooksFromFavorites() successfully. Added "
                + favorites.size() + " books.");
        return true;
    }

    private Boolean writeFavorites() {
        try {
            JSONObject favoritesJson = new JSONObject();
            for (int key = 0; key < favorites.size(); key++) {
                JSONObject keyjson = new JSONObject();
                keyjson.put("file", favorites.get(key).getFilePath());
                keyjson.put("order", key);
                favoritesJson.put(String.valueOf(key), keyjson);
            }
            collectionJson.put("favorites", favoritesJson);
        } catch (JSONException e) {
            return false;
        }
        return KOReaderLuaReadWrite.writeLuaFile(collectionFilePath, collectionJson);
    }
}