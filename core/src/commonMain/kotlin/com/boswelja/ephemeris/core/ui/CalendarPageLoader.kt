package com.boswelja.ephemeris.core.ui

import com.boswelja.ephemeris.core.data.CalendarPageSource
import com.boswelja.ephemeris.core.data.FocusMode
import com.boswelja.ephemeris.core.model.DisplayDate
import com.boswelja.ephemeris.core.model.DisplayRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.math.abs

/**
 * Handles loading data from [CalendarPageSource], combining with [FocusMode], caching and
 * prefetching entries. Platform UIs should make use of this class for their data loading. If the
 * page source or focus mode change, it is expected a new instance will be created and the existing
 * instance discarded. Prefetch and cache operations are handled asynchronously via [coroutineScope].
 */
@OptIn(FlowPreview::class)
public class CalendarPageLoader(
    private val coroutineScope: CoroutineScope,
    public val calendarPageSource: CalendarPageSource,
    public val focusMode: FocusMode
) {
    private val pageCache = mutableMapOf<Int, Set<DisplayRow>>()

    private val lastLoadedPage = MutableStateFlow(0)

    init {
        // Launch the page cache job
        coroutineScope.launch {
            lastLoadedPage
                .debounce(PageChangeDebounceMillis) // Debounce here to reduce overlapping jobs
                .collect {
                    if (tryBuildCache(it)) {
                        trimCache(it)
                    }
                }
        }
    }

    /**
     * Loads [ChunkSize] pages into the cache from the given page [fromPage]. If [reverse] is true,
     * a chunk will be loaded behind the given page. Note that [fromPage] is excluded when caching.
     */
    private fun cacheChunk(fromPage: Int, reverse: Boolean) {
        // Iterate from the given page to the chunk size
        val range = if (reverse) {
            (fromPage - ChunkSize) until fromPage
        } else {
            (fromPage + 1)..(fromPage + ChunkSize)
        }
        range.forEach { page ->
            pageCache[page] = calendarPageSource.loadPageData(page) { date, month ->
                DisplayDate(
                    date,
                    focusMode(date, month)
                )
            }
        }
    }

    /**
     * Builds additional cache based around the given page if necessary.
     * @return true if cache was built on, false otherwise.
     */
    private fun tryBuildCache(page: Int): Boolean {
        val cacheForward = pageCache[page + PrefetchDistance] == null
        val cacheBackward = pageCache[page - PrefetchDistance] == null
        if (cacheForward) {
            cacheChunk(page, false)
        }
        if (cacheBackward) {
            cacheChunk(page, true)
        }
        return cacheForward || cacheBackward
    }

    /**
     * If the cache size is growing too large, trim the furthest elements from the given page.
     */
    private fun trimCache(page: Int) {
        if (pageCache.size > UpperCacheLimit) {
            val maxDistance = UpperCacheLimit / 2
            pageCache.keys
                .filter { abs(page - it) > maxDistance }
                .forEach {
                    pageCache.remove(it)
                }
        }
    }

    /**
     * Gets the data to display for the given page. If necessary, a cache load operation will be
     * started to ensure there's enough data available ahead of time.
     */
    public fun getPageData(page: Int): Set<DisplayRow> {
        lastLoadedPage.tryEmit(page)
        return pageCache[page] ?: calendarPageSource.loadPageData(page) { date, month ->
            DisplayDate(date, focusMode(date, month))
        }
    }

    /**
     * Gets the range of dates displayed on the given page.
     */
    public fun getDateRangeFor(page: Int): ClosedRange<LocalDate> {
        // Cast to non-null here since in theory a page has already been loaded
        val pageData = pageCache[page]!!
        val startDate = pageData.first().dates.first().date
        val endDate = pageData.last().dates.last().date
        return startDate..endDate
    }

    public companion object {
        private const val PageChangeDebounceMillis = 50L
        private const val PrefetchDistance = 5
        private const val ChunkSize = 20
        private const val UpperCacheLimit = 250
    }
}
