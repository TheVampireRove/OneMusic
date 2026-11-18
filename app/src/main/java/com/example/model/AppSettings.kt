package com.example.model

import androidx.compose.ui.graphics.Color

enum class DualPaneSetting(val label: String, val description: String) {
    AUTO("Auto (Responsive)", "Switches to dual-pane on foldables and tablets (>= 720dp)"),
    ALWAYS("Always Dual-Pane", "Forces dual-pane studio layout regardless of screen width"),
    OFF("Single Pane Only", "Standard handheld layout with sliding now playing bar")
}

enum class AlbumLayoutSetting(val label: String, val columns: Int) {
    GRID_2("2-Column Grid", 2),
    GRID_3("3-Column Grid", 3),
    LIST("Detailed List", 1)
}

enum class AccentTheme(
    val displayName: String,
    val primary: Color,
    val secondary: Color,
    val accent: Color
) {
    ONE_UI_BLUE("Samsung Blue", Color(0xFF0072DE), Color(0xFF2979FF), Color(0xFF82B1FF)),
    CYBER_PURPLE("Cyber Purple", Color(0xFF7C4DFF), Color(0xFF00E5FF), Color(0xFFD1C4E9)),
    NEON_EMERALD("Neon Emerald", Color(0xFF00C853), Color(0xFF00E676), Color(0xFFB9F6CA)),
    SUNSET_CORAL("Sunset Coral", Color(0xFFFF6E6C), Color(0xFFFF9100), Color(0xFFFFE082)),
    CHERRY_BLOSSOM("Cherry Blossom", Color(0xFFFF2A85), Color(0xFFFF80AB), Color(0xFFFCE4EC)),
    DEEP_INDIGO("Deep Indigo", Color(0xFF3F51B5), Color(0xFF536DFE), Color(0xFFC5CAE9))
}

enum class DynamicThemeEngine(val displayName: String, val description: String) {
    ANDROID_17_MONET("Android 17 Monet", "Extracts Material You tonal palettes from system wallpaper & theme"),
    TRACK_ALBUM_ART("Album Art Dynamic", "Extracts vibrant expressive colors from currently playing album art"),
    STATIC_ACCENT("Static Accent Palette", "Uses signature curated color swatches"),
    CUSTOM_HEX("Custom Hex Color", "Custom hex code with dynamic monochromatic & triadic harmony"),
    INDIVIDUAL_ELEMENTS("Individual Element Hexes", "Custom hex colors for each individual element in the app")
}

enum class TabletopTopLayout(val label: String, val description: String) {
    STANDARD("Standard Switcher", "Switch between Art, Lyrics, and Visualizer via mode pills"),
    SPLIT_ART_LYRICS("Split: Art & Lyrics", "Display Album Art on the left side and live scrolling lyrics on the right")
}

enum class VisualizerStyle(val label: String, val description: String) {
    BARS("Dynamic Bars", "Frequency bars with rounded tips and vertical gradients"),
    LINES("Thin Spectrum Lines", "Ultra-thin high-density resonance lines"),
    WAVEFORM("Continuous Waveform", "Smooth connected oscillating waveform curve"),
    MIRRORED_BARS("Mirrored Dual-Spectrum", "Symmetrical mirrored spectrum reflecting above and below"),
    CIRCULAR_DOTS("Pulse Dots", "Floating energetic glowing amplitude nodes"),
    DIGITAL_FLAMES("Digital Beat Flames", "Dancing fiery neon tongues leaping to bass and rhythmic beats"),
    LIQUID_WAVES("Liquid Water Waves", "Fluid harmonic aquatic ripples and surging water waves"),
    NEON_AURORA("Neon Aurora", "Luminous flowing ribbons of chromatic energy shifting with frequency"),
    RADIAL_ORBIT("Radial Orbital Pulse", "Dynamic circular orbital starburst pulsating outwards to beats"),
    PARTICLE_RAIN("Cyber Particle Matrix", "Cascading illuminated raindrops with rhythmic density bursts"),
    RETRO_GRID("Synthwave Horizon", "Perspective cyberpunk wireframe grid surging to bass vibrations"),
    SOUND_BLOX("Equalizer Stacks", "3D isometric stacked glowing equalizer blocks"),
    RECORD_PLAYING("Record Playing", "Spinning vinyl record with dynamic equalizer grooves and tonearm"),
    KALEIDOSCOPE_MANDALA("Kaleidoscope Mandala", "Rotating symmetrical geometric petals blooming with bass and treble"),
    DNA_DOUBLE_HELIX("DNA Beat Helix", "Intertwined 3D glowing double helix twisting and pulsing to audio rhythm"),
    FIREFLY_SWARM("Bioluminescent Swarm", "Glowing orbital fireflies dancing in harmonic Lissajous trails to the beat"),
    GALAXY_SUPERNOVA("Cosmic Supernova", "Spiral nebula arms and twinkling starfield surging around a pulsing core"),
    OSCILLOSCOPE_LISSAJOUS("XY Oscilloscope", "Classic phosphor CRT harmonic loops warped by bass and treble"),
    CASSETTE_TAPE("Retro Cassette Deck", "Spinning dual-reel analog cassette tape with live VU level meters")
}

