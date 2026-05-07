package com.example.tada_nfc.models

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector

@Stable
sealed class AppState {
    @Immutable object Splash : AppState()
    @Immutable object WaitingForCard : AppState()
    @Immutable object Processing : AppState()
    @Immutable object NfcDisabled : AppState()
    @Immutable data class CardResult(
        val balance: String,
        val cardNumber: String,
        val userType: String,
        val transactions: List<TransactionPlaceholder> = emptyList()
    ) : AppState()
    @Immutable data class History(val result: CardResult) : AppState()
    @Immutable data class TopUp(val cardData: CardData, val amount: Int = 0) : AppState()
    @Stable sealed class Error(val message: String, val isRetry: Boolean = false) : AppState() {
        @Immutable data class CardNotSupported(val msg: String) : Error(msg, false)
        @Immutable data class Retry(val msg: String) : Error(msg, true)
    }
}

@Immutable
data class TransactionPlaceholder(
    val type: String,
    val amount: String,
    val date: String,
    val balanceAfter: String,
    val icon: ImageVector
)

data class CardDataInternal(
    val balance: String,
    val number: String,
    val userType: String,
    val transactions: List<TransactionPlaceholder>
)

data class CardData(
    val balance: String,
    val number: String,
    val userType: String
)
