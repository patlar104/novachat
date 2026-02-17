package com.novachat.feature.ai.data.observability

/**
 * SPEC-1: Emit client events for submit_start, ack_received, completion_received,
 * failover_switch, local_queue_retry, settings_corruption_detected.
 * Include request_id and app_version where applicable.
 */
interface ChatObservability {

    fun emit(event: String, props: Map<String, Any?> = emptyMap())
}
