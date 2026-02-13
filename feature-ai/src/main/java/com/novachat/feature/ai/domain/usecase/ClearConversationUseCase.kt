package com.novachat.feature.ai.domain.usecase

import com.novachat.feature.ai.domain.repository.MessageRepository
import javax.inject.Inject

class ClearConversationUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return messageRepository.clearAllMessages()
    }
}
