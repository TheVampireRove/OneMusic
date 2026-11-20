package com.example.data

import android.content.Context
import com.example.model.AccentTheme
import com.example.model.AlbumArtSize
import com.example.model.AlbumLayoutSetting
import com.example.model.AppSettings
import com.example.model.DualPaneSetting
import com.example.model.DynamicThemeEngine
import com.example.model.ElementColors
import com.example.model.FilterType
import com.example.model.MusicTab
import com.example.model.PlayerButtonSize
import com.example.model.ProgressBarDesign
import com.example.model.ProgressBarThickness
import com.example.model.SortOption
import com.example.model.SoundAlivePreset
import com.example.model.TabletopAlbumArtSize
import com.example.model.TabletopButtonSize
import com.example.model.TabletopControlPositions
import com.example.model.TabletopControlStyle
import com.example.model.TabletopTopLayout
import com.example.model.ButtonAnimationEffect
import com.example.model.LyricsAnimationSpeed
import com.example.model.LyricsAnimationStyle
import com.example.model.VisualizerStyle
import org.json.JSONObject
import java.io.File

object AppSettingsCache {
    private const val SETTINGS_FILE_NAME = "cached_app_settings.json"

    fun saveSettings(context: Context, settings: AppSettings) {
        try {
            val root = JSONObject().apply {
                put("dynamicThemeEnabled", settings.dynamicThemeEnabled)
                put("themeEngine", settings.themeEngine.name)
                put("accentTheme", settings.accentTheme.name)
                put("customHexColor", settings.customHexColor)
                put("useCustomElementColors", settings.useCustomElementColors)

                val elemObj = JSONObject().apply {
                    put("background", settings.elementColors.background)
                    put("primary", settings.elementColors.primary)
                    put("secondary", settings.elementColors.secondary)
                    put("cardBackground", settings.elementColors.cardBackground)
                    put("scrubber", settings.elementColors.scrubber)
                    put("tabHighlight", settings.elementColors.tabHighlight)
                    put("textPrimary", settings.elementColors.textPrimary)
                    put("textSecondary", settings.elementColors.textSecondary)

                    // Header & Top App Bar
                    put("headerExpandedBackground", settings.elementColors.headerExpandedBackground)
                    put("headerCollapsedBackground", settings.elementColors.headerCollapsedBackground)
                    put("headerTitleColor", settings.elementColors.headerTitleColor)
                    put("headerSubtitleColor", settings.elementColors.headerSubtitleColor)

                    // Side Rail
                    put("railBackground", settings.elementColors.railBackground)
                    put("railSelectedItem", settings.elementColors.railSelectedItem)
                    put("railUnselectedItem", settings.elementColors.railUnselectedItem)
                    put("railHeaderIcon", settings.elementColors.railHeaderIcon)

                    // Track List
                    put("trackListCardBg", settings.elementColors.trackListCardBg)
                    put("trackTitleColor", settings.elementColors.trackTitleColor)
                    put("trackSubtitleColor", settings.elementColors.trackSubtitleColor)
                    put("trackActiveHighlight", settings.elementColors.trackActiveHighlight)
                    put("trackDurationColor", settings.elementColors.trackDurationColor)
                    put("trackFavoriteColor", settings.elementColors.trackFavoriteColor)

                    // Visualizer
                    put("visualizerPeakColor", settings.elementColors.visualizerPeakColor)
                    put("visualizerBarColor", settings.elementColors.visualizerBarColor)
                    put("visualizerBaseColor", settings.elementColors.visualizerBaseColor)
                    put("visualizerGlowColor", settings.elementColors.visualizerGlowColor)
                    put("visualizerBackgroundGlow", settings.elementColors.visualizerBackgroundGlow)
                    put("visualizerWaveformStroke", settings.elementColors.visualizerWaveformStroke)

                    // Settings Menu Elements
                    put("settingsBackground", settings.elementColors.settingsBackground)
                    put("settingsCardBg", settings.elementColors.settingsCardBg)
                    put("settingsCategoryHeaderColor", settings.elementColors.settingsCategoryHeaderColor)
                    put("settingsTextColor", settings.elementColors.settingsTextColor)
                    put("settingsSwitchTrack", settings.elementColors.settingsSwitchTrack)
                    put("settingsBackButtonColor", settings.elementColors.settingsBackButtonColor)

                    // Player Elements
                    put("playerBackgroundTop", settings.elementColors.playerBackgroundTop)
                    put("playerBackgroundBottom", settings.elementColors.playerBackgroundBottom)
                    put("playerControlPlayBtn", settings.elementColors.playerControlPlayBtn)
                    put("playerControlSkipBtn", settings.elementColors.playerControlSkipBtn)
                    put("playerTimestampColor", settings.elementColors.playerTimestampColor)
                    put("playerVolumeTrack", settings.elementColors.playerVolumeTrack)

                    // Tabletop Mode Comprehensive Colors
                    put("tabletopTopHalfBackground", settings.elementColors.tabletopTopHalfBackground)
                    put("tabletopBottomHalfBackground", settings.elementColors.tabletopBottomHalfBackground)
                    put("tabletopReflectionTint", settings.elementColors.tabletopReflectionTint)
                    put("tabletopPlayButton", settings.elementColors.tabletopPlayButton)
                    put("tabletopPlayIcon", settings.elementColors.tabletopPlayIcon)
                    put("tabletopSkipButtons", settings.elementColors.tabletopSkipButtons)
                    put("tabletopSecondaryButtons", settings.elementColors.tabletopSecondaryButtons)
                    put("tabletopActiveButtonTint", settings.elementColors.tabletopActiveButtonTint)
                    put("tabletopTrackTitle", settings.elementColors.tabletopTrackTitle)
                    put("tabletopTrackArtist", settings.elementColors.tabletopTrackArtist)
                    put("tabletopLyricsText", settings.elementColors.tabletopLyricsText)
                    put("tabletopLyricsHighlight", settings.elementColors.tabletopLyricsHighlight)
                    put("tabletopScrubberActive", settings.elementColors.tabletopScrubberActive)
                    put("tabletopScrubberInactive", settings.elementColors.tabletopScrubberInactive)
                    put("tabletopVolumeTrack", settings.elementColors.tabletopVolumeTrack)
                    put("tabletopVisualizerColor", settings.elementColors.tabletopVisualizerColor)
                    put("tabletopBackgroundColor", settings.elementColors.tabletopBackgroundColor)
                }
                put("elementColors", elemObj)

                put("dualPaneMode", settings.dualPaneMode.name)
                put("autoFlexMode", settings.autoFlexMode)
                put("albumLayout", settings.albumLayout.name)
                put("startupTab", settings.startupTab.name)
                put("keepScreenOn", settings.keepScreenOn)
                put("gaplessPlayback", settings.gaplessPlayback)
                put("highFidelityVisualizer", settings.highFidelityVisualizer)
                put("defaultSoundPreset", settings.defaultSoundPreset.name)
                put("progressBarDesign", settings.progressBarDesign.name)
                put("progressBarThickness", settings.progressBarThickness.name)
                put("playerButtonSize", settings.playerButtonSize.name)
                put("tabletopButtonSize", settings.tabletopButtonSize.name)
                put("tabletopControlStyle", settings.tabletopControlStyle.name)
                put("tabletopAlbumArtSize", settings.tabletopAlbumArtSize.name)
                put("tabletopControlsScale", settings.tabletopControlsScale.toDouble())

                val posObj = JSONObject().apply {
                    put("isCustomLayoutEnabled", settings.tabletopControlPositions.isCustomLayoutEnabled)
                    put("shuffleX", settings.tabletopControlPositions.shuffleX.toDouble())
                    put("shuffleY", settings.tabletopControlPositions.shuffleY.toDouble())
                    put("prevX", settings.tabletopControlPositions.prevX.toDouble())
                    put("prevY", settings.tabletopControlPositions.prevY.toDouble())
                    put("playX", settings.tabletopControlPositions.playX.toDouble())
                    put("playY", settings.tabletopControlPositions.playY.toDouble())
                    put("nextX", settings.tabletopControlPositions.nextX.toDouble())
                    put("nextY", settings.tabletopControlPositions.nextY.toDouble())
                    put("repeatX", settings.tabletopControlPositions.repeatX.toDouble())
                    put("repeatY", settings.tabletopControlPositions.repeatY.toDouble())
                    put("volumeX", settings.tabletopControlPositions.volumeX.toDouble())
                    put("volumeY", settings.tabletopControlPositions.volumeY.toDouble())
                    put("speedX", settings.tabletopControlPositions.speedX.toDouble())
                    put("speedY", settings.tabletopControlPositions.speedY.toDouble())
                    put("queueX", settings.tabletopControlPositions.queueX.toDouble())
                    put("queueY", settings.tabletopControlPositions.queueY.toDouble())
                    put("rewind10X", settings.tabletopControlPositions.rewind10X.toDouble())
                    put("rewind10Y", settings.tabletopControlPositions.rewind10Y.toDouble())
                    put("forward10X", settings.tabletopControlPositions.forward10X.toDouble())
                    put("forward10Y", settings.tabletopControlPositions.forward10Y.toDouble())
                    put("panOffsetX", settings.tabletopControlPositions.panOffsetX.toDouble())
                    put("panOffsetY", settings.tabletopControlPositions.panOffsetY.toDouble())
                    put("customScale", settings.tabletopControlPositions.customScale.toDouble())
                    put("isGridSnapEnabled", settings.tabletopControlPositions.isGridSnapEnabled)
                    put("isInfiniteCanvasEnabled", settings.tabletopControlPositions.isInfiniteCanvasEnabled)
                }
                put("tabletopControlPositions", posObj)

                put("tabletopImmersiveMode", settings.tabletopImmersiveMode)
                put("tabletopTopLayout", settings.tabletopTopLayout.name)
                put("visualizerStyle", settings.visualizerStyle.name)
                put("tabletopReflectedBackgroundEnabled", settings.tabletopReflectedBackgroundEnabled)
                put("tabletopShowVolumeBar", settings.tabletopShowVolumeBar)
                put("albumArtSize", settings.albumArtSize.name)
                put("visualizerAlbumArtSize", settings.visualizerAlbumArtSize.name)
                put("trackListAlbumArtSize", settings.trackListAlbumArtSize.name)
                if (settings.lastPlayedTrackId != null) {
                    put("lastPlayedTrackId", settings.lastPlayedTrackId)
                }
                put("lastPlayedPositionMs", settings.lastPlayedPositionMs)
                val queueArray = org.json.JSONArray()
                settings.lastPlayedQueueIds.forEach { queueArray.put(it) }
                put("lastPlayedQueueIds", queueArray)
                put("sortOption", settings.sortOption.name)
                put("activeFilter", settings.activeFilter.name)
                put("liveNotificationsEnabled", settings.liveNotificationsEnabled)
                put("liveNotificationShowFavorite", settings.liveNotificationShowFavorite)
                put("buttonAnimationEffect", settings.buttonAnimationEffect.name)
                put("liveNotificationShowProgress", settings.liveNotificationShowProgress)
                put("liveNotificationLockScreenAOD", settings.liveNotificationLockScreenAOD)
                put("liveNotificationSoundBadge", settings.liveNotificationSoundBadge)
                put("autoFetchLyricsOnScan", settings.autoFetchLyricsOnScan)
                put("lyricsSyncWifiOnly", settings.lyricsSyncWifiOnly)
                put("lyricsAnimationStyle", settings.lyricsAnimationStyle.name)
                put("lyricsAnimationSpeed", settings.lyricsAnimationSpeed.name)
                put("lyricsGlowEnabled", settings.lyricsGlowEnabled)
                put("enableAnimatedCovers", settings.enableAnimatedCovers)
                put("autoFetchAnimatedCoversWifiOnly", settings.autoFetchAnimatedCoversWifiOnly)
                put("simulateMotionOnStaticCovers", settings.simulateMotionOnStaticCovers)
            }

            val file = File(context.filesDir, SETTINGS_FILE_NAME)
            file.writeText(root.toString())
        } catch (_: Exception) {}
    }

