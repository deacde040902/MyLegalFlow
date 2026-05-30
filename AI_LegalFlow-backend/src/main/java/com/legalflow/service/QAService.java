package com.legalflow.service;

import com.legalflow.dto.KnowledgeSearchResult;
import com.legalflow.dto.LegalReference;
import com.legalflow.dto.QAResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class QAService {

    private final KnowledgeBaseService knowledgeBaseService;
    private final DocumentParserService documentParserService;
    private final ContractRiskAnalysisService riskAnalysisService;

    public QAResponse answerQuestion(String question, String taskType) {
        return answerQuestionWithFiles(question, taskType, null);
    }

    public QAResponse answerQuestionWithContext(String question, String taskType, String context) {
        return answerQuestionWithFilesAndContext(question, taskType, null, context);
    }

    public QAResponse answerQuestionWithFiles(String question, String taskType, List<MultipartFile> files) {
        return answerQuestionWithFilesAndContext(question, taskType, files, "");
    }

    public QAResponse answerQuestionWithFilesAndContext(String question, String taskType, List<MultipartFile> files, String context) {
        log.info("处理问答请求: {}, 文件数量: {}, 上下文长度: {}", question, files != null ? files.size() : 0, context.length());
        
        long startTime = System.currentTimeMillis();
        
        QAResponse response = new QAResponse();
        response.setQuestion(question);
        response.setSuccess(true);
        
        try {
            StringBuilder fileContentBuilder = new StringBuilder();
            boolean hasFiles = files != null && !files.isEmpty();
            
            if (hasFiles) {
                for (MultipartFile file : files) {
                    String content = documentParserService.parseDocument(file);
                    if (content != null && !content.trim().isEmpty()) {
                        fileContentBuilder.append(content);
                    }
                }
            }
            
            boolean isContractAnalysis = (hasFiles && fileContentBuilder.length() > 0) && 
                (question == null || question.trim().isEmpty() || 
                 question.contains("风险") || question.contains("审查") || 
                 question.contains("分析") || question.contains("扫描"));
            
            // 检查是否是追问
            boolean isFollowUp = context != null && !context.isEmpty() && !isContractAnalysis;
            
            StringBuilder answerBuilder = new StringBuilder();
            List<LegalReference> references = new ArrayList<>();
            
            if (isContractAnalysis) {
                answerBuilder.append(analyzeContractRisk(fileContentBuilder.toString()));
            } else if (isFollowUp) {
                String followUpAnswer = handleFollowUpQuestion(question, context, fileContentBuilder.toString());
                if (followUpAnswer != null) {
                    answerBuilder.append(followUpAnswer);
                } else {
                    // 如果是新的法律问题，忽略上下文，正常回答
                    answerBuilder.append(getDefaultAnswer(question));
                    references.addAll(getDefaultReferences(question));
                }
            } else {
                String combinedQuestion = question;
                if (fileContentBuilder.length() > 0) {
                    combinedQuestion = question + "\n\n参考内容：\n" + fileContentBuilder.toString().substring(0, Math.min(1000, fileContentBuilder.length()));
                }
                
                List<KnowledgeSearchResult> searchResults = knowledgeBaseService.searchWithScore(combinedQuestion, 5, null);
                
                if (!searchResults.isEmpty()) {
                    answerBuilder.append("根据知识库检索，以下是相关法律信息：\n\n");
                    
                    for (KnowledgeSearchResult result : searchResults) {
                        LegalReference ref = new LegalReference();
                        ref.setLawName(result.getSource());
                        ref.setArticleContent(result.getContent());
                        ref.setRelevanceScore(result.getScore());
                        ref.setCategory(result.getCategory());
                        ref.setTags(result.getTags());
                        references.add(ref);
                        
                        answerBuilder.append("【").append(result.getCategory()).append("】\n");
                        answerBuilder.append(result.getContent());
                        answerBuilder.append("\n\n");
                    }
                } else {
                    answerBuilder.append(getDefaultAnswer(combinedQuestion));
                    references.addAll(getDefaultReferences(combinedQuestion));
                }
            }
            
            response.setAnswer(answerBuilder.toString().trim());
            response.setLegalReferences(references);
            
        } catch (Exception e) {
            log.error("问答处理失败: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setAnswer("抱歉，处理您的问题时出现错误：" + e.getMessage());
        }
        
        long responseTime = System.currentTimeMillis() - startTime;
        response.setResponseTime(String.format("%.2f秒", responseTime / 1000.0));
        
        log.info("问答完成，耗时: {}ms", responseTime);
        
        return response;
    }

    private String handleFollowUpQuestion(String question, String context, String fileContent) {
        StringBuilder answer = new StringBuilder();
        
        answer.append(getGreetingPrefix());
        
        // 检测常见的追问模式
        if (containsAny(question, "修改", "改造", "改", "修改建议", "怎么改", "如何修改")) {
            answer.append("## 合同条款修改建议\n\n");
            answer.append("基于之前的风险分析，以下是针对高风险条款的修改建议：\n\n");
            
            // 从上下文中提取风险信息并给出修改建议
            if (context.contains("验收标准不明确")) {
                answer.append("### 验收标准条款修改\n\n");
                answer.append("**原条款问题**：以\"甲方满意\"为标准过于主观\n\n");
                answer.append("**建议修改为**：\n");
                answer.append("> 软件验收以双方确认的《功能需求说明书》和《验收标准文档》为依据，验收时应当场签署验收单。\n");
                answer.append("> 甲方应在收到交付后5个工作日内提出书面修改意见，逾期未提出视为验收通过。\n\n");
            }
            
            if (context.contains("最终解释权条款")) {
                answer.append("### 最终解释权条款修改\n\n");
                answer.append("**原条款问题**：该条款可能被认定为无效格式条款\n\n");
                answer.append("**建议修改为**：\n");
                answer.append("> 本合同的解释适用中华人民共和国法律。对本合同条款的理解发生争议的，应当按照通常理解予以解释。\n\n");
            }
            
            if (context.contains("永久保密义务")) {
                answer.append("### 保密期限条款修改\n\n");
                answer.append("**原条款问题**：保密期限过长\n\n");
                answer.append("**建议修改为**：\n");
                answer.append("> 保密义务的履行期限为合同履行期间及合同终止后3年。\n");
                answer.append("> 对于商业秘密，保密期限至该信息成为公知信息为止。\n\n");
            }
            
            if (context.contains("违约金过高")) {
                answer.append("### 违约金条款修改\n\n");
                answer.append("**原条款问题**：违约金比例过高可能被法院调整\n\n");
                answer.append("**建议修改为**：\n");
                answer.append("> 若乙方逾期交付超过30天，甲方有权单方解除合同，乙方应退还已收款项，并支付合同总额20%的违约金。\n\n");
            }
            
            if (context.contains("需求变更不增费")) {
                answer.append("### 需求变更条款修改\n\n");
                answer.append("**原条款问题**：可能导致工作量失控\n\n");
                answer.append("**建议修改为**：\n");
                answer.append("> 甲方提出需求变更的，双方应评估变更范围和影响。\n");
                answer.append("> 变更工作量在原合同总量10%以内的，不增加费用；超出部分双方另行协商费用和工期。\n\n");
            }
        } else if (containsAny(question, "总结", "概括", "要点", "主要风险")) {
            answer.append("## 合同风险总结\n\n");
            answer.append("根据之前的分析，该合同的主要风险点总结如下：\n\n");
            
            if (context.contains("高风险项：")) {
                answer.append("### 🔴 关键风险（需优先处理）\n\n");
                if (context.contains("验收标准不明确")) answer.append("- 验收标准过于主观\n");
                if (context.contains("最终解释权条款")) answer.append("- 存在无效的最终解释权条款\n");
                if (context.contains("永久保密义务")) answer.append("- 保密期限过长\n");
                if (context.contains("违约金过高")) answer.append("- 违约金比例过高\n");
                if (context.contains("单方解除权")) answer.append("- 存在不公平的单方解除权\n");
                answer.append("\n");
            }
            
            if (context.contains("中风险项：")) {
                answer.append("### 🟡 中等风险（建议协商调整）\n\n");
                if (context.contains("需求变更不增费")) answer.append("- 需求变更不增加费用条款\n");
                answer.append("\n");
            }
            
            answer.append("### 💡 总体建议\n\n");
            answer.append("建议您与对方协商修改上述高风险条款，以降低法律风险。如对方不同意修改，可考虑在签署前咨询专业律师的意见。\n");
        } else if (isNewLegalQuestion(question)) {
            return null;
        } else {
            answer.append("## 关于您的问题\n\n");
            answer.append("根据之前的对话，您之前进行了合同风险扫描。\n\n");
            answer.append("如果您需要：\n");
            answer.append("1. 查看修改建议 - 可以问：\"怎么修改这些条款？\"或\"给我一些修改建议\"\n");
            answer.append("2. 查看风险总结 - 可以问：\"总结一下主要风险\"或\"概括一下\"\n");
            answer.append("3. 具体条款咨询 - 可以针对某个条款提问\n");
        }
        
        return answer.toString();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNewLegalQuestion(String question) {
        String[] greetingKeywords = {
            "你好", "您好", "hi", "hello", "嗨", "嘿",
            "你是谁", "你是", "你是干嘛的", "介绍一下", "介绍一下自己",
            "干嘛的", "做什么", "干什么"
        };
        
        for (String keyword : greetingKeywords) {
            if (question.contains(keyword)) {
                return true;
            }
        }
        
        String[] newQuestionKeywords = {
            "工资", "拖欠", "加班费", "社保", "公积金",
            "辞退", "开除", "裁员", "解除", "终止",
            "赔偿", "补偿", "违约金", "经济补偿",
            "工伤", "职业病", "医疗期", "病假",
            "假期", "年假", "产假", "婚假", "丧假",
            "合同", "协议", "条款", "规定",
            "公司", "企业", "老板", "领导", "同事",
            "劳动仲裁", "起诉", "诉讼", "法院", "投诉", "举报"
        };
        
        for (String keyword : newQuestionKeywords) {
            if (question.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }

    private String analyzeContractRisk(String content) {
        ContractRiskAnalysisService.RiskAnalysisResult result = riskAnalysisService.analyzeContract(content);
        
        StringBuilder builder = new StringBuilder();
        builder.append(getGreetingPrefix());
        builder.append("## 合同风险分析报告\n\n");
        builder.append("### 风险概览\n");
        builder.append("- 高风险项：").append(result.getHighRisks().size()).append(" 项\n");
        builder.append("- 中风险项：").append(result.getMediumRisks().size()).append(" 项\n");
        builder.append("- 低风险项：").append(result.getLowRisks().size()).append(" 项\n\n");
        
        if (result.getTotalRiskCount() == 0) {
            builder.append("**未检测到明显的合同风险。**\n");
            builder.append("建议您仔细阅读合同条款，特别关注以下方面：\n");
            builder.append("- 合同期限和终止条款\n");
            builder.append("- 违约责任和赔偿条款\n");
            builder.append("- 争议解决方式\n");
            builder.append("- 双方权利义务的约定\n");
            return builder.toString();
        }
        
        if (!result.getHighRisks().isEmpty()) {
            builder.append("### 🔴 高风险项\n");
            builder.append("---\n");
            for (ContractRiskAnalysisService.RiskItem item : result.getHighRisks()) {
                builder.append("**").append(item.getRiskType()).append("**\n");
                builder.append("- 风险描述：").append(item.getDescription()).append("\n");
                builder.append("- 法律依据：").append(item.getLegalReference()).append("\n");
                builder.append("- 相关条款：").append(item.getContext()).append("\n\n");
            }
        }
        
        if (!result.getMediumRisks().isEmpty()) {
            builder.append("### 🟡 中风险项\n");
            builder.append("---\n");
            for (ContractRiskAnalysisService.RiskItem item : result.getMediumRisks()) {
                builder.append("**").append(item.getRiskType()).append("**\n");
                builder.append("- 风险描述：").append(item.getDescription()).append("\n");
                builder.append("- 法律依据：").append(item.getLegalReference()).append("\n");
                builder.append("- 相关条款：").append(item.getContext()).append("\n\n");
            }
        }
        
        if (!result.getLowRisks().isEmpty()) {
            builder.append("### 🟢 低风险项\n");
            builder.append("---\n");
            for (ContractRiskAnalysisService.RiskItem item : result.getLowRisks()) {
                builder.append("**").append(item.getRiskType()).append("**\n");
                builder.append("- 风险描述：").append(item.getDescription()).append("\n");
                builder.append("- 相关说明：").append(item.getLegalReference()).append("\n");
                builder.append("- 相关条款：").append(item.getContext()).append("\n\n");
            }
        }
        
        builder.append("### 💡 后续操作建议\n\n");
        builder.append("您可以继续提问，例如：\n");
        builder.append("- \"怎么修改这些条款？\" - 获取修改建议\n");
        builder.append("- \"总结一下主要风险\" - 查看风险总结\n");
        builder.append("- 针对某个具体条款的问题\n\n");
        
        builder.append("### ⚠️ 风险提示\n");
        builder.append("以上风险分析仅供参考，具体法律风险需结合实际情况和专业律师意见综合判断。\n");
        
        return builder.toString();
    }

    private String getDefaultAnswer(String question) {
        // 检查是否是问候语
        if (isGreeting(question)) {
            return getGreetingAnswer();
        }
        
        // 先检查关键词匹配
        if (question.contains("工资") || question.contains("拖欠工资")) {
            return generateWageAnswer(question);
        }
        
        String[] keywords = {"竞业限制", "违约金", "试用期", "解除合同", "赔偿", 
                           "加班", "工资", "保险", "保密", "培训", "劳动合同", "合同"};
        
        for (String keyword : keywords) {
            if (question.contains(keyword)) {
                return getGreetingPrefix() + generateAnswerForKeyword(keyword);
            }
        }
        
        return getGreetingPrefix() + "感谢您的提问！关于您的法律问题，以下是一些通用信息：\n\n" +
               "1. 请明确具体的法律问题或场景\n" +
               "2. 提供相关的合同条款或法律条文\n" +
               "3. 我可以帮助您分析风险并提供建议\n\n" +
               "如需更详细的解答，请提供更多具体信息。";
    }

    private boolean isGreeting(String question) {
        String[] greetingKeywords = {
            "你好", "您好", "hi", "hello", "嗨", "嘿",
            "你是谁", "你是", "你是干嘛的", "介绍一下", "介绍一下自己",
            "干嘛的", "做什么", "干什么"
        };
        
        for (String keyword : greetingKeywords) {
            if (question.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String getGreetingAnswer() {
        return getGreetingPrefix() + "您好！很高兴为您服务！\n\n" +
               "我是一名专业的法律咨询助手，专注于劳动法领域。\n\n" +
               "### 我可以帮您：\n\n" +
               "1. **合同风险分析** - 上传您的合同文件，我会为您识别其中的法律风险\n" +
               "2. **法律条文查询** - 解答劳动法相关问题，如工资、工时、社保等\n" +
               "3. **维权建议** - 提供劳动争议解决的专业建议\n" +
               "4. **条款修改建议** - 针对合同中的风险条款提供修改方案\n\n" +
               "### 使用方法：\n\n" +
               "- 直接输入您的法律问题，我会尽力为您解答\n" +
               "- 上传合同文件并输入\"扫描风险\"，即可分析合同风险\n" +
               "- 追问\"改造风险条款\"可获取修改建议\n\n" +
               "### 温馨提示：\n\n" +
               "⚠️ 本服务仅提供参考意见，不构成正式法律建议。具体法律问题建议咨询专业律师。\n\n" +
               "请问有什么可以帮助您的？";
    }

    private String getGreetingPrefix() {
        return "您好！我是您的法律咨询助手。\n\n";
    }

    private String generateWageAnswer(String question) {
        StringBuilder answer = new StringBuilder();
        answer.append(getGreetingPrefix());
        answer.append("## 工资相关法律规定\n\n");
        
        if (question.contains("拖欠") || question.contains("克扣")) {
            answer.append("### 拖欠工资的法律应对\n\n");
            answer.append("根据《劳动法》和《工资支付暂行规定》：\n\n");
            answer.append("**1. 工资支付要求**\n");
            answer.append("- 用人单位应当按时足额支付工资\n");
            answer.append("- 不得克扣或者无故拖欠劳动者的工资\n");
            answer.append("- 工资至少每月支付一次\n\n");
            
            answer.append("**2. 拖欠工资的法律责任**\n");
            answer.append("- 劳动行政部门可以责令支付工资及补偿金\n");
            answer.append("- 劳动者可以申请劳动仲裁\n");
            answer.append("- 可以要求支付拖欠工资25%的经济补偿金\n\n");
            
            answer.append("**3. 维权途径**\n");
            answer.append("① **协商解决**：首先与用人单位协商\n");
            answer.append("② **劳动监察投诉**：向当地劳动监察部门举报\n");
            answer.append("③ **申请劳动仲裁**：向当地劳动争议仲裁委员会申请仲裁（免费）\n");
            answer.append("④ **法院起诉**：对仲裁裁决不服可向法院起诉\n\n");
            
            answer.append("**4. 证据准备**\n");
            answer.append("- 劳动合同\n");
            answer.append("- 工资条、银行流水\n");
            answer.append("- 考勤记录\n");
            answer.append("- 工作证、工牌等\n");
            answer.append("- 与老板沟通的记录（微信、短信等）\n");
        } else {
            answer.append("根据《劳动法》和《工资支付暂行规定》：\n\n");
            answer.append("**1. 最低工资标准**\n");
            answer.append("- 不得低于当地最低工资标准\n\n");
            
            answer.append("**2. 工资支付周期**\n");
            answer.append("- 至少每月支付一次\n");
            answer.append("- 约定日期支付\n\n");
            
            answer.append("**3. 工资构成**\n");
            answer.append("- 包括基本工资、奖金、津贴等\n");
            answer.append("- 以货币形式支付\n\n");
            
            answer.append("**4. 风险提示**\n");
            answer.append("- 不得克扣或无故拖欠工资\n");
            answer.append("- 加班工资应当依法支付\n");
        }
        
        return answer.toString();
    }

    private String generateAnswerForKeyword(String keyword) {
        switch (keyword) {
            case "竞业限制":
                return "**竞业限制条款相关法律规定**\n\n" +
                       "根据《中华人民共和国劳动合同法》第23条和第24条：\n\n" +
                       "1. **适用范围**：仅适用于高级管理人员、高级技术人员和其他负有保密义务的人员\n\n" +
                       "2. **限制期限**：不得超过二年\n\n" +
                       "3. **经济补偿**：用人单位需在竞业限制期限内按月给予劳动者经济补偿\n\n" +
                       "4. **违约责任**：劳动者违反竞业限制约定的，应当按照约定向用人单位支付违约金\n\n" +
                       "**风险提示**：\n- 竞业限制条款必须明确约定补偿标准\n- 限制范围和地域应当合理\n- 未约定补偿或补偿过低可能导致条款无效";
            case "违约金":
                return "**违约金条款相关法律规定**\n\n" +
                       "1. **劳动合同违约金**：除竞业限制和服务期外，用人单位不得约定由劳动者承担违约金\n\n" +
                       "2. **服务期违约金**：劳动者违反服务期约定的，违约金不得超过培训费用\n\n" +
                       "3. **竞业限制违约金**：由双方约定，但过高可能被法院调整\n\n" +
                       "**风险提示**：\n- 违约金金额应当合理\n- 需明确约定违约情形\n- 过高的违约金可能不被支持";
            case "试用期":
                return "**试用期相关法律规定**\n\n" +
                       "根据《中华人民共和国劳动合同法》第19条：\n\n" +
                       "1. **试用期期限**：\n   - 劳动合同期限3个月以上不满1年的，试用期不得超过1个月\n   - 劳动合同期限1年以上不满3年的，试用期不得超过2个月\n   - 3年以上固定期限和无固定期限的劳动合同，试用期不得超过6个月\n\n" +
                       "2. **试用期工资**：不得低于本单位相同岗位最低档工资的80%或劳动合同约定工资的80%\n\n" +
                       "3. **试用期次数**：同一用人单位与同一劳动者只能约定一次试用期\n\n" +
                       "**风险提示**：\n- 试用期包含在劳动合同期限内\n- 试用期内用人单位同样需要缴纳社保";
            case "解除合同":
                return "**劳动合同解除相关法律规定**\n\n" +
                       "1. **协商解除**：双方协商一致可以解除劳动合同\n\n" +
                       "2. **劳动者提前通知解除**：提前30日书面通知，试用期提前3日\n\n" +
                       "3. **用人单位解除**：需要符合法定情形\n\n" +
                       "4. **经济补偿**：符合条件的解除需要支付经济补偿金\n\n" +
                       "**风险提示**：\n- 违法解除劳动合同需要支付赔偿金\n- 解除理由必须合法\n- 需依法支付经济补偿";
            case "赔偿":
                return "**劳动赔偿相关法律规定**\n\n" +
                       "1. **经济补偿金**：按工作年限计算，每满1年支付1个月工资\n\n" +
                       "2. **赔偿金**：违法解除或终止劳动合同的，支付2倍经济补偿金\n\n" +
                       "3. **代通知金**：特定情形下未提前通知的，额外支付1个月工资\n\n" +
                       "**风险提示**：\n- 经济补偿有上限规定\n- 需区分经济补偿和赔偿金";
            case "加班":
                return "**加班相关法律规定**\n\n" +
                       "根据《劳动法》第44条：\n\n" +
                       "1. **工作日加班**：支付不低于工资的150%的工资报酬\n\n" +
                       "2. **休息日加班**：不能安排补休的，支付不低于工资的200%的工资报酬\n\n" +
                       "3. **法定节假日加班**：支付不低于工资的300%的工资报酬\n\n" +
                       "**风险提示**：\n- 加班时间有上限规定\n- 不能用调休替代法定节假日加班工资";
            case "工资":
                return "**工资相关法律规定**\n\n" +
                       "1. **最低工资标准**：不得低于当地最低工资标准\n\n" +
                       "2. **工资支付周期**：至少每月支付一次\n\n" +
                       "3. **工资构成**：包括基本工资、奖金、津贴等\n\n" +
                       "**风险提示**：\n- 工资必须以货币形式支付\n- 不得克扣或无故拖欠工资";
            case "保险":
                return "**社会保险相关法律规定**\n\n" +
                       "1. **五险**：养老保险、医疗保险、失业保险、工伤保险、生育保险\n\n" +
                       "2. **缴纳义务**：用人单位和劳动者都有缴纳义务\n\n" +
                       "3. **缴纳比例**：由各地规定\n\n" +
                       "**风险提示**：\n- 用人单位必须为员工缴纳社保\n- 未缴纳社保员工可以要求补缴";
            case "保密":
                return "**保密协议相关法律规定**\n\n" +
                       "1. **保密义务**：劳动者对用人单位的商业秘密负有保密义务\n\n" +
                       "2. **保密期限**：可以约定在劳动合同终止后继续有效\n\n" +
                       "3. **保密范围**：应当明确约定\n\n" +
                       "**风险提示**：\n- 保密协议应当合理\n- 可以约定违约金";
            case "培训":
                return "**培训与服务期相关法律规定**\n\n" +
                       "1. **服务期约定**：用人单位为劳动者提供专项培训费用，可以约定服务期\n\n" +
                       "2. **违约金**：劳动者违反服务期约定的，需支付违约金\n\n" +
                       "3. **违约金上限**：不得超过用人单位提供的培训费用\n\n" +
                       "**风险提示**：\n- 只有专项培训才能约定服务期\n- 普通岗前培训不能约定服务期";
            case "劳动合同":
            case "合同":
                return "**劳动合同相关法律规定**\n\n" +
                       "1. **订立形式**：应当采用书面形式\n\n" +
                       "2. **必备条款**：包括工作内容、工作地点、劳动报酬等\n\n" +
                       "3. **合同期限**：固定期限、无固定期限、以完成一定工作任务为期限\n\n" +
                       "**合同风险检查要点**：\n- 未签订书面劳动合同需支付双倍工资\n- 连续工作满10年可要求签订无固定期限合同\n- 合同条款是否违反法律强制性规定\n- 违约金条款是否合法";
            default:
                return "关于" + keyword + "的相关法律问题，建议您提供更具体的问题，以便我为您提供更准确的解答。";
        }
    }

    private List<LegalReference> getDefaultReferences(String question) {
        List<LegalReference> references = new ArrayList<>();
        
        LegalReference laborLaw = new LegalReference();
        laborLaw.setLawName("中华人民共和国劳动合同法");
        laborLaw.setArticleNumber("第23条");
        laborLaw.setArticleContent("用人单位与劳动者可以在劳动合同中约定保守用人单位的商业秘密和与知识产权相关的保密事项。对负有保密义务的劳动者，用人单位可以在劳动合同或者保密协议中与劳动者约定竞业限制条款，并约定在解除或者终止劳动合同后，在竞业限制期限内按月给予劳动者经济补偿。");
        laborLaw.setRelevanceScore(0.95);
        laborLaw.setCategory("劳动法");
        references.add(laborLaw);
        
        LegalReference article24 = new LegalReference();
        article24.setLawName("中华人民共和国劳动合同法");
        article24.setArticleNumber("第24条");
        article24.setArticleContent("竞业限制的人员限于用人单位的高级管理人员、高级技术人员和其他负有保密义务的人员。竞业限制的范围、地域、期限由用人单位与劳动者约定，竞业限制的约定不得违反法律、法规的规定。");
        article24.setRelevanceScore(0.90);
        article24.setCategory("劳动法");
        references.add(article24);
        
        return references;
    }
}
