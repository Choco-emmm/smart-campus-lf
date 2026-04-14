package com.choco.smartlf.utils;

/**
 * WebSocket 系统通知文案常量池
 * 使用 %s 作为动态参数的占位符
 */
public class WsNoticeConstant {

    // ================= 认领相关通知 =================

    /**
     * 发布的帖子收到了新的认领申请
     * 参数 1: 帖子title
     */
    public static final String NEW_CLAIM_APPLY = "🔔 提示：您发布的帖子【%s】收到了新的认领申请，请前往系统后台审核！";

    /**
     * 认领申请的状态发生了变更（通过/拒绝/补充材料）
     * 参数 1: 物品名称
     * 参数 2: 最新状态描述 (如：已同意、已被拒绝)
     */
    public static final String CLAIM_STATUS_CHANGED = "📝 进度更新：您对【%s】的认领申请状态已变更为「%s」，请及时查看详细信息。";


    // ================= 互动交流通知 =================

    /**
     * 发布的帖子收到了新留言
     * 参数 1: 物品名称
     */
    public static final String NEW_COMMENT = "💬 留言提醒：您的帖子【%s】收到了一条新留言，快去看看吧！";

    /**
     * 收到了新的私信
     * 参数 1: 发信人昵称
     * 💡 注意：这条通常用于用户不在聊天界面时，右下角弹出的轻提示
     */
    public static final String NEW_PRIVATE_MESSAGE = "✉️ 私信提醒：您收到了一条来自【%s】的新私信。";


    // ================= AI 辅助通知 (昨天加的) =================
    
    /**
     * AI 处理完成
     * 参数 1: 物品名称
     */
    public static final String AI_POLISH_FINISH = "✨ 智能助手：AI 已为您的帖子【%s】补全了类型和详细描述！";

}