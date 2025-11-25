package com.dolphin.jetpack.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3ShowcaseScreen(
    onBackClick: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<ShowcaseCategory?>(null) }

    if (selectedCategory != null) {
        ComponentDetailScreen(
            category = selectedCategory!!,
            onBackClick = { selectedCategory = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Material3 Showcase",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Material Design 3 (M3) is the latest version of Google's open-source design system.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(ShowcaseCategory.values()) { category ->
                    ShowcaseCategoryCard(
                        category = category,
                        onClick = { selectedCategory = category }
                    )
                }
            }
        }
    }
}

@Composable
fun ShowcaseCategoryCard(
    category: ShowcaseCategory,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentDetailScreen(
    category: ShowcaseCategory,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // TopAppBar as a direct component (not in Scaffold)
        TopAppBar(
            title = {
                Text(
                    category.title,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when (category) {
                ShowcaseCategory.BUTTONS -> item { ButtonsDemo() }
                ShowcaseCategory.TEXT_FIELDS -> item { TextFieldsDemo() }
                ShowcaseCategory.SLIDERS -> item { SlidersDemo() }
                ShowcaseCategory.CHIPS -> item { ChipsDemo() }
                ShowcaseCategory.CARDS -> item { CardsDemo() }
                ShowcaseCategory.DIALOGS -> item { DialogsDemo() }
                ShowcaseCategory.PROGRESS_INDICATORS -> item { ProgressIndicatorsDemo() }
                ShowcaseCategory.NAVIGATION -> item { NavigationDemo() }
                ShowcaseCategory.ANIMATION -> item { AnimationDemo() }
                ShowcaseCategory.PALETTE -> item { PaletteDemo() }
                ShowcaseCategory.FONTS -> item { FontsDemo() }
                ShowcaseCategory.BADGES -> item { BadgesDemo() }
                ShowcaseCategory.TOOLTIPS -> item { TooltipsDemo() }
            }
        }
    }
}

// ========== BUTTONS DEMO ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonsDemo() {
    var currentButtonIndex by remember { mutableIntStateOf(0) }
    var showCode by remember { mutableStateOf(false) }

    val buttonTypes = listOf(
        "Elevated Button", "Filled Button", "Filled Tonal Button", "Outlined Button",
        "Text Button", "Radio Button", "Switch Button", "Single Choice Segmented Button",
        "Multi Choice Segmented Button", "Icon Button", "Filled Icon Button",
        "Filled Tonal Icon Button", "Outlined Icon Button", "Icon Toggle Button",
        "Filled Icon Toggle Button", "Filled Tonal Icon Toggle Button", "Outlined Icon Toggle Button"
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Row 1: Navigation buttons only (Back | Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button (with circular navigation)
            IconButton(
                onClick = {
                    showCode = false // Reset code view when navigating
                    currentButtonIndex = if (currentButtonIndex > 0) {
                        currentButtonIndex - 1
                    } else {
                        buttonTypes.size - 1 // Go to last button
                    }
                }
            ) {
                Icon(Icons.Default.ArrowBack, "Previous")
            }

            // Next button (with circular navigation)
            IconButton(
                onClick = {
                    showCode = false // Reset code view when navigating
                    currentButtonIndex = if (currentButtonIndex < buttonTypes.size - 1) {
                        currentButtonIndex + 1
                    } else {
                        0 // Go back to first button
                    }
                }
            ) {
                Icon(Icons.Default.ArrowForward, "Next")
            }
        }

        // Row 2: Component heading and code toggle button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buttonTypes[currentButtonIndex],
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Code view toggle button
            IconButton(onClick = { showCode = !showCode }) {
                Icon(
                    if (showCode) Icons.Default.Visibility else Icons.Default.Code,
                    contentDescription = if (showCode) "Show Demo" else "Show Code"
                )
            }
        }

        // Show code view if toggled
        AnimatedVisibility(
            visible = showCode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CodeView(buttonType = buttonTypes[currentButtonIndex])
        }

        // Always show button demo (below code if code is visible)
        when (currentButtonIndex) {
            0 -> ElevatedButtonDemo()
            1 -> FilledButtonDemo()
            2 -> FilledTonalButtonDemo()
            3 -> OutlinedButtonDemo()
            4 -> TextButtonDemo()
            5 -> RadioButtonDemo()
            6 -> SwitchButtonDemo()
            7 -> SingleChoiceSegmentedButtonDemo()
            8 -> MultiChoiceSegmentedButtonDemo()
            9 -> IconButtonDemo()
            10 -> FilledIconButtonDemo()
            11 -> FilledTonalIconButtonDemo()
            12 -> OutlinedIconButtonDemo()
            13 -> IconToggleButtonDemo()
            14 -> FilledIconToggleButtonDemo()
            15 -> FilledTonalIconToggleButtonDemo()
            16 -> OutlinedIconToggleButtonDemo()
        }
    }
}

@Composable
fun ElevatedButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }
    var selectedElevation by remember { mutableStateOf("Level1 (1.0.dp)") }
    var selectedDisabledElevation by remember { mutableStateOf("Level1 (1.0.dp)") }

    // Convert selected elevation strings to dp values
    val defaultElevation = when (selectedElevation) {
        "Level1 (1.0.dp)" -> 1.dp
        "Level2 (2.0.dp)" -> 2.dp
        "Level3 (3.0.dp)" -> 3.dp
        "Level4 (4.0.dp)" -> 4.dp
        "Level5 (5.0.dp)" -> 5.dp
        else -> 1.dp
    }

    val disabledElevation = when (selectedDisabledElevation) {
        "Level1 (1.0.dp)" -> 1.dp
        "Level2 (2.0.dp)" -> 2.dp
        "Level3 (3.0.dp)" -> 3.dp
        "Level4 (4.0.dp)" -> 4.dp
        "Level5 (5.0.dp)" -> 5.dp
        else -> 1.dp
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Button preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                ElevatedButton(
                    onClick = { },
                    enabled = isEnabled,
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = defaultElevation,
                        disabledElevation = disabledElevation
                    )
                ) {
                    Text("Elevated Button")
                }
            }
        }

        // Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }

        ElevationDropdown(
            label = "Default Elevation :",
            selected = selectedElevation,
            onSelect = { selectedElevation = it }
        )

        ElevationDropdown(
            label = "Disabled Elevation :",
            selected = selectedDisabledElevation,
            onSelect = { selectedDisabledElevation = it }
        )
    }
}

@Composable
fun FilledButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { },
                    enabled = isEnabled
                ) {
                    Text("Button")
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }
    }
}

@Composable
fun FilledTonalButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                FilledTonalButton(
                    onClick = { },
                    enabled = isEnabled
                ) {
                    Text("FilledTonal Button")
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }
    }
}

@Composable
fun OutlinedButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = { },
                    enabled = isEnabled
                ) {
                    Text("Outlined Button")
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }
    }
}

@Composable
fun TextButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { },
                    enabled = isEnabled
                ) {
                    Text("textButton")
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }
    }
}

@Composable
fun RadioButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }
    var selected by remember { mutableIntStateOf(1) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    RadioButton(
                        selected = selected == 1,
                        onClick = { selected = 1 },
                        enabled = isEnabled
                    )
                    RadioButton(
                        selected = selected == 2,
                        onClick = { selected = 2 },
                        enabled = isEnabled
                    )
                    RadioButton(
                        selected = selected == 3,
                        onClick = { selected = 3 },
                        enabled = isEnabled
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }
    }
}

