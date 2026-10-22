package archive.methods;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DownloadUsingSelenium {

    public static void main(final String... args) throws IOException {
        final var content = downloadPageSource("https://carnivore.diet/page/1/?s=Abdominal+Pain");
        System.out.println(content);
    }

    private static String downloadPageSource(final String url) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        final var driver = new ChromeDriver(options);
        try {
//            driver.manage().window().maximize();
            driver.get(url);
            return driver.getPageSource();
        } finally {
            driver.quit();
        }
    }

}