enum class LyricsAnimationStyle(val label: String, val description: String) {
    SMOOTH_FLOAT("Smooth Glide", "Gentle spring scale, soft glow, and smooth focus glide between lines"),
    KARAOKE_SWEEP("Karaoke Highlight Sweep", "Progressive left-to-right color wipe synced across the active lyric line"),
    ELASTIC_POP("Elastic Beat Pop", "Playful bouncy spring pop with vibrant halo on every line change"),
    CINEMATIC_FOCUS("Cinematic Depth Focus", "Active lyric snaps into crisp focus while surrounding lines softly recede"),
    WAVE_FLOW("Harmonic Wave", "Gentle floating vertical wave motion following the music rhythm"),
    TYPEWRITER_REVEAL("Lyric Typewriter", "Active line progressively illuminates word-by-word in sync with the track")
}

enum class LyricsAnimationSpeed(val label: String, val durationMultiplier: Float) {
    RELAXED("Relaxed", 1.45f),
    BALANCED("Balanced", 1.0f),
    SNAPPY("Snappy", 0.55f)
}

enum class ProgressBarThickness(val label: String, val thicknessDp: Int) {
    HAIRLINE("Ultra Thin (2dp)", 2),
    SLENDER("Slender (3dp)", 3),
    COMPACT("Compact (4dp)", 4),
    MEDIUM("Standard (7dp)", 7),
    SUBSTANTIAL("Substantial (9dp)", 9),
    LARGE("Thick (11dp)", 11),
    HEAVY("Bold Heavy (14dp)", 14),
    EXTRA_LARGE("Jumbo Tactile (18dp)", 18),
    MASSIVE("Massive Pill (22dp)", 22),
    COLOSSAL("Colossal Console (26dp)", 26)
}

enum class ButtonAnimationEffect(val label: String, val description: String) {
    BOUNCY_SPRING("Bouncy Spring & Pulse", "Dynamic bouncy compression with springy release (DampingRatioMediumBouncy)"),
    ROTATION_TILT("Tactile 3D Tilt", "Rotational angular tilt in the direction of the tap (±12°)"),
    NEON_GLOW_POP("Neon Glow Pop", "Vibrant expanding neon radiance flash and scale flare"),
    JELLY_SQUISH("Jelly Squish", "Playful organic squash-and-stretch physics (scaleX 1.16f, scaleY 0.84f)"),
    SUBTLE_HAPTIC("Subtle Depth Sink", "Minimalist sleek depression into the glass surface (scale 0.94f)")
}

enum class ProgressBarDesign(val label: String, val description: String) {
    SMOOTH("Classic Smooth", "Sleek minimalist pill scrubber"),
    WAVY("Expressive Wavy", "Dynamic undulating sinusoidal wave playback track"),
    SQUIGGLY("Playful Squiggly", "High-frequency expressive oscillating squiggle"),
    PULSE("Neon Pulse", "Segmented rhythmic pulsing bar"),
    GRADIENT_GLOW("Neon Gradient Glow", "Vibrant illuminated neon bar with multi-layered diffuse glow"),
    SEGMENTED("Segmented Equalizer Blocks", "Discrete illuminated high-tech audio blocks"),
    MINIMAL_DOT("Minimalist Track & Dot", "Ultra-sleek hairline track with prominent pulsing position orb"),
    STEPPED_LADDER("Ladder Rhythm", "Stepped micro-ticks along the playback duration")
}

enum class PlayerButtonSize(val label: String, val scale: Float) {
    SMALL("Small", 0.85f),
    MEDIUM("Medium", 1.0f),
    LARGE("Large", 1.18f),
    LARGER("Extra Large", 1.35f)
}

enum class TabletopButtonSize(val label: String, val description: String) {
    SMALL("Small", "Compact controls with extra room for album details"),
    MEDIUM("Medium", "Balanced standard tabletop layout"),
    LARGE("Large", "Prominent oversized tactile controls"),
    LARGER("Larger (Fill)", "Expansive controls auto-arranged to fill available tabletop shelf")
}

enum class TabletopControlStyle(val label: String, val description: String) {
    ONE_UI_TACTILE("One UI Tactile", "Samsung Galaxy-styled tabletop buttons with optional free placement"),
    NETFLIX_DUO("Netflix Duo Style", "Netflix-inspired dual screen console with 10s jump skip, hero center toggle, and quick audio tools")
}

