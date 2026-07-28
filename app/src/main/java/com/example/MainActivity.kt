package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.BoldPrimary
import com.example.ui.theme.BoldBackground
import com.example.ui.theme.BoldTextPrimary
import com.example.ui.theme.BoldTextSecondary
import com.example.ui.theme.BoldSurfaceVar
import com.example.ui.theme.BoldActivePurple
import com.example.ui.theme.BoldTextAmethyst
import com.example.ui.theme.BoldSoftBlue
import com.example.ui.theme.BoldTextNavy
import com.example.ui.theme.BoldBorder
import com.example.ui.theme.BoldTerminalBg
import com.example.ui.theme.BoldTerminalHighlight
import com.example.ui.theme.BoldTerminalTxt
import com.example.ui.theme.BoldTerminalNeon
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: FolderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reactively refresh permission state when coming back from Settings
        viewModel.updatePermissionStatus()
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    viewModel: FolderViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val targetPath by viewModel.targetPath.collectAsStateWithLifecycle()
    val isDryRun by viewModel.isDryRun.collectAsStateWithLifecycle()
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val actions by viewModel.actions.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val showSystemPicker by viewModel.showSystemPicker.collectAsStateWithLifecycle()

    val BoldPrimary = when (currentTheme) {
        ThemeStyle.FOREST_MINT -> Color(0xFF2E6B48)
        ThemeStyle.ROYAL_AMETHYST -> Color(0xFF6200EE)
        ThemeStyle.NORDIC_OCEAN -> Color(0xFF0284C7)
        ThemeStyle.SUNSET_AMBER -> Color(0xFFD97706)
        ThemeStyle.CRIMSON_CHERRY -> Color(0xFFDC2626)
        ThemeStyle.DEEP_SAPPHIRE -> Color(0xFF1D4ED8)
        ThemeStyle.RED_PEACH -> Color(0xFFE05A47)
    }

    val BoldBackground = when (currentTheme) {
        ThemeStyle.FOREST_MINT -> Color(0xFFF4FBF5)
        ThemeStyle.ROYAL_AMETHYST -> Color(0xFFFBF8FF)
        ThemeStyle.NORDIC_OCEAN -> Color(0xFFF0F9FF)
        ThemeStyle.SUNSET_AMBER -> Color(0xFFFFFBEB)
        ThemeStyle.CRIMSON_CHERRY -> Color(0xFFFEF2F2)
        ThemeStyle.DEEP_SAPPHIRE -> Color(0xFFEFF6FF)
        ThemeStyle.RED_PEACH -> Color(0xFFFFF5F2)
    }

    val BoldTextPrimary = when (currentTheme) {
        ThemeStyle.FOREST_MINT -> Color(0xFF181D19)
        ThemeStyle.ROYAL_AMETHYST -> Color(0xFF1C0D30)
        ThemeStyle.NORDIC_OCEAN -> Color(0xFF0C4A6E)
        ThemeStyle.SUNSET_AMBER -> Color(0xFF78350F)
        ThemeStyle.CRIMSON_CHERRY -> Color(0xFF7F1D1D)
        ThemeStyle.DEEP_SAPPHIRE -> Color(0xFF1E3A8A)
        ThemeStyle.RED_PEACH -> Color(0xFF5E2218)
    }

    val BoldTextSecondary = when (currentTheme) {
        ThemeStyle.FOREST_MINT -> Color(0xFF414942)
        ThemeStyle.ROYAL_AMETHYST -> Color(0xFF564966)
        ThemeStyle.NORDIC_OCEAN -> Color(0xFF334155)
        ThemeStyle.SUNSET_AMBER -> Color(0xFF78716C)
        ThemeStyle.CRIMSON_CHERRY -> Color(0xFF991B1B)
        ThemeStyle.DEEP_SAPPHIRE -> Color(0xFF1E40AF)
        ThemeStyle.RED_PEACH -> Color(0xFF8B5147)
    }

    val BoldSurfaceVar = when (currentTheme) {
        ThemeStyle.FOREST_MINT -> Color(0xFFE0EFE3)
        ThemeStyle.ROYAL_AMETHYST -> Color(0xFFF0E5FC)
        ThemeStyle.NORDIC_OCEAN -> Color(0xFFE0F2FE)
        ThemeStyle.SUNSET_AMBER -> Color(0xFFFEF3C7)
        ThemeStyle.CRIMSON_CHERRY -> Color(0xFFFEE2E2)
        ThemeStyle.DEEP_SAPPHIRE -> Color(0xFFDBEAFE)
        ThemeStyle.RED_PEACH -> Color(0xFFFFE5DE)
    }

    val BoldActivePurple = when (currentTheme) {
        ThemeStyle.FOREST_MINT -> Color(0xFFC7EED0)
        ThemeStyle.ROYAL_AMETHYST -> Color(0xFFE4D3FC)
        ThemeStyle.NORDIC_OCEAN -> Color(0xFF7DD3FC)
        ThemeStyle.SUNSET_AMBER -> Color(0xFFFCD34D)
        ThemeStyle.CRIMSON_CHERRY -> Color(0xFFFECACA)
        ThemeStyle.DEEP_SAPPHIRE -> Color(0xFFBFDBFE)
        ThemeStyle.RED_PEACH -> Color(0xFFFFD1C4)
    }

    val BoldTextAmethyst = when (currentTheme) {
        ThemeStyle.FOREST_MINT -> Color(0xFF0D2517)
        ThemeStyle.ROYAL_AMETHYST -> Color(0xFF320094)
        ThemeStyle.NORDIC_OCEAN -> Color(0xFF0369A1)
        ThemeStyle.SUNSET_AMBER -> Color(0xFF92400E)
        ThemeStyle.CRIMSON_CHERRY -> Color(0xFF450A0A)
        ThemeStyle.DEEP_SAPPHIRE -> Color(0xFF172554)
        ThemeStyle.RED_PEACH -> Color(0xFF7D2214)
    }

    val BoldSoftBlue = when (currentTheme) {
        ThemeStyle.FOREST_MINT -> Color(0xFFD2E8DA)
        ThemeStyle.ROYAL_AMETHYST -> Color(0xFFE8E0FF)
        ThemeStyle.NORDIC_OCEAN -> Color(0xFFE0F2FE)
        ThemeStyle.SUNSET_AMBER -> Color(0xFFFEF3C7)
        ThemeStyle.CRIMSON_CHERRY -> Color(0xFFFEE2E2)
        ThemeStyle.DEEP_SAPPHIRE -> Color(0xFFDBEAFE)
        ThemeStyle.RED_PEACH -> Color(0xFFFFE5DE)
    }

    val BoldTextNavy = when (currentTheme) {
        ThemeStyle.FOREST_MINT -> Color(0xFF051C0E)
        ThemeStyle.ROYAL_AMETHYST -> Color(0xFF12003D)
        ThemeStyle.NORDIC_OCEAN -> Color(0xFF0B3B59)
        ThemeStyle.SUNSET_AMBER -> Color(0xFF451A03)
        ThemeStyle.CRIMSON_CHERRY -> Color(0xFF450A0A)
        ThemeStyle.DEEP_SAPPHIRE -> Color(0xFF172554)
        ThemeStyle.RED_PEACH -> Color(0xFF5E2218)
    }

    val BoldBorder = when (currentTheme) {
        ThemeStyle.FOREST_MINT -> Color(0xFFB1C9B7)
        ThemeStyle.ROYAL_AMETHYST -> Color(0xFFD8C4F6)
        ThemeStyle.NORDIC_OCEAN -> Color(0xFFBAE6FD)
        ThemeStyle.SUNSET_AMBER -> Color(0xFFFDE68A)
        ThemeStyle.CRIMSON_CHERRY -> Color(0xFFFCA5A5)
        ThemeStyle.DEEP_SAPPHIRE -> Color(0xFF93C5FD)
        ThemeStyle.RED_PEACH -> Color(0xFFF7C2B7)
    }

    val BoldTerminalBg = BoldBackground
    val BoldTerminalHighlight = BoldPrimary
    val BoldTerminalTxt = BoldTextPrimary
    val BoldTerminalNeon = when (currentTheme) {
        ThemeStyle.FOREST_MINT -> Color(0xFF137333)
        ThemeStyle.ROYAL_AMETHYST -> Color(0xFF8A00FF)
        ThemeStyle.NORDIC_OCEAN -> Color(0xFF0EA5E9)
        ThemeStyle.SUNSET_AMBER -> Color(0xFFEA580C)
        ThemeStyle.CRIMSON_CHERRY -> Color(0xFFDC2626)
        ThemeStyle.DEEP_SAPPHIRE -> Color(0xFF2563EB)
        ThemeStyle.RED_PEACH -> Color(0xFFE05A47)
    }

    val terminalListState = rememberLazyListState()

    var showBrowser by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showInternalFolderPicker by remember { mutableStateOf(false) }

    var isValidDirectory by remember { mutableStateOf(false) }
    val childrenDirs = remember { mutableStateListOf<File>() }

    LaunchedEffect(targetPath) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val d = File(targetPath)
                val valid = d.exists() && d.isDirectory
                val subdirs = if (valid) {
                    d.listFiles { f -> f.isDirectory }
                        ?.sortedBy { it.name.lowercase() } ?: emptyList()
                } else {
                    emptyList()
                }
                valid to subdirs
            } catch (e: Exception) {
                false to emptyList<File>()
            }
        }.let { (valid, subdirs) ->
            isValidDirectory = valid
            childrenDirs.clear()
            childrenDirs.addAll(subdirs)
        }
    }

    // Scroll automatically to end of terminal logs when new logs are added
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            terminalListState.animateScrollToItem(logs.size - 1)
        }
    }

    // Document tree selector launcher
    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                // Ignore if not supportable
            }
            
            val resolvedPath = getPathFromTreeUri(context, uri)
            if (resolvedPath != null) {
                viewModel.setTargetPath(resolvedPath)
                Toast.makeText(context, "Resolved path: $resolvedPath", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not resolve absolute folder path", Toast.LENGTH_LONG).show()
                viewModel.setTargetPath(uri.toString())
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.updatePermissionStatus()
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(context, "Permissions granted successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Storage permissions are required to scan custom directories.", Toast.LENGTH_LONG).show()
        }
    }

    // Confirmation Alert Definition themed in Bold/Purple style
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isDryRun) Icons.Default.Search else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isDryRun) BoldPrimary else Color(0xFFEF4444)
                    )
                    Text(
                        text = if (isDryRun) "Confirm Safe Preview" else "Confirm Folder Flattening",
                        fontWeight = FontWeight.Bold,
                        color = BoldTextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "You are about to scan and untangle:",
                        color = BoldTextSecondary
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BoldSurfaceVar, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = targetPath,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = BoldTextPrimary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isDryRun) {
                        Text(
                            text = "⏩ Safe Preview Mode is active. The engine will inspect files and show results safely in the log panel. Your actual files will not be touched or changed.",
                            fontSize = 12.sp,
                            color = BoldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = "⚠ WARNING: Live Mode is active! This operation modifies files physically:\n" +
                                   "• Files in subfolders are promoted up into parent folder.\n" +
                                   "• Name conflicts are resolved automatically by adding custom numbering.\n" +
                                   "• Redundant empty containers are deleted permanently.",
                            fontSize = 12.sp,
                            color = Color(0xFFB91C1C),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.runUntangler()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDryRun) BoldPrimary else Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (isDryRun) "Safe Preview" else "Clean Live",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmDialog = false },
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, BoldBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BoldTextSecondary)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // MAIN BOLD SCAFFOLD THEME WRAPPING APP
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BoldBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. THEMED BOLD HEADER TITLE CONTROLS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FOLDER",
                        style = androidx.compose.ui.text.TextStyle(
                            color = BoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Flattener",
                        style = androidx.compose.ui.text.TextStyle(
                            fontWeight = FontWeight.Black,
                            color = BoldTextPrimary,
                            fontSize = 32.sp,
                            letterSpacing = (-0.5).sp
                        )
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "By BlazeFTL",
                        style = androidx.compose.ui.text.TextStyle(
                            color = BoldTextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                    // Settings Icon Button
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BoldActivePurple)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Theme Preferences Configuration Dialog",
                            tint = BoldTextAmethyst,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // --- 2. STORAGE PERMISSION WARNING DOCK ---
            if (!hasPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECEF)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Missing permissions attention label",
                                tint = Color(0xFFEF4444)
                            )
                            Text(
                                text = "Permission Required",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            )
                        }

                        Text(
                            text = "To allow full automated directory flattening on direct directories such as IDMP folders, Android 11+ requires granting 'All Files Access' permission.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF7F1D1D))
                        )

                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    try {
                                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                        context.startActivity(intent)
                                    }
                                } else {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        )
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BoldPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Grant All Files Access", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- 3. TARGET DIRECTORY PRESET & SELECTION CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BoldSurfaceVar),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(BoldActivePurple, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = BoldTextAmethyst
                            )
                        }

                        Column {
                            Text(
                                text = "Target Directory",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = BoldTextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = targetPath.ifBlank { "No path selected" },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BoldTextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Folder Path selector (Clicking opens folder picker directly without keyboard)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showInternalFolderPicker = true }
                    ) {
                        OutlinedTextField(
                            value = targetPath,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Target Folder Path") },
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = BoldTextPrimary, fontWeight = FontWeight.Bold),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = BoldTextPrimary,
                                disabledBorderColor = BoldBorder,
                                disabledContainerColor = Color.White,
                                disabledLabelColor = BoldTextSecondary,
                                disabledLeadingIconColor = BoldPrimary,
                                disabledTrailingIconColor = BoldTextSecondary
                            ),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = "Select Folder",
                                    tint = BoldPrimary
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Pick from device folder
                    Button(
                        onClick = { showInternalFolderPicker = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BoldPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Select Folder",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Fallback system picker
                    if (showSystemPicker) {
                        Button(
                            onClick = { directoryPickerLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BoldSurfaceVar,
                                contentColor = BoldPrimary
                            ),
                            border = BorderStroke(1.5.dp, BoldBorder),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Use System Storage Picker (External/SD)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- 4. ENGINE SETTINGS & RUN DOCK ---
            if (isDryRun) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BoldSurfaceVar),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Safe Preview Mode",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BoldTextPrimary
                                    )
                                )
                                Text(
                                    text = "Inspects folders for nesting flattener candidates cleanly without altering any of your actual storage files.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = BoldTextSecondary)
                                )
                            }

                            Switch(
                                checked = isDryRun,
                                onCheckedChange = { viewModel.toggleDryRun(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = BoldPrimary,
                                    uncheckedThumbColor = BoldTextSecondary,
                                    uncheckedTrackColor = BoldBorder
                                )
                            )
                        }

                        HorizontalDivider(color = BoldBorder.copy(alpha = 0.4f), thickness = 1.dp)

                        // TRIGGER ACTIONS ROW
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { showConfirmDialog = true },
                                enabled = !isRunning && targetPath.isNotEmpty(),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BoldPrimary,
                                    disabledContainerColor = BoldBorder
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                if (isRunning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Running Clean...", fontSize = 14.sp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Preview Clean",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Button(
                    onClick = { showConfirmDialog = true },
                    enabled = !isRunning && targetPath.isNotEmpty(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BoldPrimary,
                        disabledContainerColor = BoldBorder
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Running Clean...", fontSize = 14.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Run Live Clean",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // --- 5. REDESIGNED BOLD STATS DATA GRID ---
            AnimatedVisibility(
                visible = summary != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                summary?.let { sum ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Results",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BoldTextPrimary
                            )
                        )

                        // 4 Cards Grid - Light amethyst and soft blue accents
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Checked Card
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(BoldActivePurple, RoundedCornerShape(20.dp))
                                        .padding(16.dp)
                                ) {
                                    Text("PROCESSED", fontSize = 10.sp, color = BoldTextAmethyst, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        sum.foldersProcessed.toString(),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            color = BoldTextAmethyst,
                                            fontSize = 24.sp
                                        )
                                    )
                                }

                                // Unnested Card
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(BoldSoftBlue, RoundedCornerShape(20.dp))
                                        .padding(16.dp)
                                ) {
                                    Text("UNNESTED", fontSize = 10.sp, color = BoldTextNavy, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        sum.foldersFlattened.toString(),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            color = BoldTextNavy,
                                            fontSize = 24.sp
                                        )
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Files Moved Up Card
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0xFFE8DEF8), RoundedCornerShape(20.dp))
                                        .padding(16.dp)
                                ) {
                                    Text("FILES MOVED UP", fontSize = 10.sp, color = BoldTextAmethyst, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        sum.filesPromoted.toString(),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            color = BoldTextAmethyst,
                                            fontSize = 24.sp
                                        )
                                    )
                                }

                                // Folders Deleted Card
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White, RoundedCornerShape(20.dp))
                                        .border(1.dp, BoldBorder, RoundedCornerShape(20.dp))
                                        .padding(16.dp)
                                ) {
                                    Text("FOLDERS DELETED", fontSize = 10.sp, color = BoldTextSecondary, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        sum.foldersDeleted.toString(),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            color = BoldTextPrimary,
                                            fontSize = 24.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- 6. MODERN MATERIAL 3 LOG CONSOLE PANEL ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, BoldActivePurple, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCF8FF)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column {
                    // Header of Console
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3EDF7))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = BoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Logs",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = BoldTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isDryRun && logs.isNotEmpty()) {
                                Surface(
                                    color = BoldActivePurple,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(
                                        "PREVIEW ONLY",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = BoldTextAmethyst,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            
                            // Copy button
                            IconButton(
                                onClick = {
                                    if (logs.isNotEmpty()) {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Folder Flattener Logs", logs.joinToString("\n"))
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Logs copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "No logs to copy", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy logs to clipboard",
                                    tint = BoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Clear button
                            IconButton(
                                onClick = {
                                    viewModel.clearLogs()
                                    Toast.makeText(context, "Logs cleared", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Clear logs list",
                                    tint = Color(0xFFBA1A1A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Core console body
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(Color.White)
                            .padding(12.dp)
                    ) {
                        if (logs.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Rule,
                                    contentDescription = null,
                                    tint = BoldBorder.copy(alpha = 0.8f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Ready to inspect target folder.\nChoose a target and run a clean sweep.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = BoldTextSecondary,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )
                                )
                            }
                        } else {
                            LazyColumn(
                                state = terminalListState,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(logs) { log ->
                                    val (color, icon) = when {
                                        log.contains("ERROR:") || log.contains("❌") -> 
                                            Color(0xFFB3261E) to Icons.Default.Error
                                        log.contains("✔") || log.contains("Success") || log.contains("Successfully") -> 
                                            Color(0xFF137333) to Icons.Default.CheckCircle
                                        log.contains("✨") -> 
                                            Color(0xFF137333) to Icons.Default.Stars
                                        log.contains("🗑") || log.contains("Deleted") -> 
                                            Color(0xFFB06000) to Icons.Default.Delete
                                        log.contains("⏩") || log.contains("SAFE PREVIEW") -> 
                                            Color(0xFF7D5260) to Icons.Default.RemoveRedEye
                                        log.contains("⚡") || log.contains("LIVE") -> 
                                            Color(0xFF0F52BA) to Icons.Default.ElectricBolt
                                        else -> 
                                            BoldTextPrimary to Icons.Default.ChevronRight
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(color.copy(alpha = 0.05f))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = color,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = log,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 13.sp,
                                                color = if (color == BoldTextPrimary) BoldTextPrimary else color,
                                                fontWeight = if (color == BoldTextPrimary) FontWeight.Normal else FontWeight.Medium
                                            ),
                                            lineHeight = 16.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } // Closes Column
    }

    // Branding and adaptive settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Column {
                    Text("Flattener Settings Options", fontWeight = FontWeight.Bold, color = BoldTextPrimary)
                    Text(
                        text = "By BlazeFTL",
                        style = androidx.compose.ui.text.TextStyle(
                            color = BoldTextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Customize the operations behavior of the flattener utility below:",
                        fontSize = 13.sp,
                        color = BoldTextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enforce Safe Preview Mode", fontSize = 13.sp, color = BoldTextPrimary, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = isDryRun,
                            onCheckedChange = { viewModel.toggleDryRun(it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = BoldPrimary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show System Storage Picker", fontSize = 13.sp, color = BoldTextPrimary, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = showSystemPicker,
                            onCheckedChange = { viewModel.toggleSystemPicker(it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = BoldPrimary)
                        )
                    }

                    HorizontalDivider(color = BoldBorder.copy(alpha = 0.5f))

                    Text("App Theme Style", fontSize = 13.sp, color = BoldTextPrimary, fontWeight = FontWeight.Bold)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ThemeStyle.values().toList().chunked(3).forEach { rowStyles ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowStyles.forEach { style ->
                                    val isSelected = style == currentTheme
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) BoldPrimary else BoldSurfaceVar)
                                            .clickable { viewModel.setThemeStyle(style) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (style) {
                                                ThemeStyle.FOREST_MINT -> "Mint"
                                                ThemeStyle.ROYAL_AMETHYST -> "Amethyst"
                                                ThemeStyle.NORDIC_OCEAN -> "Ocean"
                                                ThemeStyle.SUNSET_AMBER -> "Amber"
                                                ThemeStyle.CRIMSON_CHERRY -> "Crimson"
                                                ThemeStyle.DEEP_SAPPHIRE -> "Sapphire"
                                                ThemeStyle.RED_PEACH -> "Peach"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else BoldTextPrimary
                                        )
                                    }
                                }
                                if (rowStyles.size < 3) {
                                    repeat(3 - rowStyles.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = BoldBorder.copy(alpha = 0.5f))

                    Text(
                        text = "App version: v1.1.0-bold\nTheme: Bold Typography style\nMaterial 3 design layout specifications",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = BoldTextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BoldPrimary)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }


    // Full screen folder picker overlay using the entire UI
    AnimatedVisibility(
        visible = showInternalFolderPicker,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        FolderPickerSelectionDialog(
            initialPath = targetPath,
            onDismiss = { showInternalFolderPicker = false },
            onFolderSelected = { selectedPath ->
                viewModel.setTargetPath(selectedPath)
            }
        )
    }

}

// Helper methods to resolve DocumentTree Android Document URIs to clean absolute paths
fun getPathFromTreeUri(context: Context, uri: Uri): String? {
    val uriString = uri.toString()
    if (uri.authority == "com.android.providers.downloads.documents") {
        val documentId = try {
            if (android.provider.DocumentsContract.isTreeUri(uri)) {
                android.provider.DocumentsContract.getTreeDocumentId(uri)
            } else {
                android.provider.DocumentsContract.getDocumentId(uri)
            }
        } catch (e: Exception) {
            ""
        }
        if (documentId.startsWith("raw:")) {
            return documentId.substring(4)
        }
        if (documentId.startsWith("msf:") || documentId == "downloads" || documentId.isBlank()) {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        }
        val split = documentId.split(":")
        if (split.size >= 2) {
            val relativePath = split[1]
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/" + relativePath
        }
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    }
    if (uriString.startsWith("content://com.android.providers.externalstorage.documents/tree/")) {
        val treeId = Uri.decode(uriString.substringAfter("/tree/").substringBefore("/document/"))
        val split = treeId.split(":")
        if (split.size >= 2) {
            val type = split[0]
            val relativePath = split[1]
            if (type.equals("primary", ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().absolutePath + "/" + relativePath
            } else {
                return "/storage/$type/$relativePath"
            }
        } else if (split.size == 1) {
            val type = split[0]
            if (type.equals("primary", ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().absolutePath
            }
        }
    }
    return getPathFromDocUri(context, uri)
}

fun getPathFromDocUri(context: Context, uri: Uri): String? {
    if (uri.scheme == "file") {
        return uri.path
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        val docId = try {
            if (android.provider.DocumentsContract.isTreeUri(uri)) {
                android.provider.DocumentsContract.getTreeDocumentId(uri)
            } else {
                android.provider.DocumentsContract.getDocumentId(uri)
            }
        } catch (e: Exception) {
            return null
        }
        val split = docId.split(":")
        val type = split.getOrNull(0)
        val relativePath = split.getOrNull(1)

        if ("primary".equals(type, ignoreCase = true)) {
            val baseDir = Environment.getExternalStorageDirectory().absolutePath
            return if (relativePath != null) {
                "$baseDir/$relativePath"
            } else {
                baseDir
            }
        } else {
            if (type != null && relativePath != null) {
                return "/storage/$type/$relativePath"
            }
        }
    }
    return null
}

@Composable
fun FolderPickerSelectionDialog(
    initialPath: String,
    onDismiss: () -> Unit,
    onFolderSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var currentPath by remember { mutableStateOf(if (initialPath.isNotBlank() && File(initialPath).exists()) initialPath else "/storage/emulated/0") }
    
    val canGoBack = currentPath != "/storage" && currentPath != "/"
    androidx.activity.compose.BackHandler(enabled = true) {
        if (canGoBack) {
            val parent = File(currentPath).parentFile
            if (parent != null) {
                currentPath = parent.absolutePath
            } else {
                onDismiss()
            }
        } else {
            onDismiss()
        }
    }

    val currentDir = File(currentPath)
    val subfolders = remember(currentPath) {
        try {
            if (currentPath == "/storage" || currentPath == "/") {
                val list = mutableListOf<File>()
                
                // Add Internal Storage
                val internalDir = File("/storage/emulated/0")
                if (internalDir.exists() && internalDir.isDirectory) {
                    list.add(internalDir)
                } else {
                    val sdcard = File("/sdcard")
                    if (sdcard.exists()) list.add(sdcard)
                }
                
                // Add via StorageManager for modern API levels (R+)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    val sm = context.getSystemService(android.os.storage.StorageManager::class.java)
                    sm?.storageVolumes?.forEach { volume ->
                        try {
                            val method = volume.javaClass.getMethod("getDirectory")
                            val dir = method.invoke(volume) as? File
                            if (dir != null && dir.exists() && dir.isDirectory) {
                                if (!list.any { it.absolutePath == dir.absolutePath }) {
                                    list.add(dir)
                                }
                            }
                        } catch (e: Exception) {
                            // suppress
                        }
                    }
                }
                
                // Add via context.getExternalFilesDirs to discover external SD cards and USB drives
                context.getExternalFilesDirs(null)?.forEach { file ->
                    if (file != null) {
                        val path = file.absolutePath
                        val idx = path.indexOf("/Android")
                        if (idx > 0) {
                            val rootPath = path.substring(0, idx)
                            val rootFile = File(rootPath)
                            if (rootFile.exists() && rootFile.isDirectory) {
                                if (!list.any { it.absolutePath == rootFile.absolutePath }) {
                                    list.add(rootFile)
                                }
                            }
                        }
                    }
                }
                
                // Try to scan standard /storage directory
                File("/storage").listFiles()?.forEach { file ->
                    if (file.isDirectory && file.name != "self" && file.name != "emulated") {
                        if (!list.any { it.absolutePath == file.absolutePath }) {
                            list.add(file)
                        }
                    }
                }
                
                list.distinctBy { it.absolutePath }.sortedBy { it.name.lowercase() }
            } else {
                currentDir.listFiles { file -> file.isDirectory }
                    ?.sortedBy { it.name.lowercase() } ?: emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BoldBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BoldSurfaceVar)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Picker",
                            tint = BoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Select Any Folder",
                        fontWeight = FontWeight.Bold,
                        color = BoldTextPrimary,
                        fontSize = 18.sp
                    )
                }

                IconButton(
                    onClick = {
                        currentPath = "/storage/emulated/0"
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BoldSurfaceVar)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Go to Storage Root Home",
                        tint = BoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Current Path Info Row with Back Navigation
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BoldSurfaceVar),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (currentPath != "/storage" && currentPath != "/") {
                        IconButton(
                            onClick = {
                                if (currentPath == "/storage/emulated/0") {
                                    currentPath = "/storage"
                                } else {
                                    val parent = File(currentPath).parentFile
                                    if (parent != null && parent.absolutePath.startsWith("/storage")) {
                                        currentPath = parent.absolutePath
                                    } else {
                                        currentPath = "/storage"
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BoldPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Navigate to Parent Directory",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = when (currentPath) {
                            "/storage" -> "Storage Devices / Roots"
                            else -> currentPath.replace("/storage/emulated/0", "Internal Shared Storage")
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = BoldTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Create New Folder Accent Button
            if (currentPath != "/storage" && currentPath != "/") {
                OutlinedButton(
                    onClick = { showCreateFolderDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BoldPrimary.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BoldPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create New Subfolder Here", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Main folders list
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.5.dp, BoldBorder, RoundedCornerShape(16.dp))
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(6.dp)
            ) {
                if (subfolders.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = BoldBorder.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Subfolders Allowed or Found",
                            color = BoldTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(subfolders) { folder ->
                            val displayName = if (currentPath == "/storage" || currentPath == "/") {
                                when {
                                    folder.absolutePath.endsWith("/emulated/0") || folder.name == "0" -> "Internal Shared Storage"
                                    folder.name == "emulated" -> "Internal Shared Storage"
                                    folder.name == "self" -> "System Link Storage"
                                    else -> "External SD Card (${folder.name})"
                                }
                            } else {
                                folder.name
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BoldSurfaceVar.copy(alpha = 0.3f))
                                    .clickable {
                                        if (currentPath == "/storage" || currentPath == "/") {
                                            if (folder.name == "emulated") {
                                                currentPath = "/storage/emulated/0"
                                            } else {
                                                currentPath = folder.absolutePath
                                            }
                                        } else {
                                            currentPath = folder.absolutePath
                                        }
                                    }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = "Folder",
                                    tint = BoldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = displayName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BoldTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Open",
                                    tint = BoldTextSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Actions Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BoldBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BoldTextSecondary),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Button(
                    onClick = {
                        onFolderSelected(currentPath)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BoldPrimary),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1.5f),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text("Select This Folder", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                }
            }
        }
    }

    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("New Folder", fontWeight = FontWeight.Bold, color = BoldTextPrimary, fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name") },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BoldPrimary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            val newDir = File(currentPath, newFolderName.trim())
                            try {
                                if (newDir.mkdirs()) {
                                    currentPath = newDir.absolutePath
                                    newFolderName = ""
                                    showCreateFolderDialog = false
                                    Toast.makeText(context, "Folder created", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Could not create folder", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BoldPrimary)
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
