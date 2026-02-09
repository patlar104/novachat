package com.novachat.feature.ai.ui.preview

import android.content.res.Configuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.novachat.feature.ai.presentation.model.ChatUiEvent
import com.novachat.feature.ai.presentation.model.ChatUiState
import com.novachat.feature.ai.ui.ChatScreenContent
import com.novachat.feature.ai.ui.theme.NovaChatTheme

@Composable
private fun ChatScreenPreviewContainer(
    uiState: ChatUiState,
    draftMessage: String = ""
) {
    val snackbarHostState = remember { SnackbarHostState() }

    NovaChatTheme {
        Surface {
            ChatScreenContent(
                uiState = uiState,
                draftMessage = draftMessage,
                snackbarHostState = snackbarHostState,
                onEvent = { _: ChatUiEvent -> },
                onDraftMessageChange = {}
            )
        }
    }
}

@Preview(name = "Initial Empty")
@Composable
fun ChatScreenInitialPreview() {
    ChatScreenPreviewContainer(PreviewChatScreenData.initialState())
}

@Preview(name = "Loading")
@Composable
fun ChatScreenLoadingPreview() {
    ChatScreenPreviewContainer(PreviewChatScreenData.loadingState())
}

@Preview(name = "Success - Single Exchange")
@Composable
fun ChatScreenSuccessSinglePreview() {
    ChatScreenPreviewContainer(PreviewChatScreenData.successSingleExchange())
}

@Preview(name = "Success - Processing")
@Composable
fun ChatScreenSuccessProcessingPreview() {
    ChatScreenPreviewContainer(
        uiState = PreviewChatScreenData.successProcessing(),
        draftMessage = "Working on response..."
    )
}

@Preview(name = "Success - Long Conversation")
@Composable
fun ChatScreenSuccessLongPreview() {
    ChatScreenPreviewContainer(PreviewChatScreenData.successLongConversation())
}

@Preview(name = "Success - Error Banner")
@Composable
fun ChatScreenSuccessErrorBannerPreview() {
    ChatScreenPreviewContainer(PreviewChatScreenData.successWithErrorBanner())
}

@Preview(name = "Critical Error")
@Composable
fun ChatScreenCriticalErrorPreview() {
    ChatScreenPreviewContainer(PreviewChatScreenData.criticalError())
}

@Preview(
    name = "Small Phone",
    device = PreviewDevices.DEVICE_PHONE_SMALL
)
@Composable
fun ChatScreenSmallPhonePreview() {
    ChatScreenPreviewContainer(PreviewChatScreenData.successSingleExchange())
}

@Preview(
    name = "Large Phone",
    device = PreviewDevices.DEVICE_PHONE_LARGE
)
@Composable
fun ChatScreenLargePhonePreview() {
    ChatScreenPreviewContainer(PreviewChatScreenData.successSingleExchange())
}

@Preview(
    name = "Phone Landscape",
    device = PreviewDevices.DEVICE_PHONE_LANDSCAPE
)
@Composable
fun ChatScreenPhoneLandscapePreview() {
    ChatScreenPreviewContainer(PreviewChatScreenData.successSingleExchange())
}

@Preview(
    name = "Tablet Portrait",
    device = PreviewDevices.DEVICE_TABLET_PORTRAIT
)
@Composable
fun ChatScreenTabletPortraitPreview() {
    ChatScreenPreviewContainer(PreviewChatScreenData.successSingleExchange())
}

@Preview(
    name = "Tablet Landscape",
    device = PreviewDevices.DEVICE_TABLET_LANDSCAPE
)
@Composable
fun ChatScreenTabletLandscapePreview() {
    ChatScreenPreviewContainer(PreviewChatScreenData.successSingleExchange())
}

@Preview(
    name = "Foldable",
    device = PreviewDevices.DEVICE_FOLDABLE
)
@Composable
fun ChatScreenFoldablePreview() {
    ChatScreenPreviewContainer(PreviewChatScreenData.successSingleExchange())
}

@PreviewScreenSizes
@Composable
fun ChatScreenScreenSizesPreview() {
    ChatScreenPreviewContainer(PreviewChatScreenData.successSingleExchange())
}

@PreviewLightDark
@Composable
fun ChatScreenLightDarkPreview() {
    ChatScreenPreviewContainer(PreviewChatScreenData.successSingleExchange())
}

@Preview(
    name = "Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ChatScreenDarkPreview() {
    ChatScreenPreviewContainer(PreviewChatScreenData.successSingleExchange())
}

@Preview(
    name = "Large Font",
    fontScale = 1.5f
)
@Composable
fun ChatScreenLargeFontPreview() {
    ChatScreenPreviewContainer(PreviewChatScreenData.successSingleExchange())
}
