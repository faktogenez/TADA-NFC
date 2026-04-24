package com.example.tada_nfc

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Динамическая конфигурация приложения TADA.
 */
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
            Language.EN to "Hold card to the back for 5 seconds",
            Language.RU to "Приложите карту к задней панели на 5 секунд",
            Language.KO to "카드를 뒷면에 5초 동안 대주세요",
            Language.JA to "カードを背面に5秒間かざしてください",
            Language.ZH to "请将卡片贴在背面5秒钟"
        ),
        // Переводы типов пользователей
        "ADULT" to mapOf(Language.EN to "ADULT", Language.RU to "ВЗРОСЛЫЙ", Language.KO to "일반", Language.JA to "大人", Language.ZH to "成人"),
        "CHILD" to mapOf(Language.EN to "CHILD", Language.RU to "ДЕТСКИЙ", Language.KO to "어린и", Language.JA to "小児", Language.ZH to "儿童"),
        "YOUTH" to mapOf(Language.EN to "YOUTH", Language.RU to "ПОДРОСТОК", Language.KO to "청소년", Language.JA to "中高生", Language.ZH to "青少年"),
        "HIPASS" to mapOf(Language.EN to "HI-PASS", Language.RU to "HI-PASS", Language.KO to "하이패스", Language.JA to "ハイパス", Language.ZH to "高速通行卡"),
        "UNKNOWN" to mapOf(Language.EN to "CARD", Language.RU to "КАРТА", Language.KO to "카드", Language.JA to "カード", Language.ZH to "卡"),
        
        "unsupported_card" to mapOf(
            Language.EN to "Unsupported Card",
            Language.RU to "Карта не поддерживается",
            Language.KO to "지원되지 않는 카드입니다",
            Language.JA to "サポートされていないカードです"
        ),

        // Инфо о возрасте
        "age_child" to mapOf(Language.EN to "~12 years", Language.RU to "до 12 лет", Language.KO to "만 12세 이하", Language.JA to "12歳まで", Language.ZH to "12岁以下"),
        "age_youth" to mapOf(Language.EN to "13~18 years", Language.RU to "13-18 лет", Language.KO to "만 13~18세", Language.JA to "13~18歳", Language.ZH to "13-18岁")
    )

    fun translate(key: String): String = translations[key]?.get(currentLanguage) ?: key

    fun getAppLink(packageName: String) = "https://play.google.com/store/apps/details?id=$packageName"

    val languageLabel get() = translate("language")
    val shareAppLabel get() = translate("share")
    val shareAppMessage get() = translate("share_msg")
    val videoInstruction get() = translate("instruction")

    // --- Настройки инструкции (Overlay) ---
    var instructionYOffset by mutableStateOf(0.65f)
    var instructionFontSize by mutableStateOf(25.sp)
    var instructionFontWeight by mutableStateOf(FontWeight.ExtraBold)
    var instructionColor by mutableStateOf(Color(0xFF333333)) // Угольный цвет
    var instructionShadowColor by mutableStateOf(Color.White.copy(alpha = 0.5f))
    var instructionShadowBlur by mutableStateOf(4f)

    // --- Параметры карточки (Адаптированный дизайн) ---
    val cardWidthFraction = 0.9f
    val cardAspectRatio = 1.6f
    val cardCornerRadius = 16.dp 
    val cardScreenAlignment = BiasAlignment(0f, -0.35f)
    val cardScreenTopPadding = 0.dp
    val flipAnimationDuration = 500
    val cameraDistance = 12f

    // Базовые цвета
    val bodyBackgroundColor = Color(0xFFFBFBFC)
    val bodyTextColor = Color(0xFF111111)
    val secondaryTextColor = Color(0xFF999999)
    val headerTextColor = Color.White.copy(alpha = 0.9f)

    // Динамические цвета в зависимости от типа карты
    fun getHeaderColor(userType: String): Color {
        return when (userType.uppercase()) {
            "HIPASS" -> Color(0xFF1A5296) // Глубокий синий
            "ADULT" -> Color(0xFF333333)  // Строгий темно-серый
            "CHILD" -> Color(0xFFF57C00)  // Оранжевый (безопасность)
            "YOUTH" -> Color(0xFF00897B)  // Мятный/Зеленый
            else -> Color(0xFF1A5296)     // По умолчанию синий
        }
    }

    // Вспомогательные алиасы
    val activeBg get() = bodyBackgroundColor
    val activeAccent get() = getHeaderColor("UNKNOWN")
    val activeText get() = bodyTextColor

    // Размеры
    val headerHeight = 52.dp
    val logoSize = 28.dp
    val logoTextSize = 16.sp
    val closeIconSize = 42.dp
    val userTypeSize = 18.sp
    val ageInfoSize = 11.sp // Размер для инфо о возрасте
    val balanceTextSize = 64.sp 
    val balanceSymbolSize = 32.sp
    val balanceSymbolColor = Color(0xFFCCCCCC)
    val cardNumberSize = 18.sp
    val settingsIconSize = 36.dp
    val logoIconLetterSize = 18.sp
}
