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
 */

package org.koreaderhistfavparser;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.testng.Assert.*;

/**
 * Test class for KOReaderHistFav class.
 */
public class KOReaderHistFavTest extends KOReaderCommonTest {
    private final String koreaderDir = resBuildDir + "/koreader";

    private KOReaderHistFav histFav;
    private KOReaderBook[] koBooks = new KOReaderBook[3];

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
        histFav = new KOReaderHistFav(koreaderDir);
        for (int i = 0; i < 3; i++)
            koBooks[i] = books[i].koBook;
    }

    @Test
    public void testGetters() throws FileNotFoundException {
        assertEquals(koreaderDir, histFav.getKoreaderDirectoryPath());
        assertEquals(koreaderDir + "/history.lua", histFav.getKoreaderHistoryFilePath());
        assertEquals(koreaderDir + "/settings/collection.lua",
                histFav.getKoreaderCollectionFilePath());
        assertEquals(koBooks[0].getFilePath(),
                histFav.getBook(booksDir + "/book1.epub").getFilePath());
        assertEquals(2, histFav.getFavorites().size());
        assertEquals(koBooks[0], histFav.getFavorites().get(0));
        assertEquals(koBooks[2], histFav.getFavorites().get(1));
        assertEquals(2, histFav.getHistory().size());
        assertEquals(koBooks[0], histFav.getHistory().get(0));
        assertEquals(koBooks[1], histFav.getHistory().get(1));
        assertEquals(3, histFav.getLibrary().size());
        assertTrue(histFav.getLibrary().containsValue(koBooks[0]));
        assertTrue(histFav.getLibrary().containsValue(koBooks[1]));
        assertTrue(histFav.getLibrary().containsValue(koBooks[2]));

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
        KOReaderHistFav.setExternalStoragePath(booksDir);
        assertEquals(booksDir, KOReaderHistFav.getExternalStoragePath());
    }

    @Test
    public void testAddRemoveBook() {
        // manipulates KOReader setting files history.lua and settings/collection.lua
        assertTrue(histFav.removeBookFromFavorites(koBooks[0].getFilePath()));
        assertEquals(1, histFav.getFavorites().size());
        assertFalse(histFav.removeBookFromFavorites(koBooks[1].getFilePath()));
        assertEquals(1, histFav.getFavorites().size());
        assertTrue(histFav.removeBookFromFavorites(koBooks[2].getFilePath()));
        assertEquals(0, histFav.getFavorites().size());
        assertTrue(histFav.addBookToFavorites(koBooks[2].getFilePath()));
        assertEquals(1, histFav.getFavorites().size());
        assertTrue(histFav.addBookToFavorites(koBooks[0].getFilePath()));
        assertEquals(2, histFav.getFavorites().size());
        assertEquals(koBooks[0], histFav.getFavorites().get(0));
        assertEquals(koBooks[2], histFav.getFavorites().get(1));
        
        assertTrue(histFav.removeBookFromHistory(koBooks[0].getFilePath()));
        assertEquals(1, histFav.getHistory().size());
        assertTrue(histFav.removeBookFromHistory(koBooks[1].getFilePath()));
        assertEquals(0, histFav.getHistory().size());
        assertFalse(histFav.removeBookFromHistory(koBooks[2].getFilePath()));
        assertEquals(0, histFav.getHistory().size());
        assertTrue(histFav.addBookToHistory(koBooks[1].getFilePath()));
        assertEquals(1, histFav.getHistory().size());
        assertTrue(histFav.addBookToHistory(koBooks[0].getFilePath()));
        assertEquals(2, histFav.getHistory().size());
        assertEquals(koBooks[0], histFav.getHistory().get(0));
        assertEquals(koBooks[1], histFav.getHistory().get(1));

        assertTrue(histFav.removeBookFromLibrary(koBooks[0].getFilePath()));
        assertEquals(2, histFav.getLibrary().size());
        assertTrue(histFav.removeBookFromLibrary(koBooks[1].getFilePath()));
        assertEquals(1, histFav.getLibrary().size());
        assertTrue(histFav.removeBookFromLibrary(koBooks[2].getFilePath()));
        assertEquals(0, histFav.getLibrary().size());
        assertTrue(histFav.addBookToLibrary(koBooks[0].getFilePath()));
        assertEquals(1, histFav.getLibrary().size());
        assertEquals(0, histFav.getFavorites().size());
        assertEquals(0, histFav.getHistory().size());
        assertFalse(histFav.addBookToLibrary(koBooks[0].getFilePath()));
    }
}
