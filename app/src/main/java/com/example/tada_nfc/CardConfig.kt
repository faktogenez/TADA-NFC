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
    
    const val APP_NAME = "Tada" // Название приложения

    // --- Настройки языков ---
    enum class Language(val code: String, val label: String) {
        EN("en", "English"),
        RU("ru", "Русский"),
        KO("ko", "한국어"),
        JA("ja", "日本語"),
        ZH("zh", "中文")
    }

    // Текущий язык приложения
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
            Language.EN to "Hold card to the back for 5 seconds",
            Language.RU to "Приложите карту к задней панели на 5 секунд",
            Language.KO to "카드를 뒷면에 5초 동안 대주세요",
            Language.JA to "カードを背面に5秒間かざしてください",
            Language.ZH to "请将卡片贴在背面5秒钟"
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
        "HIPASS" to mapOf(Language.EN to "HI-PASS", Language.RU to "HI-PASS", Language.KO to "하이패스", Language.JA to "ハイパス", Language.ZH to "高速通行卡"),
        "UNKNOWN" to mapOf(Language.EN to "CARD", Language.RU to "КАРТА", Language.KO to "카드", Language.JA to "카드", Language.ZH to "卡"),
        "unsupported_card" to mapOf(
            Language.EN to "Unsupported Card",
            Language.RU to "Карта не поддерживается",
            Language.KO to "지원되지 않는 카드입니다",
            Language.JA to "サポートされていないカードです"
        ),
        "age_child" to mapOf(Language.EN to "~12 years", Language.RU to "до 12 лет", Language.KO to "만 12세 이하", Language.JA to "12歳まで", Language.ZH to "12岁以下"),
        "age_youth" to mapOf(Language.EN to "13~18 years", Language.RU to "13-18 лет", Language.KO to "만 13~18세", Language.JA to "13~18歳", Language.ZH to "13-18岁"),
        "top_up" to mapOf(
            Language.EN to "Top Up",
            Language.RU to "Пополнить",
            Language.KO to "충전하기",
            Language.JA to "チャージ",
            Language.ZH to "充值"
        ),
        "commission" to mapOf(
            Language.EN to "Commission",
            Language.RU to "Комиссия",
            Language.KO to "수수료",
            Language.JA to "手数料",
            Language.ZH to "手续费"
        ),
        "total" to mapOf(
            Language.EN to "Total",
            Language.RU to "Итого",
            Language.KO to "합계",
            Language.JA to "合計",
            Language.ZH to "合计"
        ),
        "hold_card_writing" to mapOf(
            Language.EN to "Hold card... writing data",
            Language.RU to "Не убирайте карту... идет запись",
            Language.KO to "카드를 대고 계세요... 기록 중",
            Language.JA to "カードをかざしたままにしてください... 書き込み中",
            Language.ZH to "请拿稳卡片... 正在写入"
        ),
        "card_not_supported" to mapOf(
            Language.EN to "Card not supported",
            Language.RU to "Карта не поддерживается",
            Language.KO to "지원되지 않는 카드입니다",
            Language.JA to "サポートされていないカードです",
            Language.ZH to "不支持该卡片"
        ),
        "card_not_supported_desc" to mapOf(
            Language.EN to "Please use T-money, Hi-pass or other transit cards",
            Language.RU to "Используйте T-money, Hi-pass или другие транспортные карты",
            Language.KO to "T-money, Hi-pass 또는 기타 교통카드를 사용해 주세요",
            Language.JA to "T-money、Hi-pass、またはその他の交通系ICカードを使用してください",
            Language.ZH to "请使用 T-money、Hi-pass 或其他交通卡"
        )
    )

    // Функция для получения перевода по ключу
    fun translate(key: String): String = translations[key]?.get(currentLanguage) ?: key

    // Геттеры для локализованных строк
    val languageLabel get() = translate("language")
    val shareAppLabel get() = translate("share")
    val shareAppMessage get() = translate("share_msg")
    val videoInstruction get() = translate("instruction")

    // --- Настройки инструкции (Overlay) ---
    var instructionYOffset by mutableStateOf(0.65f) // Смещение инструкции по вертикали (0.0 - 1.0)
    var instructionFontSize by mutableStateOf(25.sp) // Размер шрифта текста инструкции
    var instructionFontWeight by mutableStateOf(FontWeight.ExtraBold) // Толщина шрифта инструкции
    var instructionColor by mutableStateOf(Color(0xFF333333)) // Цвет текста инструкции
    var instructionShadowColor by mutableStateOf(Color.White.copy(alpha = 0.5f)) // Цвет тени текста инструкции
    var instructionShadowBlur by mutableStateOf(4f) // Размытие тени текста инструкции

    // --- Параметры карточки (Адаптированный дизайн) ---
    val cardWidthFraction = 0.85f // Ширина карточки относительно ширины экрана
    val cardCornerRadius = 16.dp // Радиус скругления углов карточки
    val cardScreenAlignment = BiasAlignment(0f, -0.35f) // Выравнивание карточки на экране
    val flipAnimationDuration = 500 // Длительность анимации переворота (мс)
    val cameraDistance = 12f // Расстояние виртуальной камеры для 3D эффекта переворота

    // --- Цвета интерфейса ---
    val bodyBackgroundColor = Color(0xFFFBFBFC) // Фон основной (нижней) части карточки
    val bodyTextColor = Color(0xFF111111) // Основной цвет текста (для баланса)
    val secondaryTextColor = Color(0xFF999999) // Цвет второстепенного текста (для номера карты и доп. инфо)
    val headerTextColor = Color.White.copy(alpha = 0.9f) // Цвет текста в шапке карточки

    // Функция для получения цвета шапки в зависимости от типа карты
    fun getHeaderColor(userType: String): Color {
        return when (userType.uppercase()) {
            "HIPASS" -> Color(0xFF4F46E5) // Насыщенный индиго для Hi-Pass
            "ADULT" -> Color(0xFF0F172A)  // Глубокий сланцево-черный для взрослого тарифа
            "CHILD" -> Color(0xFFF59E0B)  // Теплый янтарный для детского тарифа
            "YOUTH" -> Color(0xFF10B981)  // Яркий изумрудный для подросткового тарифа
            "UNKNOWN" -> Color(0xFFB91C1C) // Темно-красный (Red 700) для неподдерживаемых карт
            "RETRY" -> Color(0xFFF97316)   // Насыщенный оранжевый (Orange 500) для предупреждения
            else -> Color(0xFF64748B)     // Нейтральный сизо-серый для остальных случаев
        }
    }

    // Вспомогательные свойства для совместимости со старым кодом
    val activeBg get() = bodyBackgroundColor
    val activeAccent get() = getHeaderColor("UNKNOWN")
    val activeText get() = bodyTextColor

    // --- Размеры элементов карточки ---
    val headerHeight = 52.dp // Высота верхней синей (цветной) шапки
    val logoSize = 28.dp // Размер круглого белого логотипа
    val logoTextSize = 16.sp // Размер шрифта надписи "TADA" в шапке
    val closeIconSize = 42.dp // Размер иконки (кнопки) закрытия
    val userTypeSize = 16.sp // Размер шрифта названия категории (например, ПОДРОСТОК)
    val ageInfoSize = 12.sp // Размер шрифта информации о возрасте (например, 13-18 лет)
    val balanceTextSize = 65.sp // Размер шрифта суммы баланса
    val balanceSymbolSize = 30.sp // Размер шрифта символа валюты (₩)
    val balanceSymbolColor = Color(0xFF999999) // Цвет символа валюты
    val cardNumberSize = 16.sp // Размер шрифта номера карты
    val settingsIconSize = 32.dp // Размер иконки шестеренки (настроек)
    val logoIconLetterSize = 18.sp // Размер буквы 'T' внутри круглого логотипа

    // --- Расстояния между элементами (Vertical Spacing) ---
    val spacingCategoryToAge = 2.dp // Расстояние между названием категории и возрастом
    val spacingAgeToBalance = 10.dp // Расстояние между информацией о возрасте и балансом
    val spacingBalanceToNumber = 10.dp // Расстояние между балансом и номером карты
    val spacingBodyPadding = 16.dp // Внутренний отступ тела карточки

    // Формирование ссылки на приложение в Google Play
    fun getAppLink(packageName: String) = "https://play.google.com/store/apps/details?id=$packageName"
}
