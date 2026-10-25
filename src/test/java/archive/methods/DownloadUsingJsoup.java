package archive.methods;

import org.jsoup.Jsoup;

import java.io.IOException;

public class DownloadUsingJsoup {

    public static byte[] downloadUsingJsoup(final String url) throws IOException {
        return Jsoup.connect(url).execute().bodyAsBytes();
    }

}
