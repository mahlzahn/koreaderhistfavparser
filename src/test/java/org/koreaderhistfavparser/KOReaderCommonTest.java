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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * A class with common setup for KOReaderHistFavTest and KOReaderBookTest classes.
 */
public class KOReaderCommonTest {
    private final String resSrcDir = "src/test/res";
    final String resBuildDir = "build/test-res";
    final String booksDir = resBuildDir + "/books";

    class TestBook {
        KOReaderBook koBook;
        String[] authors;
        String filePath;
        Boolean finished;
        String[] keywords;
        String language;
        Integer pages;
        Double percentFinished;
        String series;
        String title;

        TestBook(KOReaderBook koBook, String[] authors, String filePath, String title, String[] keywords,
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

    TestBook[] books = new TestBook[3];

    private void copyFile(Path path) throws IOException {
        if (new File(path.toString()).isDirectory())
            for (Path child : Files.newDirectoryStream(path)) {
                copyFile(child);
            }
        else {
            Path dest = new File(path.toString().replace(resSrcDir, resBuildDir)).toPath();
            Files.createDirectories(dest.getParent());
            Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @BeforeMethod
    public void setUp() throws IOException {
        copyFile(new File(resSrcDir).toPath());
        books[0] = new TestBook(
                new KOReaderBook(KOReaderHistFav.uniqueFilePath(booksDir + "/book1.epub")),
                new String[] {"Karl May"},
                KOReaderHistFav.uniqueFilePath(booksDir + "/book1.epub"),
                "Durch Wüste und Harem / Gesammelte Reiseromane, Band I",
                new String[] {"Spaß", "Abenteuer,"},
                "de",
                "",
                0.0017699115044248,
                1000,
                false);
        books[1] = new TestBook(
                new KOReaderBook(KOReaderHistFav.uniqueFilePath(booksDir + "/book2.epub")),
                new String[] {"Max Brod", "Franz Kafka"},
                KOReaderHistFav.uniqueFilePath(booksDir + "/book2.epub"),
                "Erstes Kapitel des Buches \"Richard und Samuel\" / Die erste lange" +
                        " Eisenbahnfahrt (Prag-Zürich)",
                new String[] {"Abenteuer", "Zwei Männer in einem Zug"},
                "de",
                "",
                0.017543859649123,
                60,
                true);
        books[2] = new TestBook( // no sdr file
                new KOReaderBook(KOReaderHistFav.uniqueFilePath(booksDir + "/book3.epub")),
                null,
                KOReaderHistFav.uniqueFilePath(booksDir + "/book3.epub"),
                null, null, null, null,
                null, null, false);
    }
}
