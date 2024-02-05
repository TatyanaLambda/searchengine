package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.BadRequest;
import searchengine.dto.statistics.Response;
import searchengine.model.SitePage;
import searchengine.model.Status;
import searchengine.parsers.IndexParser;
import searchengine.parsers.LemmaParser;
import searchengine.parsers.SiteParser;
import searchengine.repository.SiteRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.IndexRepository;




import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingServiceImpl implements IndexingService{
    private static final int THREAD_COUNT = 4;
    private final SitesList sitesList;
    private ExecutorService executorService;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaParser lemmaParser;
    private final IndexParser indexParser;

    @Override
    public ResponseEntity<?> indexingAllSites() {
        if (isIndexingActive()) {
            String errText = "Индексация уже запущена";
            log.info(errText);
            return new ResponseEntity<>(new BadRequest(false, errText), HttpStatus.BAD_REQUEST);
        }
        List<Site> siteList = sitesList.getSites();
        executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        for (Site site : siteList) {
            String url = site.getUrl();
            Site sitePage = new Site();
            sitePage.setName(site.getName());
            log.info("Парсинг сайта: " + site.getName());
            executorService.submit(new SiteParser(siteRepository, pageRepository, lemmaRepository, indexRepository, lemmaParser, indexParser, url, sitesList, THREAD_COUNT));
        }
        return new ResponseEntity<>(new Response(true), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> indexingByUrl(String url) {
        if (checkExistsUrl(url)) {
            log.info("Началась индексация сайта: - " + url);
            executorService = Executors.newFixedThreadPool(THREAD_COUNT);
            executorService.submit(new SiteParser(siteRepository, pageRepository, lemmaRepository, indexRepository, lemmaParser, indexParser, url, sitesList, THREAD_COUNT));
            executorService.shutdown();
            return new ResponseEntity<>(new Response(true), HttpStatus.OK);
        } else {
            String errText = "Недопустимый адрес сайта";
            log.info(errText);
            return new ResponseEntity<>(new BadRequest(false, errText), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> stopIndexing() {
        if (isIndexingActive()) {
            log.info("Индексация остановлена");
            executorService.shutdownNow();
            return new ResponseEntity<>(new Response(true), HttpStatus.OK);
        }
        String errText = "Индексация не может быть остановлена, так как ещё не начата";
        log.info(errText);
        return new ResponseEntity<>(new BadRequest(false, errText),HttpStatus.BAD_REQUEST);
    }
    private boolean isIndexingActive() {
        siteRepository.flush();
        Iterable<SitePage> siteList = siteRepository.findAll();
        for (SitePage site : siteList) {
            if (site.getStatus() == Status.INDEXING) {
                return true;
            }
        }
        return false;
    }

    private boolean checkExistsUrl(String url) {
        List<Site> urlList = sitesList.getSites();
        for (Site site : urlList) {
            if (site.getUrl().equals(url)) {
                return true;
            }
        }
        return false;
    }
}
