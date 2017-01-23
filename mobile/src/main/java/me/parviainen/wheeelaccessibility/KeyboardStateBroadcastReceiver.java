package me.parviainen.wheeelaccessibility;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;

/**
 * Created by visa on 23.1.2017.
 *
 * The point of this class is to receive a message from the keyboard when it is active
 * so that we know to disable the accessibility service keypress handling
 */

public class KeyboardStateBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub



    }

}
