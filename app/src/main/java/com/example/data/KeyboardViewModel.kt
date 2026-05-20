package com.example.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class KeyboardViewModel(
    application: Application,
    private val repository: KeyboardRepository
) : AndroidViewModel(application) {

    // --- 1. Keyboard & Input States ---
    private val _typedText = MutableStateFlow("")
    val typedText = _typedText.asStateFlow()

    private val _currentLayout = MutableStateFlow("PHONETIC_SINGLISH") // PHONETIC_SINGLISH, SINHALA_DIRECT, ENGLISH_STANDARD
    val currentLayout = _currentLayout.asStateFlow()

    private val _activeWordBuffer = MutableStateFlow("") // Tracks current untransliterated letters
    val activeWordBuffer = _activeWordBuffer.asStateFlow()

    private val _predictions = MutableStateFlow<List<String>>(emptyList())
    val predictions = _predictions.asStateFlow()

    private val _selectedEmojiCategory = MutableStateFlow("SMILEYS") // SMILEYS, FOOD, OBJECTS, FLAGS
    val selectedEmojiCategory = _selectedEmojiCategory.asStateFlow()

    // --- 2. Customization, Fonts & Theme Settings ---
    private val _themeMode = MutableStateFlow("dark") // dark, light
    val themeMode = _themeMode.asStateFlow()

    private val _themePreset = MutableStateFlow("aura") // aura (frosted glass), helakuru (orange), cosmic (dark), pastel (light), custom
    val themePreset = _themePreset.asStateFlow()

    // Custom colors (only active if preset is "custom")
    private val _customBgColor = MutableStateFlow("#1E1E1E")
    val customBgColor = _customBgColor.asStateFlow()

    private val _customKeyColor = MutableStateFlow("#2A2A2A")
    val customKeyColor = _customKeyColor.asStateFlow()

    private val _customTextColor = MutableStateFlow("#FFA500")
    val customTextColor = _customTextColor.asStateFlow()

    private val _keyboardFont = MutableStateFlow("Space Grotesk") // Space Grotesk, JetBrains Mono, Playfair, Default
    val keyboardFont = _keyboardFont.asStateFlow()

    private val _keyboardFontSize = MutableStateFlow(16) // 14, 16, 18, 20
    val keyboardFontSize = _keyboardFontSize.asStateFlow()

    private val _keyRoundness = MutableStateFlow(8) // 0dp to 24dp radius
    val keyRoundness = _keyRoundness.asStateFlow()

    // --- 3. News Feed States ---
    private val _newsSearchQuery = MutableStateFlow("")
    val newsSearchQuery = _newsSearchQuery.asStateFlow()

    private val _newsCategoryFilter = MutableStateFlow("All") // All, Technology, National, Guides, Bookmarked
    val newsCategoryFilter = _newsCategoryFilter.asStateFlow()

    // --- 4. Privacy & Securities Toggle ---
    private val _incognitoMode = MutableStateFlow(false)
    val incognitoMode = _incognitoMode.asStateFlow()

    private val _autocorrectEnabled = MutableStateFlow(true)
    val autocorrectEnabled = _autocorrectEnabled.asStateFlow()

    private val _predictiveTextEnabled = MutableStateFlow(true)
    val predictiveTextEnabled = _predictiveTextEnabled.asStateFlow()

    // CCPA/GDPR personal dictionary list of learned words
    private val _learnedWords = MutableStateFlow(listOf("හෙළකුරු", "සුභ", "ලංකා", "සිංග්ලිෂ්", "සුභ_උදෑසනක්", "ආයුබෝවන්"))
    val learnedWords = _learnedWords.asStateFlow()

    // --- 5. Gemini AI writing assistant State ---
    private val _aiResult = MutableStateFlow("")
    val aiResult = _aiResult.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading = _aiLoading.asStateFlow()

    // --- 6. Feedback Form Data ---
    private val _feedbackRating = MutableStateFlow(5)
    val feedbackRating = _feedbackRating.asStateFlow()

    private val _feedbackComment = MutableStateFlow("")
    val feedbackComment = _feedbackComment.asStateFlow()

    private val _feedbackStatus = MutableStateFlow("") // "", "Submitting", "Success"
    val feedbackStatus = _feedbackStatus.asStateFlow()

    // --- Database-linked variables ---
    val clipboardHistory = repository.allClipboardItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val newsList = repository.allNews.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Combined or Filtered news stream
    val filteredNews = combine(newsList, newsCategoryFilter, newsSearchQuery) { list, category, query ->
        list.filter { article ->
            val matchesCategory = when (category) {
                "All" -> true
                "Bookmarked" -> article.isBookmarked
                else -> article.category == category
            }
            val matchesQuery = query.isBlank() || 
                    article.title.contains(query, ignoreCase = true) || 
                    article.content.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Load and pre-populate initial items
            repository.prepopulateNewsIfEmpty()
            
            // Sync initial settings from Database if they exist
            _themePreset.value = repository.getSettingValue("theme_preset", "aura")
            _themeMode.value = repository.getSettingValue("theme_mode", "dark")
            _keyboardFont.value = repository.getSettingValue("keyboard_font", "Space Grotesk")
            _keyboardFontSize.value = repository.getSettingValue("keyboard_font_size", "16").toIntOrNull() ?: 16
            _keyRoundness.value = repository.getSettingValue("key_roundness", "8").toIntOrNull() ?: 8
            _customBgColor.value = repository.getSettingValue("custom_bg_color", "#1E1E1E")
            _customKeyColor.value = repository.getSettingValue("custom_key_color", "#2A2A2A")
            _customTextColor.value = repository.getSettingValue("custom_text_color", "#FFA500")
            _incognitoMode.value = repository.getSettingValue("incognito_mode", "false").toBoolean()
            _autocorrectEnabled.value = repository.getSettingValue("autocorrect", "true").toBoolean()
            _predictiveTextEnabled.value = repository.getSettingValue("predictive_text", "true").toBoolean()
        }
    }

    // --- 7. Functions & Actions ---

    fun onSelectedEmojiCategoryChanged(cat: String) {
        _selectedEmojiCategory.value = cat
    }

    fun getTransliteratedPreview(input: String): String {
        return transliterateSinglishToSinhala(input)
    }

    fun onKeyboardLayoutChanged(layout: String) {
        _currentLayout.value = layout
        _activeWordBuffer.value = ""
        _predictions.value = emptyList()
    }

    fun onThemePresetChanged(preset: String) {
        _themePreset.value = preset
        viewModelScope.launch {
            repository.setSetting("theme_preset", preset)
        }
    }

    fun onThemeModeToggled() {
        _themeMode.value = if (_themeMode.value == "dark") "light" else "dark"
        viewModelScope.launch {
            repository.setSetting("theme_mode", _themeMode.value)
        }
    }

    fun setCustomColors(bg: String, key: String, text: String) {
        _customBgColor.value = bg
        _customKeyColor.value = key
        _customTextColor.value = text
        viewModelScope.launch {
            repository.setSetting("custom_bg_color", bg)
            repository.setSetting("custom_key_color", key)
            repository.setSetting("custom_text_color", text)
        }
    }

    fun onFontChanged(font: String) {
        _keyboardFont.value = font
        viewModelScope.launch {
            repository.setSetting("keyboard_font", font)
        }
    }

    fun onFontSizeChanged(size: Int) {
        _keyboardFontSize.value = size
        viewModelScope.launch {
            repository.setSetting("keyboard_font_size", size.toString())
        }
    }

    fun onKeyRoundnessChanged(radius: Int) {
        _keyRoundness.value = radius
        viewModelScope.launch {
            repository.setSetting("key_roundness", radius.toString())
        }
    }

    fun onIncognitoChanged(enabled: Boolean) {
        _incognitoMode.value = enabled
        viewModelScope.launch {
            repository.setSetting("incognito_mode", enabled.toString())
        }
    }

    fun onAutocorrectChanged(enabled: Boolean) {
        _autocorrectEnabled.value = enabled
        viewModelScope.launch {
            repository.setSetting("autocorrect", enabled.toString())
        }
    }

    fun onPredictiveTextChanged(enabled: Boolean) {
        _predictiveTextEnabled.value = enabled
        viewModelScope.launch {
            repository.setSetting("predictive_text", enabled.toString())
        }
    }

    fun onNewsSearchQueryChanged(q: String) {
        _newsSearchQuery.value = q
    }

    fun onNewsCategoryChanged(cat: String) {
        _newsCategoryFilter.value = cat
    }

    fun toggleNewsBookmark(id: String, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleNewsBookmark(id, isBookmarked)
        }
    }

    fun onFeedbackCommentChanged(comment: String) {
        _feedbackComment.value = comment
    }

    fun onFeedbackRatingChanged(rating: Int) {
        _feedbackRating.value = rating
    }

    fun submitFeedback() {
        if (_feedbackComment.value.isBlank()) return
        viewModelScope.launch {
            _feedbackStatus.value = "Submitting"
            kotlinx.coroutines.delay(1200) // Simulate web submission or endpoint syncing
            _feedbackStatus.value = "Success"
            _feedbackComment.value = ""
        }
    }

    fun resetFeedbackStatus() {
        _feedbackStatus.value = ""
    }

    // --- 8. Typing Engine Logic (Singlish Transliteration, Predictions) ---

    fun onKeyPress(char: String) {
        if (_currentLayout.value == "ENGLISH_STANDARD") {
            appendChar(char)
        } else if (_currentLayout.value == "SINHALA_DIRECT") {
            appendChar(char)
        } else { // PHONETIC_SINGLISH mode
            _activeWordBuffer.value += char
            updatePhoneticLiveWord()
        }
    }

    private fun appendChar(char: String) {
        _typedText.value += char
        generateTypePredictions(char)
    }

    fun onKeyBackspace() {
        if (_currentLayout.value == "PHONETIC_SINGLISH" && _activeWordBuffer.value.isNotEmpty()) {
            _activeWordBuffer.value = _activeWordBuffer.value.dropLast(1)
            updatePhoneticLiveWord()
        } else {
            if (_typedText.value.isNotEmpty()) {
                _typedText.value = _typedText.value.dropLast(1)
            }
        }
        if (_typedText.value.isEmpty() && _activeWordBuffer.value.isEmpty()) {
            _predictions.value = emptyList()
        }
    }

    fun onKeySpace() {
        // In Singlish mode, commit any uncommitted active phonetic buffer first
        if (_currentLayout.value == "PHONETIC_SINGLISH" && _activeWordBuffer.value.isNotEmpty()) {
            val converted = transliterateSinglishToSinhala(_activeWordBuffer.value)
            _typedText.value += converted
            // Learn typed word if not incognito
            if (!_incognitoMode.value && converted.isNotBlank()) {
                addLearnedWord(converted)
            }
            _activeWordBuffer.value = ""
        }
        _typedText.value += " "
        _predictions.value = emptyList()
    }

    fun onKeyClear() {
        _typedText.value = ""
        _activeWordBuffer.value = ""
        _predictions.value = emptyList()
    }

    fun onSelectPrediction(word: String) {
        if (_currentLayout.value == "PHONETIC_SINGLISH" && _activeWordBuffer.value.isNotEmpty()) {
            _activeWordBuffer.value = ""
        }
        
        // Remove trailing letters of currently typed word structure if any to replace with selected prediction
        val currentTyped = _typedText.value
        val lastIdx = currentTyped.lastIndexOf(' ')
        if (lastIdx != -1) {
            _typedText.value = currentTyped.substring(0, lastIdx + 1) + word + " "
        } else {
            _typedText.value = word + " "
        }
        _predictions.value = emptyList()
    }

    private fun updatePhoneticLiveWord() {
        val word = _activeWordBuffer.value
        if (word.isEmpty()) {
            _predictions.value = emptyList()
            return
        }
        
        // Custom instant phonetic conversions
        val sinhalaTrans = transliterateSinglishToSinhala(word)
        generateTypePredictions(sinhalaTrans)
    }

    private fun generateTypePredictions(input: String) {
        if (!_predictiveTextEnabled.value) {
            _predictions.value = emptyList()
            return
        }
        
        // Standard high priority prediction list mapped dynamically
        val isSinhala = isTextSinhala(input)
        val suggestions = if (isSinhala) {
            val pool = listOf(
                "හෙළකුරු", "හෙළ", "හෙලෝ", "අපි", "අම්මා", "ආයුබෝවන්", 
                "ලංකා", "ශ්‍රී_ලංකා", "ස්මාර්ට්", "යතුරුපුවරුව", "සුභ_උදෑසනක්", 
                "සුභ_රාත්‍රියක්", "කරදරයක්", "ක්‍රමයක්", "කරනවා", "නියමයි"
            ) + _learnedWords.value
            pool.filter { it.startsWith(input) || it.contains(input) }.take(4)
        } else {
            val pool = listOf(
                "hello", "helakuru", "hela", "smart", "keyboard", "sinhala", 
                "singlish", "great", "awesome", "today", "news", "privacy", 
                "font", "layout", "custom", "security", "backup", "sync"
            )
            pool.filter { it.startsWith(input.lowercase()) }.take(4)
        }
        _predictions.value = if (suggestions.isNotEmpty()) suggestions else listOf(input)
    }

    private fun isTextSinhala(text: String): Boolean {
        for (char in text) {
            if (char.code in 0x0D80..0x0DFF) return true
        }
        return false
    }

    private fun addLearnedWord(word: String) {
        val cleaned = word.trim()
        if (cleaned.isNotEmpty() && !cleaned.contains(" ") && !_learnedWords.value.contains(cleaned)) {
            _learnedWords.value = _learnedWords.value + cleaned
        }
    }

    fun clearLearnedDictionary() {
        _learnedWords.value = emptyList()
    }

    // --- 9. Clipboard operations ---
    
    fun copyCurrentToClipboard() {
        val text = _typedText.value
        if (text.isNotBlank()) {
            viewModelScope.launch {
                repository.insertClipboardItem(text)
            }
        }
    }

    fun deleteClipboardItem(id: Int) {
        viewModelScope.launch {
            repository.deleteClipboardItem(id)
        }
    }

    fun toggleClipboardPinned(id: Int, isPinned: Boolean) {
        viewModelScope.launch {
            repository.toggleClipboardPin(id, isPinned)
        }
    }

    fun wipeClipboard() {
        viewModelScope.launch {
            repository.clearClipboard()
        }
    }

    // --- 10. Gemini Assistant Calls ---

    fun askGeminiToAssist(input: String, mode: String) {
        if (input.isBlank()) {
            _aiResult.value = "Please write some text in the Typing Buffer first to get AI assistance."
            return
        }

        viewModelScope.launch {
            _aiLoading.value = true
            _aiResult.value = ""
            try {
                val response = GeminiService.translateOrRephrase(input, mode)
                _aiResult.value = response
            } catch (e: Exception) {
                _aiResult.value = "Unable to connect with Gemini AI: ${e.localizedMessage}"
            } finally {
                _aiLoading.value = false
            }
        }
    }

    // --- 11. Helper to Phonetically Transliterate Live Singlish -> Sinhala ---
    // High-fidelity map matching standard Singlish patterns (phonetics of Sri Lankan language)
    private fun transliterateSinglishToSinhala(input: String): String {
        var str = input.lowercase()
        if (str.isEmpty()) return ""

        // Sub string replacements prioritised by length to capture complex sounds
        val conversions = listOf(
            "helakuru" to "හෙළකුරු",
            "singlish" to "සිංග්ලිෂ්",
            "shree" to "ශ්‍රී",
            "subha" to "සුභ",
            "amma" to "අම්මා",
            "shri" to "ශ්‍රී",
            "thth" to "ත්ත්",
            "chch" to "ච්ච්",
            "sh" to "ශ",
            "th" to "ත",
            "dh" to "ධ",
            "ch" to "ච",
            "ks" to "ක්ස්",
            "aa" to "ආ",
            "ae" to "ඈ",
            "ee" to "ඒ",
            "ii" to "ඊ",
            "oo" to "ඕ",
            "uu" to "ඌ",
            "a" to "අ",
            "b" to "බ",
            "c" to "ච",
            "d" to "ද",
            "e" to "එ",
            "f" to "ෆ",
            "g" to "ග",
            "h" to "හ",
            "i" to "ඉ",
            "j" to "ජ",
            "k" to "ක",
            "l" to "ල",
            "m" to "ම",
            "n" to "න",
            "o" to "ඔ",
            "p" to "ප",
            "q" to "කු",
            "r" to "ර",
            "s" to "ස",
            "t" to "ට",
            "u" to "උ",
            "v" to "ව",
            "w" to "ව",
            "x" to "ක්ස්",
            "y" to "ය",
            "z" to "ස"
        )

        for ((lat, sin) in conversions) {
            str = str.replace(lat, sin)
        }
        return str
    }
}

// ViewModel Factory
class KeyboardViewModelFactory(
    private val application: Application,
    private val repository: KeyboardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KeyboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KeyboardViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
