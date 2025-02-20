package com.evani.homechefai.ui.screens.baking

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.res.stringResource
import com.evani.homechefai.viewmodel.BakingViewModel
import com.evani.homechefai.CameraPermissionDialog
import com.evani.homechefai.viewmodel.ChatMessage
import com.evani.homechefai.R
import com.evani.homechefai.ui.state.UiState
import com.evani.homechefai.checkCameraPermission
import com.evani.homechefai.registerCameraLauncher
import com.evani.homechefai.registerImagePicker
import com.evani.homechefai.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BakingScreen(
    onNavigateToSettings: () -> Unit,
    bakingViewModel: BakingViewModel = viewModel()
) {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    var selectedImage = remember { mutableStateOf<Bitmap?>(null) }
    val uiState by bakingViewModel.uiState.collectAsState()
    
    // Image picker launcher
    val imagePickerLauncher = registerImagePicker(context, selectedImage)
    
    // Camera launcher
    val cameraLauncher = registerCameraLauncher(selectedImage)
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    // Permission Dialog if needed
    if (showPermissionDialog) {
        CameraPermissionDialog(
            onDismiss = { showPermissionDialog = false },
            onGranted = {
                showPermissionDialog = false
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
    }

    // Clear only the UI image preview when AI responds
    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            selectedImage.value = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
            TopAppBar(
                title = { 
        Text(
                        text = stringResource(R.string.text_eato),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.title_settings),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            // LazyColumn for messages
            val lazyListState = rememberLazyListState()
            val messages by bakingViewModel.messages.collectAsState()

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.large),
                state = lazyListState,
                contentPadding = PaddingValues(vertical = Dimens.medium)
            ) {
                items(messages) { message ->
                    when (message) {
                        is ChatMessage.User -> {
                            UserMessageBubble {
                                Column {
                                    Text(
                                        text = message.text,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    message.image?.let { bitmap ->
                                        Spacer(modifier = Modifier.height(Dimens.medium))
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .height(Dimens.height_200)
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(Dimens.medium)),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }
                        is ChatMessage.Assistant -> {
                            AssistantMessageBubble {
                                StyledText(message.text)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.small))
                }

                // Loading indicator
                if (uiState is UiState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.large),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimens.extraLarge),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = Dimens.extraSmall
                            )
                        }
                    }
                }
            }

            // Chat input area with image preview
            ChatInputArea(
                onMessageSent = { text -> bakingViewModel.sendPrompt(selectedImage.value, text) },
                onImageSelected = { uri -> /* Handle image selection */ },
                selectedImage = selectedImage.value,
                onClearImage = { selectedImage.value = null },
                showImageOptions = remember { mutableStateOf(false) },
                onGalleryClick = { imagePickerLauncher.launch("image/*") },
                onCameraClick = {
                    if (checkCameraPermission(context)) {
                        cameraLauncher.launch(null)
                    } else {
                        showPermissionDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(Dimens.large)
            )
        }
    }
}

@Composable
private fun UserMessageBubble(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = Dimens.padding_64)
    ) {
        Card(
            modifier = Modifier.widthIn(max = 340.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(Dimens.padding_12)
        ) {
            Box(modifier = Modifier.padding(Dimens.large)) {
                content()
            }
        }
    }
}

@Composable
private fun AssistantMessageBubble(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Dimens.padding_64)
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .align(Alignment.TopEnd),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.padding(Dimens.large)) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StyledText(text: String) {
    val segments = parseStyledText(text)
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
    ) {
        segments.forEach { segment ->
            Text(
                text = segment.text,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = segment.weight,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ChatInputArea(
    onMessageSent: (String) -> Unit,
    onImageSelected: (String) -> Unit,
    selectedImage: Bitmap?,
    onClearImage: () -> Unit,
    showImageOptions: MutableState<Boolean>,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        // Show selected image preview if any - Moved to top
        selectedImage?.let { bitmap ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = onClearImage,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear image",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attachment Button with Dropdown
            Box {
                IconButton(
                    onClick = { showImageOptions.value = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add attachment",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = showImageOptions.value,
                    onDismissRequest = { showImageOptions.value = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Choose from Gallery") },
                        onClick = {
                            showImageOptions.value = false
                            onGalleryClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Take Photo") },
                        onClick = {
                            showImageOptions.value = false
                            onCameraClick()
                        }
                    )
                    if (selectedImage != null) {
                        DropdownMenuItem(
                            text = { Text("Clear Image") },
                            onClick = {
                                showImageOptions.value = false
                                onClearImage()
                            }
                        )
                    }
                }
            }

            // Text Input Field
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                placeholder = { Text("Type a message...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                ),
                maxLines = 4
            )

            // Send Button
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onMessageSent(messageText)
                        messageText = ""
                    }
                },
                enabled = messageText.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message",
                    tint = if (messageText.isNotBlank()) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun AttachmentPreview(
    bitmap: Bitmap?,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    bitmap?.let {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(120.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(8.dp)
                )
        ) {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Attachment preview",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear attachment",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Add this utility function to handle text styling
private fun parseStyledText(text: String): List<StyledTextSegment> {
    val segments = mutableListOf<StyledTextSegment>()
    var currentIndex = 0
    var currentText = ""
    
    while (currentIndex < text.length) {
        when {
            text.startsWith("**", currentIndex) -> {
                if (currentText.isNotEmpty()) {
                    segments.add(StyledTextSegment(currentText, FontWeight.Normal))
                    currentText = ""
                }
                val endIndex = text.indexOf("**", currentIndex + 2)
                if (endIndex != -1) {
                    segments.add(
                        StyledTextSegment(
                        text.substring(currentIndex + 2, endIndex),
                        FontWeight.Bold
                    )
                    )
                    currentIndex = endIndex + 2
                    continue
                }
            }
            text.startsWith("*", currentIndex) -> {
                if (currentText.isNotEmpty()) {
                    segments.add(StyledTextSegment(currentText, FontWeight.Normal))
                    currentText = ""
                }
                val endIndex = text.indexOf("*", currentIndex + 1)
                if (endIndex != -1) {
                    segments.add(
                        StyledTextSegment(
                        text.substring(currentIndex + 1, endIndex),
                        FontWeight.SemiBold
                    )
                    )
                    currentIndex = endIndex + 1
                    continue
                }
            }
            else -> {
                currentText += text[currentIndex]
            }
        }
        currentIndex++
    }
    
    if (currentText.isNotEmpty()) {
        segments.add(StyledTextSegment(currentText, FontWeight.Normal))
    }
    
    return segments
}

private data class StyledTextSegment(
    val text: String,
    val weight: FontWeight
)

