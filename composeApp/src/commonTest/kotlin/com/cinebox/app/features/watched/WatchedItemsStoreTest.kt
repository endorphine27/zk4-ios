package com.cinebox.app.features.watched

import com.cinebox.app.features.tracking.TrackingProviderId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class WatchedItemsStoreTest {
    @Test
    fun `concurrent updates publish coherent item snapshots`() = runBlocking {
        val store = WatchedItemsStore()

        coroutineScope {
            repeat(4) { writer ->
                launch(Dispatchers.Default) {
                    repeat(500) { index ->
                        val key = "$writer:$index"
                        val item = WatchedItem(
                            id = key,
                            type = "movie",
                            name = key,
                            markedAtEpochMs = index.toLong(),
                        )
                        store.update { cineboxItems, providerItems, dirtyCineboxKeys, dirtyProviderKeys ->
                            cineboxItems[key] = item
                            providerItems
                                .getOrPut(TrackingProviderId.TRAKT, ::mutableMapOf)[key] = item
                            dirtyCineboxKeys += key
                            dirtyProviderKeys
                                .getOrPut(TrackingProviderId.TRAKT, ::mutableSetOf) += key
                        }
                    }
                }
            }
            repeat(4) {
                launch(Dispatchers.Default) {
                    repeat(500) {
                        store.read { cineboxItems, providerItems, dirtyCineboxKeys, dirtyProviderKeys ->
                            val cineboxKeys = cineboxItems.keys.toSet()
                            assertEquals(cineboxKeys, providerItems[TrackingProviderId.TRAKT].orEmpty().keys)
                            assertEquals(cineboxKeys, dirtyCineboxKeys)
                            assertEquals(cineboxKeys, dirtyProviderKeys[TrackingProviderId.TRAKT].orEmpty())
                        }
                    }
                }
            }
        }

        store.read { cineboxItems, providerItems, dirtyCineboxKeys, dirtyProviderKeys ->
            assertEquals(2_000, cineboxItems.size)
            assertEquals(cineboxItems.keys, providerItems[TrackingProviderId.TRAKT].orEmpty().keys)
            assertEquals(cineboxItems.keys, dirtyCineboxKeys)
            assertEquals(cineboxItems.keys, dirtyProviderKeys[TrackingProviderId.TRAKT])
        }
    }
}
