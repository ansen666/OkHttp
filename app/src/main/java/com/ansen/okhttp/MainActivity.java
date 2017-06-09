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
    private OkHttpClient client = new OkHttpClient();//创建okHttpClient对象
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
                getUserInfo();
            }else if(view.getId()==R.id.btn_post){
                login();
            }else if(view.getId()==R.id.btn_upload_file){//上传文件
                uploadFile();
            }
        }
    };

    private void uploadFile(){
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.addFormDataPart("username", "ansen");//表单参数
        builder.addFormDataPart("password", "123456");//表单参数

        builder.setType(MultipartBody.FORM);
        MediaType mediaType = MediaType.parse("application/octet-stream");

        byte[] bytes=getUploadFileBytes();//获取文件内容存入byte数组
        //上传文件 参数1:name 参数2:文件名称 参数3:文件byte数组
        builder.addFormDataPart("upload_file", "ansen.txt",RequestBody.create(mediaType,bytes));
        RequestBody requestBody = builder.build();
        Request.Builder requestBuider = new Request.Builder();
        requestBuider.url("http://139.196.35.30:8080/OkHttpTest/uploadFile.do");
        requestBuider.post(requestBody);
        execute(requestBuider);
    }


    private byte[] getUploadFileBytes(){
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
        return bytes;
    }

    private void getUserInfo(){
        //创建一个Request
        Request.Builder builder = new Request.Builder().url("http://139.196.35.30:8080/OkHttpTest/getUserInfo.do");
        execute(builder);
    }

    private void login(){
        //把请求参数封装到RequestBody里面
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("username","ansen");//请求参数一
        formBuilder.add("password","123");//请求参数二
        RequestBody requestBody = formBuilder.build();

        Request.Builder builder = new Request.Builder().url("http://139.196.35.30:8080/OkHttpTest/login.do").post(requestBody);
        execute(builder);
    }

    //执行请求
    private void execute(Request.Builder builder){
        Call call = client.newCall(builder.build());
        call.enqueue(callback);//加入调度队列
    }

    //请求回调
    private Callback callback=new Callback(){
        @Override
        public void onFailure(Call call, IOException e) {
            Log.i("MainActivity","onFailure");
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            //从response从获取服务器返回的数据，转成字符串处理
            String str = new String(response.body().bytes(),"utf-8");
            Log.i("MainActivity","onResponse:"+str);

            //通过handler更新UI
            Message message=handler.obtainMessage();
            message.obj=str;
            message.sendToTarget();
        }
    };
}