@Composable
fun SwitchButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }
    var isChecked by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Switch(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
                    enabled = isEnabled
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleChoiceSegmentedButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf("First", "Second", "Third")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                SingleChoiceSegmentedButtonRow {
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            shape = SegmentedButtonDefaults.itemShape(index, options.size),
                            enabled = isEnabled,
                            icon = {
                                if (selectedIndex == index) {
                                    Icon(
                                        Icons.Default.Done,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiChoiceSegmentedButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }
    var selectedOptions by remember { mutableStateOf(setOf(0)) }
    val options = listOf(
        "Favorites" to Icons.Default.Star,
        "Trending" to Icons.Default.TrendingUp,
        "Saved" to Icons.Default.Bookmark
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                MultiChoiceSegmentedButtonRow {
                    options.forEachIndexed { index, (label, icon) ->
                        SegmentedButton(
                            checked = index in selectedOptions,
                            onCheckedChange = {
                                selectedOptions = if (index in selectedOptions) {
                                    selectedOptions - index
                                } else {
                                    selectedOptions + index
                                }
                            },
                            shape = SegmentedButtonDefaults.itemShape(index, options.size),
                            enabled = isEnabled,
                            icon = {
                                SegmentedButtonDefaults.Icon(active = index in selectedOptions) {
                                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }
    }
}

@Composable
fun IconButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { },
                    enabled = isEnabled
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }
    }
}

@Composable
fun FilledIconButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                FilledIconButton(
                    onClick = { },
                    enabled = isEnabled
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }
    }
}

@Composable
fun FilledTonalIconButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                FilledTonalIconButton(
                    onClick = { },
                    enabled = isEnabled
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }
    }
}

@Composable
fun OutlinedIconButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedIconButton(
                    onClick = { },
                    enabled = isEnabled
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }
    }
}

@Composable
fun IconToggleButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }
    var isChecked by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                IconToggleButton(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
                    enabled = isEnabled
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isChecked, onCheckedChange = { isChecked = it })
            Text("Checked")
        }
    }
}

@Composable
fun FilledIconToggleButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }
    var isChecked by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                FilledIconToggleButton(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
                    enabled = isEnabled
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isChecked, onCheckedChange = { isChecked = it })
            Text("Checked")
        }
    }
}

@Composable
fun FilledTonalIconToggleButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }
    var isChecked by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                FilledTonalIconToggleButton(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
                    enabled = isEnabled
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isChecked, onCheckedChange = { isChecked = it })
            Text("Checked")
        }
    }
}

@Composable
fun OutlinedIconToggleButtonDemo() {
    var isEnabled by remember { mutableStateOf(true) }
    var isChecked by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedIconToggleButton(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
                    enabled = isEnabled
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isEnabled, onCheckedChange = { isEnabled = it })
            Text("Enabled")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = isChecked, onCheckedChange = { isChecked = it })
            Text("Checked")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElevationDropdown(
    label: String,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Level1 (1.0.dp)", "Level2 (2.0.dp)", "Level3 (3.0.dp)", "Level4 (4.0.dp)", "Level5 (5.0.dp)")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selected.replace("Level", "L"),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .width(120.dp),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                textStyle = MaterialTheme.typography.bodySmall
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CodeView(buttonType: String) {
    val code = getCodeForButtonType(buttonType)
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    var showCopiedMessage by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2B2B2B) // Dark IDE background
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Code Example",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCCCCCC)
                    )
                    Text(
                        text = "Kotlin",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6897BB),
                        modifier = Modifier
                            .background(
                                Color(0xFF3C3F41),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Copy button
                IconButton(
                    onClick = {
                        val clip = android.content.ClipData.newPlainText("Code", code)
                        clipboardManager.setPrimaryClip(clip)
                        showCopiedMessage = true
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (showCopiedMessage) Icons.Default.Done else Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = if (showCopiedMessage) Color(0xFF4CAF50) else Color(0xFFCCCCCC),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Copied message
            if (showCopiedMessage) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    showCopiedMessage = false
                }
            }

            SyntaxHighlightedCode(code)
        }
    }
}

@Composable
fun SyntaxHighlightedCode(code: String) {
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val keywords = listOf(
            "var", "val", "by", "remember", "mutableStateOf", "mutableIntStateOf",
            "mutableFloatStateOf", "Row", "Column", "Icon", "Text", "Icons", "Default",
            "Modifier", "size", "forEachIndexed", "index", "label", "checked", "in",
            "if", "else", "onClick", "onCheckedChange", "selected", "enabled", "shape",
            "value", "onValueChange", "visible", "targetState", "initialValue", "targetValue",
            "animationSpec", "animation", "repeatMode", "contentDescription", "containerColor",
            "leadingIcon", "trailingIcon", "placeholder", "valueRange", "steps", "colors",
            "padding", "fillMaxWidth", "horizontalArrangement", "verticalArrangement",
            "spacedBy", "title", "text", "confirmButton", "dismissButton", "onDismissRequest",
            "progress", "icon", "active", "enter", "exit", "transitionSpec", "togetherWith"
        )
        val functions = listOf(
            "ElevatedButton", "Button", "FilledTonalButton", "OutlinedButton",
            "TextButton", "RadioButton", "Switch", "IconButton", "FilledIconButton",
            "FilledTonalIconButton", "OutlinedIconButton", "IconToggleButton",
            "FilledIconToggleButton", "FilledTonalIconToggleButton", "OutlinedIconToggleButton",
            "SingleChoiceSegmentedButtonRow", "MultiChoiceSegmentedButtonRow",
            "SegmentedButton", "listOf", "setOf", "TextField", "OutlinedTextField",
            "Slider", "FilterChip", "AssistChip", "SuggestionChip", "Card", "ElevatedCard",
            "OutlinedCard", "AlertDialog", "CircularProgressIndicator", "LinearProgressIndicator",
            "NavigationBar", "NavigationBarItem", "NavigationRail", "NavigationRailItem",
            "AnimatedVisibility", "AnimatedContent", "rememberInfiniteTransition",
            "animateFloat", "infiniteRepeatable", "tween", "fadeIn", "fadeOut",
            "expandVertically", "shrinkVertically", "slideInVertically", "slideOutVertically",
            "CardDefaults", "cardColors", "MaterialTheme", "colorScheme", "typography",
            "titleMedium", "bodySmall", "primaryContainer", "onPrimaryContainer",
            "secondaryContainer", "tertiaryContainer", "surfaceVariant", "Arrangement",
            "Alignment", "CenterVertically", "SegmentedButtonDefaults", "itemShape",
            "RepeatMode", "Reverse", "TextButton", "Done", "Home", "Search", "Person",
            "Notifications", "Star", "TrendingUp", "Bookmark", "AccessTime"
        )

        while (currentIndex < code.length) {
            var matched = false

            // Check for keywords
            for (keyword in keywords) {
                if (code.startsWith(keyword, currentIndex) &&
                    (currentIndex + keyword.length >= code.length ||
                            !code[currentIndex + keyword.length].isLetterOrDigit())
                ) {
                    withStyle(SpanStyle(color = Color(0xFFCC7832))) { // Orange for keywords
                        append(keyword)
                    }
                    currentIndex += keyword.length
                    matched = true
                    break
                }
            }

            if (!matched) {
                // Check for functions/composables
                for (function in functions) {
                    if (code.startsWith(function, currentIndex) &&
                        (currentIndex + function.length >= code.length ||
                                !code[currentIndex + function.length].isLetterOrDigit())
                    ) {
                        withStyle(SpanStyle(color = Color(0xFFFFC66D))) { // Yellow for functions
                            append(function)
                        }
                        currentIndex += function.length
                        matched = true
                        break
                    }
                }
            }

            if (!matched) {
                // Check for strings
                if (code[currentIndex] == '"') {
                    var endIndex = currentIndex + 1
                    while (endIndex < code.length && code[endIndex] != '"') {
                        endIndex++
                    }
                    if (endIndex < code.length) {
                        endIndex++
                        withStyle(SpanStyle(color = Color(0xFF6A8759))) { // Green for strings
                            append(code.substring(currentIndex, endIndex))
                        }
                        currentIndex = endIndex
                        matched = true
                    }
                }
            }

            if (!matched) {
                // Check for numbers
                if (code[currentIndex].isDigit()) {
                    var endIndex = currentIndex
                    while (endIndex < code.length && code[endIndex].isDigit()) {
                        endIndex++
                    }
                    withStyle(SpanStyle(color = Color(0xFF6897BB))) { // Blue for numbers
                        append(code.substring(currentIndex, endIndex))
                    }
                    currentIndex = endIndex
                    matched = true
                }
            }

            if (!matched) {
                // Check for special characters and operators
                if (code[currentIndex] in listOf('=', '{', '}', '(', ')', ',', ':', '.', '+', '-')) {
                    withStyle(SpanStyle(color = Color(0xFFCCCCCC))) { // Light gray for operators
                        append(code[currentIndex])
                    }
                    currentIndex++
                } else {
                    // Default text color
                    withStyle(SpanStyle(color = Color(0xFFA9B7C6))) { // Light blue-gray for regular text
                        append(code[currentIndex])
                    }
                    currentIndex++
                }
            }
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    )
}

