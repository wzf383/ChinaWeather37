
package com.example.administrator.chinaweather37;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import android.os.Handler;

import android.os.Message;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;


import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;


public class MainActivity extends AppCompatActivity  implements  Runnable{
     HttpURLConnection  httpConn=null;
    InputStream din=null;
    Vector<String> cityname=new Vector<String>();
    Vector<String> low=new Vector<String>();
    Vector<String> high=new Vector<String>();
    Vector<String> icon=new Vector<String>();
    Vector<Bitmap> bitmap=new Vector<Bitmap>();
    Vector<String> summary=new Vector<String>();
    int weatherIndex[]=new int[20];
    String city="guangzhou";
    boolean bPress=false;
    boolean bHasData=false;
    LinearLayout body;
    Button find;
    EditText value;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("天气查询");
        body=(LinearLayout)findViewById(R.id.my_body);
        find=(Button)findViewById(R.id.find);
        value=(EditText)findViewById(R.id.value);
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                body.removeAllViews();//移除当前的所有结果
                city=value.getText().toString();
                Toast.makeText(MainActivity.this,"正在查询天气信息...",Toast.LENGTH_LONG).show();
                Thread th=new Thread(MainActivity.this);
                th.start();
            }
        });

    }

    @Override
    public void run() {
        cityname.removeAllElements();
        low.removeAllElements();
        high.removeAllElements();
        icon.removeAllElements();
        bitmap.removeAllElements();
        summary.removeAllElements();
        //获取数据
        parseData();
        //下载图片
        downImage();
        //通知UI线程显示结果
        Message message=new Message();
        message.what=1;
       handler.sendMessage(message);
    }
    //获取数据
    public  void  parseData(){
        int i=0;
        String sValue;
        String weatherUrl="http://flash.weather.com.cn/wmaps/xml/"+city+".xml";
        //表示天气情况图标网址
        String  weatherIcon="http://m.weather.com.cn/img/c";
        try {
            URL url = new URL(weatherUrl);
            //建立天气预报查询连接
            httpConn=(HttpURLConnection)url.openConnection();
            //采用get请求方法
            httpConn.setRequestMethod("GET");
            //打开数据输入流
            din=httpConn.getInputStream();
            //获得xmlp解析器
            XmlPullParser xmlParser= Xml.newPullParser();
            xmlParser.setInput(din,"UTF-8");
            //获得解析到的时间类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件
            int evtType=xmlParser.getEventType();
            while(evtType!=XmlPullParser.END_DOCUMENT)//一直循环，直到文档结束
            {
                switch (evtType)
                {
                    case XmlPullParser.START_TAG:
                        String tag=xmlParser.getName();
                        //如果是city标签开始,则说明需要实例化对象了
                        if(tag.equalsIgnoreCase("city"))
                        {
                            //城市天气预报
                            cityname.addElement(xmlParser.getAttributeValue(null,"cityname")+"天气:");
                            //天气情况概述
                            summary.addElement(xmlParser.getAttributeValue(null,"stateDetailed"));
                            //最低温度
                            low.addElement("最低: "+xmlParser.getAttributeValue(null,"tem2"));
                            high.addElement("最高: "+xmlParser.getAttributeValue(null,"tem1"));
                            //天气情况图标网址
                            icon.addElement(weatherIcon+xmlParser.getAttributeValue(null,"state2")+".gif");
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        //标签结束
                        default:break;
                }
                //如果xml没有结束,则导航到下一节点
                evtType=xmlParser.next();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            //释放连接
            try{
                din.close();
                httpConn.disconnect();

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
    //下载图片
    private  void downImage(){
        //天气情况图标获取
        int i=0;
        for(i=0;i<icon.size();i++){
            try{
                URL url=new URL(icon.elementAt(i));
                System.out.println(icon.elementAt(i));
                httpConn=(HttpURLConnection)url.openConnection();
                httpConn.setRequestMethod("GET");
                din=httpConn.getInputStream();
                //图片数据bitmap
                bitmap.addElement(BitmapFactory.decodeStream(httpConn.getInputStream()));//查询
            }catch (Exception ex){
                ex.printStackTrace();
            }finally {
                //释放连接
                try{
                    din.close();
                    httpConn.disconnect();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }
    //显示结果handler
    private  final Handler handler=new Handler() {
        public  void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    showData();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    //显示结果
    public  void showData(){
        body.removeAllViews();//清除存储旧的查询结果的组件
        body.setOrientation(LinearLayout.VERTICAL);
       LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(
               LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight=80;
        params.height=50;//查询
        for(int i=0;i<cityname.size();i++){
            LinearLayout linearLayout=new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);//查询
            //城市
            TextView dayView=new TextView(this);
            dayView.setLayoutParams(params);
            dayView.setText(cityname.elementAt(i));
            linearLayout.addView(dayView);
            //描述
            TextView summaryView=new TextView(this);
            summaryView.setLayoutParams(params);
            summaryView.setText(summary.elementAt(i));
            linearLayout.addView(summaryView);
            //图标
            ImageView icon=new ImageView(this);
            icon.setLayoutParams(params);
            icon.setImageBitmap(bitmap.elementAt(i));
            linearLayout.addView(icon);
            //最低气温
            TextView lowView=new TextView(this);
            lowView.setLayoutParams(params);
            lowView.setText(low.elementAt(i));
            linearLayout.addView(lowView);
            //最高气温
            TextView highView=new TextView(this);
            highView.setLayoutParams(params);
            highView.setText(high.elementAt(i));
            linearLayout.addView(highView);
            body.addView(linearLayout);
        }
    }
}

