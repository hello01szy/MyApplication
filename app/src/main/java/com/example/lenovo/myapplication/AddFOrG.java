package com.example.lenovo.myapplication;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AddFOrG extends AppCompatActivity {

    ListView listView;
    Button btn;
    ArrayAdapter<String> adapter;
    EditText editText;
    List<String> myList = new ArrayList<String>();
    ProgressDialog dialog;
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 1112:
                    adapter = new ArrayAdapter<String>(AddFOrG.this, R.layout.array_item, myList);
                    listView.setAdapter(adapter);
                    dialog.dismiss();
                    break;
                case 1113:
                    Toast.makeText(AddFOrG.this, "未找到该用户", Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_for_g);
        iniView();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myList.clear();
                searchUser("http://192.168.43.208:8080/IM/GetMatchUser", editText.getText().toString());
                showDialog();
            }
        });
    }
    private void iniView() {
        listView = (ListView)findViewById(R.id.find_user_result);
        btn = (Button)findViewById(R.id.search);
        editText = (EditText)findViewById(R.id.search_user);
        dialog = new ProgressDialog(AddFOrG.this);
    }
    private void showDialog()
    {
        dialog.setTitle("waiting");
        dialog.setMessage("Please wait for a moment...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();
    }
    //查找用户
    public void searchUser(final String urlStr, final String username)
    {
        new Thread()
        {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlStr + "?username=" + username);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5*1000);
                    connection.setReadTimeout(5*1000);
                    connection.setRequestMethod("GET");
                    connection.connect();
                    String result = "";
                    if(connection.getResponseCode() == 200)
                    {
                        Log.v("AddFOrG","网络错误");
                        InputStream inputStream = connection.getInputStream();
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        int len = -1;
                        byte[] buff = new byte[1024];
                        while((len = inputStream.read(buff))!=-1)
                        {
                            bos.write(buff, 0, len);
                        }
                        result = new String(bos.toByteArray());
                        List<String> list = (List<String>)JSONArray.parseArray(result, String.class);
                        if(list != null)
                        {
                            myList.addAll(list);
                            for(int i = 0; i < list.size(); i++)
                            {
                                Log.v("AddFOrG",myList.get(i));
                            }
                            handler.sendEmptyMessage(1112);
                        }
                        else
                        {
                            handler.sendEmptyMessage(1113);
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                super.run();
            }
        }.start();
    }
}
