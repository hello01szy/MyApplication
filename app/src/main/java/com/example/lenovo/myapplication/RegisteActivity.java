package com.example.lenovo.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

public class RegisteActivity extends Activity {
    EditText edit_register_user;
    EditText getEdit_register_pass;
    Button btn_reg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        edit_register_user = (EditText)findViewById(R.id.edit_register_username);
        getEdit_register_pass = (EditText)findViewById(R.id.edit_register_pass);
        btn_reg = (Button)findViewById(R.id.btn_register);
        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if(!XmppUtil.checkConnection())
                    {
                        Toast.makeText(RegisteActivity.this, "未连接服务器", Toast.LENGTH_SHORT).show();
                        new Thread()
                        {
                            @Override
                            public void run() {
                                if(!XmppUtil.connect())
                                {
                                    Log.v("MainActivity","连接不成功");
                                }
                                else
                                {
                                    Log.v("MainActivity","连接成功");
                                }
                                super.run();
                            }
                        }.start();
                    }
                    else
                    {
                        Localpart user = null;
                        try {
                            user = Localpart.from(edit_register_user.getText().toString());
                        } catch (XmppStringprepException e) {
                            e.printStackTrace();
                        }
                        String pass = getEdit_register_pass.getText().toString();
                        try {
                            AccountManager accountManager =  AccountManager.getInstance(XmppUtil.getConnection());
                            if (accountManager.supportsAccountCreation()) {
                                accountManager.sensitiveOperationOverInsecureConnection(true);
                                accountManager.createAccount(user, pass);
                            }
                            Toast.makeText(RegisteActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                            new Thread()
                            {
                                @Override
                                public void run() {
                                    String url = "http://192.168.43.208:8080/IM/InsertServlet?username=" + edit_register_user.getText().toString();
                                    if(NetUtil.sendRequest(url))
                                    {
                                        Log.v("RegisterActivity",edit_register_user.getText().toString() + "插入成功");
                                    }
                                    else
                                    {
                                        Log.v("RegisterActivity",edit_register_user.getText().toString() + "插入失败");
                                    }
                                    super.run();
                                }
                            }.start();
                        } catch (SmackException.NoResponseException e) {
                            e.printStackTrace();
                            Toast.makeText(RegisteActivity.this,"出现异常",Toast.LENGTH_SHORT).show();
                        } catch (XMPPException.XMPPErrorException e) {
                            e.printStackTrace();
                            Toast.makeText(RegisteActivity.this,"出现异常",Toast.LENGTH_SHORT).show();
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                            Toast.makeText(RegisteActivity.this,"出现异常",Toast.LENGTH_SHORT).show();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Toast.makeText(RegisteActivity.this,"出现异常",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        });
    }
}
