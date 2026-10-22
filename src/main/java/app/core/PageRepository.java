package app.core;

import org.jsoup.Jsoup;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static app.util.Functions.encodeUrl;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;

public final class PageRepository {

    private final Path cacheDir;

    public PageRepository() {
        cacheDir = Paths.get(".url_cache");
    }

    public String loadPage(final String url) throws IOException {
        if (!isDirectory(cacheDir)) Files.createDirectories(cacheDir);
        final var cacheFile = cacheDir.resolve(encodeUrl(url));
        if (isRegularFile(cacheFile)) return Files.readString(cacheFile);
        final var data = downloadPage(url);
        Files.writeString(cacheFile, data);
        return data;
    }

    private static volatile ChromeDriver chrome;

    private static ChromeDriver chrome() {
        if (chrome == null) {
            synchronized (PageRepository.class) {
                if (chrome == null) chrome = loadChrome();
            }
        }
        return chrome;
    }

    private static ChromeDriver loadChrome() {
        final var options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        return new ChromeDriver(options);
    }

    public static synchronized String downloadPage(final String url) {
//        return Jsoup.connect(url).execute().body();

        chrome().get(url);
        return chrome().getPageSource();
    }

}
