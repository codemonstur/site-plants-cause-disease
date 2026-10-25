package app;

import app.core.HarvestCarnivoreDiet;
import app.core.PageRepository;
import app.model.Disease;
import com.google.gson.Gson;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static app.util.Functions.isNullOrEmpty;
import static app.util.IO.loadResource;
import static app.util.Loader.listDiseases;
import static app.util.Loader.loadDiseases;
import static java.nio.charset.StandardCharsets.UTF_8;

public enum Harvest {;

    public static void main(final String... args) throws IOException {
        final var gson = new Gson();
        final var diseases = loadDiseases(gson, listDiseases());

        findAnecdotes(diseases);

        final var articleUrls = loadArticleUrls();
        final var knownWords = loadKnownWords();

        findNewDiseases(articleUrls, knownWords);
    }

    private static Set<String> loadKnownWords() throws IOException {
        return new HashSet<>(Arrays.asList(loadResource("/known-words.txt", UTF_8).split("\n")));
    }

    private static List<String> loadArticleUrls() throws IOException {
        return Arrays.asList(loadResource("/articles.txt", UTF_8).split("\n"));
    }
    private static void findAnecdotes(final Disease[] diseases) {
        final var carnivoreDiet = new HarvestCarnivoreDiet();

        for (final var disease : diseases) {
            if (disease.carnivoreDietSearchQueries() != null) {
                for (final var query : disease.carnivoreDietSearchQueries()) {
                    carnivoreDiet.findAnecdotes(disease.uri(), disease.filteredAnecdotes(), disease.anecdotes(), query);
                }
            }
        }
    }

    private static void findNewDiseases(final List<String> articleUrls, final Set<String> knownWords) {
        final var repo = new PageRepository();

        for (final var url : articleUrls) {
            if (isNullOrEmpty(url)) continue;
            try {
                final var document = Jsoup.parse(repo.loadPage(url));
                for (final var word : document.text().replaceAll("[^a-zA-Z]", " ").split("\s")) {
                    final var clean = word.trim().toLowerCase();
                    if (clean.isEmpty()) continue;
                    if (knownWords.contains(clean)) continue;

                    knownWords.add(clean);
                    System.out.println(clean);
                }
            } catch (final IOException e) {
                System.err.println(url + ": " + e.getMessage());
            }
        }
    }


}
