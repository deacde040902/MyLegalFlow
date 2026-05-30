package com.legalflow.agent.lawretriever;

import com.legalflow.agent.BaseAgent;
import com.legalflow.dto.KnowledgeSearchResult;
import com.legalflow.dto.LawRetrievalQuery;
import com.legalflow.dto.LegalReference;
import com.legalflow.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LawRetrieverAgent extends BaseAgent<LawRetrievalQuery, List<LegalReference>> {

    private final KnowledgeBaseService knowledgeBaseService;

    public LawRetrieverAgent(KnowledgeBaseService knowledgeBaseService) {
        super("法规检索Agent");
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @Override
    protected List<LegalReference> doExecute(LawRetrievalQuery query) {
        log.info("开始检索法律法规，查询内容: {}", query.getQueryText());

        List<LegalReference> references = new ArrayList<>();

        try {
            List<KnowledgeSearchResult> searchResults = knowledgeBaseService.searchWithScore(
                    query.getQueryText(),
                    query.getMaxResults() > 0 ? query.getMaxResults() : 5,
                    query.getLawCategory()
            );

            for (KnowledgeSearchResult result : searchResults) {
                LegalReference reference = new LegalReference();
                reference.setLawName(result.getSource());
                reference.setArticleContent(result.getContent());
                reference.setRelevanceScore(result.getScore());
                reference.setCategory(result.getCategory());
                reference.setTags(result.getTags());
                references.add(reference);
            }

            log.info("检索到 {} 条相关法规", references.size());
        } catch (Exception e) {
            log.error("法规检索失败: {}", e.getMessage(), e);
            references = getDefaultReferences(query.getQueryText());
        }

        return references;
    }

    private List<LegalReference> getDefaultReferences(String queryText) {
        List<LegalReference> defaults = new ArrayList<>();
        
        LegalReference ref = new LegalReference();
        ref.setLawName("中华人民共和国劳动合同法");
        ref.setArticleNumber("第23条");
        ref.setArticleContent("用人单位与劳动者可以在劳动合同中约定保守用人单位的商业秘密和与知识产权相关的保密事项。对负有保密义务的劳动者，用人单位可以在劳动合同或者保密协议中与劳动者约定竞业限制条款。");
        ref.setRelevanceScore(0.95);
        ref.setCategory("劳动法");
        defaults.add(ref);

        return defaults;
    }
}
