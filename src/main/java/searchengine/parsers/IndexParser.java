package searchengine.parsers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import searchengine.dto.statistics.StatisticsIndex;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SitePage;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndexParser {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final Morphology morphology;
    private List<StatisticsIndex> statisticsIndexList;
    public List<StatisticsIndex> getIndexList() {

        return statisticsIndexList;
    }

    public void run(SitePage sitePage) {
        Iterable<Page> pageList = pageRepository.findBySiteId(sitePage);
        List<Lemma> lemmaList = lemmaRepository.findBySiteId(sitePage);
        statisticsIndexList = new ArrayList<>();
        for (Page page : pageList) {
            if (page.getCode() < 400) {
                long pageId = page.getId();
                String content = page.getContent();
                String title = ClearingHtml.clearHtml(content, "title");
                String body = ClearingHtml.clearHtml(content, "body");
                HashMap<String, Integer> titleList = morphology.getLemmaList(title);
                HashMap<String, Integer> bodyList = morphology.getLemmaList(body);
                for (Lemma lemma : lemmaList) {
                    Long lemmaId = lemma.getId();
                    String theExactLemma = lemma.getLemma();
                    if (titleList.containsKey(theExactLemma) || bodyList.containsKey(theExactLemma)) {
                        float wholeRank = 0.0F;
                        if (titleList.get(theExactLemma) != null) {
                            Float titleRank = Float.valueOf(titleList.get(theExactLemma));
                            wholeRank += titleRank;
                        }
                        if (bodyList.get(theExactLemma) != null) {
                            float bodyRank = (float) (bodyList.get(theExactLemma) * 0.8);
                            wholeRank += bodyRank;
                        }
                        statisticsIndexList.add(new StatisticsIndex(pageId, lemmaId, wholeRank));
                    } else {
                    }
                }
            } else {
                log.error("Ошибочный статус - " + page.getCode());
            }
        }
    }


}
