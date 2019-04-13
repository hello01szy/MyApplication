package com.example.lenovo.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class EditInfo extends AppCompatActivity implements View.OnClickListener {

    EditText infoUsername;
    EditText nickName;
    Bundle bundle;
    ImageView back;
    ImageView avator;
    Button btn_ok;
    Bitmap img;
    private File file;
    Uri imageUri;
    String name = null;
    byte[] data = null;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1000) {
                avator.setImageBitmap(img);
                saveImageToExternal(img);
            } else if (msg.what == 1002) {
                avator.setImageResource(R.drawable.bigtemp);
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);
        int permission = ActivityCompat.checkSelfPermission(EditInfo.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        bundle = getIntent().getExtras();
        infoUsername = (EditText) findViewById(R.id.info_username);
        back = (ImageView) findViewById(R.id.info_back);
        infoUsername.setText(bundle.get("username").toString());
        nickName = (EditText) findViewById(R.id.info_nickname);
        avator = (com.example.lenovo.myapplication.CircleImageView) findViewById(R.id.info_avator);
        btn_ok = (Button) findViewById(R.id.info_btn_ok);
        btn_ok.setOnClickListener(this);
        back.setOnClickListener(this);
        avator.setOnClickListener(this);
        name = bundle.get("username").toString();
        if (!setImage()) {
            new Thread() {
                @Override
                public void run() {
                    data = NetUtil.getAvator("http://192.168.43.208:8080/IM/InfoServlet?username=" + bundle.get("username").toString());
                    Message message = new Message();
                    if (data != null) {
                        message.what = 1000;
                        img = BitmapFactory.decodeByteArray(data, 0, data.length);
                        handler.sendMessage(message);
                    } else {
                        message.what = 1002;
                        handler.sendMessage(message);
                    }
                    super.run();
                }
            }.start();
        }
    }

    //从内存中加载，获取头像，获取成功返回真，获取失败返回假
    public boolean setImage() {
        boolean flag = false;
        String path = getExternalCacheDir().getPath();
        File file = new File(path);
        File[] files = file.listFiles();
        if (files.length != 0) {
            //在内存中寻找对应头像
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().equals(bundle.get("username") + "crop.jpg")) {
                    avator.setImageBitmap(BitmapFactory.decodeFile(files[i].getPath()));
                    flag = true;
                }
            }
        }
        return flag;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.info_back:
                finish();
                break;
            case R.id.info_btn_ok:
                new Thread() {
                    @Override
                    public void run() {

                        super.run();
                    }
                }.start();
                break;
            case R.id.info_avator:
                show();
                break;
        }
    }

    //弹出拍照或者从相册选取提示框
    private void show() {
        final Dialog bottomDialog = new Dialog(this, R.style.BottomDialog);
        View contentView = LayoutInflater.from(this).inflate(R.layout.bottom_dialog, null);
        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.setCancelable(true);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.getWindow().findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
                useCamera();
            }
        });
        bottomDialog.getWindow().findViewById(R.id.album).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
                useAlbum();
            }
        });
        bottomDialog.show();
    }

    //开启相册
    private void useAlbum() {
        Intent albumI = new Intent(Intent.ACTION_GET_CONTENT);
        albumI.setType("image/*");
        startActivityForResult(albumI, 200);
    }

    //将图片从网络端加载之后保存在内存中
    private void saveImageToExternal(Bitmap bitmap)
    {
         String filePath = getExternalCacheDir().getPath();
         File newFile = new File(filePath, name + "crop.jpg");
        try {
            FileOutputStream fos = new FileOutputStream(newFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //打开相机
    private void useCamera() {
        File outputFile = new File(getExternalCacheDir(), bundle.get("username").toString() + ".jpg");
        if (outputFile.exists()) {
            outputFile.delete();
        }
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(this, "com.example.lenovo.myapplication.fileProvider", outputFile);
        } else {
            imageUri = Uri.fromFile(outputFile);
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, 100);
    }

    //对图像进行剪裁
    private void cropPhoto(Uri uri) {
        File cropFile = new File(getExternalCacheDir(), bundle.get("username") + "crop.jpg");
        if (cropFile.exists()) {
            cropFile.delete();
        }
        try {
            cropFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageUri = Uri.fromFile(cropFile);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        intent.putExtra("return-data", true);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, 300);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 100:
                if (resultCode == RESULT_CANCELED) {
                    return;
                }
                cropPhoto(imageUri);
                break;
            case 200:
                if (resultCode == RESULT_CANCELED) {
                    return;
                }
                final Uri uri = data.getData();
                String oldPath = GetPathFromUri.getPath(this, uri);
                if (oldPath != null) {
                    CopyFile.copyFile(oldPath, getExternalCacheDir().getPath() + bundle.get("username") + ".jpg");
                    Log.v("EditInfo", "复制完成");
                }
                cropPhoto(uri);
                break;
            case 300:
                if (resultCode == RESULT_CANCELED) {
                    return;
                }
                if (data == null) {
                    return;
                } else {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        final Bitmap bt = extras.getParcelable("data");
                        avator.setImageBitmap(bt);
                        new Thread() {
                            @Override
                            public void run() {
                                upImage();
                                super.run();
                            }
                        }.start();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //将图片上传至服务器
    public void upImage() {
        String url = "http://192.168.43.208:8080/IM/GetImageServlet";

        Map<String ,String> map = new HashMap<String, String>();
        map.put("username", name);
        String filePath = getExternalCacheDir().getPath() + "/" + name + "crop.jpg";
        Log.v("EditInfo",filePath);
        BufferedInputStream bis = null;
        byte[] body_data = null;
        try
        {
            bis = new BufferedInputStream(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.v("EditInfo","上传失败");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int c = 0;
        byte[] buffer = new byte[8 * 1024];
        try
        {
            while((c = bis.read(buffer)) != -1)
            {
                baos.write(buffer, 0, c);
                baos.flush();
            }
            body_data = baos.toByteArray();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String result = doPostSubmit(url, map, filePath, body_data, "utf-8");
        Log.v("EditInfo",result);
    }



    private String doPostSubmit(String url, Map<String, String> map, String filePath, byte[] body_data, String s) {
        final String NEWLINE = "\r\n";
        final String PREFIX = "--";
        final String BOUNDARY = "#";
        HttpURLConnection connection = null;
        BufferedInputStream bis = null;
        DataOutputStream dos = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            URL urlObj =  new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary = " + BOUNDARY);
            connection.connect();
            dos = new DataOutputStream(connection.getOutputStream());
            if(map != null & !map.isEmpty())
            {
                for(Map.Entry<String, String> entry : map.entrySet())
                {
                    String key = entry.getKey();
                    String value = map.get(key);
                    dos.writeBytes(PREFIX + BOUNDARY  + NEWLINE);
                    dos.writeBytes("Content-Disposition: form-data;" + "name=\"" + key + "\"" + NEWLINE);
                    dos.writeBytes(NEWLINE);
                    dos.writeBytes(URLEncoder.encode(value.toString(), s));
                    dos.writeBytes(NEWLINE);
                }
            }
            if(body_data != null && body_data.length > 0)
            {
                dos.writeBytes(PREFIX + BOUNDARY + NEWLINE);
                String fileName = filePath.substring(filePath.lastIndexOf(File.separatorChar));
                dos.writeBytes("Content-Disposition:form-data;" + "name = \"" + "image" + "\"" + "; filename = \"" + fileName + "\"" + NEWLINE);
                dos.writeBytes(NEWLINE);
                dos.write(body_data);
                dos.writeBytes(NEWLINE);
            }
            dos.writeBytes(PREFIX + BOUNDARY + PREFIX + NEWLINE);
            dos.flush();
            byte[] buffer = new byte[8 * 1024];
            int c = 0;
            if(connection.getResponseCode() == 200)
            {
                bis = new BufferedInputStream(connection.getInputStream());
                while((c = bis.read(buffer)) != -1)
                {
                    baos.write(buffer, 0, c);
                    baos.flush();
                }
            }
            return new String(baos.toByteArray(), s);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(bis != null)
                {
                    bis.close();
                }
                if(dos != null)
                {
                    dos.close();
                }
                if(baos != null)
                {
                    baos.close();
                }
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}