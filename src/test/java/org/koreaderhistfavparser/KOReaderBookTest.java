package org.koreaderhistfavparser;

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.*;

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
        assertEquals("Karl May: Durch Wüste und Harem / Gesammelte Reiseromane, Band I (0%)",
                books[0].koBook.toString());
        assertEquals("Max Brod: Erstes Kapitel des Buches \"Richard und Samuel\" / Die erste" +
                " lange Eisenbahnfahrt (Prag-Zürich) (2%)", books[1].koBook.toString());
        assertEquals("(no title)", books[2].koBook.toString());

        KOReaderBook.setStringFormat("[(series %s[, %l]) ]%a[ (%l)]");
        assertEquals("[(series %s[, %l]) ]%a[ (%l)]", KOReaderBook.getStringFormat());
        assertEquals("Karl May (de)", books[0].koBook.toString());
        assertEquals("Max Brod (de)", books[1].koBook.toString());
        assertEquals("(no author)", books[2].koBook.toString());
        KOReaderBook.setStringFormat(null);
    }
}