    fun loadSettings(context: Context): AppSettings {
        try {
            val file = File(context.filesDir, SETTINGS_FILE_NAME)
            if (!file.exists()) return AppSettings()
            val text = file.readText()
            if (text.isBlank()) return AppSettings()
            val obj = JSONObject(text)

            val themeEngine = safeValueOf<DynamicThemeEngine>(obj.optString("themeEngine")) ?: DynamicThemeEngine.ANDROID_17_MONET
            val accentTheme = safeValueOf<AccentTheme>(obj.optString("accentTheme")) ?: AccentTheme.ONE_UI_BLUE
            val dualPaneMode = safeValueOf<DualPaneSetting>(obj.optString("dualPaneMode")) ?: DualPaneSetting.AUTO
            val albumLayout = safeValueOf<AlbumLayoutSetting>(obj.optString("albumLayout")) ?: AlbumLayoutSetting.GRID_2
            val startupTab = safeValueOf<MusicTab>(obj.optString("startupTab")) ?: MusicTab.TRACKS
            val defaultSoundPreset = safeValueOf<SoundAlivePreset>(obj.optString("defaultSoundPreset")) ?: SoundAlivePreset.NORMAL
            val progressBarDesign = safeValueOf<ProgressBarDesign>(obj.optString("progressBarDesign")) ?: ProgressBarDesign.WAVY
            val progressBarThickness = safeValueOf<ProgressBarThickness>(obj.optString("progressBarThickness")) ?: ProgressBarThickness.MEDIUM
            val playerButtonSize = safeValueOf<PlayerButtonSize>(obj.optString("playerButtonSize")) ?: PlayerButtonSize.MEDIUM
            val tabletopButtonSize = safeValueOf<TabletopButtonSize>(obj.optString("tabletopButtonSize")) ?: TabletopButtonSize.MEDIUM
            val tabletopControlStyle = safeValueOf<TabletopControlStyle>(obj.optString("tabletopControlStyle")) ?: TabletopControlStyle.ONE_UI_TACTILE
            val tabletopAlbumArtSize = safeValueOf<TabletopAlbumArtSize>(obj.optString("tabletopAlbumArtSize")) ?: TabletopAlbumArtSize.MEDIUM
            val tabletopTopLayout = safeValueOf<TabletopTopLayout>(obj.optString("tabletopTopLayout")) ?: TabletopTopLayout.STANDARD
            val visualizerStyle = safeValueOf<VisualizerStyle>(obj.optString("visualizerStyle")) ?: VisualizerStyle.BARS
            val tabletopReflectedBackgroundEnabled = obj.optBoolean("tabletopReflectedBackgroundEnabled", false)
            val tabletopShowVolumeBar = obj.optBoolean("tabletopShowVolumeBar", true)
            val albumArtSize = safeValueOf<AlbumArtSize>(obj.optString("albumArtSize")) ?: AlbumArtSize.MEDIUM
            val sortOption = safeValueOf<SortOption>(obj.optString("sortOption")) ?: SortOption.TITLE_AZ
            val activeFilter = safeValueOf<FilterType>(obj.optString("activeFilter")) ?: FilterType.ALL
            val buttonAnimationEffect = safeValueOf<ButtonAnimationEffect>(obj.optString("buttonAnimationEffect")) ?: ButtonAnimationEffect.BOUNCY_SPRING
            val lyricsAnimationStyle = safeValueOf<LyricsAnimationStyle>(obj.optString("lyricsAnimationStyle")) ?: LyricsAnimationStyle.SMOOTH_FLOAT
            val lyricsAnimationSpeed = safeValueOf<LyricsAnimationSpeed>(obj.optString("lyricsAnimationSpeed")) ?: LyricsAnimationSpeed.BALANCED
            val lyricsGlowEnabled = obj.optBoolean("lyricsGlowEnabled", true)

            val elemObj = obj.optJSONObject("elementColors")
            val elementColors = if (elemObj != null) {
                ElementColors(
                    background = elemObj.optString("background", "#0F101E"),
                    primary = elemObj.optString("primary", "#0072DE"),
                    secondary = elemObj.optString("secondary", "#2979FF"),
                    cardBackground = elemObj.optString("cardBackground", "#1E223D"),
                    scrubber = elemObj.optString("scrubber", "#00E5FF"),
                    tabHighlight = elemObj.optString("tabHighlight", "#0072DE"),
                    textPrimary = elemObj.optString("textPrimary", "#FFFFFF"),
                    textSecondary = elemObj.optString("textSecondary", "#99FFFFFF"),

                    // Header & Top App Bar
                    headerExpandedBackground = elemObj.optString("headerExpandedBackground", "#00000000"),
                    headerCollapsedBackground = elemObj.optString("headerCollapsedBackground", "#E60F101E"),
                    headerTitleColor = elemObj.optString("headerTitleColor", "#FFFFFF"),
                    headerSubtitleColor = elemObj.optString("headerSubtitleColor", "#00E5FF"),

                    // Side Rail
                    railBackground = elemObj.optString("railBackground", "#15182C"),
                    railSelectedItem = elemObj.optString("railSelectedItem", "#0072DE"),
                    railUnselectedItem = elemObj.optString("railUnselectedItem", "#88FFFFFF"),
                    railHeaderIcon = elemObj.optString("railHeaderIcon", "#00E5FF"),

                    // Track List
                    trackListCardBg = elemObj.optString("trackListCardBg", "#1E223D"),
                    trackTitleColor = elemObj.optString("trackTitleColor", "#FFFFFF"),
                    trackSubtitleColor = elemObj.optString("trackSubtitleColor", "#99FFFFFF"),
                    trackActiveHighlight = elemObj.optString("trackActiveHighlight", "#00E5FF"),
                    trackDurationColor = elemObj.optString("trackDurationColor", "#77FFFFFF"),
                    trackFavoriteColor = elemObj.optString("trackFavoriteColor", "#FF2A85"),

                    // Visualizer
                    visualizerPeakColor = elemObj.optString("visualizerPeakColor", "#00E5FF"),
                    visualizerBarColor = elemObj.optString("visualizerBarColor", "#7C4DFF"),
                    visualizerBaseColor = elemObj.optString("visualizerBaseColor", "#0072DE"),
                    visualizerGlowColor = elemObj.optString("visualizerGlowColor", "#2979FF"),
                    visualizerBackgroundGlow = elemObj.optString("visualizerBackgroundGlow", "#1976D2"),
                    visualizerWaveformStroke = elemObj.optString("visualizerWaveformStroke", "#00E5FF"),

                    // Settings Menu Elements
                    settingsBackground = elemObj.optString("settingsBackground", "#0F101E"),
                    settingsCardBg = elemObj.optString("settingsCardBg", "#1B1D36"),
                    settingsCategoryHeaderColor = elemObj.optString("settingsCategoryHeaderColor", "#0072DE"),
                    settingsTextColor = elemObj.optString("settingsTextColor", "#FFFFFF"),
                    settingsSwitchTrack = elemObj.optString("settingsSwitchTrack", "#2979FF"),
                    settingsBackButtonColor = elemObj.optString("settingsBackButtonColor", "#00E5FF"),

                    // Player Elements
                    playerBackgroundTop = elemObj.optString("playerBackgroundTop", "#1A1D36"),
                    playerBackgroundBottom = elemObj.optString("playerBackgroundBottom", "#0D0E1A"),
                    playerControlPlayBtn = elemObj.optString("playerControlPlayBtn", "#0072DE"),
                    playerControlSkipBtn = elemObj.optString("playerControlSkipBtn", "#FFFFFF"),
                    playerTimestampColor = elemObj.optString("playerTimestampColor", "#88FFFFFF"),
                    playerVolumeTrack = elemObj.optString("playerVolumeTrack", "#0072DE"),

                    // Tabletop Mode Comprehensive Colors
                    tabletopTopHalfBackground = elemObj.optString("tabletopTopHalfBackground", "#1A1D36"),
                    tabletopBottomHalfBackground = elemObj.optString("tabletopBottomHalfBackground", "#0D0E1A"),
                    tabletopReflectionTint = elemObj.optString("tabletopReflectionTint", "#40000000"),
                    tabletopPlayButton = elemObj.optString("tabletopPlayButton", "#0072DE"),
                    tabletopPlayIcon = elemObj.optString("tabletopPlayIcon", "#FFFFFF"),
                    tabletopSkipButtons = elemObj.optString("tabletopSkipButtons", "#FFFFFF"),
                    tabletopSecondaryButtons = elemObj.optString("tabletopSecondaryButtons", "#88FFFFFF"),
                    tabletopActiveButtonTint = elemObj.optString("tabletopActiveButtonTint", "#00E5FF"),
                    tabletopTrackTitle = elemObj.optString("tabletopTrackTitle", "#FFFFFF"),
                    tabletopTrackArtist = elemObj.optString("tabletopTrackArtist", "#99FFFFFF"),
                    tabletopLyricsText = elemObj.optString("tabletopLyricsText", "#FFFFFF"),
                    tabletopLyricsHighlight = elemObj.optString("tabletopLyricsHighlight", "#00E5FF"),
                    tabletopScrubberActive = elemObj.optString("tabletopScrubberActive", "#00E5FF"),
                    tabletopScrubberInactive = elemObj.optString("tabletopScrubberInactive", "#35FFFFFF"),
                    tabletopVolumeTrack = elemObj.optString("tabletopVolumeTrack", "#0072DE"),
                    tabletopVisualizerColor = elemObj.optString("tabletopVisualizerColor", "#00E5FF"),
                    tabletopBackgroundColor = elemObj.optString("tabletopBackgroundColor", "#0C0D17")
                )
            } else {
                ElementColors()
            }

            val posObj = obj.optJSONObject("tabletopControlPositions")
            val positions = if (posObj != null) {
                val isCustom = posObj.optBoolean("isCustomLayoutEnabled", false)
                val prevX = posObj.optDouble("prevX", 0.0).toFloat()
                val rewind10X = posObj.optDouble("rewind10X", 0.0).toFloat()
                val shuffleX = posObj.optDouble("shuffleX", 0.0).toFloat()
                // Sanitize: detect old legacy raw-pixel positions that were packed < 180px
                val isLegacyCorrupted = isCustom && (
                    (prevX != 0f && kotlin.math.abs(prevX) < 180f) ||
                    (rewind10X != 0f && kotlin.math.abs(rewind10X) < 120f) ||
                    (shuffleX != 0f && kotlin.math.abs(shuffleX) < 220f)
                )
                if (isLegacyCorrupted) {
                    TabletopControlPositions()
                } else {
                    TabletopControlPositions(
                        isCustomLayoutEnabled = isCustom,
                        shuffleX = shuffleX,
                        shuffleY = posObj.optDouble("shuffleY", 0.0).toFloat(),
                        prevX = prevX,
                        prevY = posObj.optDouble("prevY", 0.0).toFloat(),
                        playX = posObj.optDouble("playX", 0.0).toFloat(),
                        playY = posObj.optDouble("playY", 0.0).toFloat(),
                        nextX = posObj.optDouble("nextX", 0.0).toFloat(),
                        nextY = posObj.optDouble("nextY", 0.0).toFloat(),
                        repeatX = posObj.optDouble("repeatX", 0.0).toFloat(),
                        repeatY = posObj.optDouble("repeatY", 0.0).toFloat(),
                        volumeX = posObj.optDouble("volumeX", 0.0).toFloat(),
                        volumeY = posObj.optDouble("volumeY", 0.0).toFloat(),
                        speedX = posObj.optDouble("speedX", 0.0).toFloat(),
                        speedY = posObj.optDouble("speedY", 0.0).toFloat(),
                        queueX = posObj.optDouble("queueX", 0.0).toFloat(),
                        queueY = posObj.optDouble("queueY", 0.0).toFloat(),
                        rewind10X = rewind10X,
                        rewind10Y = posObj.optDouble("rewind10Y", 0.0).toFloat(),
                        forward10X = posObj.optDouble("forward10X", 0.0).toFloat(),
                        forward10Y = posObj.optDouble("forward10Y", 0.0).toFloat(),
                        panOffsetX = posObj.optDouble("panOffsetX", 0.0).toFloat(),
                        panOffsetY = posObj.optDouble("panOffsetY", 0.0).toFloat(),
                        customScale = posObj.optDouble("customScale", 1.0).toFloat().coerceIn(0.80f, 1.35f),
                        isGridSnapEnabled = posObj.optBoolean("isGridSnapEnabled", true),
                        isInfiniteCanvasEnabled = posObj.optBoolean("isInfiniteCanvasEnabled", true)
                    )
                }
            } else {
                TabletopControlPositions()
            }

            val visualizerAlbumArtSize = safeValueOf<AlbumArtSize>(obj.optString("visualizerAlbumArtSize")) ?: albumArtSize
            val trackListAlbumArtSize = safeValueOf<AlbumArtSize>(obj.optString("trackListAlbumArtSize")) ?: albumArtSize
            val lastPlayedTrackId = if (obj.has("lastPlayedTrackId")) obj.optString("lastPlayedTrackId").takeIf { it.isNotBlank() } else null
            val lastPlayedPositionMs = obj.optLong("lastPlayedPositionMs", 0L)
            val lastPlayedQueueIds = mutableListOf<String>()
            obj.optJSONArray("lastPlayedQueueIds")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val item = arr.optString(i)
                    if (item.isNotBlank()) lastPlayedQueueIds.add(item)
                }
            }
            val tabletopImmersiveMode = obj.optBoolean("tabletopImmersiveMode", false)

