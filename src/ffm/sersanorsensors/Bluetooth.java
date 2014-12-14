package ffm.sersanorsensors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Bluetooth {

	private static final String SERVICE_NAME = "BluetoothSocket";
	private static final String TAG = "Bluetooth";
	private static final UUID MY_UUID = UUID
			.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

	private final BluetoothAdapter mAdapter;
	private conThread mConThread;
	private dataThread mDataThread;
	private final Handler mHandler;
	private int mState;
	
    // CONSTANTS
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_CONNECTING = 1; // now initiating an outgoing
													// // connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device

	public Bluetooth(Context context, Handler handler) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = handler;
		mState = STATE_NONE;
	}

	public String getLocalAdress() {
		return mAdapter.getAddress();
	}

	public String getLocalName() {
		return mAdapter.getName();
	}

	public synchronized void setState(int state) {
		Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;
	}

	public synchronized int getState() {
		return mState;
	}

	public synchronized void start() {
		Log.d(TAG, "start");

		// Cancel any thread attempting to make a connection
		if (mConThread != null) {
			mConThread.cancel();
			mConThread = null;
		}

		// Cancel any thread currently running a connection
		if (mDataThread != null) {
			mDataThread.cancel();
			mDataThread = null;
		}
	}

	public synchronized void connect(BluetoothDevice device) {
		Log.d(TAG, "connect to: " + device);

		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING) {
			if (mConThread != null) {
				mConThread.cancel();
				mConThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (mDataThread != null) {
			mDataThread.cancel();
			mDataThread = null;
		}

		// Start the thread to connect with the given device
		mConThread = new conThread(device);
		mConThread.start();
		setState(STATE_CONNECTING);
	}

	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		Log.d(TAG, "Connected");

		// Cancel the thread that completed the connection
		if (mConThread != null) {
			mConThread.cancel();
			mConThread = null;
		}

		// Cancel any thread currently running a connection
		if (mDataThread != null) {
			mDataThread.cancel();
			mDataThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		mDataThread = new dataThread(socket);
		mDataThread.start();

		// Send the name of the connected device back to the UI Activity
		Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		setState(STATE_CONNECTED);
	}

	public synchronized void stop() {
		Log.d(TAG, "stop");

		if (mConThread != null) {
			mConThread.cancel();
			mConThread = null;
		}

		if (mDataThread != null) {
			mDataThread.cancel();
			mDataThread = null;
		}
		setState(STATE_NONE);
	}

	public void write(byte[] out) {
		// Create temporary object
		dataThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (mState != STATE_CONNECTED)
				return;
			r = mDataThread;
		}
		// Perform the write unsynchronized
		r.write(out);
	}
	private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        Bluetooth.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        Bluetooth.this.start();
    }
	// THREADS ---------------------------------------------------------------

	private class conThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public conThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket tmp = null;
			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);

			} catch (IOException e) {
				Log.e(TAG, "Socket creation failed", e);
			}
			mmSocket = tmp;
		}

		public void run() {
			Log.i(TAG, "BEGIN Connection THREAD");
			setName("conThread");

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
					Log.e(TAG, "unable to close socket during connection failure", e2);
				}
				connectionFailed();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (Bluetooth.this) {
				mConThread = null;
			}

			// Start the connected thread
			connected(mmSocket, mmDevice);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
	
	private class dataThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public dataThread(BluetoothSocket socket) {
            Log.d(TAG, "Create dataThread ");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN dataThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode
                    Bluetooth.this.start();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
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
