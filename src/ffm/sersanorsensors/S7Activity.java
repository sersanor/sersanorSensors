package ffm.sersanorsensors;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.support.v7.app.ActionBarActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class S7Activity extends ActionBarActivity {

	private SensorManager senSensorManager;
	private Sensor senLight;
	private BluetoothAdapter BA;
	TextView vx, vy, vz, name, vendor, version, power, xlab, ylab, zlab;
	Button On, Off, Visible, list;
	ListView lv;
	private Set<BluetoothDevice> pairedDevices;
	Bluetooth bts;
	BluetoothDevice btServer;
	private static final String TAG = "PROXIMITY";
	//private static final String MAC = "00:0E:A1:32:22:77"; // FIXED MAC
	private static String MAC = null;
	private Handler myHandler;
	Timer timer, timer2;
	private static final int SENDING = 0;
	private static final int NSENDING = 1;
	private static int state = NSENDING;
	SoundMeter snd = null;
	private double sound=0.0;
	Handler mHandler = null;

	protected void onResume() {
		super.onResume();
	}

	protected void onPause() {
		super.onPause();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_s1);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		init();
		sensorInfo();

		// HANDLER
		mHandler = new Handler() {
		    public void handleMessage(Message msg){
		    	vx.setText(Integer.toString((int)sound)+" dB");
		    }
		    
		};
		
		createSoundTask();
	}

	public void createSoundTask() {
		snd = new SoundMeter();
		snd.start();
		timer2 = new Timer();
		timer2.schedule(new callSound(), 0, 200);
	}

	public void server() {

		if (BA == null) {
			this.finish();
			Log.e("BLUETOOTH", "NO BT ADAPTER");
		}

		pairedDevices = BA.getBondedDevices();
		for (BluetoothDevice bt : pairedDevices) {
			if (bt.getAddress().equals(MAC))
				btServer = bt;
		}
		myHandler = new Handler();
		bts = new Bluetooth(this, myHandler);
		bts.start();
		bts.connect(btServer);
	}

	public void init() {
		vx = (TextView) findViewById(R.id.vxT);
		vy = (TextView) findViewById(R.id.vyT);
		vz = (TextView) findViewById(R.id.vzT);
		vendor = (TextView) findViewById(R.id.s_vendor);
		power = (TextView) findViewById(R.id.s_power);
		version = (TextView) findViewById(R.id.s_version);
		name = (TextView) findViewById(R.id.s_name);

		// BT
		On = (Button) findViewById(R.id.Button01);
		Off = (Button) findViewById(R.id.Button02);
		Visible = (Button) findViewById(R.id.button03);
		list = (Button) findViewById(R.id.button04);
		lv = (ListView) findViewById(R.id.listView1);
		BA = BluetoothAdapter.getDefaultAdapter();
		vy.setEnabled(false);
		vz.setEnabled(false);

		xlab = (TextView) findViewById(R.id.xLabel);
		ylab = (TextView) findViewById(R.id.yLabel);
		zlab = (TextView) findViewById(R.id.zLabel);
		xlab.setText("Sound Level: ");
		ylab.setText("");
		ylab.setEnabled(false);
		zlab.setText("");
		zlab.setEnabled(false);
	}

	public void sensorInfo() {

		name.setText("---");
		vendor.setText("---");
		version.setText("---");
		power.setText("---");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.s1, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void captureBT(View view) {
		Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(turnOn, 0);
	}

	public void on(View view) {
		if (!BA.isEnabled()) {
			Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(turnOn, 0);
			Toast.makeText(getApplicationContext(), "Turned on",
					Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getApplicationContext(), "Already on",
					Toast.LENGTH_LONG).show();
		}
	}

	public void off(View view) {
		BA.disable();
		Toast.makeText(getApplicationContext(), "Turned off", Toast.LENGTH_LONG)
				.show();
	}

	public void visible(View view) {
		Intent getVisible = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		startActivityForResult(getVisible, 0);

	}

	public void list(View view) {
		pairedDevices = BA.getBondedDevices();

		ArrayList list = new ArrayList();
		for (BluetoothDevice bt : pairedDevices) {
			list.add(bt.getName() + bt.getAddress());
		}
		Toast.makeText(getApplicationContext(), "Showing Paired Devices",
				Toast.LENGTH_SHORT).show();
		final ArrayAdapter adapter = new ArrayAdapter(this,
				android.R.layout.simple_list_item_1, list);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String item = ((TextView)view).getText().toString();
				String[] aux = item.split("\\\n");
				MAC = aux[1];
				Log.i("MAC", MAC);
				if(MAC!=null)server();
			}
		
		});

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (timer != null)
			timer.cancel();
		if(bts != null)bts.stop();
		if (timer2 != null)
			timer2.cancel();
		snd.stop();
	}

	public void sendData(View view) {
		if (bts != null) {
			switch (state) {
			case SENDING:
				timer.cancel();
				state = NSENDING;
				break;
			case NSENDING:
				timer = new Timer();
				timer.schedule(new transferData(), 0, 250);
				state = SENDING;
				break;
			}
		}

	}

	class transferData extends TimerTask {
		public void run() {
			
	    	String aux = vx.getText().toString();
	    	String delims = "[ ]+";
	        String[] tokens = aux.split(delims);
	    	aux = tokens[0];
			
			String tmp = "SoundLevel: " + aux + " s: 7 "; // SENDS	INFO
			bts.write(tmp.getBytes());
		}
	}

	class callSound extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (snd != null){
				sound = snd.getAmplitude();

				mHandler.obtainMessage(1).sendToTarget();
			}
		}

	}
	
}