enum class TabletopAlbumArtSize(val label: String, val sizeDp: Int) {
    MEDIUM("Standard (180dp)", 180),
    LARGE("Large (220dp)", 220),
    EXTRA_LARGE("Extra Large (260dp)", 260),
    JUMBO("Jumbo Studio (300dp)", 300),
    MASSIVE("Massive (360dp)", 360),
    COLOSSAL("Colossal Cinema (420dp)", 420),
    MAXIMUM("Maximum Full Display (500dp)", 500)
}

enum class AlbumArtSize(val label: String, val sizeDp: Int, val cornerRadiusDp: Int) {
    SMALL("Small", 44, 12),
    MEDIUM("Medium", 52, 14),
    LARGE("Large", 68, 18),
    EXTRA_LARGE("Extra Large", 86, 22),
    JUMBO("Jumbo Studio", 108, 26)
}

enum class SortOption(val label: String) {
    TITLE_AZ("Title (A-Z)"),
    TITLE_ZA("Title (Z-A)"),
    ARTIST("Artist"),
    ALBUM("Album"),
    DURATION("Duration"),
    DATE_ADDED("Recently Added")
}

enum class FilterType(val label: String) {
    ALL("All"),
    FAVORITES("Favorites"),
    LOCAL_ONLY("Device Storage"),
    SYNTHWAVE("Synthwave"),
    LOFI("Lo-Fi"),
    ELECTRONIC("Electronic"),
    AMBIENT("Ambient"),
    KPOP("K-Pop")
}

data class ElementColors(
    val background: String = "#0F101E",
    val primary: String = "#0072DE",
    val secondary: String = "#2979FF",
    val cardBackground: String = "#1E223D",
    val scrubber: String = "#00E5FF",
    val tabHighlight: String = "#0072DE",
    val textPrimary: String = "#FFFFFF",
    val textSecondary: String = "#99FFFFFF",

    // Expanded Header & Collapsed Top App Bar Elements
    val headerExpandedBackground: String = "#00000000",
    val headerCollapsedBackground: String = "#E60F101E",
    val headerTitleColor: String = "#FFFFFF",
    val headerSubtitleColor: String = "#00E5FF",

    // Side Rail Elements (Dual-Pane / Foldable)
    val railBackground: String = "#15182C",
    val railSelectedItem: String = "#0072DE",
    val railUnselectedItem: String = "#88FFFFFF",
    val railHeaderIcon: String = "#00E5FF",

    // Track List Elements
    val trackListCardBg: String = "#1E223D",
    val trackTitleColor: String = "#FFFFFF",
    val trackSubtitleColor: String = "#99FFFFFF",
    val trackActiveHighlight: String = "#00E5FF",
    val trackDurationColor: String = "#77FFFFFF",
    val trackFavoriteColor: String = "#FF2A85",

    // Visualizer Elements
    val visualizerPeakColor: String = "#00E5FF",
    val visualizerBarColor: String = "#7C4DFF",
    val visualizerBaseColor: String = "#0072DE",
    val visualizerGlowColor: String = "#2979FF",
    val visualizerBackgroundGlow: String = "#1976D2",
    val visualizerWaveformStroke: String = "#00E5FF",

    // Settings Menu Elements
    val settingsBackground: String = "#0F101E",
    val settingsCardBg: String = "#1B1D36",
    val settingsCategoryHeaderColor: String = "#0072DE",
    val settingsTextColor: String = "#FFFFFF",
    val settingsSwitchTrack: String = "#2979FF",
    val settingsBackButtonColor: String = "#00E5FF",

    // Now Playing & Player Elements
    val playerBackgroundTop: String = "#1A1D36",
    val playerBackgroundBottom: String = "#0D0E1A",
    val playerControlPlayBtn: String = "#0072DE",
    val playerControlSkipBtn: String = "#FFFFFF",
    val playerTimestampColor: String = "#88FFFFFF",
    val playerVolumeTrack: String = "#0072DE",

    // Tabletop Mode Comprehensive Colors
    val tabletopTopHalfBackground: String = "#1A1D36",
    val tabletopBottomHalfBackground: String = "#0D0E1A",
    val tabletopReflectionTint: String = "#40000000",
    val tabletopPlayButton: String = "#0072DE",
    val tabletopPlayIcon: String = "#FFFFFF",
    val tabletopSkipButtons: String = "#FFFFFF",
    val tabletopSecondaryButtons: String = "#88FFFFFF",
    val tabletopActiveButtonTint: String = "#00E5FF",
    val tabletopTrackTitle: String = "#FFFFFF",
    val tabletopTrackArtist: String = "#99FFFFFF",
    val tabletopLyricsText: String = "#FFFFFF",
    val tabletopLyricsHighlight: String = "#00E5FF",
    val tabletopScrubberActive: String = "#00E5FF",
    val tabletopScrubberInactive: String = "#35FFFFFF",
    val tabletopVolumeTrack: String = "#0072DE",
    val tabletopVisualizerColor: String = "#00E5FF",
    val tabletopBackgroundColor: String = "#0C0D17"
)