fun getCodeForButtonType(buttonType: String): String {
    return when (buttonType) {
        "Elevated Button" -> """
ElevatedButton(onClick = { }) {
    Text("Elevated Button")
}
        """.trimIndent()

        "Filled Button" -> """
Button(onClick = { }) {
    Text("Button")
}
        """.trimIndent()

        "Filled Tonal Button" -> """
FilledTonalButton(onClick = { }) {
    Text("FilledTonal Button")
}
        """.trimIndent()

        "Outlined Button" -> """
OutlinedButton(onClick = { }) {
    Text("Outlined Button")
}
        """.trimIndent()

        "Text Button" -> """
TextButton(onClick = { }) {
    Text("textButton")
}
        """.trimIndent()

        "Radio Button" -> """
var selected by remember { mutableIntStateOf(1) }
Row(verticalAlignment = Alignment.CenterVertically) {
    RadioButton(
        selected = selected == 1,
        onClick = { selected = 1 }
    )
    RadioButton(
        selected = selected == 2,
        onClick = { selected = 2 }
    )
    RadioButton(
        selected = selected == 3,
        onClick = { selected = 3 }
    )
}
        """.trimIndent()

        "Switch Button" -> """
var checked by remember { mutableStateOf(true) }
Switch(
    checked = checked,
    onCheckedChange = { checked = it }
)
        """.trimIndent()

        "Single Choice Segmented Button" -> """
var selectedIndex by remember { mutableIntStateOf(0) }
val options = listOf("First", "Second", "Third")

SingleChoiceSegmentedButtonRow {
    options.forEachIndexed { index, label ->
        SegmentedButton(
            selected = selectedIndex == index,
            onClick = { selectedIndex = index },
            shape = SegmentedButtonDefaults.itemShape(
                index, options.size
            )
        ) {
            Text(label)
        }
    }
}
        """.trimIndent()

        "Multi Choice Segmented Button" -> """
var selectedOptions by remember {
    mutableStateOf(setOf(0))
}
val options = listOf(
    "Favorites" to Icons.Default.Star,
    "Trending" to Icons.Default.TrendingUp,
    "Saved" to Icons.Default.Bookmark
)

MultiChoiceSegmentedButtonRow {
    options.forEachIndexed { index, (label, icon) ->
        SegmentedButton(
            checked = index in selectedOptions,
            onCheckedChange = {
                selectedOptions = if (index in selectedOptions) {
                    selectedOptions - index
                } else {
                    selectedOptions + index
                }
            },
            shape = SegmentedButtonDefaults.itemShape(
                index, options.size
            ),
            icon = {
                SegmentedButtonDefaults.Icon(
                    active = index in selectedOptions
                ) {
                    Icon(icon, contentDescription = null)
                }
            }
        ) {
            Text(label)
        }
    }
}
        """.trimIndent()

        "Icon Button" -> """
IconButton(onClick = { }) {
    Icon(Icons.Default.AccessTime, contentDescription = null)
}
        """.trimIndent()

        "Filled Icon Button" -> """
FilledIconButton(onClick = { }) {
    Icon(Icons.Default.AccessTime, contentDescription = null)
}
        """.trimIndent()

        "Filled Tonal Icon Button" -> """
FilledTonalIconButton(onClick = { }) {
    Icon(Icons.Default.AccessTime, contentDescription = null)
}
        """.trimIndent()

        "Outlined Icon Button" -> """
OutlinedIconButton(onClick = { }) {
    Icon(Icons.Default.AccessTime, contentDescription = null)
}
        """.trimIndent()

        "Icon Toggle Button" -> """
var checked by remember { mutableStateOf(false) }
IconToggleButton(
    checked = checked,
    onCheckedChange = { checked = it }
) {
    Icon(Icons.Default.AccessTime, contentDescription = null)
}
        """.trimIndent()

        "Filled Icon Toggle Button" -> """
var checked by remember { mutableStateOf(false) }
FilledIconToggleButton(
    checked = checked,
    onCheckedChange = { checked = it }
) {
    Icon(Icons.Default.AccessTime, contentDescription = null)
}
        """.trimIndent()

        "Filled Tonal Icon Toggle Button" -> """
var checked by remember { mutableStateOf(false) }
FilledTonalIconToggleButton(
    checked = checked,
    onCheckedChange = { checked = it }
) {
    Icon(Icons.Default.AccessTime, contentDescription = null)
}
        """.trimIndent()

        "Outlined Icon Toggle Button" -> """
var checked by remember { mutableStateOf(true) }
OutlinedIconToggleButton(
    checked = checked,
    onCheckedChange = { checked = it }
) {
    Icon(Icons.Default.AccessTime, contentDescription = null)
}
        """.trimIndent()

        // ========== TEXT FIELDS ==========
        "Filled TextField" -> """
TextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("Label") },
    leadingIcon = {
        Icon(Icons.Default.Home, contentDescription = null)
    },
    placeholder = { Text("Enter text...") }
)
        """.trimIndent()

        "Outlined TextField" -> """
OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    label = { Text("Label") },
    leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = null)
    },
    trailingIcon = {
        Icon(Icons.Default.Done, contentDescription = null)
    }
)
        """.trimIndent()

        // ========== SLIDERS ==========
        "Continuous Slider" -> """
var sliderValue by remember { mutableFloatStateOf(0f) }
Column {
    Slider(
        value = sliderValue,
        onValueChange = { sliderValue = it },
        valueRange = 0f..10f
    )
    Text("Value: ${"$"}{"%.1f".format(sliderValue)}")
}
        """.trimIndent()

        "Discrete Slider" -> """
var sliderValue by remember { mutableFloatStateOf(5f) }
Column {
    Slider(
        value = sliderValue,
        onValueChange = { sliderValue = it },
        valueRange = 0f..10f,
        steps = 9
    )
    Text("Value: ${"$"}{"%.0f".format(sliderValue)}")
}
        """.trimIndent()

        // ========== CHIPS ==========
        "Filter Chips" -> """
var selected1 by remember { mutableStateOf(false) }
var selected2 by remember { mutableStateOf(true) }
Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    FilterChip(
        selected = selected1,
        onClick = { selected1 = !selected1 },
        label = { Text("Chip 1") },
        leadingIcon = if (selected1) {
            {
                Icon(
                    Icons.Default.Done,
                    contentDescription = null,
                    Modifier.size(18.dp)
                )
            }
        } else null
    )
    FilterChip(
        selected = selected2,
        onClick = { selected2 = !selected2 },
        label = { Text("Chip 2") },
        leadingIcon = if (selected2) {
            {
                Icon(
                    Icons.Default.Done,
                    contentDescription = null,
                    Modifier.size(18.dp)
                )
            }
        } else null
    )
}
        """.trimIndent()

        "Assist Chip" -> """
AssistChip(
    onClick = { },
    label = { Text("Assist Chip") },
    leadingIcon = {
        Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            Modifier.size(18.dp)
        )
    }
)
        """.trimIndent()

        "Suggestion Chip" -> """
SuggestionChip(
    onClick = { },
    label = { Text("Suggestion") }
)
        """.trimIndent()

        // ========== CARDS ==========
        "Elevated Card" -> """
ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.elevatedCardElevation(
        defaultElevation = 6.dp  // 0.dp, 1.dp, 3.dp, 6.dp, 8.dp, 12.dp
    )
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Card Title",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "This is an elevated card with shadow.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
        """.trimIndent()

        "Filled Card" -> """
Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Card Title",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "This is a filled card with a surface variant background.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
        """.trimIndent()

        "Outlined Card" -> """
OutlinedCard(
    modifier = Modifier.fillMaxWidth(),
    border = BorderStroke(
        1.dp,  // 0.5.dp, 1.dp, 2.dp, 3.dp
        MaterialTheme.colorScheme.outline
    )
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Card Title",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "This is an outlined card with a border.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
        """.trimIndent()

        // ========== DIALOGS ==========
        "Basic Dialog" -> """
var showDialog by remember { mutableStateOf(false) }

Button(onClick = { showDialog = true }) {
    Text("Show Case")
}

if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text("Basic Dialog") },
        text = {
            Text("This is a basic alert dialog with a title and message.")
        },
        confirmButton = {
            TextButton(onClick = { showDialog = false }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = { showDialog = false }) {
                Text("Cancel")
            }
        }
    )
}
        """.trimIndent()

        "Basic Dialog with Icon" -> """
var showDialog by remember { mutableStateOf(false) }
var showIcon by remember { mutableStateOf(true) }

Button(onClick = { showDialog = true }) {
    Text("Show Case")
}

if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        icon = if (showIcon) {
            { Icon(Icons.Default.Info, contentDescription = null) }
        } else null,
        title = { Text("Dialog with Icon") },
        text = {
            Text("This dialog includes an optional icon at the top.")
        },
        confirmButton = {
            TextButton(onClick = { showDialog = false }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = { showDialog = false }) {
                Text("Cancel")
            }
        }
    )
}
        """.trimIndent()

        "Full-screen Dialog" -> """
var showDialog by remember { mutableStateOf(false) }

Button(onClick = { showDialog = true }) {
    Text("Show Case")
}

if (showDialog) {
    Dialog(
        onDismissRequest = { showDialog = false },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Full-screen Dialog") },
                    navigationIcon = {
                        IconButton(onClick = { showDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Save")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Full-screen Dialog Content",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "This is a full-screen dialog...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
        """.trimIndent()

        // ========== PROGRESS INDICATORS ==========
        "Indeterminate Circular Progress" -> """
CircularProgressIndicator()
        """.trimIndent()

        "Determinate Circular Progress" -> """
var progress by remember { mutableFloatStateOf(0.7f) }

Column {
    CircularProgressIndicator(progress = { progress })

    Text("Value: ${"$"}{(progress * 100).toInt()}%")

    Slider(
        value = progress,
        onValueChange = { progress = it },
        valueRange = 0f..1f
    )
}
        """.trimIndent()

        "Indeterminate Linear Progress" -> """
LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        """.trimIndent()

        "Determinate Linear Progress" -> """
var progress by remember { mutableFloatStateOf(0.7f) }

Column {
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth()
    )

    Text("Value: ${"$"}{(progress * 100).toInt()}%")

    Slider(
        value = progress,
        onValueChange = { progress = it },
        valueRange = 0f..1f
    )
}
        """.trimIndent()

        // ========== NAVIGATION ==========
        "Navigation Bar" -> """
var selectedItem by remember { mutableIntStateOf(0) }
val items = listOf("Home", "Search", "Profile")
val icons = listOf(
    Icons.Default.Home,
    Icons.Default.Search,
    Icons.Default.Person
)

NavigationBar {
    items.forEachIndexed { index, item ->
        NavigationBarItem(
            icon = {
                Icon(icons[index], contentDescription = item)
            },
            label = { Text(item) },
            selected = selectedItem == index,
            onClick = { selectedItem = index }
        )
    }
}
        """.trimIndent()

        "Navigation Rail (Vertical)" -> """
var selectedItem by remember { mutableIntStateOf(0) }
val items = listOf("Home", "Search", "Profile")
val icons = listOf(
    Icons.Default.Home,
    Icons.Default.Search,
    Icons.Default.Person
)

NavigationRail {
    items.forEachIndexed { index, item ->
        NavigationRailItem(
            icon = {
                Icon(icons[index], contentDescription = item)
            },
            label = { Text(item) },
            selected = selectedItem == index,
            onClick = { selectedItem = index }
        )
    }
}
        """.trimIndent()

        // ========== ANIMATION ==========
        "AnimatedVisibility" -> """
var visible by remember { mutableStateOf(true) }

Button(onClick = { visible = !visible }) {
    Text(if (visible) "Hide" else "Show")
}

AnimatedVisibility(
    visible = visible,
    enter = fadeIn() + expandVertically(),
    exit = fadeOut() + shrinkVertically()
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            "This content animates!",
            modifier = Modifier.padding(16.dp),
            color =
                MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
        """.trimIndent()

        "AnimatedContent" -> """
var expanded by remember { mutableStateOf(false) }

Button(onClick = { expanded = !expanded }) {
    Text("Toggle Content")
}

AnimatedContent(
    targetState = expanded,
    transitionSpec = {
        fadeIn() + slideInVertically() togetherWith
            fadeOut() + slideOutVertically()
    },
    label = "content"
) { isExpanded ->
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Text(
            if (isExpanded) "Expanded State"
            else "Collapsed State",
            modifier = Modifier.padding(16.dp)
        )
    }
}
        """.trimIndent()

        // ========== TOOLTIPS ==========
        "Plain Tooltip" -> """
TooltipBox(
    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
    tooltip = {
        PlainTooltip {
            Text("This is a plain tooltip")
        }
    },
    state = rememberTooltipState()
) {
    Icon(
        Icons.Default.AccessTime,
        contentDescription = "Clock",
        modifier = Modifier.size(48.dp)
    )
}
        """.trimIndent()

        "Rich Tooltip" -> """
val tooltipState = rememberTooltipState(isPersistent = true)

TooltipBox(
    positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
    tooltip = {
        RichTooltip(
            title = { Text("Rich Tooltip") },
            action = {
                TextButton(onClick = { }) {
                    Text("Action")
                }
            }
        ) {
            Text("This is a rich tooltip with a title and action button.")
        }
    },
    state = tooltipState
) {
    Icon(
        Icons.Default.AccessTime,
        contentDescription = "Clock",
        modifier = Modifier.size(48.dp)
    )
}
        """.trimIndent()

        // ========== BADGES ==========
        "Small Badge" -> """
BadgedBox(
    badge = {
        Badge()
    }
) {
    Icon(
        Icons.Default.Email,
        contentDescription = "Email",
        modifier = Modifier.size(32.dp)
    )
}
        """.trimIndent()

        "Small Badge with Count" -> """
var count by remember { mutableIntStateOf(8) }

BadgedBox(
    badge = {
        Badge {
            Text("${"$"}count")
        }
    }
) {
    Icon(
        Icons.Default.Email,
        contentDescription = "Email",
        modifier = Modifier.size(32.dp)
    )
}
        """.trimIndent()

        "Large Badge with Label" -> """
var count by remember { mutableIntStateOf(8) }

BadgedBox(
    badge = {
        Badge(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ) {
            Text(
                "${"$"}count",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
) {
    Icon(
        Icons.Default.Email,
        contentDescription = "Email",
        modifier = Modifier.size(32.dp)
    )
}
        """.trimIndent()

        "Badge in Navigation Item" -> """
var count by remember { mutableIntStateOf(999) }

NavigationBar {
    NavigationBarItem(
        selected = true,
        onClick = { },
        icon = {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Text(
                            if (count > 999) "999+" else "${"$"}count",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            ) {
                Icon(Icons.Default.Person, contentDescription = "Photos")
            }
        },
        label = { Text("Photos") }
    )
}
        """.trimIndent()

        "Infinite Animation" -> """
val infiniteTransition = rememberInfiniteTransition(
    label = "infinite"
)
val animatedAlpha by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 0.3f,
    animationSpec = infiniteRepeatable(
        animation = tween(1000),
        repeatMode = RepeatMode.Reverse
    ),
    label = "alpha"
)

Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme
            .surfaceVariant.copy(alpha = animatedAlpha)
    )
) {
    Text(
        "Pulsing animation",
        modifier = Modifier.padding(16.dp)
    )
}
        """.trimIndent()

        else -> "// Code not available"
    }
}

