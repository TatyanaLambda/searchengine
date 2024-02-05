package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import searchengine.model.IndexModel;
import searchengine.model.SitePage;
import searchengine.dto.statistics.*;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.parsers.ClearingHtml;
import searchengine.parsers.Morphology;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {
    private final Morphology morphology;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;
    @Override
    public ResponseEntity<?> search(String request, String site, int offset, int limit){
        if (request.isEmpty()) {
            return new ResponseEntity<>(new BadRequest(false, "Задан пустой поисковый запрос"), HttpStatus.BAD_REQUEST);
        }
        List<SearchData> searchData;
        if (!site.isEmpty()) {
            if (siteRepository.findByUrl(site) == null) {
                return new ResponseEntity<>(new BadRequest(false, "Заданная страница не найдена"),HttpStatus.BAD_REQUEST);
            }
            searchData = siteSearch(request, site, offset, limit);
        }
        else {
            searchData = allSiteSearch(request, offset, limit);
        }
        return new ResponseEntity<>(new SearchResponse(true, searchData.size(), searchData), HttpStatus.OK);
    }

    private List<SearchData> allSiteSearch(String searchText, int offset, int limit) {
        log.info("Получение результата поиска \"" + searchText + "\"");
        List<SitePage> sitePageList = siteRepository.findAll();
        List<Lemma> foundLemmaList = new ArrayList<>();
        List<String> textLemmaList = getLemmaFromSearchText(searchText);
        for (SitePage sitePage : sitePageList) {
            foundLemmaList.addAll(getLemmaListFromSite(textLemmaList, sitePage));
        }
        List<SearchData> searchData = new ArrayList<>(getSearchDtoList(foundLemmaList, textLemmaList, offset, limit));
        searchData.sort((o1, o2) -> Float.compare(o2.getRelevance(), o1.getRelevance()));
        log.info("Поиск окончен. Получены результаты.");
        return searchData;
    }

    private List<SearchData> siteSearch(String searchText, String url, int offset, int limit) {
        log.info("Получение результата поиска \"" + searchText + "\" in - " + url);
        SitePage sitePage = siteRepository.findByUrl(url);
        List<String> textLemmaList = getLemmaFromSearchText(searchText);
        List<Lemma> foundLemmaList = getLemmaListFromSite(textLemmaList, sitePage);
        log.info("Поиск окончен. Получены результаты.");
        return getSearchDtoList(foundLemmaList, textLemmaList, offset, limit);
    }

    private List<String> getLemmaFromSearchText(String searchText) {
        String[] words = searchText.toLowerCase(Locale.ROOT).split(" ");
        List<String> lemmaList = new ArrayList<>();
        for (String lemma : words) {
            List<String> list = morphology.getLemma(lemma);
            lemmaList.addAll(list);
        }
        return lemmaList;
    }

    private List<Lemma> getLemmaListFromSite(List<String> lemmas, SitePage sitePage) {
        lemmaRepository.flush();
        List<Lemma> lemmaList = lemmaRepository.findLemmaListBySite(lemmas, sitePage);
        List<Lemma> result = new ArrayList<>(lemmaList);
        result.sort(Comparator.comparingInt(Lemma::getFrequency));
        return result;
    }

    private List<SearchData> getSearchData(Hashtable<Page, Float> pageList, List<String> textLemmaList) {
        List<SearchData> result = new ArrayList<>();
        for (Page page : pageList.keySet()) {
            String uri = page.getPath();
            String content = page.getContent();
            SitePage sitePage = page.getSiteId();
            String site = sitePage.getUrl();
            String siteName = sitePage.getName();
            Float absRelevance = pageList.get(page);
            StringBuilder clearContent = new StringBuilder();
            String title = ClearingHtml.clearHtml(content, "title");
            String body = ClearingHtml.clearHtml(content, "body");
            clearContent.append(title).append(" ").append(body);
            String snippet = getSnippet(clearContent.toString(), textLemmaList);
            result.add(new SearchData(site, siteName, uri, title, snippet, absRelevance));
        }
        return result;
    }

    private String getSnippet(String content, List<String> lemmaList) {
        List<Integer> lemmaIndex = new ArrayList<>();
        StringBuilder result = new StringBuilder();
        for (String lemma : lemmaList) {
            lemmaIndex.addAll(morphology.findLemmaIndexInText(content, lemma));
        }
        Collections.sort(lemmaIndex);
        List<String> wordsList = getWordsFromContent(content, lemmaIndex);
        int wordListSize = wordsList.size();
        int limit = Math.min(wordListSize, 3);
        for (int i = 0; i <= limit; i++) {
            result.append(wordsList.get(i)).append("... ");
        }
        return result.toString();
    }

    private List<String> getWordsFromContent(String content, List<Integer> lemmaIndex) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < lemmaIndex.size(); i++) {
            int start = lemmaIndex.get(i);
            int end = content.indexOf(" ", start);
            int nextPoint = i + 1;
            while (nextPoint < lemmaIndex.size() && lemmaIndex.get(nextPoint) - end > 0 && lemmaIndex.get(nextPoint) - end < 5) {
                end = content.indexOf(" ", lemmaIndex.get(nextPoint));
                nextPoint += 1;
            }
            i = nextPoint - 1;
            String text = getWordsFromIndex(start, end, content);
            result.add(text);
        }
        result.sort(Comparator.comparingInt(String::length).reversed());
        return result;
    }

    private String getWordsFromIndex(int start, int end, String content) {
        String word = content.substring(start, end);
        int prevPoint;
        int lastPoint;
        if (content.lastIndexOf(" ", start) != -1) {
            prevPoint = content.lastIndexOf(" ", start);
        } else prevPoint = start;
        if (content.indexOf(" ", end + 30) != -1) {
            lastPoint = content.indexOf(" ", end + 30);
        } else lastPoint = content.indexOf(" ", end);
        String text = content.substring(prevPoint, lastPoint);
        try {
            text = text.replaceAll(word, "<b>" + word + "</b>");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return text;
    }

    private List<SearchData> getSearchDtoList(List<Lemma> lemmaList, List<String> textLemmaList, int offset, int limit) {
        List<SearchData> result = new ArrayList<>();
        pageRepository.flush();
        if (lemmaList.size() >= textLemmaList.size()) {
            List<Page> foundPageList = pageRepository.findByLemmaList(lemmaList);
            indexRepository.flush();
            List<IndexModel> foundIndexListModel = indexRepository.findByPagesAndLemmas(lemmaList, foundPageList);
            Hashtable<Page, Float> sortedPageByAbsRelevance = getPageAbsRelevance(foundPageList, foundIndexListModel);
            List<SearchData> dataList = getSearchData(sortedPageByAbsRelevance, textLemmaList);
            int dataListSize = dataList.size();
            if (offset > dataListSize) {
                return new ArrayList<>();
            }
            if (dataListSize > limit) {
                for (int i = offset; i < limit; i++) {
                    result.add(dataList.get(i));
                }
                return result;
            }
            return dataList;
        }
        return result;
    }

    private Hashtable<Page, Float> getPageAbsRelevance(List<Page> pageList, List<IndexModel> indexModelList) {
        HashMap<Page, Float> pageWithRelevance = new HashMap<>();
        for (Page page : pageList) {
            float relevant = 0;
            for (IndexModel indexModel : indexModelList) {
                if (indexModel.getPage() == page) {
                    relevant += indexModel.getRank();
                }
            }
            pageWithRelevance.put(page, relevant);
        }
        HashMap<Page, Float> pageWithAbsRelevance = new HashMap<>();
        for (Page page : pageWithRelevance.keySet()) {
            float absRelevant = pageWithRelevance.get(page) / Collections.max(pageWithRelevance.values());
            pageWithAbsRelevance.put(page, absRelevant);
        }
        return pageWithAbsRelevance.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, Hashtable::new));
    }



}
