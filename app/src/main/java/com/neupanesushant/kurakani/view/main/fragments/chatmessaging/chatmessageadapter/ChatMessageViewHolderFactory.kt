package com.neupanesushant.kurakani.view.main.fragments.chatmessaging.chatmessageadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.neupanesushant.kurakani.view.main.fragments.chatmessaging.chatmessageadapter.ChatMessageAdapter.Companion.FROM_AUDIO
import com.neupanesushant.kurakani.view.main.fragments.chatmessaging.chatmessageadapter.ChatMessageAdapter.Companion.FROM_IMAGE
import com.neupanesushant.kurakani.view.main.fragments.chatmessaging.chatmessageadapter.ChatMessageAdapter.Companion.FROM_TEXT
import com.neupanesushant.kurakani.view.main.fragments.chatmessaging.chatmessageadapter.ChatMessageAdapter.Companion.TO_AUDIO
import com.neupanesushant.kurakani.view.main.fragments.chatmessaging.chatmessageadapter.ChatMessageAdapter.Companion.TO_IMAGE
import com.neupanesushant.kurakani.view.main.fragments.chatmessaging.chatmessageadapter.ChatMessageAdapter.Companion.TO_TEXT
import com.neupanesushant.kurakani.databinding.*

class ChatMessageViewHolderFactory {
    fun getViewHolder(
        chatMessageAdapter: ChatMessageAdapter,
        parent: ViewGroup,
        viewType: Int
    ): ChatMessageViewHolder {

        val viewTypeHolders = hashMapOf(
            Pair(
                FROM_TEXT, ViewHolderFromText(
                    chatMessageAdapter,
                    ChatMessageFromLayoutBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            ),
            Pair(
                TO_TEXT, ViewHolderToText(
                    chatMessageAdapter,
                    ChatMessageToLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            ),
            Pair(
                FROM_IMAGE, ViewHolderFromImage(
                    chatMessageAdapter,
                    ChatImageFromLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            ),
            Pair(
                TO_IMAGE, ViewHolderToImage(
                    chatMessageAdapter,
                    ChatImageToLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            ),
            Pair(
                FROM_AUDIO, ViewHolderFromAudio(
                    chatMessageAdapter,
                    ChatAudioFromLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            ),
            Pair(
                TO_AUDIO, ViewHolderToAudio(
                    chatMessageAdapter,
                    ChatAudioToLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            )

        )

        return viewTypeHolders[viewType]!!
    }
}