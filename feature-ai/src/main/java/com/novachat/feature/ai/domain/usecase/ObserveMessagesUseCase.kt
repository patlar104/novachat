package com.novachat.feature.ai.domain.usecase

import com.novachat.feature.ai.domain.repository.MessageRepository
import javax.inject.Inject

class ObserveMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    operator fun invoke() = messageRepository.observeMessages()
}