// ========== TEXT FIELDS DEMO ==========
@Composable
fun TextFieldsDemo() {
    var currentFieldIndex by remember { mutableIntStateOf(0) }
    var showCode by remember { mutableStateOf(false) }
    var text1 by remember { mutableStateOf("") }
    var text2 by remember { mutableStateOf("") }

    val fieldTypes = listOf("Filled TextField", "Outlined TextField")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Row 1: Navigation buttons only (Back | Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    showCode = false
                    currentFieldIndex = if (currentFieldIndex > 0) currentFieldIndex - 1 else fieldTypes.size - 1
                }
            ) {
                Icon(Icons.Default.ArrowBack, "Previous")
            }

            IconButton(
                onClick = {
                    showCode = false
                    currentFieldIndex = if (currentFieldIndex < fieldTypes.size - 1) currentFieldIndex + 1 else 0
                }
            ) {
                Icon(Icons.Default.ArrowForward, "Next")
            }
        }

        // Row 2: Component heading and code toggle button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fieldTypes[currentFieldIndex],
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { showCode = !showCode }) {
                Icon(
                    if (showCode) Icons.Default.Visibility else Icons.Default.Code,
                    contentDescription = if (showCode) "Show Demo" else "Show Code"
                )
            }
        }

        // Code view
        AnimatedVisibility(
            visible = showCode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CodeView(buttonType = fieldTypes[currentFieldIndex])
        }

        // Row 3: Demo area
        when (currentFieldIndex) {
            0 -> {
                TextField(
                    value = text1,
                    onValueChange = { text1 = it },
                    label = { Text("Label") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    placeholder = { Text("Enter text...") }
                )
            }
            1 -> {
                OutlinedTextField(
                    value = text2,
                    onValueChange = { text2 = it },
                    label = { Text("Label") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = { Icon(Icons.Default.Done, contentDescription = null) }
                )
            }
        }
    }
}

