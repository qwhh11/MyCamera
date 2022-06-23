package com.myapp.mycamera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class DownLoad extends AppCompatActivity {

    private Button download1;
    private Button yulan1;
    private Button download2;
    private Button yulan2;
    private Button download3;
    private Button yulan3;

    private Button update;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_load);

        download1=findViewById(R.id.download1);
        yulan1=findViewById(R.id.yulan1);
        download2=findViewById(R.id.download2);
        yulan2=findViewById(R.id.yulan2);
        download3=findViewById(R.id.download3);
        yulan3=findViewById(R.id.yulan3);

        update=findViewById(R.id.update);

        download1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inten=new Intent();
                inten.setAction(Intent.ACTION_VIEW);
                inten.setData(Uri.parse("https://www.pgyer.com/PJsx"));
                startActivity(inten);
            }
        });
        yulan1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inten=new Intent();
                inten.setAction(Intent.ACTION_VIEW);
                inten.setData(Uri.parse("https://v.kuaishou.com/lsxnP3"));
                startActivity(inten);
            }
        });

        download2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inten=new Intent();
                inten.setAction(Intent.ACTION_VIEW);
                inten.setData(Uri.parse("https://www.pgyer.com/tAfn"));
                startActivity(inten);
            }
        });
        yulan2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inten=new Intent();
                inten.setAction(Intent.ACTION_VIEW);
                inten.setData(Uri.parse("https://v.kuaishou.com/jdBA9x"));
                startActivity(inten);
            }
        });
        download3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inten=new Intent();
                inten.setAction(Intent.ACTION_VIEW);
                inten.setData(Uri.parse("https://www.pgyer.com/Q7Wr"));
                startActivity(inten);
            }
        });
        yulan3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inten=new Intent();
                inten.setAction(Intent.ACTION_VIEW);
                inten.setData(Uri.parse("https://v.kuaishou.com/lTOaQp"));
                startActivity(inten);
            }
        });



        String extras=getIntent().getStringExtra("extras");
        if (extras!=null){
            String s6 = extras.substring(8,extras.length()-2); //返回一个新字符串，内容为指定位置开始到字符串末尾的所有字符
            String s7 = s6.replace("\\",""); //返回一个url
            url=s7;
        }else {
            url="https://www.pgyer.com/Q7Wr";
        }

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });





    }
}