data class TabletopControlPositions(
    val isCustomLayoutEnabled: Boolean = false,
    val shuffleX: Float = 0f,
    val shuffleY: Float = 0f,
    val prevX: Float = 0f,
    val prevY: Float = 0f,
    val playX: Float = 0f,
    val playY: Float = 0f,
    val nextX: Float = 0f,
    val nextY: Float = 0f,
    val repeatX: Float = 0f,
    val repeatY: Float = 0f,
    val volumeX: Float = 0f,
    val volumeY: Float = 0f,
    val speedX: Float = 0f,
    val speedY: Float = 0f,
    val queueX: Float = 0f,
    val queueY: Float = 0f,
    val rewind10X: Float = 0f,
    val rewind10Y: Float = 0f,
    val forward10X: Float = 0f,
    val forward10Y: Float = 0f,
    val panOffsetX: Float = 0f,
    val panOffsetY: Float = 0f,
    val customScale: Float = 1.0f,
    val isGridSnapEnabled: Boolean = true,
    val isInfiniteCanvasEnabled: Boolean = true
)

data class AppSettings(
    val dynamicThemeEnabled: Boolean = true,
    val themeEngine: DynamicThemeEngine = DynamicThemeEngine.ANDROID_17_MONET,
    val accentTheme: AccentTheme = AccentTheme.ONE_UI_BLUE,
    val customHexColor: String = "#0072DE",
    val useCustomElementColors: Boolean = false,
    val elementColors: ElementColors = ElementColors(),
    val dualPaneMode: DualPaneSetting = DualPaneSetting.AUTO,
    val autoFlexMode: Boolean = true,
    val albumLayout: AlbumLayoutSetting = AlbumLayoutSetting.GRID_2,
    val startupTab: MusicTab = MusicTab.TRACKS,
    val keepScreenOn: Boolean = false,
    val gaplessPlayback: Boolean = true,
    val highFidelityVisualizer: Boolean = true,
    val defaultSoundPreset: SoundAlivePreset = SoundAlivePreset.NORMAL,
    val progressBarDesign: ProgressBarDesign = ProgressBarDesign.WAVY,
    val progressBarThickness: ProgressBarThickness = ProgressBarThickness.MEDIUM,
    val playerButtonSize: PlayerButtonSize = PlayerButtonSize.MEDIUM,
    val tabletopButtonSize: TabletopButtonSize = TabletopButtonSize.MEDIUM,
    val tabletopControlStyle: TabletopControlStyle = TabletopControlStyle.ONE_UI_TACTILE,
    val tabletopAlbumArtSize: TabletopAlbumArtSize = TabletopAlbumArtSize.MEDIUM,
    val tabletopControlsScale: Float = 1.0f,
    val tabletopControlPositions: TabletopControlPositions = TabletopControlPositions(),
    val tabletopImmersiveMode: Boolean = false,
    val tabletopTopLayout: TabletopTopLayout = TabletopTopLayout.STANDARD,
    val visualizerStyle: VisualizerStyle = VisualizerStyle.BARS,
    val tabletopReflectedBackgroundEnabled: Boolean = false,
    val tabletopShowVolumeBar: Boolean = true,
    val albumArtSize: AlbumArtSize = AlbumArtSize.MEDIUM,
    val visualizerAlbumArtSize: AlbumArtSize = AlbumArtSize.MEDIUM,
    val trackListAlbumArtSize: AlbumArtSize = AlbumArtSize.MEDIUM,
    val lastPlayedTrackId: String? = null,
    val lastPlayedPositionMs: Long = 0L,
    val lastPlayedQueueIds: List<String> = emptyList(),
    val sortOption: SortOption = SortOption.TITLE_AZ,
    val activeFilter: FilterType = FilterType.ALL,
    val liveNotificationsEnabled: Boolean = true,
    val liveNotificationShowFavorite: Boolean = true,
    val liveNotificationShowProgress: Boolean = true,
    val liveNotificationLockScreenAOD: Boolean = true,
    val liveNotificationSoundBadge: Boolean = true,
    val buttonAnimationEffect: ButtonAnimationEffect = ButtonAnimationEffect.BOUNCY_SPRING,
    val autoFetchLyricsOnScan: Boolean = false,
    val lyricsSyncWifiOnly: Boolean = false,
    val lyricsAnimationStyle: LyricsAnimationStyle = LyricsAnimationStyle.SMOOTH_FLOAT,
    val lyricsAnimationSpeed: LyricsAnimationSpeed = LyricsAnimationSpeed.BALANCED,
    val lyricsGlowEnabled: Boolean = true
)
