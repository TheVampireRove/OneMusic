package com.example.ui.diagnostics

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diagnostics.CrashReportItem
import com.example.diagnostics.CrashReportManager
import com.example.ui.theme.OneUIPillShape
import com.example.ui.theme.OneUISquircleLarge
import com.example.ui.theme.OneUISquircleMedium
import com.example.ui.theme.glassmorphism

/**
 * Crash Log Viewer & Exporter UI:
 * - Lists saved diagnostic crash files sorted newest first
 * - Scrollable full preview of stack trace & system info
 * - Export / Share sheet trigger via FileProvider & Intent.ACTION_SEND
 * - Delete All Logs & single report management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashReportsScreen(
    accentColor: Color,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var reports by remember { mutableStateOf<List<CrashReportItem>>(emptyList()) }
    var selectedReport by remember { mutableStateOf<CrashReportItem?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    fun refreshReports() {
        reports = CrashReportManager.getSavedCrashReports(context)
    }

    LaunchedEffect(Unit) {
        refreshReports()
    }

    BackHandler {
        if (selectedReport != null) {
            selectedReport = null
        } else {
            onBack()
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F101E),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Diagnostic Crash Reports",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = if (reports.isEmpty()) "No saved reports" else "${reports.size} report(s) captured",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0x99FFFFFF)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedReport != null) {
                                selectedReport = null
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.testTag("crash_reports_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { refreshReports() },
                        modifier = Modifier.testTag("crash_reports_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                    if (reports.isNotEmpty()) {
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier.testTag("crash_reports_delete_all_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Delete All Reports",
                                tint = Color(0xFFFF5252)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF16182B))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (reports.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0x18FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Crash Reports Recorded",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "When an unexpected crash occurs, diagnostic logs will automatically be saved here so you can review and export them.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0x99FFFFFF),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    FilledTonalButton(
                        onClick = {
                            CrashReportManager.generateTestReport(context)
                            refreshReports()
                            Toast.makeText(context, "Test report generated", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = accentColor.copy(alpha = 0.22f),
                            contentColor = accentColor
                        ),
                        shape = OneUIPillShape
                    ) {
                        Text("Generate Test Diagnostic Log")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reports, key = { it.fileName }) { item ->
                        CrashReportCard(
                            item = item,
                            accentColor = accentColor,
                            onOpenPreview = { selectedReport = item },
                            onShare = {
                                val shareIntent = CrashReportManager.createShareIntent(context, item.file)
                                context.startActivity(Intent.createChooser(shareIntent, "Export Diagnostic Crash Log"))
                            },
                            onDelete = {
                                CrashReportManager.deleteReport(item.file)
                                refreshReports()
                            }
                        )
                    }
                }
            }

            // Preview Modal Dialog
            selectedReport?.let { report ->
                ReportPreviewDialog(
                    report = report,
                    accentColor = accentColor,
                    onDismiss = { selectedReport = null },
                    onShare = {
                        val shareIntent = CrashReportManager.createShareIntent(context, report.file)
                        context.startActivity(Intent.createChooser(shareIntent, "Export Diagnostic Crash Log"))
                    }
                )
            }

            // Delete All Confirmation Dialog
            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("Delete All Crash Reports?", color = Color.White) },
                    text = {
                        Text(
                            "This will permanently delete ${reports.size} saved diagnostic crash report(s) from your device storage.",
                            color = Color(0xCCFFFFFF)
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                CrashReportManager.deleteAllReports(context)
                                refreshReports()
                                showDeleteConfirmDialog = false
                                Toast.makeText(context, "All crash reports deleted", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Delete All", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = false }) {
                            Text("Cancel", color = Color.White)
                        }
                    },
                    containerColor = Color(0xFF1E2038)
                )
            }
        }
    }
}

@Composable
private fun CrashReportCard(
    item: CrashReportItem,
    accentColor: Color,
    onOpenPreview: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(OneUISquircleMedium)
            .border(1.dp, Color(0x22FFFFFF), OneUISquircleMedium)
            .clickable { onOpenPreview() },
        colors = CardDefaults.cardColors(containerColor = Color(0x3517192C))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x25FF5252)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFFFF6D6D),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.fileName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.timestampFormatted} • ${(item.sizeBytes / 1024).coerceAtLeast(1)} KB",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0x99FFFFFF)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete",
                            tint = Color(0x88FFFFFF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Micro stack trace preview snippet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(OneUISquircleLarge)
                    .background(Color(0xFF0C0D18))
                    .padding(8.dp)
            ) {
                Text(
                    text = item.previewText.take(240) + if (item.previewText.length > 240) "..." else "",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        lineHeight = 12.sp
                    ),
                    color = Color(0xFFB0B4CE),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ReportPreviewDialog(
    report: CrashReportItem,
    accentColor: Color,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    val fullContent = remember(report) { CrashReportManager.readReportText(report.file) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Crash Diagnostics",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = report.fileName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0x88FFFFFF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                SelectionContainer(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(OneUISquircleMedium)
                        .background(Color(0xFF0A0B14))
                        .padding(10.dp)
                ) {
                    Text(
                        text = fullContent,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        ),
                        color = Color(0xFFCFD3E8),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Crash Report", fullContent))
                        Toast.makeText(context, "Full report copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", color = Color.White)
                }

                FilledTonalButton(
                    onClick = onShare,
                    shape = OneUIPillShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = accentColor,
                        contentColor = Color(0xFF0F101E)
                    )
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export / Share", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {},
        containerColor = Color(0xFF16182C)
    )
}