// ========== SLIDERS DEMO ==========
@Composable
fun SlidersDemo() {
    var currentSliderIndex by remember { mutableIntStateOf(0) }
    var showCode by remember { mutableStateOf(false) }
    var sliderValue1 by remember { mutableFloatStateOf(0f) }
    var sliderValue2 by remember { mutableFloatStateOf(5f) }

    val sliderTypes = listOf("Continuous Slider", "Discrete Slider")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Row 1: Navigation buttons only (Back | Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    showCode = false
                    currentSliderIndex = if (currentSliderIndex > 0) currentSliderIndex - 1 else sliderTypes.size - 1
                }
            ) {
                Icon(Icons.Default.ArrowBack, "Previous")
            }

            IconButton(
                onClick = {
                    showCode = false
                    currentSliderIndex = if (currentSliderIndex < sliderTypes.size - 1) currentSliderIndex + 1 else 0
                }
            ) {
                Icon(Icons.Default.ArrowForward, "Next")
            }
        }

        // Row 2: Component heading and code toggle button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sliderTypes[currentSliderIndex],
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { showCode = !showCode }) {
                Icon(
                    if (showCode) Icons.Default.Visibility else Icons.Default.Code,
                    contentDescription = if (showCode) "Show Demo" else "Show Code"
                )
            }
        }

        // Code view
        AnimatedVisibility(
            visible = showCode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CodeView(buttonType = sliderTypes[currentSliderIndex])
        }

        // Row 3: Demo area
        when (currentSliderIndex) {
            0 -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Slider(
                        value = sliderValue1,
                        onValueChange = { sliderValue1 = it },
                        valueRange = 0f..10f
                    )
                    Text("Value: %.1f".format(sliderValue1), style = MaterialTheme.typography.bodySmall)
                }
            }
            1 -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Slider(
                        value = sliderValue2,
                        onValueChange = { sliderValue2 = it },
                        valueRange = 0f..10f,
                        steps = 9
                    )
                    Text("Value: %.0f".format(sliderValue2), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ========== CHIPS DEMO ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipsDemo() {
    var currentChipIndex by remember { mutableIntStateOf(0) }
    var showCode by remember { mutableStateOf(false) }
    var selected1 by remember { mutableStateOf(false) }
    var selected2 by remember { mutableStateOf(true) }

    val chipTypes = listOf("Filter Chips", "Assist Chip", "Suggestion Chip")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Row 1: Navigation buttons only (Back | Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    showCode = false
                    currentChipIndex = if (currentChipIndex > 0) currentChipIndex - 1 else chipTypes.size - 1
                }
            ) {
                Icon(Icons.Default.ArrowBack, "Previous")
            }

            IconButton(
                onClick = {
                    showCode = false
                    currentChipIndex = if (currentChipIndex < chipTypes.size - 1) currentChipIndex + 1 else 0
                }
            ) {
                Icon(Icons.Default.ArrowForward, "Next")
            }
        }

        // Row 2: Component heading and code toggle button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = chipTypes[currentChipIndex],
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { showCode = !showCode }) {
                Icon(
                    if (showCode) Icons.Default.Visibility else Icons.Default.Code,
                    contentDescription = if (showCode) "Show Demo" else "Show Code"
                )
            }
        }

        // Code view
        AnimatedVisibility(
            visible = showCode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CodeView(buttonType = chipTypes[currentChipIndex])
        }

        // Row 3: Demo area
        when (currentChipIndex) {
            0 -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selected1,
                        onClick = { selected1 = !selected1 },
                        label = { Text("Chip 1") },
                        leadingIcon = if (selected1) {
                            { Icon(Icons.Default.Done, contentDescription = null, Modifier.size(18.dp)) }
                        } else null
                    )
                    FilterChip(
                        selected = selected2,
                        onClick = { selected2 = !selected2 },
                        label = { Text("Chip 2") },
                        leadingIcon = if (selected2) {
                            { Icon(Icons.Default.Done, contentDescription = null, Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }
            1 -> {
                AssistChip(
                    onClick = { },
                    label = { Text("Assist Chip") },
                    leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null, Modifier.size(18.dp)) }
                )
            }
            2 -> {
                SuggestionChip(
                    onClick = { },
                    label = { Text("Suggestion") }
                )
            }
        }
    }
}

// ========== CARDS DEMO ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsDemo() {
    var currentCardIndex by remember { mutableIntStateOf(0) }
    var showCode by remember { mutableStateOf(false) }

    val cardTypes = listOf("Elevated Card", "Filled Card", "Outlined Card")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Cards contain content and actions that relate information about a subject.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Row 1: Navigation buttons only (Back | Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button (with circular navigation)
            IconButton(
                onClick = {
                    showCode = false
                    currentCardIndex = if (currentCardIndex > 0) {
                        currentCardIndex - 1
                    } else {
                        cardTypes.size - 1
                    }
                }
            ) {
                Icon(Icons.Default.ArrowBack, "Previous")
            }

            // Next button (with circular navigation)
            IconButton(
                onClick = {
                    showCode = false
                    currentCardIndex = if (currentCardIndex < cardTypes.size - 1) {
                        currentCardIndex + 1
                    } else {
                        0
                    }
                }
            ) {
                Icon(Icons.Default.ArrowForward, "Next")
            }
        }

        // Row 2: Component heading and code toggle button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = cardTypes[currentCardIndex],
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Code view toggle button
            IconButton(onClick = { showCode = !showCode }) {
                Icon(
                    if (showCode) Icons.Default.Visibility else Icons.Default.Code,
                    contentDescription = if (showCode) "Show Demo" else "Show Code"
                )
            }
        }

        // Show code view if toggled
        AnimatedVisibility(
            visible = showCode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CodeView(buttonType = cardTypes[currentCardIndex])
        }

        // Always show card demo
        when (currentCardIndex) {
            0 -> ElevatedCardDemo()
            1 -> FilledCardDemo()
            2 -> OutlinedCardDemo()
        }
    }
}

