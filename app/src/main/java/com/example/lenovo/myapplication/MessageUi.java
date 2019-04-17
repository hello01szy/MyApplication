package com.example.lenovo.myapplication;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageUi extends AppCompatActivity implements View.OnClickListener{
    private MessageFragment messageFragment;
    private NewsFragment newsFragment;
    private ContactsFragment contactsFragment;
    private View messageLayout;
    private View newsLayout;
    private View contactsLayout;
    private FragmentManager fragmentManager;
    private ImageView messageImage;
    private ImageView contactsImage;
    private ImageView newsImage;
    private ImageView addImage;
    private com.example.lenovo.myapplication.CircleImageView titleAvator;
    private byte[] data = null;
    public static String loginName;
    public static List<Msg> listMsg = new ArrayList<Msg>();
    public static ChatAdapter adapter;
    Bitmap img;
    Bundle bundle;
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1200)
            {
                titleAvator.setImageBitmap(img);
            }
            else if(msg.what == 1000)
            {
                titleAvator.setImageResource(R.drawable.tempavator);
                Log.v("MessageUI","图片获取失败");
            }
            super.handleMessage(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        bundle = getIntent().getExtras();
        loginName = (String) bundle.get("username");
        setContentView(R.layout.activity_message_ui);
        iniUI();
        fragmentManager = getFragmentManager();
        setIndex(0);
        setTitleAvator();
        XmppUtil.getAllGroup();
    }

    @Override
    protected void onResume() {
        setTitleAvator();
        super.onResume();
    }
    //进入主页面之后对头像进行设置
    public void setTitleAvator()
    {
        boolean flag = false;
        String path = getExternalCacheDir().getPath();
        File file = new File(path);
        File[] files = file.listFiles();
        if(files.length != 0)
        {
            for(int i = 0; i < files.length; i++)
            {
                if(files[i].getName().equals(bundle.get("username") + "crop.jpg"))
                {
                    titleAvator.setImageBitmap(BitmapFactory.decodeFile(files[i].getPath()));
                    flag = true;
                }
            }
        }
        if(!flag)
        {
            getAvatorFromNet();
        }
    }
    /*如果内存中没有头像信息直接从网络获取*/
    public void getAvatorFromNet()
    {
        new Thread()
        {
            @Override
            public void run() {
                Message msg = new Message();
                data = NetUtil.getAvator("http://192.168.43.208:8080/IM/InfoServlet?username=" + bundle.get("username").toString());
                if(data != null)
                {
                    img = BitmapFactory.decodeByteArray(data, 0, data.length);
                    msg.what = 1200;
                    handler.sendMessage(msg);
                }
                else
                {
                    msg.what = 1000;
                    handler.sendMessage(msg);
                }
                super.run();
            }
        }.start();
    }
    //初始化各种UI控件
    public void iniUI()
    {
        messageLayout = findViewById(R.id.message_layout);
        newsLayout = findViewById(R.id.news_layout);
        contactsLayout = findViewById(R.id.contacts_layout);
        messageImage = (ImageView) findViewById(R.id.message_image);
        newsImage = (ImageView)findViewById(R.id.icon_news);
        contactsImage = (ImageView)findViewById(R.id.icon_contacts);
        messageLayout.setOnClickListener(this);
        newsLayout.setOnClickListener(this);
        contactsLayout.setOnClickListener(this);
        titleAvator = (com.example.lenovo.myapplication.CircleImageView)findViewById(R.id.title_image);
        titleAvator.setOnClickListener(this);
        addImage = (ImageView)findViewById(R.id.message_add);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenu(addImage);
            }
        });
    }
    //显示添加好友/添加群组对话框
    private void showMenu(View view)
    {
        final PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.poup, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch(menuItem.getItemId())
                {
                    case R.id.addFriends:
                        Toast.makeText(MessageUi.this, "添加好友", Toast.LENGTH_SHORT).show();
                        Intent intent_f = new Intent(MessageUi.this, AddFOrG.class);
                        intent_f.putExtras(bundle);
                        startActivity(intent_f);
                        break;
                    case R.id.createGroup:
                        Toast.makeText(MessageUi.this, "创建一个群", Toast.LENGTH_SHORT).show();
                        break;
                }
                popupMenu.dismiss();
                return true;
            }
        });
        popupMenu.show();
    }
    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.message_layout:
                setIndex(0);
                break;
            case R.id.contacts_layout:
                setIndex(1);
                break;
            case R.id.news_layout:
                setIndex(2);
                break;
            case R.id.title_image:
                Intent intent = new Intent(MessageUi.this, EditInfo.class);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
        }
    }
    public void setIndex(int index)
    {
        clearSelection();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        hideFragment(ft);
        switch(index)
        {
            case 0:
                messageImage.setImageResource(R.drawable.message_a);
                if(messageFragment == null)
                {
                    messageFragment = new MessageFragment();
                    ft.add(R.id.contents,messageFragment);
                    ft.show(messageFragment);
                }
                else
                {
                    ft.show(messageFragment);
                }
                break;
            case 1:
                contactsImage.setImageResource(R.drawable.contact_a);
                if(contactsFragment == null)
                {
                    contactsFragment = new ContactsFragment();
                    ft.add(R.id.contents, contactsFragment);
                    ft.show(contactsFragment);
                }
                else
                {
                    ft.show(contactsFragment);
                }
                break;
            case 2:
                newsImage.setImageResource(R.drawable.news_a);
                if(newsFragment == null)
                {
                    newsFragment = new NewsFragment();
                    ft.add(R.id.contents, newsFragment);
                    ft.show(newsFragment);
                }
                else
                {
                    ft.show(newsFragment);
                }
                break;
        }
        ft.commit();
    }

    private void hideFragment(FragmentTransaction ft) {
        if(messageFragment != null)
        {
            ft.hide(messageFragment);
        }
        if(contactsFragment != null)
        {
            ft.hide(contactsFragment);
        }
        if(newsFragment != null)
        {
            ft.hide(newsFragment);
        }
    }

    private void clearSelection() {
        messageImage.setImageResource(R.drawable.message_b);
        contactsImage.setImageResource(R.drawable.contact_b);
        newsImage.setImageResource(R.drawable.news_b);
    }

    @Override
    protected void onDestroy() {
        Log.v("MessageUI","我被销毁了，连接就断了");
        XmppUtil.getConnection().disconnect();
        super.onDestroy();
    }
}
