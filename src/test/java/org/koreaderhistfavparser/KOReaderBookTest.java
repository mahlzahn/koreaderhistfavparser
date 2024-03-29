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

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Test class for KOReaderBook class.
 */
public class KOReaderBookTest extends KOReaderCommonTest {
    @Test
    public void testGetters() {
        for (TestBook book : books) {
            System.out.println("testGetters(): Run tests on " + book.filePath);
            assertNotNull(book);
            assertTrue(new File(book.filePath).exists());
            KOReaderBook koBook = book.koBook;

            if (book.authors != null)
                for (int i = 0; i < book.authors.length; i++)
                    assertEquals(book.authors[i], koBook.getAuthors()[i]);
            else
                assertNull(koBook.getAuthors());
            assertEquals(book.filePath, koBook.getFilePath());
            assertEquals(book.finished, koBook.getFinished());
            if (book.keywords != null)
                for (int i = 0; i < book.keywords.length; i++)
                    assertEquals(book.keywords[i], koBook.getKeywords()[i]);
            else
                assertNull(koBook.getKeywords());
            assertEquals(book.language, koBook.getLanguage());
            assertEquals((long) 0, (long) koBook.getLastRead());
            assertEquals(book.pages, koBook.getPages());
            assertEquals(book.percentFinished, koBook.getPercentFinished());
            assertEquals(book.series, koBook.getSeries());
            assertEquals("[%a: ]%t[ (%p%)]", KOReaderBook.getStringFormat());
            assertEquals(book.title, koBook.getTitle());
        }
    }

    @Test
    public void testSetters() {
        KOReaderBook book0 = books[0].koBook;
        KOReaderBook book2 = books[2].koBook;
        book0.setLastRead((long) 1000);
        assertEquals((long) 1000, (long) book0.getLastRead());

        assertFalse(book0.getFinished());
        assertTrue(book0.setFinished());
        assertTrue(book0.getFinished());
        assertTrue(book0.setReading());
        assertFalse(book0.getFinished());

        // no sdr file for book2, will create one
        assertFalse(book2.getFinished());
        assertTrue(book2.setFinished());
        assertTrue(book2.getFinished());
        assertTrue(book2.setReading());
        assertFalse(book2.getFinished());
    }

    @Test
    public void testEqualsHashCode() {
        assertNotEquals(books[0], books[1]);
        assertNotEquals(books[0], books[2]);
        KOReaderBook book = new KOReaderBook(books[0].filePath);
        assertEquals(books[0].koBook, book);
        assertEquals(book, books[0].koBook);
        assertEquals(books[0].filePath.hashCode(), books[0].koBook.hashCode());
    }

    @Test
    public void testStringFormatting() {
        assertEquals("[%a: ]%t[ (%p%)]", KOReaderBook.getStringFormat());
        assertEquals(books[0].authors[0] + ": " + books[0].title + " (0%)",
                books[0].koBook.toString());
        assertEquals(books[1].authors[0] + ": " + books[1].title + " (2%)",
                books[1].koBook.toString());
        assertEquals("(no title)", books[2].koBook.toString());

        KOReaderBook.setStringFormat("[(series %s[, %l]) ]%a[ (%l)]");
        assertEquals("[(series %s[, %l]) ]%a[ (%l)]", KOReaderBook.getStringFormat());
        assertEquals("Karl May (de)", books[0].koBook.toString());
        assertEquals("Max Brod (de)", books[1].koBook.toString());
        assertEquals("(no author)", books[2].koBook.toString());


        KOReaderBook.setStringFormat("%d/%f");
        assertEquals(books[0].filePath, books[0].koBook.toString());
        assertEquals(books[1].filePath, books[1].koBook.toString());
        assertEquals(books[2].filePath, books[2].koBook.toString());

        KOReaderBook.setStringFormat(null);
    }
}
