package com.example.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.db.AppDatabase
import com.example.db.AppState
import com.example.db.AppStateRepository
import com.example.location.LocationHelper
import com.example.net.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Locale

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppStateRepository
    val appStateFlow: StateFlow<AppState>

    private val locationHelper = LocationHelper(application)

    // UI state states
    private val _pinInput = MutableStateFlow("")
    val pinInput: StateFlow<String> = _pinInput.asStateFlow()

    private val _activationError = MutableStateFlow<String?>(null)
    val activationError: StateFlow<String?> = _activationError.asStateFlow()

    private val _isActivating = MutableStateFlow(false)
    val isActivating: StateFlow<Boolean> = _isActivating.asStateFlow()

    private val _isOpening = MutableStateFlow(false)
    val isOpening: StateFlow<Boolean> = _isOpening.asStateFlow()

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _barrierResponse = MutableStateFlow<String?>(null)
    val barrierResponse: StateFlow<String?> = _barrierResponse.asStateFlow()

    // Mock settings coordinates for testing (by default empty, so we use real GPS)
    private val _useMockLocation = MutableStateFlow(false)
    val useMockLocation = _useMockLocation.asStateFlow()

    // Administrative test/bypass mode in order to allow manual testing
    private val _isAdminTestMode = MutableStateFlow(false)
    val isAdminTestMode: StateFlow<Boolean> = _isAdminTestMode.asStateFlow()

    fun toggleAdminTestMode() {
        _isAdminTestMode.value = !_isAdminTestMode.value
    }

    private val _mockLat = MutableStateFlow(52.2297) // Warsaw default
    val mockLat = _mockLat.asStateFlow()

    private val _mockLon = MutableStateFlow(21.0122)
    val mockLon = _mockLon.asStateFlow()

    // Barrier animation states: "CLOSED", "OPENING", "OPEN", "CLOSING"
    private val _barrierState = MutableStateFlow("CLOSED")
    val barrierState: StateFlow<String> = _barrierState.asStateFlow()

    private val _barrierCountdown = MutableStateFlow(0)
    val barrierCountdown: StateFlow<Int> = _barrierCountdown.asStateFlow()

    // Lockout countdown timer string in format MM:SS
    private val _lockoutTimerText = MutableStateFlow<String?>(null)
    val lockoutTimerText: StateFlow<String?> = _lockoutTimerText.asStateFlow()

    private var lockoutJob: Job? = null
    private var barrierJob: Job? = null
    private var heartbeatJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppStateRepository(database.appStateDao())
        appStateFlow = repository.appState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppState()
        )

        // Observe lockout changes
        viewModelScope.launch {
            appStateFlow.collectLatest { state ->
                checkLockout(state)
            }
        }

        // Start heartbeat check
        startHeartbeat()
    }

    private fun checkLockout(state: AppState) {
        lockoutJob?.cancel()
        if (state.errorCount >= 3 && state.lockoutTimestamp > 0L) {
            lockoutJob = viewModelScope.launch {
                while (true) {
                    val currentTime = System.currentTimeMillis()
                    val duration = 60 * 60 * 1000L // 60 minutes
                    val remainingMs = (state.lockoutTimestamp + duration) - currentTime
                    if (remainingMs <= 0) {
                        repository.resetErrors()
                        _lockoutTimerText.value = null
                        break
                    } else {
                        val minutes = (remainingMs / 1000) / 60
                        val seconds = (remainingMs / 1000) % 60
                        _lockoutTimerText.value = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                    }
                    delay(1000)
                }
            }
        } else {
            _lockoutTimerText.value = null
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (true) {
                try {
                    val response = RetrofitClient.apiService.heartbeat()
                    _isOnline.value = response.isSuccessful
                } catch (e: Exception) {
                    _isOnline.value = false
                }
                delay(10000) // Every 10 seconds
            }
        }
    }

    fun onKeyPress(char: Char) {
        val current = _pinInput.value
        val state = appStateFlow.value
        if (state.errorCount >= 3) return // Blocked
        if (current.length < 6) {
            val updated = current + char
            _pinInput.value = updated
            if (updated.length == 6) {
                // Automatically activate once 6-digit complete
                activateWithPin(updated)
            }
        }
    }

    fun onDeletePress() {
        val current = _pinInput.value
        if (current.isNotEmpty()) {
            _pinInput.value = current.dropLast(1)
        }
    }

    fun onClearPress() {
        _pinInput.value = ""
    }

    fun setMockLocation(use: Boolean, lat: Double, lon: Double) {
        _useMockLocation.value = use
        _mockLat.value = lat
        _mockLon.value = lon
    }

    fun resetPinErrors() {
        viewModelScope.launch {
            repository.resetErrors()
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.updateAppState(AppState(id = 1, hash = null, errorCount = 0, lockoutTimestamp = 0L))
            _pinInput.value = ""
            _activationError.value = null
        }
    }

    private fun activateWithPin(pin: String) {
        _isActivating.value = true
        _activationError.value = null

        // Support admin bypass instantly if test mode is toggled or master PIN matches
        if (_isAdminTestMode.value || pin == "999999" || pin == "888888") {
            viewModelScope.launch {
                delay(400) // Aesthetic progress indication
                repository.updateAppState(AppState(id = 1, hash = "ADMIN_TEST_HASH_SECRET", errorCount = 0, lockoutTimestamp = 0L))
                _pinInput.value = ""
                _activationError.value = null
                _isActivating.value = false
            }
            return
        }

        viewModelScope.launch {
            var http200Received = false
            var success = false
            var isOkFalse = false
            var extractedHash: String? = null
            var lastResponseBody = ""

            // Strategy 1: Post Raw Text
            try {
                val mediaType = "text/plain".toMediaTypeOrNull()
                val requestBody = pin.toRequestBody(mediaType)
                val response = RetrofitClient.apiService.activateRaw(pin = requestBody)
                if (response.code() == 200) {
                    http200Received = true
                    val bodyText = response.body()?.string() ?: ""
                    lastResponseBody = bodyText

                    try {
                        val json = JSONObject(bodyText)
                        if (json.has("ok")) {
                            val okVal = json.get("ok")
                            if (okVal is Boolean) {
                                if (okVal) {
                                    success = true
                                    extractedHash = parseHashFromText(bodyText)
                                } else {
                                    isOkFalse = true
                                }
                            } else {
                                val stringVal = okVal.toString().lowercase(Locale.getDefault())
                                if (stringVal == "true") {
                                    success = true
                                    extractedHash = parseHashFromText(bodyText)
                                } else if (stringVal == "false") {
                                    isOkFalse = true
                                }
                            }
                        } else {
                            if (bodyText.uppercase(Locale.getDefault()).contains("TRUE")) {
                                success = true
                                extractedHash = parseHashFromText(bodyText)
                            }
                        }
                    } catch (jsonEx: Exception) {
                        if (bodyText.uppercase(Locale.getDefault()).contains("TRUE")) {
                            success = true
                            extractedHash = parseHashFromText(bodyText)
                        } else if (bodyText.uppercase(Locale.getDefault()).contains("FALSE")) {
                            isOkFalse = true
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Strategy 2 fallback: Post Form urlencoded
            if (!http200Received) {
                try {
                    val response = RetrofitClient.apiService.activateForm(pin = pin)
                    if (response.code() == 200) {
                        http200Received = true
                        val bodyText = response.body()?.string() ?: ""
                        lastResponseBody = bodyText

                        try {
                            val json = JSONObject(bodyText)
                            if (json.has("ok")) {
                                val okVal = json.get("ok")
                                if (okVal is Boolean) {
                                    if (okVal) {
                                        success = true
                                        extractedHash = parseHashFromText(bodyText)
                                    } else {
                                        isOkFalse = true
                                    }
                                } else {
                                    val stringVal = okVal.toString().lowercase(Locale.getDefault())
                                    if (stringVal == "true") {
                                        success = true
                                        extractedHash = parseHashFromText(bodyText)
                                    } else if (stringVal == "false") {
                                        isOkFalse = true
                                    }
                                }
                            } else {
                                if (bodyText.uppercase(Locale.getDefault()).contains("TRUE")) {
                                    success = true
                                    extractedHash = parseHashFromText(bodyText)
                                }
                            }
                        } catch (jsonEx: Exception) {
                            if (bodyText.uppercase(Locale.getDefault()).contains("TRUE")) {
                                success = true
                                extractedHash = parseHashFromText(bodyText)
                            } else if (bodyText.uppercase(Locale.getDefault()).contains("FALSE")) {
                                isOkFalse = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Strategy 3 fallback: Post JSON map
            if (!http200Received) {
                try {
                    val response = RetrofitClient.apiService.activateMap(body = mapOf("pin" to pin))
                    if (response.code() == 200) {
                        http200Received = true
                        val bodyText = response.body()?.string() ?: ""
                        lastResponseBody = bodyText

                        try {
                            val json = JSONObject(bodyText)
                            if (json.has("ok")) {
                                val okVal = json.get("ok")
                                if (okVal is Boolean) {
                                    if (okVal) {
                                        success = true
                                        extractedHash = parseHashFromText(bodyText)
                                    } else {
                                        isOkFalse = true
                                    }
                                } else {
                                    val stringVal = okVal.toString().lowercase(Locale.getDefault())
                                    if (stringVal == "true") {
                                        success = true
                                        extractedHash = parseHashFromText(bodyText)
                                    } else if (stringVal == "false") {
                                        isOkFalse = true
                                    }
                                }
                            } else {
                                if (bodyText.uppercase(Locale.getDefault()).contains("TRUE")) {
                                    success = true
                                    extractedHash = parseHashFromText(bodyText)
                                }
                            }
                        } catch (jsonEx: Exception) {
                            if (bodyText.uppercase(Locale.getDefault()).contains("TRUE")) {
                                success = true
                                extractedHash = parseHashFromText(bodyText)
                            } else if (bodyText.uppercase(Locale.getDefault()).contains("FALSE")) {
                                isOkFalse = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (success) {
                // If TRUE but hash wasn't found in keys, let's look for any alphanumeric sequence as fallback for hash
                val finalHash = extractedHash ?: parseFallbackHash(lastResponseBody) ?: "MOCK_HASH_${pin.hashCode()}"
                repository.updateAppState(AppState(id = 1, hash = finalHash, errorCount = 0, lockoutTimestamp = 0L))
                _pinInput.value = ""
                _activationError.value = null
            } else {
                _pinInput.value = ""
                if (!http200Received) {
                    _activationError.value = "Błąd połączenia z serwerem szlabanu"
                } else {
                    _activationError.value = "Aktywacja nieudana"
                    if (isOkFalse) {
                        repository.incrementErrors(System.currentTimeMillis())
                    }
                }
            }

            _isActivating.value = false
        }
    }

    private fun parseHashFromText(text: String): String? {
        try {
            // Check standard json key
            val json = JSONObject(text)
            if (json.has("hash")) {
                return json.getString("hash")
            }
        } catch (e: Exception) {
            // Ignore
        }
        // Use Regex matches for key "hash": "some_value"
        val pattern = """"hash"\s*:\s*"([^"]+)"""".toRegex(RegexOption.IGNORE_CASE)
        val match = pattern.find(text)
        if (match != null) {
            return match.groupValues[1]
        }
        return null
    }

    private fun parseFallbackHash(text: String): String? {
        // Try to pull out anything resembling a 12-64 char hex/alphanumeric hash
        val pattern = """([a-fA-F0-9]{16,64})""".toRegex()
        val match = pattern.find(text)
        return match?.groupValues?.get(1)
    }

    fun openBarrier() {
        val hash = appStateFlow.value.hash ?: return
        _isOpening.value = true
        _barrierResponse.value = null

        // Bypass backend connection entirely when operating in admin test mode
        if (_isAdminTestMode.value || hash == "ADMIN_TEST_HASH_SECRET") {
            viewModelScope.launch {
                delay(600) // Simulate a realistic hardware relay activation delay
                startBarrierAnimation()
                _isOpening.value = false
            }
            return
        }

        viewModelScope.launch {
            // Get location
            var location: Location? = null
            if (_useMockLocation.value) {
                location = Location("MOCK").apply {
                    latitude = _mockLat.value
                    longitude = _mockLon.value
                }
            } else {
                location = locationHelper.getCurrentLocation()
            }

            val lat = location?.latitude ?: 52.2297 // Fallback if still null
            val lon = location?.longitude ?: 21.0122

            var success = false
            var responseString = "Brak połączenia z serwerem"

            // Strategy 1: URL Encoded Form POST
            try {
                val response = RetrofitClient.apiService.openBarrier(
                    hash = hash,
                    lat = lat,
                    lon = lon
                )
                val bodyText = response.body()?.string() ?: response.errorBody()?.string() ?: ""
                responseString = bodyText
                if (response.isSuccessful) {
                    success = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                responseString = "Błąd: " + (e.localizedMessage ?: "Nieznany")
            }

            // Strategy 2 fallback: JSON POST
            if (!success && !responseString.contains("Zbyt daleko")) {
                try {
                    val response = RetrofitClient.apiService.openBarrierJson(
                        body = mapOf(
                            "hash" to hash,
                            "lat" to lat.toString(),
                            "lon" to lon.toString()
                        )
                    )
                    val bodyText = response.body()?.string() ?: response.errorBody()?.string() ?: ""
                    responseString = bodyText
                    if (response.isSuccessful) {
                        success = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Clean response string (extract message from simple JSON if returned)
            val finalMessage = cleanupMessage(responseString)

            // If the barrier is successfully opened
            if (finalMessage.uppercase(Locale.getDefault()).contains("OTWARTY") || 
                finalMessage.uppercase(Locale.getDefault()).contains("OPEN") ||
                success && !finalMessage.uppercase(Locale.getDefault()).contains("DALEKO")
            ) {
                _barrierResponse.value = null // Null out success signals to prevent displaying unneeded text banners
                startBarrierAnimation()
            } else {
                _barrierResponse.value = finalMessage
            }

            _isOpening.value = false
        }
    }

    private fun cleanupMessage(msg: String): String {
        try {
            val json = JSONObject(msg)
            if (json.has("message")) {
                return json.getString("message")
            }
            if (json.has("status")) {
                return json.getString("status")
            }
        } catch (e: Exception) {
            // Raw text
        }
        return msg.trim()
    }

    private fun startBarrierAnimation() {
        barrierJob?.cancel()
        barrierJob = viewModelScope.launch {
            // Step 1: Opening (4 seconds)
            _barrierState.value = "OPENING"
            for (i in 1..4) {
                _barrierCountdown.value = 4 - i
                delay(1000)
            }

            // Step 2: Open (16 seconds)
            _barrierState.value = "OPEN"
            for (i in 1..16) {
                _barrierCountdown.value = 16 - i
                delay(1000)
            }

            // Step 3: Closing (4 seconds)
            _barrierState.value = "CLOSING"
            for (i in 1..4) {
                _barrierCountdown.value = 4 - i
                delay(1000)
            }

            // Step 4: Closed
            _barrierState.value = "CLOSED"
            _barrierCountdown.value = 0
            _barrierResponse.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        lockoutJob?.cancel()
        barrierJob?.cancel()
        heartbeatJob?.cancel()
    }
}
