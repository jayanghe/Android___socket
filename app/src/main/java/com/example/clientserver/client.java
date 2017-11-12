package com.example.clientserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class client extends Activity {
	private ImageButton startButton,sendButtonClient;
	private EditText IPText,editMsgTextClient;
	private Context mContext;
	private boolean isConnecting=false;
	private Thread mThreadClient=null;
	private Socket mSocketClient=null;
	static BufferedReader mBufferedReaderClient=null;
	static PrintWriter mPrintWriterClient=null;
	private String recvMessageClient="";
	private String sendMessageClient="";
	private TextView recvText;
	private TextView sendText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);
		mContext=this;
		
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.detectDiskReads()
		.detectDiskWrites()
		.detectNetwork()
		.penaltyLog()
		.build()
		);
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		.detectLeakedSqlLiteObjects()
		.penaltyLog()
		.penaltyDeath()
		.build());
		
		IPText=(EditText) findViewById(R.id.IPText);
		IPText.setText("192.168.43.1:51413");
		startButton=(ImageButton) findViewById(R.id.StartConnect);
		startButton.setOnClickListener(StartClickListener);
		
		editMsgTextClient=(EditText) findViewById(R.id.clientMessageText);
		editMsgTextClient.setText("UP");
		
		sendButtonClient=(ImageButton) findViewById(R.id.SendButtonClient);
		sendButtonClient.setOnClickListener(SendClickListenerClient);
		
		sendText=(TextView) findViewById(R.id.tv1);
		sendText.setMovementMethod(ScrollingMovementMethod.getInstance());
		
		recvText=(TextView) findViewById(R.id.tv1);
		recvText.setMovementMethod(ScrollingMovementMethod.getInstance());
	}
	
	private OnClickListener StartClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(isConnecting)
			{
				isConnecting = false;
				try
				{
					if(mSocketClient!=null)
					{
						mSocketClient.close();
						mSocketClient = null;
						
						mPrintWriterClient.close();
						mPrintWriterClient = null;
					}
				}catch (IOException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				mThreadClient.interrupt();
				
				startButton.setTag("开始连接");
				IPText.setEnabled(true);
				recvText.setText("信息:\n");
				sendText.setText("信息:\n");
				
			}else
			{
				isConnecting=true;
				startButton.setTag("停止连接");
				IPText.setEnabled(false);
				
				mThreadClient = new Thread(mRunnable);
				mThreadClient.start();
			}
		}
	};

	private OnClickListener SendClickListenerClient = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(isConnecting&&mSocketClient!=null)
			{
				String msgText = editMsgTextClient.getText().toString();
				if(msgText.length()<=0)
				{
					Toast.makeText(mContext, "发送内容不能为空！", Toast.LENGTH_SHORT).show();
				}else
				{
					try
					{
						mPrintWriterClient.print(msgText);
						mPrintWriterClient.flush();
						Toast.makeText(mContext, "Client:"+msgText, Toast.LENGTH_SHORT).show();//弹窗显示 
						
						if(msgText.length()>0)
						{
							recvText.append("Client:"+msgText+"\r\n");
						
						}
						
					}catch (Exception e) {
						// TODO: handle exception
						Toast.makeText(mContext, "发送异常："+e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				}
			}else
			{
				Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
			}
		}
	}; 
	
	//线程：监听服务器发来的消息
	private Runnable mRunnable = new Runnable() {
		
		@Override
		public void run() {
			String msgText = IPText.getText().toString();
			if(msgText.length()<=0)
			{
				recvMessageClient="IP不能为空！\n";//消息换行
				Message msg = new Message();
				msg.what = 1;
				mHandler.sendMessage(msg);
				return;
			}
			int start = msgText.indexOf(":");
			if((start==-1)||(start+1>=msgText.length()))
			{
				recvMessageClient = "IP地址不合法\n";
				Message msg = new Message();
				msg.what = 1;
				mHandler.sendMessage(msg);
				return;
			}
			String sIP= msgText.substring(0,start);
			String sPort = msgText.substring(start+1);
			int port = Integer.parseInt(sPort);
			
			Log.d("gjz", "IP"+sIP+":"+port);
			
			try
			{
				//连接服务器
				mSocketClient = new Socket(sIP,port);
				//取得输入、输出流
				mBufferedReaderClient=new BufferedReader(new InputStreamReader(mSocketClient.getInputStream()));
				mPrintWriterClient=new PrintWriter(mSocketClient.getOutputStream(),true);
				recvMessageClient="已经连接server！\n";
				Message msg = new Message();
				msg.what = 1;
				mHandler.sendMessage(msg);
			}catch (Exception e) {
				// TODO: handle exception
				recvMessageClient = "连接IP异常:" + e.toString() + e.getMessage() + "\n";//消息换行
				Message msg = new Message();
                msg.what = 1;
				mHandler.sendMessage(msg);
				return;
			}
			
			char[] buffer = new char[256];
			int count = 0;
			while(isConnecting)
			{
				try
				{
					if((count = mBufferedReaderClient.read(buffer))>0)
					{
						recvMessageClient = getInfoBuff(buffer,count);
						Message msg = new Message();
						msg.what = 1;
						mHandler.sendMessage(msg);
					}
				}catch (Exception e) {
					// TODO: handle exception
					recvMessageClient = "接收异常:" + e.getMessage() + "\n";//消息换行
					Message msg = new Message();
	                msg.what = 1;
					mHandler.sendMessage(msg);
				}
			}                                        
		}
	};
	Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			super.handleMessage(msg);
			if(msg.what==1)
			{
				recvText.append("Server:"+recvMessageClient);//刷新
			}
		};
	};
	
	private String getInfoBuff(char[] buff,int count)
	{
		char[] temp = new char[count];
		for (int i = 0; i < count; i++) {
			temp[i]=buff[i];
		}
		return new String(temp);
	}
	
}