@Composable
fun ElevatedCardDemo() {
    var selectedElevation by remember { mutableStateOf("Level1 (1.0.dp)") }

    val elevation = when (selectedElevation) {
        "Level0 (0.0.dp)" -> 0.dp
        "Level1 (1.0.dp)" -> 1.dp
        "Level2 (3.0.dp)" -> 3.dp
        "Level3 (6.0.dp)" -> 6.dp
        "Level4 (8.0.dp)" -> 8.dp
        "Level5 (12.0.dp)" -> 12.dp
        else -> 1.dp
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Card preview
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = elevation
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Card Title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This is an elevated card with shadow. Adjust the elevation to see the shadow effect.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Elevation dropdown
        CardElevationDropdown(
            label = "Elevation:",
            selected = selectedElevation,
            onSelect = { selectedElevation = it }
        )
    }
}

@Composable
fun FilledCardDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Card Title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This is a filled card with a surface variant background color.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun OutlinedCardDemo() {
    var selectedBorderWidth by remember { mutableStateOf("Default (1.0.dp)") }

    val borderWidth = when (selectedBorderWidth) {
        "Thin (0.5.dp)" -> 0.5.dp
        "Default (1.0.dp)" -> 1.dp
        "Medium (2.0.dp)" -> 2.dp
        "Thick (3.0.dp)" -> 3.dp
        else -> 1.dp
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(
                borderWidth,
                MaterialTheme.colorScheme.outline
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Card Title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This is an outlined card with a border. Adjust the border width to see the effect.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Border width dropdown
        CardBorderWidthDropdown(
            label = "Border Width:",
            selected = selectedBorderWidth,
            onSelect = { selectedBorderWidth = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardElevationDropdown(
    label: String,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        "Level0 (0.0.dp)",
        "Level1 (1.0.dp)",
        "Level2 (3.0.dp)",
        "Level3 (6.0.dp)",
        "Level4 (8.0.dp)",
        "Level5 (12.0.dp)"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selected.replace("Level", "L"),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .width(120.dp),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                textStyle = MaterialTheme.typography.bodySmall
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardBorderWidthDropdown(
    label: String,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        "Thin (0.5.dp)",
        "Default (1.0.dp)",
        "Medium (2.0.dp)",
        "Thick (3.0.dp)"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .width(140.dp),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                textStyle = MaterialTheme.typography.bodySmall
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// ========== DIALOGS DEMO ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogsDemo() {
    var currentDialogIndex by remember { mutableIntStateOf(0) }
    var showCode by remember { mutableStateOf(false) }

    val dialogTypes = listOf(
        "Basic Dialog",
        "Basic Dialog with Icon",
        "Full-screen Dialog"
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Dialogs provide important prompts in a user flow.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Row 1: Navigation buttons only (Back | Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button (with circular navigation)
            IconButton(
                onClick = {
                    showCode = false
                    currentDialogIndex = if (currentDialogIndex > 0) {
                        currentDialogIndex - 1
                    } else {
                        dialogTypes.size - 1
                    }
                }
            ) {
                Icon(Icons.Default.ArrowBack, "Previous")
            }

            // Next button (with circular navigation)
            IconButton(
                onClick = {
                    showCode = false
                    currentDialogIndex = if (currentDialogIndex < dialogTypes.size - 1) {
                        currentDialogIndex + 1
                    } else {
                        0
                    }
                }
            ) {
                Icon(Icons.Default.ArrowForward, "Next")
            }
        }

        // Row 2: Component heading and code toggle button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dialogTypes[currentDialogIndex],
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Code view toggle button
            IconButton(onClick = { showCode = !showCode }) {
                Icon(
                    if (showCode) Icons.Default.Visibility else Icons.Default.Code,
                    contentDescription = if (showCode) "Show Demo" else "Show Code"
                )
            }
        }

        // Show code view if toggled
        AnimatedVisibility(
            visible = showCode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CodeView(buttonType = dialogTypes[currentDialogIndex])
        }

        // Always show dialog demo
        when (currentDialogIndex) {
            0 -> BasicDialogDemo()
            1 -> BasicDialogWithIconDemo()
            2 -> FullScreenDialogDemo()
        }
    }
}

@Composable
fun BasicDialogDemo() {
    var showDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { showDialog = true }) {
                    Text("Show Case")
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Basic Dialog") },
            text = { Text("This is a basic alert dialog with a title and message.") },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BasicDialogWithIconDemo() {
    var showDialog by remember { mutableStateOf(false) }
    var showIcon by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { showDialog = true }) {
                    Text("Show Case")
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(checked = showIcon, onCheckedChange = { showIcon = it })
            Text("Show Icon")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = if (showIcon) {
                { Icon(Icons.Default.Info, contentDescription = null) }
            } else null,
            title = { Text("Dialog with Icon") },
            text = { Text("This dialog includes an optional icon at the top.") },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenDialogDemo() {
    var showDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { showDialog = true }) {
                    Text("Show Case")
                }
            }
        }
    }

    if (showDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Full-screen Dialog") },
                        navigationIcon = {
                            IconButton(onClick = { showDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        },
                        actions = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Save")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Full-screen Dialog Content",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "This is a full-screen dialog that takes up the entire screen. It's useful for complex forms or detailed content that needs more space.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ========== PROGRESS INDICATORS DEMO ==========
@Composable
fun ProgressIndicatorsDemo() {
    var currentIndicatorIndex by remember { mutableIntStateOf(0) }
    var showCode by remember { mutableStateOf(false) }

    val indicatorTypes = listOf(
        "Indeterminate Circular Progress",
        "Determinate Circular Progress",
        "Indeterminate Linear Progress",
        "Determinate Linear Progress"
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Row 1: Navigation buttons only (Back | Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button (with circular navigation)
            IconButton(
                onClick = {
                    showCode = false
                    currentIndicatorIndex = if (currentIndicatorIndex > 0) {
                        currentIndicatorIndex - 1
                    } else {
                        indicatorTypes.size - 1
                    }
                }
            ) {
                Icon(Icons.Default.ArrowBack, "Previous")
            }

            // Next button (with circular navigation)
            IconButton(
                onClick = {
                    showCode = false
                    currentIndicatorIndex = if (currentIndicatorIndex < indicatorTypes.size - 1) {
                        currentIndicatorIndex + 1
                    } else {
                        0
                    }
                }
            ) {
                Icon(Icons.Default.ArrowForward, "Next")
            }
        }

        // Row 2: Component heading and code toggle button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = indicatorTypes[currentIndicatorIndex],
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Code view toggle button
            IconButton(onClick = { showCode = !showCode }) {
                Icon(
                    if (showCode) Icons.Default.Visibility else Icons.Default.Code,
                    contentDescription = if (showCode) "Show Demo" else "Show Code"
                )
            }
        }

        // Show code view if toggled
        AnimatedVisibility(
            visible = showCode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CodeView(buttonType = indicatorTypes[currentIndicatorIndex])
        }

        // Always show progress indicator demo
        when (currentIndicatorIndex) {
            0 -> IndeterminateCircularProgressDemo()
            1 -> DeterminateCircularProgressDemo()
            2 -> IndeterminateLinearProgressDemo()
            3 -> DeterminateLinearProgressDemo()
        }
    }
}

@Composable
fun IndeterminateCircularProgressDemo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun DeterminateCircularProgressDemo() {
    var progress by remember { mutableFloatStateOf(0.7f) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(progress = { progress })
            }
        }

        Text(
            "Value: ${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium
        )

        Slider(
            value = progress,
            onValueChange = { progress = it },
            valueRange = 0f..1f
        )
    }
}

@Composable
fun IndeterminateLinearProgressDemo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun DeterminateLinearProgressDemo() {
    var progress by remember { mutableFloatStateOf(0.7f) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Text(
            "Value: ${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium
        )

        Slider(
            value = progress,
            onValueChange = { progress = it },
            valueRange = 0f..1f
        )
    }
}

// ========== NAVIGATION DEMO ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDemo() {
    var currentNavIndex by remember { mutableIntStateOf(0) }
    var showCode by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Home", "Search", "Profile")
    val icons = listOf(Icons.Default.Home, Icons.Default.Search, Icons.Default.Person)

    val navTypes = listOf("Navigation Bar", "Navigation Rail (Vertical)")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Row 1: Navigation buttons only (Back | Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    showCode = false
                    currentNavIndex = if (currentNavIndex > 0) currentNavIndex - 1 else navTypes.size - 1
                }
            ) {
                Icon(Icons.Default.ArrowBack, "Previous")
            }

            IconButton(
                onClick = {
                    showCode = false
                    currentNavIndex = if (currentNavIndex < navTypes.size - 1) currentNavIndex + 1 else 0
                }
            ) {
                Icon(Icons.Default.ArrowForward, "Next")
            }
        }

        // Row 2: Component heading and code toggle button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = navTypes[currentNavIndex],
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { showCode = !showCode }) {
                Icon(
                    if (showCode) Icons.Default.Visibility else Icons.Default.Code,
                    contentDescription = if (showCode) "Show Demo" else "Show Code"
                )
            }
        }

        // Code view
        AnimatedVisibility(
            visible = showCode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CodeView(buttonType = navTypes[currentNavIndex])
        }

        // Row 3: Demo area
        when (currentNavIndex) {
            0 -> {
                NavigationBar {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(icons[index], contentDescription = item) },
                            label = { Text(item) },
                            selected = selectedItem == index,
                            onClick = { selectedItem = index }
                        )
                    }
                }
            }
            1 -> {
                NavigationRail {
                    items.forEachIndexed { index, item ->
                        NavigationRailItem(
                            icon = { Icon(icons[index], contentDescription = item) },
                            label = { Text(item) },
                            selected = selectedItem == index,
                            onClick = { selectedItem = index }
                        )
                    }
                }
            }
        }
    }
}

