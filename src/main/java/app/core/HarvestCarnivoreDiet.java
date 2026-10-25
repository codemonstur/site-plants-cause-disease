package app.core;

import app.model.Anecdote;
import app.model.CarnivoreDietArticle;
import org.jsoup.Jsoup;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static app.util.Functions.encodeUrl;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

public class HarvestCarnivoreDiet {

    private static final Gson GSON = new Gson();

    private final PageRepository repo;

    public HarvestCarnivoreDiet() {
        repo = new PageRepository();
    }

    public void findAnecdotes(final String disease, final List<String> filteredAnecdotes
            , final List<Anecdote> knownAnecdotes, final String query) {
        try {

            final var knownAnecdoteUrls = toUrlSet(knownAnecdotes, filteredAnecdotes);
            final var lastPageNumber = findLastPageNumber(query);
            for (int i = 1; i <= lastPageNumber; i++) {
                final var json = listArticlesForPage(i, query).stream()
                    .filter(article -> !knownAnecdoteUrls.contains(article.url()))
                    .map(HarvestCarnivoreDiet::toAnecdoteJson)
                    .collect(joining(",\n"));
                if (!json.isEmpty())
                    System.out.println("New Anecdotes found for " + disease + ":\n" + json);
            }

        } catch (final Exception e) {
            System.err.println("Unable to extract anecdotes: " + e.getMessage());
        }
    }

    private static String toAnecdoteJson(final CarnivoreDietArticle article) {
        final var map = new java.util.LinkedHashMap<String, Object>();
        map.put("visible", false);
        map.put("uri", article.url());
        map.put("title", article.title());
        map.put("date", article.date());
        return GSON.toJson(map);
    }

    private static Set<String> toUrlSet(final List<Anecdote> list, final List<String> filteredAnecdotes) {
        final var set = new HashSet<String>();
        if (list != null) list.stream().map(Anecdote::uri).forEach(set::add);
        if (filteredAnecdotes != null) set.addAll(filteredAnecdotes);
        return set;
    }

    private int findLastPageNumber(final String query) throws IOException {
        final var html = repo.loadPage("https://carnivore.diet/page/1/?s=" + query);
        final var page = Jsoup.parse(html);
        final var nothing = page.selectFirst("div.elementor-posts-nothing-found");
        if (nothing != null) return 0;

        final var lastLink = page.selectFirst("nav.elementor-pagination a:last-child");
        if (lastLink == null) return 1;

        return Integer.parseInt(lastLink.ownText());
    }

    private List<CarnivoreDietArticle> listArticlesForPage(final int pageNum, final String query) throws IOException {
        final var page = repo.loadPage("https://carnivore.diet/page/" + pageNum + "/?s=" + query);
        final var articles = Jsoup.parse(page).getElementsByTag("article");

        final var list = new ArrayList<CarnivoreDietArticle>();
        for (final var article : articles) {
            final var title = article.selectFirst("h3");
            list.add(new CarnivoreDietArticle(
                title.text(),
                title.selectFirst("a").attr("href"),
                article.selectFirst("p").text(),
                article.selectFirst(".elementor-post-date").text()
            ));
        }
        return list;
    }

}
