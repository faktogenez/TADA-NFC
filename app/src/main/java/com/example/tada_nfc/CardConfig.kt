package com.example.tada_nfc

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Динамическая конфигурация приложения TADA.
 * Содержит настройки локализации, дизайна и визуальных параметров.
 */
object CardConfig {

    const val APP_NAME = "Tada"

    // --- Настройки языков ---
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

    // --- Локализация (Переводы) ---
    private val translations = mapOf(
        "language" to mapOf(Language.EN to "Language", Language.RU to "Язык", Language.KO to "언어", Language.JA to "言語", Language.ZH to "语言"),
        "share" to mapOf(Language.EN to "Share with a friend", Language.RU to "Поделиться с другом", Language.KO to "친구에게 공유하기", Language.JA to "友達に共有する", Language.ZH to "分享给朋友"),
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
        "CHILD" to mapOf(Language.EN to "CHILD", Language.RU to "ДЕТСКИЙ", Language.KO to "어린이", Language.JA to "小児", Language.ZH to "儿童"),
        "YOUTH" to mapOf(Language.EN to "YOUTH", Language.RU to "ПОДРОСТОК", Language.KO to "청소년", Language.JA to "中高生", Language.ZH to "青少年"),
        "HIPASS" to mapOf(Language.EN to "HI-PASS", Language.RU to "HI-PASS", Language.KO to "하이패스", Language.JA to "ハイパス", Language.ZH to "高速通行卡"),
        "UNKNOWN" to mapOf(Language.EN to "CARD", Language.RU to "КАРТА", Language.KO to "카드", Language.JA to "カード", Language.ZH to "卡"),
        "unsupported_card" to mapOf(
            Language.EN to "Unsupported Card",
            Language.RU to "Карта не поддерживается",
            Language.KO to "지원되지 않는 카드입니다",
            Language.JA to "サポートされていないカードです",
            Language.ZH to "不支持该卡片"
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
            Language.EN to "Enable NFC in settings and press back",
            Language.RU to "Включите NFC в настройках и нажмите назад",
            Language.KO to "설정에서 NFC를 켜고 뒤로 가기를 누르세요",
            Language.JA to "設定でNFCを有効にして、戻るを押してください",
            Language.ZH to "在设置中开启NFC并点击返回"
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
        )
    )

    fun translate(key: String): String = translations[key]?.get(currentLanguage) ?: key

    val languageLabel get() = translate("language")
    val shareAppLabel get() = translate("share")
    val shareAppMessage get() = translate("share_msg")
    val videoInstruction get() = translate("instruction")

    // --- Настройки инструкции (Overlay) ---
    var instructionYOffset by mutableStateOf(0.65f)
    var instructionFontSize by mutableStateOf(25.sp)
    var instructionFontWeight by mutableStateOf(FontWeight.ExtraBold)
    var instructionColor by mutableStateOf(Color(0xFF333333))
    var instructionShadowColor by mutableStateOf(Color.White.copy(alpha = 0.5f))
    var instructionShadowBlur by mutableStateOf(4f)

    // --- Параметры карточки ---
    val cardWidthFraction = 0.85f
    val cardCornerRadius = 16.dp
    val cardScreenAlignment = BiasAlignment(0f, -0.35f)
    val flipAnimationDuration = 500
    val cameraDistance = 12f

    // --- ОБНОВЛЕННЫЕ ЦВЕТА ИНТЕРФЕЙСА ---
    val bodyBackgroundColor = Color(0xFFFBFBFC)
    val bodyTextColor = Color(0xFF111111)
    val secondaryTextColor = Color(0xFF999999)
    val headerTextColor = Color.White.copy(alpha = 0.9f)

    // Новый акцентный цвет: Electric Blue (вместо Indigo)
    val dialogAccent = Color(0xFF007BFF)
    val accentSoft = dialogAccent.copy(alpha = 0.12f) // Мягкий фон для иконок

    /**
     * Возвращает цвет шапки в зависимости от типа пользователя.
     */
    fun getHeaderColor(userType: String): Color {
        return when (userType.uppercase()) {
            "HIPASS" -> Color(0xFF1E40AF) // Deep Blue
            "ADULT" -> Color(0xFF0F172A)  // Slate Black
            "CHILD" -> Color(0xFFF59E0B)  // Amber
            "YOUTH" -> Color(0xFF10B981)  // Emerald Green
            "UNKNOWN" -> Color(0xFFEF4444) // Soft Red
            "RETRY" -> Color(0xFFF97316)   // Orange
            else -> Color(0xFF94A3B8)     // Slate Gray
        }
    }

    // Совместимость
    val activeBg get() = bodyBackgroundColor
    val activeAccent get() = dialogAccent
    val activeText get() = bodyTextColor

    // --- Размеры элементов ---
    val headerHeight = 52.dp
    val logoSize = 28.dp
    val logoTextSize = 16.sp
    val closeIconSize = 42.dp
    val userTypeSize = 16.sp
    val ageInfoSize = 12.sp
    val balanceTextSize = 65.sp
    val balanceSymbolSize = 30.sp
    val balanceSymbolColor = Color(0xFF999999)
    val cardNumberSize = 16.sp
    val settingsIconSize = 32.dp
    val logoIconLetterSize = 18.sp

    // --- Параметры уведомления ---
    val notificationCircleColor = Color(0xFF007BFF)
    val notificationLabelColor = Color(0xFF777777)
    val notificationValueColor = Color(0xFF111111)
    val notificationLabelFontSize = 15.sp
    val notificationBalanceFontSize = 28.sp

    // --- Размеры и отступы (Spacing) ---
    val spacingBodyPadding = 32.dp
    val spacingAgeToBalance = 12.dp
    val spacingBalanceToNumber = 4.dp

    // --- Параметры истории ---
    val historyListSpacing = 10.dp
    val historyItemCornerRadius = 14.dp
    val historyItemHeight = 76.dp
    val historyItemPadding = 16.dp
    val historyIconSize = 34.dp
    val historyAmountTextSize = 20.sp
    val historyBalanceTextSize = 13.sp

    // --- Параметры кнопки "ИСТОРИЯ" ---
    val historyBtnHeight = 44.dp
    val historyBtnWidth = 180.dp
    val historyBtnCornerRadius = 14.dp
    val historyBtnIconSize = 20.dp
    val historyBtnFontSize = 16.sp
    val historyBtnPadding = 16.dp
    val historyBtnTopSpacing = 20.dp

    // Формирование ссылки
    fun getAppLink(packageName: String) = "https://play.google.com/store/apps/details?id=$packageName"
}