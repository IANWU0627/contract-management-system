package com.contracthub.service;

import com.contracthub.websocket.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final WebSocketHandler webSocketHandler;
    private final UserConfigService userConfigService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public NotificationService(WebSocketHandler webSocketHandler, UserConfigService userConfigService) {
        this.webSocketHandler = webSocketHandler;
        this.userConfigService = userConfigService;
    }

    public void sendToUser(Long userId, String type, String title, String content, Map<String, Object> data) {
        if (userId == null) {
            return;
        }

        Map<String, String> configs = userConfigService.getUserConfigValues(userId);
        if (!userConfigService.getBooleanConfig(configs, "ntf_system_notification", true)) {
            logger.info("Skip notification for user {} because system notification is disabled", userId);
            return;
        }
        if (!isCategoryEnabled(configs, type)) {
            logger.info("Skip notification type {} for user {} because category is disabled", type, userId);
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("title", title);
        message.put("content", content);
        message.put("data", data);
        message.put("timestamp", LocalDateTime.now().format(FORMATTER));
        message.put("read", false);
        
        webSocketHandler.sendToUser(userId, message);
        logger.info("Notification sent to user {}: {}", userId, title);
    }

    private boolean isCategoryEnabled(Map<String, String> configs, String type) {
        if (type == null) {
            return true;
        }

        String configKey = switch (type) {
            case "approval_request", "approval_result", "renewal_request", "renewal_result" -> "ntf_approval_notification";
            case "reminder" -> "ntf_expiration_reminder";
            case "contract_comment" -> "ntf_comment_notification";
            default -> null;
        };

        if (configKey == null) {
            return true;
        }
        return userConfigService.getBooleanConfig(configs, configKey, true);
    }

    public void sendApprovalRequest(Long approverId, Long contractId, String contractNo, String title) {
        Map<String, Object> data = new HashMap<>();
        data.put("contractId", contractId);
        data.put("contractNo", contractNo);
        data.put("title", title);
        
        sendToUser(approverId, "approval_request", "新的审批请求", 
                "合同 " + contractNo + " 需要您审批", data);
    }

    public void sendApprovalResult(Long requesterId, Long contractId, String contractNo, String status, String comment) {
        Map<String, Object> data = new HashMap<>();
        data.put("contractId", contractId);
        data.put("contractNo", contractNo);
        data.put("status", status);
        data.put("comment", comment);
        
        String title = "APPROVED".equals(status) ? "合同已通过审批" : "合同审批被拒绝";
        String content = "APPROVED".equals(status) ? 
                "您的合同 " + contractNo + " 已通过审批" : 
                "您的合同 " + contractNo + " 被拒绝: " + comment;
        
        sendToUser(requesterId, "approval_result", title, content, data);
    }

    public void sendRenewalRequest(Long approverId, Long contractId, String contractNo, String oldDate, String newDate) {
        Map<String, Object> data = new HashMap<>();
        data.put("contractId", contractId);
        data.put("contractNo", contractNo);
        data.put("oldEndDate", oldDate);
        data.put("newEndDate", newDate);
        
        sendToUser(approverId, "renewal_request", "新的续约请求", 
                "合同 " + contractNo + " 续约申请: " + oldDate + " -> " + newDate, data);
    }

    public void sendRenewalResult(Long requesterId, Long contractId, String contractNo, String status) {
        Map<String, Object> data = new HashMap<>();
        data.put("contractId", contractId);
        data.put("contractNo", contractNo);
        data.put("status", status);
        
        String title = "APPROVED".equals(status) ? "续约已通过" : "续约申请被拒绝";
        String content = "合同 " + contractNo + " 的续约申请" + ("APPROVED".equals(status) ? "已通过" : "被拒绝");
        
        sendToUser(requesterId, "renewal_result", title, content, data);
    }

    public void sendReminder(Long userId, Long contractId, String contractNo, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("contractId", contractId);
        data.put("contractNo", contractNo);
        
        sendToUser(userId, "reminder", "合同提醒", message, data);
    }

    public void sendContractUpdate(Long userId, Long contractId, String contractNo, String changeType) {
        Map<String, Object> data = new HashMap<>();
        data.put("contractId", contractId);
        data.put("contractNo", contractNo);
        data.put("changeType", changeType);
        
        sendToUser(userId, "contract_update", "合同更新", 
                "合同 " + contractNo + " 发生了" + changeType, data);
    }

    public void sendCommentNotification(Long userId, Long contractId, String contractNo, String commenterName, String content) {
        Map<String, Object> data = new HashMap<>();
        data.put("contractId", contractId);
        data.put("contractNo", contractNo);
        data.put("commenterName", commenterName);

        String preview = content == null ? "" : content.trim();
        if (preview.length() > 60) {
            preview = preview.substring(0, 60) + "...";
        }

        sendToUser(userId, "contract_comment", "合同评论通知",
                commenterName + " 在合同 " + contractNo + " 中评论了您：" + preview, data);
    }

    public void broadcast(String type, String title, String content) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("title", title);
        message.put("content", content);
        message.put("timestamp", LocalDateTime.now().format(FORMATTER));
        
        webSocketHandler.broadcast(message);
    }
}
