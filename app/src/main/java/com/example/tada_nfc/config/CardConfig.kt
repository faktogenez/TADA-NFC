package com.example.tada_nfc.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

object CardConfig {

    const val APP_NAME = "Tada"

    enum class Language(val code: String, val label: String) {
        EN("en", "English"),
        RU("ru", "Русский"),
        KO("ko", "한국어"),
        JA("ja", "日本語"),
        ZH("zh", "中文")
    }

    var currentLanguage by mutableStateOf(
        Language.entries.find { it.code == Locale.getDefault().language } ?: Language.EN
    )

    private val translations = mapOf(
        "language" to mapOf(Language.EN to "Language", Language.RU to "Язык", Language.KO to "언어", Language.JA to "言語", Language.ZH to "语言"),
        "share" to mapOf(Language.EN to "Share with a friend", Language.RU to "Поделиться", Language.KO to "친구에게 공유하기", Language.JA to "友達に共有する", Language.ZH to "分享给朋友"),
        "share_msg" to mapOf(
            Language.EN to "Check out Tada — the easiest way to check your transit card balance!",
            Language.RU to "Попробуй Tada — самый простой способ проверить баланс транспортной карты!",
            Language.KO to "교통카드 잔액 확인의 가장 쉬운 방법, Tada를 확인해보세요!",
            Language.JA to "交通系ICカードの残高確認に便利なアプリ, Tada를 체크！",
            Language.ZH to "试试 Tada — 查询交通卡余额最简单的方法！"
        ),
        "close" to mapOf(Language.EN to "Close", Language.RU to "Закрыть", Language.KO to "닫기", Language.JA to "閉じる", Language.ZH to "关闭"),
        "settings" to mapOf(Language.EN to "Settings", Language.RU to "Настройки", Language.KO to "설정", Language.JA to "設定", Language.ZH to "设置"),
        "version" to mapOf(Language.EN to "Version", Language.RU to "Версия", Language.KO to "версия", Language.JA to "バージョン", Language.ZH to "版本"),
        "contact_dev" to mapOf(
            Language.EN to "Support (WhatsApp)",
            Language.RU to "Поддержка (WhatsApp)",
            Language.KO to "고객지원 (WhatsApp)",
            Language.JA to "サポート (WhatsApp)",
            Language.ZH to "技术支持 (WhatsApp)"
        ),
        "instruction" to mapOf(
            Language.EN to "Hold card to the back for 3 seconds",
            Language.RU to "Приложите карту к задней панели на 3 секунды",
            Language.KO to "카드를 뒷면에 3초 동안 대주세요",
            Language.JA to "カードを背면에 3秒間かざしてください",
            Language.ZH to "请将卡片贴在背面3秒钟"
        ),
        "tap_again" to mapOf(
            Language.EN to "Tap card again",
            Language.RU to "Приложите карту еще раз",
            Language.KO to "카드를 다시 대주세요",
            Language.JA to "もう一度カードをかざしてください",
            Language.ZH to "请再次贴上卡片"
        ),
        "ADULT" to mapOf(Language.EN to "ADULT", Language.RU to "ВЗРОСЛЫЙ", Language.KO to "일반", Language.JA to "大人", Language.ZH to "成人"),
        "CHILD" to mapOf(Language.EN to "CHILD", Language.RU to "ДЕТСКИЙ", Language.KO to "어린и", Language.JA to "小児", Language.ZH to "儿童"),
        "YOUTH" to mapOf(Language.EN to "YOUTH", Language.RU to "ПОДРОСТОК", Language.KO to "청소년", Language.JA to "中高生", Language.ZH to "青少年"),
        "CLIMATE" to mapOf(Language.EN to "CLIMATE CARD", Language.RU to "КЛИМАТИЧЕСКАЯ", Language.KO to "기후동행카드", Language.JA to "気候同行カード", Language.ZH to "气候同行卡"),
        "HIPASS" to mapOf(Language.EN to "HI-PASS", Language.RU to "HI-PASS", Language.KO to "하이패스", Language.JA to "ハイパス", Language.ZH to "高速通行卡"),
        "UNKNOWN" to mapOf(Language.EN to "CARD", Language.RU to "КАРТА", Language.KO to "카드", Language.JA to "카드", Language.ZH to "卡"),
        "unsupported_card" to mapOf(
            Language.EN to "Unsupported Card",
            Language.RU to "Карта не поддерживается",
            Language.KO to "지원되지 않는 카드입니다",
            Language.JA to "サポートされていないカードです",
            Language.ZH to "不支持该卡片"
        ),
        "card_not_supported" to mapOf(
            Language.EN to "Unsupported Card",
            Language.RU to "Карта не поддерживается",
            Language.KO to "지원되지 않는 카드",
            Language.JA to "非対応カード",
            Language.ZH to "不支持的卡"
        ),
        "card_not_supported_desc" to mapOf(
            Language.EN to "Please use a valid Hi-Pass, T-Money, Cashbee or RailPlus card",
            Language.RU to "Используйте карты Hi-Pass, T-Money, Cashbee или RailPlus",
            Language.KO to "Hi-Pass, T-Money, Cashbee 또는 RailPlus 카드를 사용해주세요",
            Language.JA to "Hi-Pass, T-Money, Cashbee, RailPlus カードを使用してください",
            Language.ZH to "请使用 Hi-Pass, T-Money, Cashbee 或 RailPlus 卡"
        ),
        "age_child" to mapOf(Language.EN to "~12 years", Language.RU to "до 12 лет", Language.KO to "만 12세 이하", Language.JA to "12歳まで", Language.ZH to "12岁以下"),
        "age_youth" to mapOf(Language.EN to "13~18 years", Language.RU to "13-18 лет", Language.KO to "만 13~18세", Language.JA to "13~18歳", Language.ZH to "13-18岁"),
        "top_up" to mapOf(Language.EN to "Top Up", Language.RU to "Пополнить", Language.KO to "충전하기", Language.JA to "チャージ", Language.ZH to "充值"),
        "history" to mapOf(Language.EN to "History", Language.RU to "История", Language.KO to "이용내역", Language.JA to "履歴", Language.ZH to "历史"),
        "balance_label" to mapOf(Language.EN to "Balance", Language.RU to "Баланс", Language.KO to "잔액", Language.JA to "残高", Language.ZH to "余额"),
        "nfc_off_title" to mapOf(
            Language.EN to "NFC is Disabled",
            Language.RU to "NFC выключен",
            Language.KO to "NFC가 꺼져 있습니다",
            Language.JA to "NFCが無効です",
            Language.ZH to "NFC已关闭"
        ),
        "nfc_off_desc" to mapOf(
            Language.EN to "NFC is required to read your card. Please enable it in settings to continue",
            Language.RU to "Для чтения карты необходим NFC. Пожалуйста, включите его в настройках",
            Language.KO to "카드를 읽으려면 NFC가 필요합니다. 계속하려면 설정에서 켜주세요",
            Language.JA to "カードを読み取るにはNFCが必要です。設定で有効にしてください",
            Language.ZH to "需要开启NFC才能读取卡片。请在设置中开启以继续"
        ),
        "enable_nfc" to mapOf(
            Language.EN to "Enable NFC",
            Language.RU to "Включить NFC",
            Language.KO to "NFC 켜기",
            Language.JA to "NFCを有効にする",
            Language.ZH to "开启NFC"
        ),
        "hold_card_writing" to mapOf(
            Language.EN to "Scanning... Do not move card",
            Language.RU to "Сканирование... Не убирайте карту",
            Language.KO to "스캔 중... 카드를 움직이지 마세요",
            Language.JA to "スキャン中... カードを動かさないでください",
            Language.ZH to "正在扫描... 请勿移动卡片"
        ),
        "coupang_disclosure" to mapOf(
            Language.EN to "This post is part of Coupang Partners activities, and I receive a certain commission accordingly.",
            Language.RU to "Этот пост является частью деятельности партнеров Coupang, и я получаю соответствующую комиссию.",
            Language.KO to "이 포스팅은 쿠팡 파트너스 활동의 일환으로, 이에 따른 일정액의 수수료를 제공받습니다.",
            Language.JA to "この投稿はCoupangパートナー活動の一環であり、これに応じて一定のコミッションを受け取ります。",
            Language.ZH to "这篇文章是 Coupang 合作伙伴活动的一部分，我将据此获得一定的佣金。"
        ),
        "top_up_tx" to mapOf(Language.EN to "Top Up", Language.RU to "Пополнение", Language.KO to "충전", Language.JA to "チャージ", Language.ZH to "充值"),
        "transit_tx" to mapOf(Language.EN to "Transit", Language.RU to "Проезд", Language.KO to "결제", Language.JA to "決済", Language.ZH to "支付")
    )

