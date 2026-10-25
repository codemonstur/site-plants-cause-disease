
I want to build a website that will contain links to pages that might be deleted
in the future. In the event that they get deleted I want to be able to prove that
it was there by having an archive of the page. My own poorman's WayBackMachine.

I've been trying to find a good way to do this and have so far come up with little.
Here are the options I came up with:

1 Download only the HTML
Use HttpClient or Jsoup or some other client to download only this one file.
You have something but no CSS or JS, and offline viewing is probably impossible.

2 Crawl the URL with a tool
Using either a commandline tool like wget or a library like crawler4j. You get
more files, including JS and CSS. However you still may not be able to view it
offline. A command line utility is a brittle solution, and the output is hard to
turn into a single archive.
Commandline tool is also harder to get to work because lots of pages fight
really hard to be automatically crawled. Default wget frequently doesn't work.
(https://serverfault.com/questions/441584/whats-the-best-way-to-create-a-static-backup-of-a-website)

3 Direct a browser and save
Use Selenium (or Playwright) to direct a browser to browse to a page, and then
direct the browser to save the page to a file. Some (all? most?) browsers can store
pages in single mhtml archives.

4 Direct a browser for screenshot
Use Selenium (or Playwright) to direct a browser to a URL. Then take a screenshot of
the page. It works well as proof, it'll be a single file. How do I ensure that the entire
relevant content is visible?

5 Use some tool already written
I'd love to do this. Haven't found one yet. A search on GitHub returns lots of BS.
(ie. ankcrimson/web-content-downloader, N-Pacis/Webpage-downloader,
memanishasharma/viewArticle, vukroxKrd/WebpageSaverForSimbirSoft, ranajits/webpageSave,
etc. etc.)

6 Instruct the WayBackMachine to do the work
Can this be done? Is there some other existing online cache that I can use?
(https://webapps.stackexchange.com/questions/170670/wayback-machine-archive-urls-without-javascript)

archive.is is fun but for a simple Reddit page it gets stuck in a login loop.

7 Convert the page to PDF
I found something called 'aspose-html'. Whats this?
(https://blog.aspose.com/html/save-webpage-url-pdf-java/)

Thoughts?