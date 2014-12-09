package ffm.sersanorsensors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class Bluetooth {

	// Unique UUID for this application
	private static final UUID MY_UUID = UUID
			.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	// Tag for log
	private static final String TAG = "BluetoothSocketService";
	// Service name
	private static final String NAME = "BluetoothSocket";
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_CONNECTING = 1; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 2; // now connected to a remote
													// device

	private Handler mHandler;
	private final BluetoothAdapter mAdapter;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;

	public Bluetooth(Context context, Handler handler) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = handler;
		mState = STATE_NONE;
	}

	public synchronized void start() {
		Log.d(TAG, "start");

		// Cancel any thread attempting to make a connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

	}

	private synchronized void setState(int state) {
		Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;
	}

	public synchronized void connect(BluetoothDevice device, boolean secure) {
		Log.d(TAG, "connect to: " + device);

		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		Log.d(TAG, "connected");

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();

		setState(STATE_CONNECTED);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		Log.d(TAG, "stop");

		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		setState(STATE_NONE);
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(out);
	}
	
	
	// THREADS
	
	 private class ConnectThread extends Thread {
	        private final BluetoothSocket mmSocket;
	        private final BluetoothDevice mmDevice;

	        public ConnectThread(BluetoothDevice device) {
	            mmDevice = device;
	            BluetoothSocket tmp = null;


	            // Get a BluetoothSocket for a connection with the
	            // given BluetoothDevice
	            try {
	              
	                    tmp = device.createRfcommSocketToServiceRecord(
	                            MY_UUID);

	            } catch (IOException e) {
	                Log.e(TAG, "Socket" + "create() failed", e);
	            }
	            mmSocket = tmp;
	        }

	        public void run() {
	            Log.i(TAG, "BEGIN mConnectThread");
	            setName("ConnectThread");

	            // Always cancel discovery because it will slow down a connection
	            mAdapter.cancelDiscovery();

	            // Make a connection to the BluetoothSocket
	            try {
	                // This is a blocking call and will only return on a
	                // successful connection or an exception
	                mmSocket.connect();
	            } catch (IOException e) {
	                // Close the socket
	                try {
	                    mmSocket.close();
	                } catch (IOException e2) {
	                    Log.e(TAG, "unable to close() " +
	                            " socket during connection failure", e2);
	                }
	                Log.i(TAG,"Connection CLOSED");
	                return;
	            }

	            // Reset the ConnectThread because we're done
	            synchronized (Bluetooth.this) {
	                mConnectThread = null;
	            }

	            // Start the connected thread
	            connected(mmSocket, mmDevice);
	        }

	        public void cancel() {
	            try {
	                mmSocket.close();
	            } catch (IOException e) {
	                Log.e(TAG, "close() of connect " + " socket failed", e);
	            }
	        }
	    }
	 
	 /**
	     * This thread runs during a connection with a remote device.
	     * It handles all incoming and outgoing transmissions.
	     */
	    private class ConnectedThread extends Thread {
	        private final BluetoothSocket mmSocket;
	        private final OutputStream mmOutStream;

	        public ConnectedThread(BluetoothSocket socket) {
	            Log.d(TAG, "create ConnectedThread");
	            mmSocket = socket;
	            OutputStream tmpOut = null;

	            // Get the BluetoothSocket input and output streams
	            try {
	                tmpOut = socket.getOutputStream();
	            } catch (IOException e) {
	                Log.e(TAG, "temp sockets not created", e);
	            }

	            mmOutStream = tmpOut;
	        }

	        public void run() {
	            Log.i(TAG, "BEGIN mConnectedThread");         
	        }

	        /**
	         * Write to the connected OutStream.
	         *
	         * @param buffer The bytes to write
	         */
	        public void write(byte[] buffer) {
	            try {
	                mmOutStream.write(buffer);

	            } catch (IOException e) {
	                Log.e(TAG, "Exception during write", e);
	            }
	        }

	        public void cancel() {
	            try {
	                mmSocket.close();
	            } catch (IOException e) {
	                Log.e(TAG, "close() of connect socket failed", e);
	            }
	        }
	    }

}