    fun translate(key: String): String = translations[key]?.get(currentLanguage) ?: key

    val languageLabel get() = translate("language")
    val shareAppLabel get() = translate("share")
    val shareAppMessage get() = translate("share_msg")
    val videoInstruction get() = translate("instruction")

    @Composable
    fun getResponsiveFontSize(baseSp: Int): TextUnit {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        val factor = (screenWidth / 360f).coerceIn(0.8f, 1.2f)
        return (baseSp * factor).sp
    }

    @Composable
    fun getResponsiveDp(baseDp: Int): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        val factor = (screenWidth / 360f).coerceIn(0.8f, 1.2f)
        return (baseDp * factor).dp
    }

    var instructionYOffset by mutableStateOf(0.65f)
    @Composable
    fun instructionFontSize() = getResponsiveFontSize(24)
    var instructionFontWeight by mutableStateOf(FontWeight.ExtraBold)
    var instructionColor by mutableStateOf(Color(0xFF263238)) // Professional Slate

    val colorError = Color(0xFFC62828) // Deep Red
    val dialogAccent = Color(0xFF263238) // Professional Slate (Adult style)
    val colorDialogBg = Color(0xFFFAF9F6) // Premium Off-White

    @Composable fun nfcOffTitleSize() = getResponsiveFontSize(24)
    @Composable fun nfcOffDescSize() = getResponsiveFontSize(18)
    @Composable fun nfcOffButtonTextSize() = getResponsiveFontSize(18)
    @Composable fun nfcOffIconSize() = getResponsiveDp(100)
    @Composable fun nfcOffHeaderPadding() = getResponsiveDp(8)
    @Composable fun nfcOffContentPadding() = getResponsiveDp(24)
    val nfcOffCornerRadius = 16.dp

    val cardWidthFraction = 0.85f
    val cardCornerRadius = 16.dp
    val cardScreenAlignment = BiasAlignment(0f, -0.35f)
    val flipAnimationDuration = 500
    val cameraDistance = 12f

    val bodyBackgroundColor = Color(0xFFFDFDFD) // Pure & Simple
    val bodyTextColor = Color(0xFF1A1A1A) // High contrast dark gray
    val secondaryTextColor = Color(0xFF546E7A) // Slate Gray for labels

    fun getHeaderColor(userType: String): Color {
        return when (userType.uppercase()) {
            "CLIMATE" -> Color(0xFF673AB7) // Royal Purple
            "HIPASS" -> Color(0xFF1565C0)  // Navigation Blue
            "ADULT" -> Color(0xFF263238)   // Professional Slate
            "CHILD" -> Color(0xFFF57C00)   // Friendly Orange
            "YOUTH" -> Color(0xFF00796B)   // Modern Teal
            "UNKNOWN" -> Color(0xFFD32F2F) // Alert Red
            "RETRY" -> Color(0xFFFF9800)   // Bright Amber
            else -> Color(0xFF78909C)      // Muted Blue Gray
        }
    }

    val activeBg get() = bodyBackgroundColor
    val activeAccent get() = dialogAccent
    val activeText get() = bodyTextColor

    @Composable fun headerHeight() = getResponsiveDp(52)
    @Composable fun logoSize() = getResponsiveDp(28)
    @Composable fun logoTextSize() = getResponsiveFontSize(16)
    @Composable fun closeIconSize() = getResponsiveDp(42)
    @Composable fun userTypeSize() = getResponsiveFontSize(16)
    @Composable fun ageInfoSize() = getResponsiveFontSize(12)
    @Composable fun balanceTextSize() = getResponsiveFontSize(60)
    @Composable fun balanceSymbolSize() = getResponsiveFontSize(30)
    val balanceSymbolColor = Color(0xFF546E7A)
    @Composable fun cardNumberSize() = getResponsiveFontSize(16)
    @Composable fun settingsIconSize() = getResponsiveDp(32)
    @Composable fun logoIconLetterSize() = getResponsiveFontSize(18)

    val notificationCircleColor = Color(0xFF007BFF)
    val notificationLabelColor = Color(0xFF777777)
    val notificationValueColor = Color(0xFF111111)
    val notificationLabelFontSize = 15.sp
    val notificationBalanceFontSize = 28.sp

    @Composable fun spacingBodyPadding() = getResponsiveDp(32)
    @Composable fun spacingAgeToBalance() = getResponsiveDp(12)
    @Composable fun spacingBalanceToNumber() = getResponsiveDp(4)

    val historyListSpacing = 10.dp
    val historyItemCornerRadius = 14.dp
    val historyItemHeight = 76.dp
    val historyItemPadding = 16.dp
    val historyIconSize = 34.dp
    val historyAmountTextSize = 20.sp
    val historyBalanceTextSize = 13.sp

    val historyBtnHeight = 44.dp
    val historyBtnWidth = 180.dp
    val historyBtnCornerRadius = 14.dp
    val historyBtnIconSize = 20.dp
    val historyBtnFontSize = 16.sp
    val historyBtnPadding = 16.dp
    val historyBtnTopSpacing = 20.dp

    val adInfoBg = Color(0xFFF5F5F5)
    val adBorderColor = Color(0xFFDDDDDD)
    val adProductCardBg = Color.White
    val adDiscountBg = Color(0xFFB91C1C)
    val adPriceColor = Color(0xFFB91C1C)
    val adTitleColor = Color(0xFF666666)

    @Composable fun adTitleSize() = getResponsiveFontSize(11)
    @Composable fun adPriceSize() = getResponsiveFontSize(18)
    @Composable fun adDiscountSize() = getResponsiveFontSize(11)

    val adImageSize = 75.dp
    val adInfoHeight = 65.dp
    val adImageRotation = -7f
    val adImageShadow = 4.dp
    
    val adImageCorner = 12.dp
    val adInfoCornerSmall = 6.dp
    val adInfoCornerLarge = 12.dp
    val adDiscountCorner = 3.dp

    val adTextPaddingStart = 65.dp
    val adTextPaddingEnd = 12.dp
    val adTextAlignment = Alignment.End
    val adPriceArrangement = Arrangement.End

    val adCarouselSpacing = 32.dp
    val adCarouselPadding = 24.dp
    val adAutoScrollDelay = 3000L
    val adScrollDuration = 1200

    fun getAppLink(packageName: String) = "https://play.google.com/store/apps/details?id=$packageName"

    const val SUPPORT_WHATSAPP = "821096688205"

    // --- Дизайн: Координаты иконок (Design: Icon coordinates) ---
    // settings_x = 16.dp (align with close icon)
    // settings_y = 12.dp (shift down from current bottom padding)
    val settingsIconPaddingEnd = 16.dp
    val settingsIconPaddingBottom = 12.dp

    const val COUPANG_PARTNERS_ID = 0
}
