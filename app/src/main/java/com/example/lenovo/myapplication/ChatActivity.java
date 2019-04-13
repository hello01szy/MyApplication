package com.example.lenovo.myapplication;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    ListView listView;
    Button button;
    EditText editText;
    ImageView imageView_back;
    ImageView imageView_me;
    TextView textView;
    String friendName;
    String loginUser;
    ChatAdapter adapter;
    List<Msg> listMsg = MessageUi.listMsg;
    final ChatManager cm = XmppUtil.getChatManager();
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(android.os.Message msg) {
            Message m = new Message();
            m = (Message)msg.obj;
            String formWho = m.getFrom().toString().split("@")[0];
            if(m.getBody() == null)
            {
                return;
            }
            Msg m2 = new Msg(formWho, m.getBody().toString(), GetSystemTime.getDate(), "FROM");
            listMsg.add(m2);
            for(int i = 0; i < listMsg.size(); i++)
            {
                Log.v("ChatActivity",listMsg.get(i).msgbody);
            }
            adapter.notifyDataSetChanged();
            super.handleMessage(msg);
        }
    };
    ChatManagerListener listener = new ChatManagerListener() {
        @Override
        public void chatCreated(Chat chat, boolean b) {
            chat.addMessageListener(new ChatMessageListener() {
                @Override
                public void processMessage(Chat chat, Message message) {
                    Log.v("ChatActivity","从spark发出的信息：" + message.getBody());
                    android.os.Message message1 = new android.os.Message();
                    message1.obj = message;
                    handler.sendMessage(message1);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        Log.v("ChatActivity","onCreate被执行了");
        if(listMsg == null)
        {
            listMsg = new ArrayList<Msg>();
        }
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        friendName = bundle.getString("friendName");
        loginUser = bundle.getString("loginUser");
        iniView();
        chatModel();
        cm.addChatListener(listener);
    }

//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if(keyCode == KeyEvent.KEYCODE_BACK)
//        {
//            finish();
//        }
//        return super.onKeyUp(keyCode, event);
//    }

    @Override
    protected void onDestroy() {
        Log.v("ChatActivity","我被销毁了");
        cm.removeChatListener(listener);
//        listMsg.clear();
//        listMsg = null;
        super.onDestroy();
    }

    private void iniView()
    {
        listView = (ListView)findViewById(R.id.chat_list);
        button = (Button)findViewById(R.id.chat_send);
        textView = (TextView)findViewById(R.id.chat_title);
        textView.setText(friendName);
        editText = (EditText)findViewById(R.id.chat_edit);
        imageView_back = (ImageView)findViewById(R.id.chat_back);
        imageView_me = (ImageView)findViewById(R.id.chat_me);
        if(MessageUi.adapter == null)
        {
            MessageUi.adapter = new ChatAdapter(listMsg, ChatActivity.this);
        }
        adapter = MessageUi.adapter;
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        Log.v("ChatActivity","onResume");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.v("ChatActivity","onStop");
        super.onStop();
    }

        @Override
    protected void onPause() {
        Log.v("ChatActivity","onPause");
        super.onPause();
    }

    private void chatModel()
    {
        if(cm != null)
        {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String content = editText.getText().toString();
                    if(content.length() > 0) {
                        try {
                            EntityJid jid = JidCreate.entityBareFrom(friendName + "@192.168.43.208");
                            Msg msg = new Msg(loginUser, content, GetSystemTime.getDate(), "TO");
                            listMsg.add(msg);
                            Log.v("ChatActivity","从手机发送的消息：" + msg.msgbody);
                            adapter.notifyDataSetChanged();
                            Chat newChat = cm.createChat(jid, null);
                            newChat.sendMessage(content);
                        } catch (XmppStringprepException e) {
                            e.printStackTrace();
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        editText.setText("");
                    }
                }
            });
        }
        else
        {
            Log.v("ChatActiviy","聊天管理器为空");
        }
    }
}