            return AppSettings(
                dynamicThemeEnabled = obj.optBoolean("dynamicThemeEnabled", true),
                themeEngine = themeEngine,
                accentTheme = accentTheme,
                customHexColor = obj.optString("customHexColor", "#0072DE"),
                useCustomElementColors = obj.optBoolean("useCustomElementColors", false),
                elementColors = elementColors,
                dualPaneMode = dualPaneMode,
                autoFlexMode = obj.optBoolean("autoFlexMode", true),
                albumLayout = albumLayout,
                startupTab = startupTab,
                keepScreenOn = obj.optBoolean("keepScreenOn", false),
                gaplessPlayback = obj.optBoolean("gaplessPlayback", true),
                highFidelityVisualizer = obj.optBoolean("highFidelityVisualizer", true),
                defaultSoundPreset = defaultSoundPreset,
                progressBarDesign = progressBarDesign,
                progressBarThickness = progressBarThickness,
                playerButtonSize = playerButtonSize,
                tabletopButtonSize = tabletopButtonSize,
                tabletopControlStyle = tabletopControlStyle,
                tabletopAlbumArtSize = tabletopAlbumArtSize,
                tabletopControlsScale = obj.optDouble("tabletopControlsScale", 1.0).toFloat(),
                tabletopControlPositions = positions,
                tabletopImmersiveMode = tabletopImmersiveMode,
                tabletopTopLayout = tabletopTopLayout,
                visualizerStyle = visualizerStyle,
                tabletopReflectedBackgroundEnabled = tabletopReflectedBackgroundEnabled,
                tabletopShowVolumeBar = tabletopShowVolumeBar,
                albumArtSize = albumArtSize,
                visualizerAlbumArtSize = visualizerAlbumArtSize,
                trackListAlbumArtSize = trackListAlbumArtSize,
                lastPlayedTrackId = lastPlayedTrackId,
                lastPlayedPositionMs = lastPlayedPositionMs,
                lastPlayedQueueIds = lastPlayedQueueIds,
                sortOption = sortOption,
                activeFilter = activeFilter,
                liveNotificationsEnabled = obj.optBoolean("liveNotificationsEnabled", true),
                liveNotificationShowFavorite = obj.optBoolean("liveNotificationShowFavorite", true),
                liveNotificationShowProgress = obj.optBoolean("liveNotificationShowProgress", true),
                liveNotificationLockScreenAOD = obj.optBoolean("liveNotificationLockScreenAOD", true),
                liveNotificationSoundBadge = obj.optBoolean("liveNotificationSoundBadge", true),
                buttonAnimationEffect = buttonAnimationEffect,
                autoFetchLyricsOnScan = obj.optBoolean("autoFetchLyricsOnScan", false),
                lyricsSyncWifiOnly = obj.optBoolean("lyricsSyncWifiOnly", false),
                lyricsAnimationStyle = lyricsAnimationStyle,
                lyricsAnimationSpeed = lyricsAnimationSpeed,
                lyricsGlowEnabled = lyricsGlowEnabled,
                enableAnimatedCovers = obj.optBoolean("enableAnimatedCovers", true),
                autoFetchAnimatedCoversWifiOnly = obj.optBoolean("autoFetchAnimatedCoversWifiOnly", true),
                simulateMotionOnStaticCovers = obj.optBoolean("simulateMotionOnStaticCovers", true)
            )
        } catch (_: Exception) {
            return AppSettings()
        }
    }

    private inline fun <reified T : Enum<T>> safeValueOf(value: String?): T? {
        if (value.isNullOrBlank()) return null
        return try {
            java.lang.Enum.valueOf(T::class.java, value)
        } catch (_: Exception) {
            null
        }
    }
}
