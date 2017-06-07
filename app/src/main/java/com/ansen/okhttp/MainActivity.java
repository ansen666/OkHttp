package com.ansen.okhttp;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private OkHttpClient client = new OkHttpClient();
    private TextView tvResult;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String result= (String) msg.obj;
            tvResult.setText(result);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvResult= (TextView) findViewById(R.id.tv_result);;
        findViewById(R.id.btn_get).setOnClickListener(onClickListener);
        findViewById(R.id.btn_post).setOnClickListener(onClickListener);
        findViewById(R.id.btn_upload_file).setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId()==R.id.btn_get){
                getUserInfo("http://139.196.35.30:8080/OkHttpTest/getUserInfo.do");
            }else if(view.getId()==R.id.btn_post){
                login("http://139.196.35.30:8080/OkHttpTest/login.do");
            }else if(view.getId()==R.id.btn_upload_file){//上传文件
                postFile("http://139.196.35.30:8080/OkHttpTest/uploadFile.do");
            }
        }
    };

    private void postFile(String url){
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.addFormDataPart("username", "ansen");
        builder.addFormDataPart("password", "123456");

        builder.setType(MultipartBody.FORM);
        MediaType mediaType = MediaType.parse("application/octet-stream");
        byte[] bytes=null;
        try {
            InputStream inputStream = getAssets().open("ansen.txt");
            Log.i("ansen","文件长度:"+inputStream.available());
            bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        builder.addFormDataPart("upload_file", "ansen.txt",RequestBody.create(mediaType,bytes));
        RequestBody requestBody = builder.build();
        Request.Builder requestBuider = new Request.Builder();
        requestBuider.url(url);
        requestBuider.post(requestBody);
        execute(requestBuider);
    }

    private void getUserInfo(String url){
        Request.Builder builder = new Request.Builder().url(url);
        execute(builder);
    }

    private void login(String url){
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("username","ansen");
        formBuilder.add("password","123");
        RequestBody requestBody = formBuilder.build();

        Request.Builder builder = new Request.Builder().url(url).post(requestBody);
        execute(builder);
    }

    private void execute(Request.Builder builder){
        Call call = client.newCall(builder.build());
        call.enqueue(callback);
    }

    private Callback callback=new Callback(){
        @Override
        public void onFailure(Call call, IOException e) {
            Log.i("MainActivity","onFailure");
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String str = new String(response.body().bytes(),"utf-8");
            Log.i("MainActivity","onResponse:"+str);

            Message message=handler.obtainMessage();
            message.obj=str;
            message.sendToTarget();
        }
    };
}
