package de.tubs.ibr.dtn.ping;

import de.tubs.ibr.dtn.api.Node;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.MenuCompat;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PingActivity extends Activity {
    
    private static final int SELECT_NEIGHBOR = 1;
	
    private PingService mService = null;
    private boolean mBound = false;
    
    private EditText mTextEid = null;
    private TextView mResult = null;
    
    @SuppressWarnings("deprecation")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuCompat.setShowAsAction(menu.findItem(R.id.itemSelectNeighbor), MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemSelectNeighbor:
                Intent intent = mService.getSelectNeighborIntent();
                startActivityForResult(intent, SELECT_NEIGHBOR);
                return true;
                
            case R.id.itemEnableStreaming:
                if (item.isChecked()) {
                    item.setChecked(false);
                    stop_stream();
                }
                else if (!item.isChecked()) {
                    item.setChecked(true);
                    start_stream();
                }
                return true;
                
            case R.id.itemSwitchStreaming:
                Intent i = new Intent(this, PingService.class);
                i.setAction(PingService.STREAM_SWITCH_GROUP);
                startService(i);
                return true;
            
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void onNeighborSelected(Node n) {
        String endpoint = n.endpoint.toString();
        
        if (endpoint.startsWith("ipn:")) {
            endpoint = endpoint + ".11";
        } else {
            endpoint = endpoint + "/echo";
        }
        
        mTextEid.setText(endpoint);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (SELECT_NEIGHBOR == requestCode) {
            if ((data != null) && data.hasExtra(de.tubs.ibr.dtn.Intent.NODE_KEY)) {
                Node n = data.getParcelableExtra(de.tubs.ibr.dtn.Intent.NODE_KEY);
                onNeighborSelected(n);
            }
            return;
        }
        
        super.onActivityResult(requestCode, resultCode, data);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mTextEid = (EditText)findViewById(R.id.editEid);
        mResult = (TextView)findViewById(R.id.textResult);
        
        // assign an action to the ping button
        Button b = (Button)findViewById(R.id.buttonPing);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ping();
            }
        });
    }
    
	@Override
	protected void onDestroy() {
        // unbind from service
        if (mBound) {
            // unbind from the PingService
            unbindService(mConnection);
            mBound = false;
        }
        
		super.onDestroy();
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        
        // unregister the receiver for the DATA_UPDATED intent
        unregisterReceiver(mDataReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        if (!mBound) {
            // bind to the PingService
            bindService(new Intent(this, PingService.class), mConnection, Context.BIND_AUTO_CREATE);
            mBound = true;
        }
        
        // register an receiver for DATA_UPDATED intent generated by the PingService
        IntentFilter filter = new IntentFilter(PingService.DATA_UPDATED);
        registerReceiver(mDataReceiver, filter);
        
        // update the displayed result
        updateResult();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((PingService.LocalBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };
    
    private void ping() {
        Intent i = new Intent(this, PingService.class);
        i.setAction(PingService.PING_INTENT);
        i.putExtra("destination", mTextEid.getText().toString());
        startService(i);
    }
    
    private void start_stream() {
        Intent i = new Intent(this, PingService.class);
        i.setAction(PingService.STREAM_START_INTENT);
        startService(i);
    }
    
    private void stop_stream() {
        Intent i = new Intent(this, PingService.class);
        i.setAction(PingService.STREAM_STOP_INTENT);
        startService(i);
    }
    
    private void updateResult() {
        runOnUiThread(new Runnable() {
            public void run()
            {
                if (mService != null) {
                    Resources res = getResources();
                    String text = String.format(res.getString(R.string.resultText), mService.getLastMeasurement());
                    mResult.setText(text);
                }
            }
        });
    }
    
    private BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("localeid")) {
                String local_eid = intent.getStringExtra("localeid");
                if (local_eid.startsWith("ipn:")) {
                    mTextEid.setText( local_eid + ".11" );
                } else {
                    mTextEid.setText( local_eid + "/echo" );
                }
            } else {
                // update the displayed result
                updateResult();
            }
        }
    };
}
