package com.example.pennypilot.presentation.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.pennypilot.R
import com.example.pennypilot.data.repository.Conversation
import com.example.pennypilot.presentation.viewmodel.AiAssistantState
import com.example.pennypilot.presentation.viewmodel.AiAssistantViewModel
import com.example.pennypilot.presentation.viewmodel.ChatMessage
import com.example.pennypilot.presentation.viewmodel.ChatHistoryViewModel
import com.example.pennypilot.presentation.viewmodel.HistoryUiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// --- Theme Colors ---
private val AiAssistantScreenBackground = Color(0xFF01140A)
private val AiAssistantPrimaryText = Color.White
private val AiAssistantSecondaryText = Color(0xFFB0B0B0)
private val AiAssistantAccentGlow = Color(0xFF00FF7F)
private val AiAssistantErrorColor = Color(0xFFCF6679)
private val AiMessageBubbleColor = Color(0xFF222222)
private val BottomSheetBackgroundColor = Color(0xFF1C1C1E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    navController: NavController,
    viewModel: AiAssistantViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var userPrompt by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // --- State for the Modal Bottom Sheet ---
    val sheetState = rememberModalBottomSheetState()
    var showHistorySheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AiAssistantTopBar(
                onBackClicked = { navController.popBackStack() },
                onHistoryClicked = {
                    showHistorySheet = true // Open the bottom sheet
                }
            )
        },
        bottomBar = {
            MessageInput(
                text = userPrompt,
                onTextChange = { userPrompt = it },
                onSendMessage = {
                    if (userPrompt.isNotBlank()) {
                        viewModel.getSuggestion(userPrompt)
                        userPrompt = "" // Clear input field after sending
                    }
                },
                isLoading = uiState is AiAssistantState.Loading
            )
        },
        containerColor = AiAssistantScreenBackground
    ) { paddingValues ->
        // --- Main Chat UI ---
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            val conversation = when (val state = uiState) {
                is AiAssistantState.Success -> state.conversation
                is AiAssistantState.Loading -> (viewModel.uiState.value as? AiAssistantState.Success)?.conversation ?: emptyList()
                else -> emptyList()
            }

            if (conversation.isEmpty() && uiState is AiAssistantState.Initial) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    InitialView()
                }
            } else {
                ChatListView(messages = conversation, listState = lazyListState)
            }

            when (val state = uiState) {
                is AiAssistantState.Loading -> {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp), contentAlignment = Alignment.BottomCenter) {
                        LoadingView()
                    }
                }
                is AiAssistantState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        ErrorView(message = state.message)
                    }
                }
                else -> {}
            }

            LaunchedEffect(conversation.size) {
                if (conversation.isNotEmpty()) {
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(conversation.size - 1)
                    }
                }
            }
        }

        // --- Modal Bottom Sheet for History ---
        if (showHistorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showHistorySheet = false },
                sheetState = sheetState,
                containerColor = BottomSheetBackgroundColor
            ) {
                ChatHistorySheetContent(
                    onConversationClicked = { conversationId ->
                        viewModel.loadConversation(conversationId)
                        coroutineScope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showHistorySheet = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiAssistantTopBar(
    onBackClicked: () -> Unit,
    onHistoryClicked: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("AI Assistant", color = AiAssistantPrimaryText, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AiAssistantPrimaryText
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = AiAssistantPrimaryText
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(BottomSheetBackgroundColor)
                ) {
                    DropdownMenuItem(
                        text = { Text("Chat History", color = AiAssistantPrimaryText) },
                        onClick = {
                            menuExpanded = false
                            onHistoryClicked()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AiAssistantScreenBackground)
    )
}

@Composable
private fun ChatHistorySheetContent(
    historyViewModel: ChatHistoryViewModel = hiltViewModel(),
    onConversationClicked: (String) -> Unit
) {
    val uiState by historyViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        historyViewModel.loadHistory()
    }

    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Text(
            text = "Chat History",
            style = MaterialTheme.typography.titleLarge,
            color = AiAssistantPrimaryText,
            modifier = Modifier.padding(16.dp)
        )

        when (val state = uiState) {
            is HistoryUiState.Loading -> {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)) {
                    CircularProgressIndicator(color = AiAssistantAccentGlow)
                }
            }
            is HistoryUiState.Success -> {
                if (state.conversations.isEmpty()) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)) {
                        Text("No chat history found.", color = AiAssistantSecondaryText)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
                        items(state.conversations) { conversation ->
                            ConversationHistoryItem(
                                conversation = conversation,
                                onItemClick = { onConversationClicked(conversation.id) }
                            )
                            HorizontalDivider(color = AiAssistantSecondaryText.copy(alpha = 0.2f))
                        }
                    }
                }
            }
            is HistoryUiState.Error -> {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)) {
                    Text(state.message, color = AiAssistantErrorColor, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun ConversationHistoryItem(
    conversation: Conversation,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.title,
                color = AiAssistantPrimaryText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTimestamp(conversation.lastUpdated),
                color = AiAssistantSecondaryText,
                fontSize = 12.sp
            )
        }
    }
}

private fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

// --- Included UI Composables from previous versions for completeness ---

@Composable
fun ChatListView(messages: List<ChatMessage>, listState: LazyListState) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(messages) { message ->
            ChatMessageItem(message = message)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Text(
            text = message.text,
            color = AiAssistantPrimaryText,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (message.isFromUser) AiAssistantAccentGlow.copy(alpha = 0.2f) else AiMessageBubbleColor)
                .padding(12.dp)
        )
    }
}

@Composable
fun InitialView() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
        Icon(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "AI Assistant Logo",
            tint = AiAssistantAccentGlow,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(AiAssistantAccentGlow.copy(alpha = 0.1f))
                .padding(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "How can I help you today?",
            color = AiAssistantSecondaryText,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LoadingView() {
    CircularProgressIndicator(color = AiAssistantAccentGlow)
}

@Composable
fun ErrorView(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = "Error",
            tint = AiAssistantErrorColor,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = AiAssistantErrorColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AiAssistantScreenBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ask for financial tips...") },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = AiAssistantSecondaryText.copy(alpha = 0.5f),
                focusedBorderColor = AiAssistantPrimaryText,
                focusedTextColor = AiAssistantPrimaryText,
                unfocusedTextColor = AiAssistantPrimaryText,
                cursorColor = AiAssistantPrimaryText,
                unfocusedContainerColor = AiMessageBubbleColor.copy(alpha = 0.5f),
                focusedContainerColor = AiMessageBubbleColor.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(24.dp),
            enabled = !isLoading,
            maxLines = 5,
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSendMessage,
            enabled = !isLoading && text.isNotBlank(),
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (!isLoading && text.isNotBlank()) AiAssistantAccentGlow.copy(alpha = 0.9f) else AiAssistantSecondaryText.copy(
                        alpha = 0.2f
                    )
                )
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black)
        }
    }
}