// ========== ANIMATION DEMO ==========
@Composable
fun AnimationDemo() {
    var currentAnimIndex by remember { mutableIntStateOf(0) }
    var showCode by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val animTypes = listOf("AnimatedVisibility", "AnimatedContent", "Infinite Animation")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Row 1: Navigation buttons only (Back | Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    showCode = false
                    currentAnimIndex = if (currentAnimIndex > 0) currentAnimIndex - 1 else animTypes.size - 1
                }
            ) {
                Icon(Icons.Default.ArrowBack, "Previous")
            }

            IconButton(
                onClick = {
                    showCode = false
                    currentAnimIndex = if (currentAnimIndex < animTypes.size - 1) currentAnimIndex + 1 else 0
                }
            ) {
                Icon(Icons.Default.ArrowForward, "Next")
            }
        }

        // Row 2: Component heading and code toggle button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = animTypes[currentAnimIndex],
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { showCode = !showCode }) {
                Icon(
                    if (showCode) Icons.Default.Visibility else Icons.Default.Code,
                    contentDescription = if (showCode) "Show Demo" else "Show Code"
                )
            }
        }

        // Code view
        AnimatedVisibility(
            visible = showCode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CodeView(buttonType = animTypes[currentAnimIndex])
        }

        // Row 3: Demo area
        when (currentAnimIndex) {
            0 -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { visible = !visible }) {
                        Text(if (visible) "Hide" else "Show")
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                "This content animates!",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            1 -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { expanded = !expanded }) {
                        Text("Toggle Content")
                    }
                    AnimatedContent(
                        targetState = expanded,
                        transitionSpec = {
                            fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
                        },
                        label = "content"
                    ) { isExpanded ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isExpanded)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else
                                    MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Text(
                                if (isExpanded) "Expanded State" else "Collapsed State",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
            2 -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = animatedAlpha)
                    )
                ) {
                    Text(
                        "Pulsing animation",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

// ========== TOOLTIPS DEMO ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipsDemo() {
    var currentTooltipIndex by remember { mutableIntStateOf(0) }
    var showCode by remember { mutableStateOf(false) }

    val tooltipTypes = listOf(
        "Plain Tooltip",
        "Rich Tooltip"
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Tooltips are informative, specific, and action-oriented text labels that provide contextual support",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Row 1: Navigation buttons only (Back | Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button (with circular navigation)
            IconButton(
                onClick = {
                    showCode = false
                    currentTooltipIndex = if (currentTooltipIndex > 0) {
                        currentTooltipIndex - 1
                    } else {
                        tooltipTypes.size - 1
                    }
                }
            ) {
                Icon(Icons.Default.ArrowBack, "Previous")
            }

            // Next button (with circular navigation)
            IconButton(
                onClick = {
                    showCode = false
                    currentTooltipIndex = if (currentTooltipIndex < tooltipTypes.size - 1) {
                        currentTooltipIndex + 1
                    } else {
                        0
                    }
                }
            ) {
                Icon(Icons.Default.ArrowForward, "Next")
            }
        }

        // Row 2: Component heading and code toggle button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tooltipTypes[currentTooltipIndex],
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Code view toggle button
            IconButton(onClick = { showCode = !showCode }) {
                Icon(
                    if (showCode) Icons.Default.Visibility else Icons.Default.Code,
                    contentDescription = if (showCode) "Show Demo" else "Show Code"
                )
            }
        }

        // Show code view if toggled
        AnimatedVisibility(
            visible = showCode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CodeView(buttonType = tooltipTypes[currentTooltipIndex])
        }

        // Always show tooltip demo
        when (currentTooltipIndex) {
            0 -> PlainTooltipDemo()
            1 -> RichTooltipDemo()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlainTooltipDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text("This is a plain tooltip")
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = "Clock",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Text(
            text = "Long press on the icon above to see a tooltip.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichTooltipDemo() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                val tooltipState = rememberTooltipState(isPersistent = true)

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                    tooltip = {
                        RichTooltip(
                            title = { Text("Rich Tooltip") },
                            action = {
                                TextButton(onClick = { }) {
                                    Text("Action")
                                }
                            }
                        ) {
                            Text("This is a rich tooltip with a title and action button.")
                        }
                    },
                    state = tooltipState
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = "Clock",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Text(
            text = "Long press on the icon above to see a tooltip.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ========== BADGES DEMO ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesDemo() {
    var currentBadgeIndex by remember { mutableIntStateOf(0) }
    var showCode by remember { mutableStateOf(false) }

    val badgeTypes = listOf(
        "Small Badge",
        "Small Badge with Count",
        "Large Badge with Label",
        "Badge in Navigation Item"
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Badges convey dynamic information, such as counts or status. A badge can include labels or numbers.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Row 1: Navigation buttons only (Back | Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button (with circular navigation)
            IconButton(
                onClick = {
                    showCode = false
                    currentBadgeIndex = if (currentBadgeIndex > 0) {
                        currentBadgeIndex - 1
                    } else {
                        badgeTypes.size - 1
                    }
                }
            ) {
                Icon(Icons.Default.ArrowBack, "Previous")
            }

            // Next button (with circular navigation)
            IconButton(
                onClick = {
                    showCode = false
                    currentBadgeIndex = if (currentBadgeIndex < badgeTypes.size - 1) {
                        currentBadgeIndex + 1
                    } else {
                        0
                    }
                }
            ) {
                Icon(Icons.Default.ArrowForward, "Next")
            }
        }

        // Row 2: Component heading and code toggle button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = badgeTypes[currentBadgeIndex],
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Code view toggle button
            IconButton(onClick = { showCode = !showCode }) {
                Icon(
                    if (showCode) Icons.Default.Visibility else Icons.Default.Code,
                    contentDescription = if (showCode) "Show Demo" else "Show Code"
                )
            }
        }

        // Show code view if toggled
        AnimatedVisibility(
            visible = showCode,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            CodeView(buttonType = badgeTypes[currentBadgeIndex])
        }

        // Always show badge demo
        when (currentBadgeIndex) {
            0 -> SmallBadgeDemo()
            1 -> SmallBadgeWithCountDemo()
            2 -> LargeBadgeWithLabelDemo()
            3 -> BadgeInNavigationDemo()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallBadgeDemo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            BadgedBox(
                badge = {
                    Badge()
                }
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = "Email",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallBadgeWithCountDemo() {
    var count by remember { mutableIntStateOf(8) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        Badge {
                            Text("$count")
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = "Email",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { if (count > 0) count-- }) {
                Text("-")
            }
            Text(
                "Count: $count",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = { count++ }) {
                Text("+")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeBadgeWithLabelDemo() {
    var count by remember { mutableIntStateOf(8) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) {
                            Text(
                                "$count",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = "Email",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { if (count > 0) count-- }) {
                Text("-")
            }
            Text(
                "Count: $count",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = { count++ }) {
                Text("+")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeInNavigationDemo() {
    var count by remember { mutableIntStateOf(999) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                NavigationBar(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(
                                            if (count > 999) "999+" else "$count",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "Photos")
                            }
                        },
                        label = { Text("Photos") }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { if (count > 0) count -= 10 }) {
                Text("-10")
            }
            Text(
                "Count: $count",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = { count += 10 }) {
                Text("+10")
            }
        }
    }
}

// ========== FONTS DEMO ==========
@Composable
fun FontsDemo() {
    val typography = MaterialTheme.typography

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Use typography to make writing legible and beautiful. Material's default type scale includes contrasting and flexible styles to support a wide range of use cases.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Typography",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        // Display Styles
        Text(
            text = "Display",
            style = typography.displayLarge
        )
        Text(
            text = "Large",
            style = typography.displayLarge
        )
        Text(
            text = "Display Medium",
            style = typography.displayMedium
        )
        Text(
            text = "Display Small",
            style = typography.displaySmall
        )

        // Headline Styles
        Text(
            text = "Headline Large",
            style = typography.headlineLarge
        )
        Text(
            text = "Headline Medium",
            style = typography.headlineMedium
        )
        Text(
            text = "Headline Small",
            style = typography.headlineSmall
        )

        // Title Styles
        Text(
            text = "Title Large",
            style = typography.titleLarge
        )
        Text(
            text = "Title Medium",
            style = typography.titleMedium
        )
        Text(
            text = "Title Small",
            style = typography.titleSmall
        )

        // Body Styles
        Text(
            text = "Body Large",
            style = typography.bodyLarge
        )
        Text(
            text = "Body Medium",
            style = typography.bodyMedium
        )
        Text(
            text = "Body Small",
            style = typography.bodySmall
        )

        // Label Styles
        Text(
            text = "Label Large",
            style = typography.labelLarge
        )
        Text(
            text = "Label Medium",
            style = typography.labelMedium
        )
        Text(
            text = "Label Small",
            style = typography.labelSmall
        )
    }
}

// ========== PALETTE DEMO ==========
@Composable
fun PaletteDemo() {
    val colorScheme = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Create accessible, personal color schemes communicating your product's hierarchy, state, and brand.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Primary Colors
        ColorPaletteCard(
            label = "Primary",
            backgroundColor = colorScheme.primary,
            textColor = colorScheme.onPrimary
        )
        ColorPaletteCard(
            label = "onPrimary",
            backgroundColor = colorScheme.onPrimary,
            textColor = colorScheme.primary
        )
        ColorPaletteCard(
            label = "PrimaryContainer",
            backgroundColor = colorScheme.primaryContainer,
            textColor = colorScheme.onPrimaryContainer
        )
        ColorPaletteCard(
            label = "OnPrimaryContainer",
            backgroundColor = colorScheme.onPrimaryContainer,
            textColor = colorScheme.primaryContainer
        )

        // Secondary Colors
        ColorPaletteCard(
            label = "Secondary",
            backgroundColor = colorScheme.secondary,
            textColor = colorScheme.onSecondary
        )
        ColorPaletteCard(
            label = "onSecondary",
            backgroundColor = colorScheme.onSecondary,
            textColor = colorScheme.secondary
        )
        ColorPaletteCard(
            label = "SecondaryContainer",
            backgroundColor = colorScheme.secondaryContainer,
            textColor = colorScheme.onSecondaryContainer
        )
        ColorPaletteCard(
            label = "OnSecondaryContainer",
            backgroundColor = colorScheme.onSecondaryContainer,
            textColor = colorScheme.secondaryContainer
        )

        // Tertiary Colors
        ColorPaletteCard(
            label = "Tertiary",
            backgroundColor = colorScheme.tertiary,
            textColor = colorScheme.onTertiary
        )
        ColorPaletteCard(
            label = "OnTertiary",
            backgroundColor = colorScheme.onTertiary,
            textColor = colorScheme.tertiary
        )
        ColorPaletteCard(
            label = "TertiaryContainer",
            backgroundColor = colorScheme.tertiaryContainer,
            textColor = colorScheme.onTertiaryContainer
        )
        ColorPaletteCard(
            label = "OnTertiaryContainer",
            backgroundColor = colorScheme.onTertiaryContainer,
            textColor = colorScheme.tertiaryContainer
        )

        // Error Colors
        ColorPaletteCard(
            label = "Error",
            backgroundColor = colorScheme.error,
            textColor = colorScheme.onError
        )
        ColorPaletteCard(
            label = "OnError",
            backgroundColor = colorScheme.onError,
            textColor = colorScheme.error
        )
        ColorPaletteCard(
            label = "ErrorContainer",
            backgroundColor = colorScheme.errorContainer,
            textColor = colorScheme.onErrorContainer
        )
        ColorPaletteCard(
            label = "OnErrorContainer",
            backgroundColor = colorScheme.onErrorContainer,
            textColor = colorScheme.errorContainer
        )

        // Surface Colors
        ColorPaletteCard(
            label = "Background",
            backgroundColor = colorScheme.background,
            textColor = colorScheme.onBackground
        )
        ColorPaletteCard(
            label = "Surface",
            backgroundColor = colorScheme.surface,
            textColor = colorScheme.onSurface
        )
        ColorPaletteCard(
            label = "OnSurface",
            backgroundColor = colorScheme.onSurface,
            textColor = colorScheme.surface
        )
        ColorPaletteCard(
            label = "Outline",
            backgroundColor = colorScheme.outline,
            textColor = colorScheme.surface
        )
        ColorPaletteCard(
            label = "Outline-Variant",
            backgroundColor = colorScheme.outlineVariant,
            textColor = colorScheme.onSurface
        )
        ColorPaletteCard(
            label = "Surface-Variant",
            backgroundColor = colorScheme.surfaceVariant,
            textColor = colorScheme.onSurfaceVariant
        )
        ColorPaletteCard(
            label = "OnSurface-Variant",
            backgroundColor = colorScheme.onSurfaceVariant,
            textColor = colorScheme.surfaceVariant
        )
    }
}

@Composable
fun ColorPaletteCard(
    label: String,
    backgroundColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = textColor
            )
        }
    }
}

enum class ShowcaseCategory(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    BUTTONS(
        "Buttons",
        "Elevated, Filled, Outlined, Text, and Icon buttons",
        Icons.Default.SmartButton
    ),
    TEXT_FIELDS(
        "Text Fields",
        "Filled and Outlined text input fields with icons",
        Icons.Default.TextFields
    ),
    SLIDERS(
        "Sliders",
        "Continuous and Discrete sliders for value selection",
        Icons.Default.TrendingUp
    ),
    CHIPS(
        "Chips",
        "Filter, Assist, and Suggestion chips",
        Icons.Default.Label
    ),
    CARDS(
        "Cards",
        "Elevated, Filled, and Outlined cards",
        Icons.Default.CreditCard
    ),
    DIALOGS(
        "Dialogs",
        "Alert dialogs and modal dialogs",
        Icons.Default.Message
    ),
    PROGRESS_INDICATORS(
        "Progress Indicators",
        "Circular and Linear progress indicators",
        Icons.Default.HourglassEmpty
    ),
    NAVIGATION(
        "Navigation",
        "Navigation Bar and Navigation Rail components",
        Icons.Default.Menu
    ),
    ANIMATION(
        "Animation",
        "AnimatedVisibility, AnimatedContent, and more",
        Icons.Default.Animation
    ),
    PALETTE(
        "Palette",
        "Material Design 3 color scheme and theming",
        Icons.Default.Palette
    ),
    FONTS(
        "Fonts",
        "Material Design 3 typography scale and text styles",
        Icons.Default.TextFields
    ),
    BADGES(
        "Badges",
        "Badges convey dynamic information such as counts or status",
        Icons.Default.Notifications
    ),
    TOOLTIPS(
        "Tooltips",
        "Tooltips provide informative, specific, and action-oriented text labels",
        Icons.Default.Info
    )
}
