/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tv.settings.accessories;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import java.util.List;

/**
 * BluetoothPairingRequest is a receiver for any Bluetooth pairing request. It
 * starts the Bluetooth Pairing activity, displaying the PIN, the passkey or a
 * confirmation entry dialog.
 */
public final class BluetoothPairingRequest extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
            return;
        }

        if (isOriginSettingsActive(context)) {
            return;
        }

        // convert broadcast intent into activity intent (same action string)
        BluetoothDevice device =
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT,
                BluetoothDevice.ERROR);
        Intent pairingIntent = new Intent();
        pairingIntent.setClass(context, BluetoothPairingDialog.class);
        pairingIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
        pairingIntent.putExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, type);
        if (type == BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION ||
                type == BluetoothDevice.PAIRING_VARIANT_DISPLAY_PASSKEY ||
                type == BluetoothDevice.PAIRING_VARIANT_DISPLAY_PIN) {
            int pairingKey = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY,
                    BluetoothDevice.ERROR);
            pairingIntent.putExtra(BluetoothDevice.EXTRA_PAIRING_KEY, pairingKey);
        }
        pairingIntent.setAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        pairingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // In Canvas, always start the pairing activity when we get the pairing broadcast,
        // as opposed to displaying a notification that will start the pairing activity.
        context.startActivity(pairingIntent);
    }

    private boolean isOriginSettingsActive(Context context) {

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = am.getRunningTasks(100);
        if(list != null && list.size() > 0) {
            String packName = list.get(0).topActivity.getPackageName();
            if(packName.equals("com.android.settings"))
                return true;
            else
                return false;
        } else {
           return false;
        }
   }
}
