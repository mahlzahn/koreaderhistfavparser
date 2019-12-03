package org.koreaderhistfavparser;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class KOReaderBookTest {
    private TestBook[] books = new TestBook[3];
    private String[][] authors = new String[3][];
    private String[] title = new String[3];
    class TestBook {
        private KOReaderBook koBook;
        private String[] authors;
        private String filePath;
        private Boolean finished;
        private String[] keywords;
        private String language;
        private Integer pages;
        private Double percentFinished;
        private String series;
        private String title;

        public TestBook(KOReaderBook koBook, String[] authors, String filePath, String title, String[] keywords,
                        String language, String series, Double percentFinished, Integer pages, Boolean finished) {
            this.koBook = koBook;
            this.authors = authors;
            this.filePath = filePath;
            this.finished = finished;
            this.keywords = keywords;
            this.language = language;
            this.pages = pages;
            this.percentFinished = percentFinished;
            this.series = series;
            this.title = title;
        }
    }

    @Before
    public void setUp() {
        books[0] = new TestBook(new KOReaderBook("src/test/res/books/book1.epub"),
                new String[] {"Karl May"},
                "src/test/res/books/book1.epub",
                "Durch Wüste und Harem / Gesammelte Reiseromane, Band I",
                new String[] {"Adventure stories", "Middle East -- Fiction", "German fiction"},
                "de",
                "",
                0.0017699115044248,
                1130,
                false);
        books[1] = new TestBook(new KOReaderBook("src/test/res/books/book2.epub"),
                new String[] {"Max Brod", "Franz Kafka"},
                "src/test/res/books/book2.epub",
                "Erstes Kapitel des Buches \"Richard und Samuel\" / Die erste lange" +
                        " Eisenbahnfahrt (Prag-Zürich)",
                new String[] {"Young men -- Fiction", "Male friendship -- Fiction",
                        "Voyages and travels -- Fiction", "Unfinished books"},
                "de",
                "",
                0.017543859649123,
                57,
                true);
        books[2] = new TestBook(new KOReaderBook("src/test/res/books/book3.epub"), // no sdr file
                null,
                "src/test/res/books/book3.epub",
                null, null, null, null,
                null, null, false);
    }

    @Test
    public void testGetters() {
        for (TestBook book : books) {
            System.out.println("testGetters(): Run tests on " + book.filePath);
            assertNotNull(book);
            assertTrue(new File(book.filePath).exists());
            KOReaderBook koBook = book.koBook;

            assertArrayEquals(book.authors, koBook.getAuthors());
            assertEquals(book.filePath, koBook.getFilePath());
            assertEquals(book.finished, koBook.getFinished());
            assertArrayEquals(book.keywords, koBook.getKeywords());
            assertEquals(book.language, koBook.getLanguage());
            assertEquals((long) 0, (long) koBook.getLastRead());
            assertEquals(book.pages, koBook.getPages());
            assertEquals(book.percentFinished, koBook.getPercentFinished());
            assertEquals(book.series, koBook.getSeries());
            assertEquals("[%a: ]%t[ (%p%)]", koBook.getStringFormat());
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