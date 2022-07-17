package com.example.zoomvideocompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zoomvideocompose.ui.theme.ZoomVideoComposeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatView(viewModel: ZoomViewModel, modifier: Modifier = Modifier) {
    val isJoined = viewModel.isJoined.observeAsState()
    val messages = viewModel.messages.observeAsState()
    val messageCount = viewModel.messageCount.observeAsState()
    val state = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    val me: String
    var chatMessages = ArrayList<ChatMessage>()
    val keyboardController = LocalSoftwareKeyboardController.current

    if (isJoined.value == true) {
        me = viewModel.mySelf.value?.getName()!!
        chatMessages = messages.value!!
    } else {
        // デバッグ用
        me = "自分"
        chatMessages.add(ChatMessage("user1", "メッセージ1"))
        chatMessages.add(ChatMessage("user2", "メッセージ2"))
        chatMessages.add(ChatMessage("user3", "メッセージ3"))
        chatMessages.add(ChatMessage(me, "返信メッセージ1"))
    }

    Column(modifier = modifier) {
        LazyColumn(
            modifier = modifier
                .background(Color(red = 211, green = 216, blue = 231))
                .padding(2.dp),
            state
        ) {
            if (messageCount.value!! >= 0) {
                items(chatMessages) { message ->
                    MessageItemView(me, message)
                    Spacer(modifier = Modifier.padding(bottom = 4.dp))
                }

                // 末尾へスクロール
                // ref: https://stackoverflow.com/questions/65065285/jetpack-compose-lazycolumn-programmatically-scroll-to-item
                CoroutineScope(Dispatchers.Main).launch {
                    if (messageCount.value!! > 0) {
                        state.scrollToItem(messageCount.value!! - 1, 0)
                    }
                }
            }
        }

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = { Text("メッセージを入力してください") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                // キーボードを閉じる
                // ref: https://qiita.com/penguinshunya/items/a9986fa2a0663d07e1cc
                keyboardController?.hide()
                viewModel.sendMessage(inputText)
                inputText = ""
            }),
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun MessageItemView(me: String, message: ChatMessage, modifier: Modifier = Modifier) {
    if (message.userName != me) {
        Row(modifier = modifier) {
//            Image(
//                painter = painterResource(id = message.user.icon),
//                contentDescription = "",
//                modifier = Modifier
//                    .height(40.dp)
//                    .padding(start = 2.dp, top = 1.dp)
//                    .clip(CircleShape)
//            )
            Column(
                modifier = Modifier.weight(4f, false)
            ) {
                Text(
                    message.userName,
                    style = MaterialTheme.typography.caption,
                    //fontSize = 8.sp
                )
                Row {
                    YourBalloonText(message.text)
                    Text(
                        message.date,
                        style = MaterialTheme.typography.caption,
                        //fontSize = 8.sp,
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .padding(start = 1.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    } else {
        Row(modifier = modifier) {
            Spacer(modifier = Modifier.weight(1f))
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(4f)
            ) {
                Text(
                    message.userName,
                    style = MaterialTheme.typography.caption,
                    //fontSize = 8.sp,
                    textAlign = TextAlign.End,
                    //modifier = Modifier.fillMaxWidth()
                )
                Row {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .padding(end = 2.dp)
                    ) {
//                        if (message.readCount > 0) {
//                            Text(
//                                " 既読" +
//                                        if (message.readCount >= 2) {
//                                            " ${message.readCount}"
//                                        } else {
//                                            ""
//                                        },
//                                style = MaterialTheme.typography.caption,
//                                //fontSize = 8.sp,
//                                modifier = Modifier.align(Alignment.End)
//                            )
//                        }
                        Text(
                            message.date,
                            style = MaterialTheme.typography.caption,
                            //fontSize = 8.sp,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                    MyBalloonText(message.text)
                }
            }
//            Image(
//                painter = painterResource(id = message.user.icon),
//                contentDescription = "",
//                modifier = Modifier
//                    .height(40.dp)
//                    .padding(start = 2.dp, top = 1.dp)
//                    .clip(CircleShape)
//            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatViewPreview() {
    val viewModel = ZoomViewModel()
    ZoomVideoComposeTheme {
        ChatView(viewModel)
    }
}