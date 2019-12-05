# KOReaderHistFavParser
Parser (read and write) for KOReader ([Homepage](https://koreader.rocks), [Github repository](https://github.com/koreader/koreader)) history, favorites and book status for external usage in Android applications

## Documentation
Lates documentation of the Java classes on the [master branch of the KOReaderHistFavParser GitHub repository](https://github.com/mahlzahn/koreaderhistfavparser) can be found at [mahlzahn.github.io/koreaderhistfavparser/docs](https://mahlzahn.github.io/koreaderhistfavparser/docs).

## Usage
```java
import org.koreaderhistfavparser.KOReaderBook;
import org.koreaderhistfavparser.KOReaderHistFav;

...

String koreaderDirectoryPath = "path/to/settings/koreader";  // optional argument to KOReaderHistFav
KOReaderHistFav koreaderHistFav = new KOReaderHistFav(koreaderDirectoryPath);

KOReaderHistFav.addBookToHistory("/path/to/ebook1");    // adds ebook1 to history and internal library
KOReaderHistFav.addBookToFavorites("/path/to/ebook2");  // adds ebook2 to favorites and internal library
KOReaderHistFav.addBookToFavorites("/path/to/ebook1");  // adds ebook1 to favorites (already in library)

ArrayList<KOReaderBook> history = KOReaderHistFav.getHistory();        // returns ebook1
ArrayList<KOReaderBook> favorites = KOReaderHistFav.getFavorites();    // returns ebook1 and ebook2
HashMap<String, KOReaderBook> library = KOReaderHistFav.getLibrary();  // returns ebook1 and ebook2

...

KOReaderHistFav.removeBookFromHistory("/path/to/ebook1");  // removes ebook1 from history but keeps in
                                                           // favorites and internal library
KOReaderHistFav.removeBookFromLibrary("/path/to/ebook2");  // removes ebook2 from favorites and library
```
