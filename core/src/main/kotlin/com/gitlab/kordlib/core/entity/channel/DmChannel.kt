package com.gitlab.kordlib.core.entity.channel

import com.gitlab.kordlib.common.annotation.KordUnstableApi
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.common.exception.RequestException
import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.behavior.UserBehavior
import com.gitlab.kordlib.core.behavior.channel.ChannelBehavior
import com.gitlab.kordlib.core.cache.data.ChannelData
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.core.supplier.EntitySupplier
import com.gitlab.kordlib.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.util.*

/**
 * An instance of a Discord DM channel.
 */
@OptIn(KordUnstableApi::class)
data class DmChannel(
        override val data: ChannelData,
        override val kord: Kord,
        override val supplier: EntitySupplier = kord.defaultSupplier
) : MessageChannel {
    /**
     * The ids of the recipients of the channel.
     */
    val recipientIds: Set<Snowflake>
        get() = data.recipients.orEmpty().asSequence()
                .map { Snowflake(it) }
                .toSet()

    /**
     * The behaviors of the recipients of the channel.
     */
    val recipientBehaviors: Set<UserBehavior> get() = recipientIds.map { UserBehavior(it, kord) }.toSet()

    /**
     * Requests to get the recipients of the channel.
     *
     * This request uses state [data] to resolve the entities belonging to the flow,
     * as such it can't guarantee an up to date representation if the [data] is outdated.
     *
     * The returned flow is lazily executed, any [RequestException] will be thrown on
     * [terminal operators](https://kotlinlang.org/docs/reference/coroutines/flow.html#terminal-flow-operators) instead.
     */
    val recipients: Flow<User>
        get() = data.recipients.orEmpty().asFlow()
                .map { supplier.getUserOrNull(Snowflake(it)) }
                .filterNotNull()

    /**
     * returns a new [DmChannel] with the given [strategy].
     */
    override fun withStrategy(strategy: EntitySupplyStrategy<*>): DmChannel =
            DmChannel(data, kord, strategy.supply(kord))


    override fun hashCode(): Int = Objects.hash(id)

    override fun equals(other: Any?): Boolean = when(other) {
        is ChannelBehavior -> other.id == id
        else -> false
    }
}