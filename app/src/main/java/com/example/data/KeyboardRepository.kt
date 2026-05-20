package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class KeyboardRepository(private val db: AppDatabase) {

    private val clipboardDao = db.clipboardDao()
    private val newsDao = db.newsDao()
    private val settingsDao = db.settingsDao()

    val allClipboardItems: Flow<List<ClipboardItem>> = clipboardDao.getAllItems()
    val allNews: Flow<List<NewsArticle>> = newsDao.getAllNewsFlow()
    val allSettings: Flow<List<AppSetting>> = settingsDao.getAllSettingsFlow()

    suspend fun insertClipboardItem(text: String) {
        if (text.isNotBlank()) {
            clipboardDao.insertItem(ClipboardItem(text = text))
        }
    }

    suspend fun deleteClipboardItem(id: Int) {
        clipboardDao.deleteItemById(id)
    }

    suspend fun clearClipboard() {
        clipboardDao.clearAll()
    }

    suspend fun toggleClipboardPin(id: Int, isPinned: Boolean) {
        clipboardDao.updatePinnedStatus(id, isPinned)
    }

    suspend fun toggleNewsBookmark(id: String, isBookmarked: Boolean) {
        newsDao.updateBookmarkStatus(id, isBookmarked)
    }

    suspend fun setSetting(key: String, value: String) {
        settingsDao.insertSetting(AppSetting(key, value))
    }

    suspend fun getSettingValue(key: String, defaultValue: String): String {
        return settingsDao.getSettingValue(key)?.value ?: defaultValue
    }

    suspend fun prepopulateNewsIfEmpty() {
        val count = newsDao.getAllNews()
        if (count.isEmpty()) {
            val initialArticles = listOf(
                NewsArticle(
                    id = "news_1",
                    title = "HelaSmart Keyboard AI Integrates Dynamic Transliteration Engine",
                    category = "Technology",
                    content = "HelaSmart has officially rolled out its offline phonetic typing upgrade! This engine empowers users to type standard Sinhala using simple phonetic Latin characters (Singlish) in record speed. The engine now features instant context learning and smart space-to-convert suggestions, offering typing speeds close to native desktop systems.",
                    imageUrl = "https://images.unsplash.com/photo-1542831371-29b0f74f9713?auto=format&fit=crop&q=80&w=400",
                    publishDate = "Today, 10:15 AM",
                    viewsCount = 1845
                ),
                NewsArticle(
                    id = "news_2",
                    title = "ශ්‍රී ලංකාවේ බස් රථ සදහා හෙට සිට නව ඩිජිටල් ප්‍රවේශපත්‍ර ක්‍රමයක්!",
                    category = "National",
                    content = "දිවයින පුරා ධාවනය වන සියලුම පෞද්ගලික සහ ලංගම බස් රථ සඳහා නව ස්මාර්ට් කාඩ්පත් සහ QR මත පදනම් වූ ඩිජිටල් ගෙවීම් ක්‍රියාවලියක් හඳුන්වා දීමට මගී ප්‍රවාහන අධිකාරිය පියවර ගෙන තිබේ. බස් රථ තුළ යතුරුපුවරු සවිකර ඇති අතර මෙමගින් මුදල් නැතිව ගමන් කිරීමට මගීන්ට පහසුකම් සලසනු ඇත.",
                    imageUrl = "https://images.unsplash.com/photo-1570125909232-eb263c188f7e?auto=format&fit=crop&q=80&w=400",
                    publishDate = "Today, 08:30 AM",
                    viewsCount = 3120
                ),
                NewsArticle(
                    id = "news_3",
                    title = "Google Gemini AI Assistant API Reaches Full Parity in Sri Lanka",
                    category = "AI & Tech",
                    content = "Developers in Sri Lanka can now utilize Google's advanced Gemini-3.5-flash model via local server-side integration. Highly optimal for smart keyboard rephrasing, tone correction, and automated emoji recommendation workflows, the API achieves unprecedented response speeds under 120ms with enhanced contextual memory.",
                    imageUrl = "https://images.unsplash.com/photo-1677442136019-21780efad99a?auto=format&fit=crop&q=80&w=400",
                    publishDate = "Yesterday, 04:45 PM",
                    viewsCount = 2095
                ),
                NewsArticle(
                    id = "news_4",
                    title = "සිංග්ලිෂ් භාවිතයෙන් වේගවත්ව සිංහල ටයිප් කරන ආකාරය",
                    category = "Guides",
                    content = "සිංග්ලිෂ් යතුරුපුවරු ටයිපිං ක්‍රමය ඉතා පහසුය. උදාහරණයක් ලෙස 'hela' ටයිප් කළ විට එය ස්වයංක්‍රීයව 'හෙළ' බවටත්, 'api' ටයිප් කළ විට 'අපි' ලෙසත් පරිවර්තනය වේ. යතුරුපුවරුවේ ඉහළ ඇති Autocomplete / Prediction Bar එක මගින් ඊළඟට ටයිප් කරන වචනය ස්මාර්ට්ව පෙන්වයි.",
                    imageUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&q=80&w=400",
                    publishDate = "3 Days Ago",
                    viewsCount = 1450
                ),
                NewsArticle(
                    id = "news_5",
                    title = "Weather Alert: Heavy showers expected in Western & Sabaragamuwa",
                    category = "General",
                    content = "The Meteorological Department has issued a high alert for districts including Colombo, Gampaha, Kalutara, and Ratnapura due to a low-pressure area developing in the Bay of Bengal. Wind speeds could reach 50kmph. Residents near river banks are advised to remain vigilant and keep emergency alert notifications active.",
                    imageUrl = "https://images.unsplash.com/photo-1534274988757-a28bf1a57c17?auto=format&fit=crop&q=80&w=400",
                    publishDate = "Yesterday, 11:20 AM",
                    viewsCount = 980
                )
            )
            newsDao.insertInitialNews(initialArticles)
        }
    }
}
