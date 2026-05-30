package com.legalflow.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ContractRiskAnalysisService {

    private static final Map<String, RiskPattern> HIGH_RISK_PATTERNS = new LinkedHashMap<>();
    private static final Map<String, RiskPattern> MEDIUM_RISK_PATTERNS = new LinkedHashMap<>();
    private static final Map<String, RiskPattern> LOW_RISK_PATTERNS = new LinkedHashMap<>();

    static {
        HIGH_RISK_PATTERNS.put("违约金过高", new RiskPattern(
            Pattern.compile("违约.*金.*(\\d+)(万|倍|%)"),
            "违约金金额过高，可能被法院调整",
            "《民法典》第585条规定，约定的违约金过分高于造成的损失的，人民法院或者仲裁机构可以根据当事人的请求予以适当减少"
        ));
        
        HIGH_RISK_PATTERNS.put("无期限竞业限制", new RiskPattern(
            Pattern.compile("竞业限制.*(无期限|终身|永久)"),
            "竞业限制期限过长",
            "根据《劳动合同法》第24条，竞业限制期限不得超过二年"
        ));
        
        HIGH_RISK_PATTERNS.put("免除法定责任", new RiskPattern(
            Pattern.compile("免除.*责任|概不负责|不负.*责任"),
            "存在免除法定责任的条款",
            "《民法典》第506条规定，提供格式条款一方免除其责任、加重对方责任、排除对方主要权利的，该条款无效"
        ));
        
        HIGH_RISK_PATTERNS.put("违约金约定不明", new RiskPattern(
            Pattern.compile("违约.*支付.*赔偿|违约.*承担.*责任"),
            "违约责任约定不明确",
            "建议明确约定违约金的具体金额或计算方式"
        ));
        
        HIGH_RISK_PATTERNS.put("验收标准不明确", new RiskPattern(
            Pattern.compile("验收.*以.*满意.*为标准|甲方满意.*为最终标准"),
            "验收标准过于主观，可能被恶意利用",
            "建议约定明确的验收标准，避免以单方主观满意为标准"
        ));
        
        HIGH_RISK_PATTERNS.put("最终解释权条款", new RiskPattern(
            Pattern.compile("最终解释权.*归.*所有"),
            "最终解释权条款可能被认定为无效",
            "《民法典》第497条规定，提供格式条款一方不合理地免除或者减轻其责任、加重对方责任、限制对方主要权利的格式条款无效"
        ));
        
        HIGH_RISK_PATTERNS.put("永久保密义务", new RiskPattern(
            Pattern.compile("保密.*(永久|终身|无限期)"),
            "保密期限过长，可能影响员工正常就业",
            "保密期限应当合理，一般建议约定为合同终止后2-5年"
        ));
        
        HIGH_RISK_PATTERNS.put("单方解除权条款", new RiskPattern(
            Pattern.compile("甲方.*有权.*单方.*解除|有权.*单方.*解除.*合同"),
            "存在严重不公平的单方解除权条款",
            "建议约定双方平等的解除权条款"
        ));

        MEDIUM_RISK_PATTERNS.put("试用期过长", new RiskPattern(
            Pattern.compile("试用期.*(\\d+)个?月"),
            "试用期可能超过法定上限",
            "根据《劳动合同法》第19条，劳动合同期限3年以上的，试用期不得超过6个月"
        ));
        
        MEDIUM_RISK_PATTERNS.put("未约定社保", new RiskPattern(
            Pattern.compile("(不缴纳|不购买|自愿放弃).*社保"),
            "约定不缴纳社会保险",
            "缴纳社会保险是用人单位的法定义务，此类约定无效"
        ));
        
        MEDIUM_RISK_PATTERNS.put("管辖法院约定", new RiskPattern(
            Pattern.compile("由.*(法院|仲裁).*管辖"),
            "管辖法院或仲裁机构约定",
            "注意管辖约定是否符合法律规定，避免管辖法院过远增加维权成本"
        ));
        
        MEDIUM_RISK_PATTERNS.put("需求变更不增费", new RiskPattern(
            Pattern.compile("需求变更.*不.*(增加|另行).*费用|变更.*不.*(增加|另行).*费用"),
            "需求变更不增加费用可能导致工作量失控",
            "建议约定变更评估机制，重大变更另行协商费用"
        ));
        
        MEDIUM_RISK_PATTERNS.put("个人收款账户", new RiskPattern(
            Pattern.compile("户名.*[一二三四五六七八九十百千万]+|[一二三四五六七八九十百千万]+.*户名|收款人.*[一二三四五六七八九十百千万]+|[一二三四五六七八九十百千万]+.*收款人"),
            "使用个人账户收款存在风险",
            "建议使用对公账户收款，避免税务及财务风险"
        ));

        LOW_RISK_PATTERNS.put("期限约定", new RiskPattern(
            Pattern.compile("有效期.*(\\d+)年|期限.*(\\d+)年"),
            "合同期限较长",
            "建议关注合同到期后的续签条款"
        ));
        
        LOW_RISK_PATTERNS.put("争议解决方式", new RiskPattern(
            Pattern.compile("协商不成.*(向.*起诉|申请仲裁)"),
            "争议解决方式约定",
            "建议确认争议解决方式是否便捷"
        ));
        
        LOW_RISK_PATTERNS.put("通知送达", new RiskPattern(
            Pattern.compile("通知.*送达|送达地址"),
            "通知送达条款",
            "确认送达地址是否准确，确保能及时收到通知"
        ));
    }

    public RiskAnalysisResult analyzeContract(String content) {
        log.info("开始分析合同风险，内容长度: {}", content.length());
        
        List<RiskItem> highRisks = new ArrayList<>();
        List<RiskItem> mediumRisks = new ArrayList<>();
        List<RiskItem> lowRisks = new ArrayList<>();

        for (Map.Entry<String, RiskPattern> entry : HIGH_RISK_PATTERNS.entrySet()) {
            RiskPattern pattern = entry.getValue();
            Matcher matcher = pattern.pattern.matcher(content);
            while (matcher.find()) {
                highRisks.add(new RiskItem(
                    entry.getKey(),
                    "高",
                    pattern.description,
                    pattern.legalReference,
                    extractRelevantClause(content, matcher.start())
                ));
            }
        }

        for (Map.Entry<String, RiskPattern> entry : MEDIUM_RISK_PATTERNS.entrySet()) {
            RiskPattern pattern = entry.getValue();
            Matcher matcher = pattern.pattern.matcher(content);
            while (matcher.find()) {
                mediumRisks.add(new RiskItem(
                    entry.getKey(),
                    "中",
                    pattern.description,
                    pattern.legalReference,
                    extractRelevantClause(content, matcher.start())
                ));
            }
        }

        for (Map.Entry<String, RiskPattern> entry : LOW_RISK_PATTERNS.entrySet()) {
            RiskPattern pattern = entry.getValue();
            Matcher matcher = pattern.pattern.matcher(content);
            while (matcher.find()) {
                lowRisks.add(new RiskItem(
                    entry.getKey(),
                    "低",
                    pattern.description,
                    pattern.legalReference,
                    extractRelevantClause(content, matcher.start())
                ));
            }
        }

        return new RiskAnalysisResult(highRisks, mediumRisks, lowRisks);
    }

    private String extractRelevantClause(String content, int matchPosition) {
        String[] lines = content.split("\\n");
        int charCount = 0;
        int startLine = 0;
        int endLine = 0;
        
        for (int i = 0; i < lines.length; i++) {
            int lineStart = charCount;
            int lineEnd = charCount + lines[i].length() + 1;
            
            if (matchPosition >= lineStart && matchPosition < lineEnd) {
                startLine = Math.max(0, i - 2);
                endLine = Math.min(lines.length - 1, i + 2);
                break;
            }
            charCount = lineEnd;
        }
        
        StringBuilder clause = new StringBuilder();
        for (int i = startLine; i <= endLine; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                clause.append(line).append(" ");
            }
        }
        
        return clause.toString().trim();
    }

    public static class RiskPattern {
        Pattern pattern;
        String description;
        String legalReference;

        RiskPattern(Pattern pattern, String description, String legalReference) {
            this.pattern = pattern;
            this.description = description;
            this.legalReference = legalReference;
        }
    }

    public static class RiskItem {
        String riskType;
        String level;
        String description;
        String legalReference;
        String context;

        RiskItem(String riskType, String level, String description, String legalReference, String context) {
            this.riskType = riskType;
            this.level = level;
            this.description = description;
            this.legalReference = legalReference;
            this.context = context;
        }

        public String getRiskType() { return riskType; }
        public String getLevel() { return level; }
        public String getDescription() { return description; }
        public String getLegalReference() { return legalReference; }
        public String getContext() { return context; }
    }

    public static class RiskAnalysisResult {
        List<RiskItem> highRisks;
        List<RiskItem> mediumRisks;
        List<RiskItem> lowRisks;

        RiskAnalysisResult(List<RiskItem> highRisks, List<RiskItem> mediumRisks, List<RiskItem> lowRisks) {
            this.highRisks = highRisks;
            this.mediumRisks = mediumRisks;
            this.lowRisks = lowRisks;
        }

        public List<RiskItem> getHighRisks() { return highRisks; }
        public List<RiskItem> getMediumRisks() { return mediumRisks; }
        public List<RiskItem> getLowRisks() { return lowRisks; }

        public int getTotalRiskCount() {
            return highRisks.size() + mediumRisks.size() + lowRisks.size();
        }
    }
}
