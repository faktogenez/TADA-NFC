package com.example.tada_nfc

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
            Language.EN to "Please use a valid T-Money, Cashbee or RailPlus card",
            Language.RU to "Используйте карты T-Money, Cashbee или RailPlus",
            Language.KO to "T-Money, Cashbee 또는 RailPlus 카드를 사용해주세요",
            Language.JA to "T-Money, Cashbee, RailPlus カードを使用してください",
            Language.ZH to "请使用 T-Money, Cashbee 或 RailPlus 卡"
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
        )
    )

    fun translate(key: String): String = translations[key]?.get(currentLanguage) ?: key

    val languageLabel get() = translate("language")
    val shareAppLabel get() = translate("share")
    val shareAppMessage get() = translate("share_msg")
    val videoInstruction get() = translate("instruction")

    // --- Адаптивные расчеты ---
    @Composable
    fun getResponsiveFontSize(baseSp: Int): TextUnit {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        // Базовый расчет относительно ширины 360dp (стандартный телефон)
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

    // --- Настройки инструкции (Overlay) ---
    var instructionYOffset by mutableStateOf(0.65f)
    @Composable
    fun instructionFontSize() = getResponsiveFontSize(24)
    var instructionFontWeight by mutableStateOf(FontWeight.ExtraBold)
    var instructionColor by mutableStateOf(Color(0xFF333333))
    var instructionShadowColor by mutableStateOf(Color.White.copy(alpha = 0.5f))
    var instructionShadowBlur by mutableStateOf(4f)

    // --- Цвета (Colors) ---
    val colorError = Color(0xFFB91C1C) // Глубокий красный (ошибки)
    val colorSafe = Color(0xFF10B981)  // Изумрудно-зеленый (безопасно)
    val colorPurpleBorder = Color(0xFF7C3AED) // Фиолетовый (рамки)
    val colorDialogBg = Color.White.copy(alpha = 0.95f) // Фон диалогов

    // --- Параметры окна "NFC Отключен" (NFC Off Dialog) ---
    @Composable fun nfcOffTitleSize() = getResponsiveFontSize(24) // Уменьшено с 32 до 24
    @Composable fun nfcOffDescSize() = getResponsiveFontSize(18)
    @Composable fun nfcOffButtonTextSize() = getResponsiveFontSize(18)
    @Composable fun nfcOffIconSize() = getResponsiveDp(100)
    @Composable fun nfcOffHeaderPadding() = getResponsiveDp(8)
    @Composable fun nfcOffContentPadding() = getResponsiveDp(24)
    val nfcOffCornerRadius = 16.dp

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

    // --- Размеры элементов (Адаптивные) ---
    @Composable fun headerHeight() = getResponsiveDp(52)
    @Composable fun logoSize() = getResponsiveDp(28)
    @Composable fun logoTextSize() = getResponsiveFontSize(16)
    @Composable fun closeIconSize() = getResponsiveDp(42)
    @Composable fun userTypeSize() = getResponsiveFontSize(16)
    @Composable fun ageInfoSize() = getResponsiveFontSize(12)
    @Composable fun balanceTextSize() = getResponsiveFontSize(60)
    @Composable fun balanceSymbolSize() = getResponsiveFontSize(30)
    val balanceSymbolColor = Color(0xFF999999)
    @Composable fun cardNumberSize() = getResponsiveFontSize(16)
    @Composable fun settingsIconSize() = getResponsiveDp(32)
    @Composable fun logoIconLetterSize() = getResponsiveFontSize(18)

    // --- Параметры уведомления ---
    val notificationCircleColor = Color(0xFF007BFF)
    val notificationLabelColor = Color(0xFF777777)
    val notificationValueColor = Color(0xFF111111)
    val notificationLabelFontSize = 15.sp
    val notificationBalanceFontSize = 28.sp

    // --- Размеры и отступы (Spacing) ---
    @Composable fun spacingBodyPadding() = getResponsiveDp(32)
    @Composable fun spacingAgeToBalance() = getResponsiveDp(12)
    @Composable fun spacingBalanceToNumber() = getResponsiveDp(4)

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
