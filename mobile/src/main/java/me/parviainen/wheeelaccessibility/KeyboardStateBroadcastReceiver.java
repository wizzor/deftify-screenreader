package me.parviainen.wheeelaccessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

/**
 * Created by visa on 23.1.2017.
 *
 * The point of this class is to receive a message from the keyboard when it is active
 * so that we know to disable the accessibility service keypress handling
 */

public class KeyboardStateBroadcastReceiver extends BroadcastReceiver {

    private SwitchAccessService as;
    private final String TAG = "KBStateBcastRec";

    public KeyboardStateBroadcastReceiver(SwitchAccessService as_initial){
        as = as_initial;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String state = intent.getStringExtra("state");
        Log.v(TAG, "KB Broadcast received");
        switch(state) {
            case "ACTIVE": {
                as.setKeyboardVisible(true);
                break;
            }
            case "INACTIVE": {
                as.setKeyboardVisible(false);
                break;
            }
            default:{
                Log.v(TAG, "Unknown state for keyboard");
                break;
            }
        }

    }

}
