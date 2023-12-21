package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexModel;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexModel, Long> {
    @Query(value = "SELECT i.* FROM `indexes` i WHERE i.lemma_id IN :lemmas AND i.page_id IN :pages", nativeQuery = true)
    List<IndexModel> findByPagesAndLemmas(@Param("lemmas") List<Lemma> lemmaListId,
                                          @Param("pages") List<Page> pageListId);

    List<IndexModel> findByLemmaId (long lemmaId);
    List<IndexModel> findByPageId (long pageId);
    IndexModel findByLemmaIdAndPageId (long lemmaId, long pageId);


}
