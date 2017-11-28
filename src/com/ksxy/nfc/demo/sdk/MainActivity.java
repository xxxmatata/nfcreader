package com.ksxy.nfc.demo.sdk;

import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import cn.com.keshengxuanyi.mobilereader.NFCReaderHelper;
import cn.com.keshengxuanyi.mobilereader.UserInfo;
import com.baidu.mobstat.StatService;



public class MainActivity extends Activity {
	NfcAdapter mNfcAdapter;
	TextView mNoteRead;

	private ImageView iv_zhaopian;
	
	static Handler uiHandler = null;

	private NFCReaderHelper mNFCReaderHelper;
	
	PendingIntent mNfcPendingIntent;


	TextView tvname, tvsex, tvnation, tvbirthday, tvcode, tvaddress, tvdate,
			tvdepar, readerstatText;
	
	EditText tvnoteRead;
	
	/**
	 * jar使用
	 */
	private static  String appKey = "941c9b37d4dd4e569ff0320b21d9071c";
	
	private static String appSecret = "8eb5c020856040f7be7e52cff4ce3a77";
	
	private Context context = null;
	
    private AsyncTask<Void, Void, String> nfcTask = null;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 去掉标题栏
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		mNoteRead = ((TextView) findViewById(R.id.noteRead));
		iv_zhaopian = (ImageView) findViewById(R.id.ivHead);
		tvname = (TextView) findViewById(R.id.tvname2);
		tvsex = (TextView) findViewById(R.id.tvsex2);
		tvnation = (TextView) findViewById(R.id.tvnation2);
		tvbirthday = (TextView) findViewById(R.id.tvbirthday2);
		tvcode = (TextView) findViewById(R.id.tvcode2);
		tvaddress = (TextView) findViewById(R.id.tvaddress2);
		tvdate = (TextView) findViewById(R.id.tvdate2);
		tvdepar = (TextView) findViewById(R.id.tvdepart2);
		readerstatText = (TextView) findViewById(R.id.readerstatText);			
		tvnoteRead= (EditText) findViewById(R.id.noteRead);
		context = this;
		uiHandler = new MyHandler(this);
				
		// 设备注册
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		// 判断设备是否可用
		if (mNfcAdapter == null) {
			toast("该设备不支持nfc!");
			return;
		}
		
		if (!mNfcAdapter.isEnabled()) {
			Toast.makeText(this, "请在系统设置中先启用NFC功能！", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
			finish();
			return;
		}
		mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		 mNFCReaderHelper = new NFCReaderHelper(this, uiHandler, appKey, appSecret);

	}
	



	@Override
	public void onResume() {
		super.onResume();
		try {
		       // 页面埋点
	        StatService.onPageStart(this, "MainActivity");
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		try {
			if (null != mNfcPendingIntent) {
				mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, null,
						null);
//				resolvIntent(getIntent());
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}


	}



	@Override
	protected void onPause() {
		super.onPause();
		try {
		       // 页面埋点
	        StatService.onPageEnd(this, "MainActivity");
		}catch(Exception ex){
			ex.printStackTrace();
		}
		try {
			if (mNfcAdapter != null) {
				mNfcAdapter.disableForegroundDispatch(this);

//				mNfcAdapter.disableForegroundNdefPush(this);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		try {			
			setIntent(intent);
			resolvIntent(intent);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	synchronized void resolvIntent(Intent intent) {
		try {
			String action = intent.getAction();
			// toast(action);
		 if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
		
	            if (nfcTask == null) {
	            	nfcTask = new NFCReadTask(intent,context).executeOnExecutor(Executors
	                        .newCachedThreadPool());
	            }
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}	
	
	
	private void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	private class NFCReadTask extends AsyncTask<Void, Void, String> {

		private Intent mIntent = null;
		private Context context = null;

		public NFCReadTask(Intent i, Context contextTemp) {
			mIntent = i;
			context = contextTemp;
		}

		@Override
		protected String doInBackground(Void... params) {

			String strCardInfo = mNFCReaderHelper.readCardWithIntent(mIntent);

			return strCardInfo;
		}

		@Override
		protected void onPostExecute(String strCardInfo) {			
			super.onPostExecute(strCardInfo);
            nfcTask = null;

			if (null != strCardInfo && strCardInfo.length() >1600) {
				UserInfo userInfo = mNFCReaderHelper.parsePersonInfo(strCardInfo);				
				tvname.setText(userInfo.name);
				tvsex.setText(userInfo.sex);
				tvnation.setText(userInfo.nation);
				tvbirthday.setText(userInfo.brithday);
				tvcode.setText(userInfo.id);
				tvaddress.setText(userInfo.address);
				tvdate.setText(userInfo.exper + "-" + userInfo.exper2);
				tvdepar.setText(userInfo.issue);
				
				Bitmap bm= mNFCReaderHelper.decodeImagexxx(strCardInfo);
				iv_zhaopian.setImageBitmap(bm);

			}
			
		
		}

	}
	
	class MyHandler extends Handler {
		private MainActivity activity;

		MyHandler(MainActivity activity) {
			this.activity = activity;
		}

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 1000:
                String msgTemp = (String) msg.obj;    
                readerstatText.setText(msgTemp);
				break;

			}
		}
	}


	
	
	

}