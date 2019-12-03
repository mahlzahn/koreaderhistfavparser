package org.koreaderhistfavparser;

import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class KOReaderHistFavTest {
    private KOReaderHistFav histFav;
    private KOReaderBook[] books = new KOReaderBook[3];
    private final String bookDir = "src/test/res/books";
    private final String koreaderDir = "src/test/res/koreader";

    @Before
    public void setUp() throws FileNotFoundException {
        histFav = new KOReaderHistFav(koreaderDir);
        books[0] = new KOReaderBook(KOReaderHistFav.uniqueFilePath(bookDir + "/book1.epub"));
        books[1] = new KOReaderBook(KOReaderHistFav.uniqueFilePath(bookDir + "/book2.epub"));
        books[2] = new KOReaderBook(KOReaderHistFav.uniqueFilePath(bookDir + "/book3.epub"));
    }

    @Test
    public void testGetters() throws FileNotFoundException {
        assertEquals(koreaderDir, histFav.getKoreaderDirectoryPath());
        assertEquals(koreaderDir + "/history.lua", histFav.getKoreaderHistoryFilePath());
        assertEquals(koreaderDir + "/settings/collection.lua",
                histFav.getKoreaderCollectionFilePath());
        assertEquals(books[0].getFilePath(),
                histFav.getBook(bookDir + "/book1.epub").getFilePath());
        assertEquals(2, histFav.getFavorites().size());
        assertEquals(books[0], histFav.getFavorites().get(0));
        assertEquals(books[2], histFav.getFavorites().get(1));
        assertEquals(2, histFav.getHistory().size());
        assertEquals(books[0], histFav.getHistory().get(0));
        assertEquals(books[1], histFav.getHistory().get(1));
        assertEquals(3, histFav.getLibrary().size());
        assertTrue(histFav.getLibrary().containsValue(books[0]));
        assertTrue(histFav.getLibrary().containsValue(books[1]));
        assertTrue(histFav.getLibrary().containsValue(books[2]));

        histFav = new KOReaderHistFav(koreaderDir);
        assertEquals(2, histFav.getFavorites().size());
        histFav = new KOReaderHistFav(koreaderDir);
        assertEquals(2, histFav.getHistory().size());
        histFav = new KOReaderHistFav(koreaderDir);
        assertEquals(3, histFav.getLibrary().size());
    }

    @Test
    public void testExternalStoragePath() throws FileNotFoundException {
        assertEquals("/storage/emulated/0", KOReaderHistFav.getExternalStoragePath());
        KOReaderHistFav.setExternalStoragePath("src/test/res/books");
        assertEquals("src/test/res/books", KOReaderHistFav.getExternalStoragePath());
    }

    @Test
    public void testAddRemoveBook() {
        // manipulates KOReader setting files history.lua and settings/collection.lua
        assertTrue(histFav.removeBookFromFavorites(books[0].getFilePath()));
        assertEquals(1, histFav.getFavorites().size());
        assertFalse(histFav.removeBookFromFavorites(books[1].getFilePath()));
        assertEquals(1, histFav.getFavorites().size());
        assertTrue(histFav.removeBookFromFavorites(books[2].getFilePath()));
        assertEquals(0, histFav.getFavorites().size());
        assertTrue(histFav.addBookToFavorites(books[2].getFilePath()));
        assertEquals(1, histFav.getFavorites().size());
        assertTrue(histFav.addBookToFavorites(books[0].getFilePath()));
        assertEquals(2, histFav.getFavorites().size());
        
        assertTrue(histFav.removeBookFromHistory(books[0].getFilePath()));
        assertEquals(1, histFav.getHistory().size());
        assertTrue(histFav.removeBookFromHistory(books[1].getFilePath()));
        assertEquals(0, histFav.getHistory().size());
        assertFalse(histFav.removeBookFromHistory(books[2].getFilePath()));
        assertEquals(0, histFav.getHistory().size());
        assertTrue(histFav.addBookToHistory(books[1].getFilePath()));
        assertEquals(1, histFav.getHistory().size());
        assertTrue(histFav.addBookToHistory(books[0].getFilePath()));
        assertEquals(2, histFav.getHistory().size());

        assertTrue(histFav.removeBookFromLibrary(books[0].getFilePath()));
        assertEquals(2, histFav.getLibrary().size());
        assertTrue(histFav.removeBookFromLibrary(books[1].getFilePath()));
        assertEquals(1, histFav.getLibrary().size());
        assertTrue(histFav.removeBookFromLibrary(books[2].getFilePath()));
        assertEquals(0, histFav.getLibrary().size());
        assertTrue(histFav.addBookToLibrary(books[0].getFilePath()));
        assertEquals(1, histFav.getLibrary().size());
        assertEquals(0, histFav.getFavorites().size());
        assertEquals(0, histFav.getHistory().size());

        // restore original order for favorites and history
        assertTrue(histFav.addBookToFavorites(books[2].getFilePath()));
        assertTrue(histFav.addBookToFavorites(books[0].getFilePath()));
        assertEquals(2, histFav.getFavorites().size());
        assertTrue(histFav.addBookToHistory(books[1].getFilePath()));
        assertTrue(histFav.addBookToHistory(books[0].getFilePath()));
        assertEquals(2, histFav.getHistory().size());
    }
}
