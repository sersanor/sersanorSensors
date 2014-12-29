package ffm.sersanorsensors;

import java.util.Iterator;
import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {
	CheckBox s1,s2,s3,s4,s5,s6,s7,s8,s9;
	Button b1,b2,b3,b4,b5,b6,b7,b8,b9;
	private SensorManager mSensorManager;
	BluetoothAdapter BA;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize vars
        init();
        //check sensors availability
        checkSensors();
    }
    public void init(){
    	BA = BluetoothAdapter.getDefaultAdapter();
    	
    	s1 = (CheckBox)findViewById((R.id.s1_CB));
    	s2 = (CheckBox)findViewById((R.id.s2_CB));
    	s3 = (CheckBox)findViewById((R.id.s3_CB));
    	s4 = (CheckBox)findViewById((R.id.s4_CB));
    	s5 = (CheckBox)findViewById((R.id.s5_CB));
    	s6 = (CheckBox)findViewById((R.id.s6_CB));
    	s7 = (CheckBox)findViewById((R.id.s7_CB));
    	s8 = (CheckBox)findViewById((R.id.s8_CB));
    	s9 = (CheckBox)findViewById((R.id.s9_CB));
    	
    	b1 = (Button)findViewById((R.id.b1));
    	b2 = (Button)findViewById((R.id.b2));
    	b3 = (Button)findViewById((R.id.b3));
    	b4 = (Button)findViewById((R.id.b4));
    	b5 = (Button)findViewById((R.id.b5));
    	b6 = (Button)findViewById((R.id.b6));
    	b7 = (Button)findViewById((R.id.b7));
    	b8 = (Button)findViewById((R.id.b8));
    	b9 = (Button)findViewById((R.id.b9));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
    
    
    // SENSORS TEST+
    
    public void checkSensors(){
        SensorManager mgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = mgr.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensors) {
            Log.d("Sensors", "" + sensor.getName());
        } 	
        
        // S1
        if (mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
        	s1.setChecked(true);
        	b1.setEnabled(true);
        	} 
        else {
        		s1.setChecked(false);
        		b1.setEnabled(false);
        	}
        
        // S2
        if (mgr.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
        	s2.setChecked(true);
        	b2.setEnabled(true);
        	} 
        else {
        		s2.setChecked(false);
        		b2.setEnabled(false);
        	}
        
        // S3
        if (mgr.getDefaultSensor(Sensor.TYPE_ORIENTATION) != null){
        	s3.setChecked(true);
        	b3.setEnabled(true);
        	} 
        else {
        		s3.setChecked(false);
        		b3.setEnabled(false);
        	}
        
        // S4
        if (mgr.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null){
        	s4.setChecked(true);
        	b4.setEnabled(true);
        	} 
        else {
        		s4.setChecked(false);
        		b4.setEnabled(false);
        	}
        
        // S5
        if (mgr.getDefaultSensor(Sensor.TYPE_TEMPERATURE) != null){
        	s5.setChecked(true);
        	b5.setEnabled(true);
        	} 
        else {
        		s5.setChecked(false);
        		b5.setEnabled(false);
        	}
        
        // S6
        if (mgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
        	s6.setChecked(true);
        	b6.setEnabled(true);
        	} 
        else {
        		s6.setChecked(false);
        		b6.setEnabled(false);
        	}
        
        // S7
        if (true){ //TODO: CHECK ANDROID VERSION
        	s7.setChecked(true);
        	b7.setEnabled(true);
        	} 
        else {
        		s7.setChecked(false);
        		b7.setEnabled(false);
        	}
        
        // S8
        if (mgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
        	s8.setChecked(true);
        	b8.setEnabled(true);
        	} 
        else {
        		s8.setChecked(false);
        		b8.setEnabled(false);
        	}
        
        //S9
        
        if (mgr.getDefaultSensor(Sensor.TYPE_PRESSURE) != null){
        	s9.setChecked(true);
        	b9.setEnabled(true);
        	} 
        else {
        		s9.setChecked(false);
        		b9.setEnabled(false);
        	}
        
    }
    
    // LAUNCH NEW ACTIVITYS
    
    public void launchS1(View view){
    	if(BA.isEnabled()){
    	Intent intent = new Intent(this, S1Activity.class);
    	startActivity(intent);
    	}
    	else
    		Toast.makeText(getApplicationContext(), "Bluetooth OFF", Toast.LENGTH_LONG)
			.show();
    }
    
    public void launchS2(View view){
    	if(BA.isEnabled()){
    	Intent intent = new Intent(this, S2Activity.class);
    	startActivity(intent);
    	}
    	else
    		Toast.makeText(getApplicationContext(), "Bluetooth OFF", Toast.LENGTH_LONG)
			.show();
    }
    
    public void launchS4(View view){
    	if(BA.isEnabled()){
    	Intent intent = new Intent(this, S4Activity.class);
    	startActivity(intent);
    	}
    	else
    		Toast.makeText(getApplicationContext(), "Bluetooth OFF", Toast.LENGTH_LONG)
			.show();
    }
    public void launchS7(View view){
    	if(BA.isEnabled()){
    	Intent intent = new Intent(this, S7Activity.class);
    	startActivity(intent);
    	}
    	else
    		Toast.makeText(getApplicationContext(), "Bluetooth OFF", Toast.LENGTH_LONG)
			.show();
    }
    
    
}
