package com.example.clientserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class server extends Activity {
	private ImageButton sendButtonServer,CreateButton;
	private EditText editMsgText;
	private TextView recvText;
	
	private Context mContext;
	private boolean isConnecting=false;
	
	private Thread mThreadServer=null;
	private Socket mSocketServer=null;

	static BufferedReader mBufferedReaderServer = null;
	static PrintWriter mPrintWriterServer = null;
	private String recvMessageServer = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server);
		
        mContext = this;
        
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()        
        .detectDiskReads()        
        .detectDiskWrites()        
        .detectNetwork()   // or .detectAll() for all detectable problems       
        .penaltyLog()        
        .build());        
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()        
        .detectLeakedSqlLiteObjects()     
        .penaltyLog()        
        .penaltyDeath()        
        .build()); 
        
        sendButtonServer=(ImageButton) findViewById(R.id.SendButtonServer);
        sendButtonServer.setOnClickListener(SendClickListenerServer);
        
        CreateButton=(ImageButton) findViewById(R.id.CreatConnect);
        CreateButton.setOnClickListener(CreateClickListener);
        
        editMsgText=(EditText) findViewById(R.id.MessageText);
        editMsgText.setText("");
        
        recvText=(TextView) findViewById(R.id.recvTv2);
        recvText.setMovementMethod(ScrollingMovementMethod.getInstance());


        recvText.setMovementMethod(ScrollingMovementMethod.getInstance());
        
	}
	
	Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) {
			  super.handleMessage(msg);			
			  if(msg.what==0)
			  {
				  recvText.append("Client: "+recvMessageServer);	// 刷新
			  }
			
		}
	};
	
	
	private String getInfoBuff(char[] buff, int count)
	{
		char[] temp = new char[count];
		for(int i=0; i<count; i++)
		{
			temp[i] = buff[i];
		}
		return new String(temp);
	}
	
	//创建服务端ServerSocket对象
	private ServerSocket serverSocket=null;
	private boolean serverRunning=false;
	
	
	private OnClickListener SendClickListenerServer=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(serverRunning&&mSocketServer!=null)
			{
				String msgText=editMsgText.getText().toString();//取得编辑框中我们输入的内容
				if(msgText.length()<=0)
				{
					Toast.makeText(mContext, "发送内容不能为空！", Toast.LENGTH_SHORT).show();
				}else
				{
					try
					{

						mPrintWriterServer.print(msgText+"\n");//发送给服务器
						mPrintWriterServer.flush();

						Toast.makeText(mContext, "server:"+msgText, Toast.LENGTH_SHORT).show();//弹窗显示 server:....
						  
							 if(msgText.length()>0)
							 {
								 recvText.append("server: "+msgText+"\r\n");
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
	
	private OnClickListener CreateClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(serverRunning)
			{
				serverRunning=false;
				try
				{
					if(serverSocket!=null)
					{
						serverSocket.close();
						serverSocket=null;
					}
					if(mSocketServer!=null)
					{
						mSocketServer.close();
						mSocketServer=null;
					}
				}catch (IOException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				mThreadServer.interrupt();
				CreateButton.setTag("创建服务");
				recvText.setText("信息：\n");
			}else
			{
				serverRunning=true;
				mThreadServer=new Thread(mcreateRunnable);
				mThreadServer.start();
				CreateButton.setTag("停止服务");
			}
		}
	};
	
	//线程监听服务器发来的消息
	private Runnable mcreateRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try
			{
				serverSocket = new ServerSocket(0);
				SocketAddress address = null;
				if(!serverSocket.isBound())
				{
					serverSocket.bind(address,0);
				}
				
				getLocalAddress();
				//方法用于等待客服连接
				mSocketServer=serverSocket.accept();
				//接收客服端数据BufferedReader对象
				mBufferedReaderServer=new BufferedReader(new InputStreamReader(mSocketServer.getInputStream()));
				//给客服端发送数据
				mPrintWriterServer=new PrintWriter(mSocketServer.getOutputStream(),true);
				mPrintWriterServer.print("Client: "+recvMessageServer);

				Message msg = new Message();
                msg.what = 0;
                recvMessageServer = "client已经连接上！\n";
                mHandler.sendMessage(msg);
			}catch (IOException e) {
				// TODO: handle exception
				Message msg = new Message();
                msg.what = 0;
				recvMessageServer = "创建异常:" + e.getMessage() + e.toString() + "\n";//消息换行
				mHandler.sendMessage(msg);
				return;
			}
			
			char[] buffer = new char[256];
			int count = 0;
			while(serverRunning)
			{
				try
				{
					//if( (recvMessageServer = mBufferedReaderServer.readLine()) != null )//获取客服端数据
					if((count = mBufferedReaderServer.read(buffer))>0);
					{						
						recvMessageServer = getInfoBuff(buffer, count) + "\n";//消息换行
						Message msg = new Message();
		                msg.what = 0;
						mHandler.sendMessage(msg);
					}
				}
				catch (Exception e)
				{
					recvMessageServer = "接收异常:" + e.getMessage() + "\n";//消息换行
					Message msg = new Message();
	                msg.what = 0;
					mHandler.sendMessage(msg);
					return;
				}
			}
		}
	};
	
	public String getLocalAddress()
	{
		try
		{
			for(Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();en.hasMoreElements();)
			{
				NetworkInterface intf = en.nextElement();
				for(Enumeration<InetAddress> enumIPAddr = intf.getInetAddresses();enumIPAddr.hasMoreElements();)
				{
					InetAddress inetAddress = enumIPAddr.nextElement();
					recvMessageServer += "请连接IP："+inetAddress.getHostAddress()+":"
							+ serverSocket.getLocalPort()+ "\n";
					
				}
			}
		}catch (SocketException ex) {
			// TODO: handle exception
			recvMessageServer = "获取IP地址异常:" + ex.getMessage() + "\n";//消息换行
			Message msg = new Message();
            msg.what = 0;
			mHandler.sendMessage(msg);
		}
		Message msg = new Message();
        msg.what = 0;
		mHandler.sendMessage(msg);
		return null;
	}

}
