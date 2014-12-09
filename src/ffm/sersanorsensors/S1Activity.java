package ffm.sersanorsensors;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Set;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class S1Activity extends ActionBarActivity implements
		SensorEventListener {

	private SensorManager senSensorManager;
	private Sensor senAccelerometer;
	private BluetoothAdapter BA;
	TextView vx, vy, vz, name, vendor, version, power;
	Button On, Off, Visible, list;
	ListView lv;
	private Set<BluetoothDevice>pairedDevices;
	Bluetooth bt;

	protected void onResume() {
		super.onResume();
		senSensorManager.registerListener(this, senAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	protected void onPause() {
		super.onPause();
		senSensorManager.unregisterListener(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_s1);
		init();
		senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		senAccelerometer = senSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		senSensorManager.registerListener(this, senAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);

		sensorInfo();
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
	}

	public void sensorInfo() {
		SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		Sensor acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		name.setText(" " + acc.getName());
		vendor.setText(" " + acc.getVendor());
		version.setText(" " + Integer.toString(acc.getVersion()));
		power.setText(" " + Float.toString(acc.getPower()) + " mA");
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

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		// TODO Auto-generated method stub
		Sensor mySensor = sensorEvent.sensor;
		DecimalFormat dec = new DecimalFormat("0.000");

		if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			double x = sensorEvent.values[0];
			double y = sensorEvent.values[1];
			double z = sensorEvent.values[2];

			vx.setText(dec.format(x));
			vy.setText(dec.format(y));
			vz.setText(dec.format(z));
		}
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
		for (BluetoothDevice bt : pairedDevices)
			list.add(bt.getName()+bt.getAddress());

		Toast.makeText(getApplicationContext(), "Showing Paired Devices",
				Toast.LENGTH_SHORT).show();
		final ArrayAdapter adapter = new ArrayAdapter(this,
				android.R.layout.simple_list_item_1, list);
		lv.setAdapter(adapter);

	}
	
	public void sendData(View view){
		
		
	}
}
