package com.example.lenovo.myapplication;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText edit_username;
    EditText edit_password;
    TextView tv_register;
    MainBroadcast mainBroadcast;
    TextView tip;
    CircleImageView loginAvator;
    Button btn;
    Bitmap bitmapFromNet;
    ProgressDialog waitDialog;
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1000)
            {
                Toast.makeText(MainActivity.this, "登录成功,用户名：" + msg.getData().get("username").toString() + "密码：" +
                        msg.getData().get("password").toString()
                        ,Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this, MessageUi.class);
                Bundle b = new Bundle();
                b.putString("username",edit_username.getText().toString());
                b.putString("password",edit_password.getText().toString());
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
            else if(msg.what == 1001)
            {
                Toast.makeText(MainActivity.this, "服务器问题",Toast.LENGTH_SHORT).show();
            }
            else if(msg.what == 1002)
            {
                Toast.makeText(MainActivity.this, "该用户已在线上", Toast.LENGTH_SHORT).show();
            }
            else if(msg.what == 1003)
            {
                loginAvator.setImageBitmap(bitmapFromNet);
            }
            else if(msg.what == 1004)
            {
                Toast.makeText(MainActivity.this, "登录失败，请检查用户名或密码是否正确", Toast.LENGTH_SHORT).show();
                XmppUtil.getConnection().disconnect();
                new Thread()
                {
                    @Override
                    public void run() {
                        while(!XmppUtil.connect())
                        {
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Log.v("MainActivity","连接不成功");
                        }
                        Log.v("MainActivity","连接成功");
                        super.run();
                    }
                }.start();
            }
            waitDialog.dismiss();
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(mainBroadcast);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.login);
        tip = (TextView)findViewById(R.id.textview_network_tip);
        edit_username = (EditText)findViewById(R.id.edit_username);
        edit_password = (EditText)findViewById(R.id.edit_pass);
        loginAvator = (com.example.lenovo.myapplication.CircleImageView)findViewById(R.id.mainactivity_avator);
        tv_register = (TextView)findViewById(R.id.textview_register);
        waitDialog = new ProgressDialog(MainActivity.this);
        mainBroadcast = new MainBroadcast();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mainBroadcast,filter); //注册广播
        new Thread()
        {
            @Override
            public void run() {
                while(!XmppUtil.connect())
                {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.v("MainActivity","连接不成功");
                }
                Log.v("MainActivity","连接成功");
                super.run();
            }
        }.start();
        if(!CheckNetWork.netAvailable(MainActivity.this))
        {
            Toast.makeText(MainActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
            tip.setVisibility(View.VISIBLE);
        }
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisteActivity.class);
                startActivity(intent);
            }
        });
        edit_username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b)
                {
                   Bitmap bitmap = setAvatorFromCache(edit_username.getText().toString());
                   if(bitmap != null)
                   {
                       loginAvator.setImageBitmap(bitmap);
                   }
                   else
                   {
                       new Thread()
                       {
                           @Override
                           public void run() {
                               bitmapFromNet = setAvatorFromNet(edit_username.getText().toString());
                               if(bitmapFromNet == null)
                               {
                                   return;
                               }
                               Message msg = new Message();
                               msg.what = 1003;
                               handler.sendMessage(msg);
                               super.run();
                           }
                       }.start();
                   }
                }
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edit_username.getText().toString().equals("") || edit_password.getText().toString().equals(""))
                {
                    Toast.makeText(MainActivity.this,"用户及密码信息不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    checkAcount(edit_username.getText().toString(),edit_password.getText().toString());
                    showDialog();
                }
            }
        });
    }
    private void checkAcount(final String username, final String password) {
        new Thread()
        {
            @Override
            public void run() {
                if (!XmppUtil.checkConnection()) {
                    handler.sendEmptyMessage(1001);
                } else {
                    try {
                        Log.v("MainActivtity",edit_username.getText().toString());
                        Log.v("MainActivtity",edit_password.getText().toString());
                        XmppUtil.getConnection().login(edit_username.getText().toString(), edit_password.getText().toString());
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putString("username", username);
                        data.putString("password", password);
                        msg.setData(data);
                        msg.what = 1000;
                        handler.sendMessage(msg);
                    } catch (XMPPException e) {
                        e.printStackTrace();
                        Log.v("MainActivity","Xmpp");
                        handler.sendEmptyMessage(1004);
                    } catch (SmackException e) {
                        e.printStackTrace();
                        Log.v("MainActivity","Smack");
                        handler.sendEmptyMessage(1004);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.v("MainActivity","IO");
                        handler.sendEmptyMessage(1004);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.v("MainActivity","Interrupt");
                        handler.sendEmptyMessage(1004);
                    }
                    return;
                }
                super.run();
            }
        }.start();
    }
    //从内存中获取头像
    public Bitmap setAvatorFromCache(String name)
    {
        Bitmap bitmap = null;
        String path = getExternalCacheDir().getPath();
        File file = new File(path);
        File[] files = file.listFiles();
        if(files.length != 0)
        {
            for(int i = 0; i < files.length; i++)
            {
                if(files[i].getName().equals(name + "crop.jpg"))
                {
                    bitmap = BitmapFactory.decodeFile(files[i].getPath());
                }
            }
        }
        return bitmap;
    }
    //如果内存中尚未存放相应的位图，则从服务器中获取位图
    public Bitmap setAvatorFromNet(final String name)
    {
        byte[] data = NetUtil.getAvator("http://192.168.43.208:8080/IM/InfoServlet?username=" + name);
        if(data != null)
        {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        else
        {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.tempavator);
            return bmp;
        }
    }
    //显示底部对话框
    private void showDialog()
    {
        waitDialog.setTitle("login");
        waitDialog.setMessage("Please wait for a moment...");
        waitDialog.setIndeterminate(true);
        waitDialog.setCancelable(false);
        waitDialog.show();
    }
    //该广播通过联网状态来控制tip提示是否可见
    class MainBroadcast extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            boolean isWifi = networkInfo.isConnected();
            networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            boolean isMobile = networkInfo.isConnected();
            if(!isWifi && !isMobile)
            {
                tip.setVisibility(View.VISIBLE);
            }
            else
            {
                tip.setVisibility(View.INVISIBLE);
            }
        }
    }
}
