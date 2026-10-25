package archive.methods;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.browsingcontext.BrowsingContext;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class TakeScreenshot {

    public static void main(final String... args) throws IOException {
        final var screenshot = takeScreenShot1("https://ecommerce-playground.lambdatest.io/");
        final var filePath = Paths.get("screenshot.png");
        Files.write(filePath, screenshot);
    }

    private static byte[] takeScreenShot1(final String url) {
        final var driver = newChromeWithWebSockets();
        try {
            driver.get(url);
            return decodeBase64(new BrowsingContext(driver, driver.getWindowHandle()).captureScreenshot());
        } finally {
            driver.quit();
        }
    }

    private static byte[] takeScreenShot2(final String url) {
        final var driver = newChromeWithWebSockets();
        try {
            driver.get(url);
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } finally {
            driver.quit();
        }
    }

    private static WebDriver newChromeWithWebSockets() {
        final var options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.setCapability("webSocketUrl", true);
        final var driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        return driver;
    }

    private static byte[] decodeBase64(final String data) {
        return Base64.getDecoder().decode(data);
    }

}
