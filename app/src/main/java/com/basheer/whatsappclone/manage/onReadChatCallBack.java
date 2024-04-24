package com.basheer.whatsappclone.manage;

import com.basheer.whatsappclone.model.Chats;

import java.util.List;

public interface onReadChatCallBack {
    void onReadSuccess(List<Chats> list);
    void onReadFailed();
}
