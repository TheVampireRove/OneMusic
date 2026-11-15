package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SoundAlivePreset
import com.example.ui.theme.OneUIPillShape
import com.example.ui.theme.OneUISquircleMedium
import com.example.ui.theme.glassmorphism

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EqualizerSheet(
    selectedPreset: SoundAlivePreset,
    bassValue: Float,
    midValue: Float,
    trebleValue: Float,
    dolbyAtmosEnabled: Boolean,
    primaryColor: Color,
    accentColor: Color,
    onSelectPreset: (SoundAlivePreset) -> Unit,
    onBassChange: (Float) -> Unit,
    onMidChange: (Float) -> Unit,
    onTrebleChange: (Float) -> Unit,
    onToggleDolbyAtmos: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF151627),
        scrimColor = Color(0x99000000),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "SoundAlive™",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Audio Enhancement & SoundAlive EQ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xAAFFFFFF)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .glassmorphism(shape = CircleShape, backgroundColor = Color(0x22FFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Equalizer",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dolby Atmos Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphism(
                        shape = OneUISquircleMedium,
                        backgroundColor = Color(0x351E203B)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (dolbyAtmosEnabled) accentColor else Color(0x22FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SurroundSound,
                            contentDescription = "Dolby Atmos",
                            tint = if (dolbyAtmosEnabled) Color(0xFF0D0F1B) else Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Dolby Atmos® 3D Sound",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            text = "Spatial immersion and wide soundstage",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0x99FFFFFF)
                        )
                    }
                }

                Switch(
                    checked = dolbyAtmosEnabled,
                    onCheckedChange = onToggleDolbyAtmos,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = accentColor,
                        uncheckedTrackColor = Color(0x33FFFFFF)
                    ),
                    modifier = Modifier.testTag("dolby_atmos_switch")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SoundAlive Presets
            Text(
                text = "SOUND PROFILES",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = Color(0x99FFFFFF)
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SoundAlivePreset.entries.forEach { preset ->
                    val isSelected = preset == selectedPreset
                    Box(
                        modifier = Modifier
                            .clip(OneUIPillShape)
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(listOf(primaryColor, accentColor))
                                } else {
                                    Brush.linearGradient(listOf(Color(0x22FFFFFF), Color(0x11FFFFFF)))
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.White.copy(alpha = 0.5f) else Color(0x1AFFFFFF),
                                shape = OneUIPillShape
                            )
                            .clickable { onSelectPreset(preset) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("preset_${preset.name}")
                    ) {
                        Text(
                            text = preset.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isSelected) Color.White else Color(0xDDFFFFFF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3-Band Equalizer Dials / Sliders
            Text(
                text = "CUSTOM 3-BAND FREQUENCY TUNING",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = Color(0x99FFFFFF)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bass Slider
            BandSlider(
                label = "Bass (Low Freq)",
                value = bassValue,
                onValueChange = onBassChange,
                accentColor = accentColor,
                testTag = "eq_bass_slider"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mid Slider
            BandSlider(
                label = "Mid (Vocals & Presence)",
                value = midValue,
                onValueChange = onMidChange,
                accentColor = accentColor,
                testTag = "eq_mid_slider"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Treble Slider
            BandSlider(
                label = "Treble (High Sparkle)",
                value = trebleValue,
                onValueChange = onTrebleChange,
                accentColor = accentColor,
                testTag = "eq_treble_slider"
            )
        }
    }
}

@Composable
fun BandSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    accentColor: Color,
    testTag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphism(
                shape = OneUISquircleMedium,
                backgroundColor = Color(0x20FFFFFF),
                elevation = 0.dp
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xDDFFFFFF)
            )
            Text(
                text = "${if (value >= 0) "+" else ""}${String.format("%.1f", value)} dB",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = -6f..6f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color(0x33FFFFFF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .testTag(testTag)
        )
    }
}
