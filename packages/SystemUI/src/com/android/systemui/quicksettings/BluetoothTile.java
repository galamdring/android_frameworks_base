package com.android.systemui.quicksettings;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.QuickSettingsController;
import com.android.systemui.statusbar.policy.BluetoothController;

import java.util.Set;

public class BluetoothTile extends QuickSettingsTile implements
        BluetoothAdapter.BluetoothStateChangeCallback,
        BluetoothController.BluetoothDeviceConnectionStateChangeCallback {

    private boolean mEnabled = false;
    private boolean mConnected = false;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothController mController;

    public BluetoothTile(Context context, QuickSettingsController qsc, BluetoothController controller) {
        super(context, qsc);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mController = controller;

        mOnClick = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEnabled) {
                    mBluetoothAdapter.disable();
                } else {
                    mBluetoothAdapter.enable();
                }
                if (isFlipTilesEnabled()) {
                    flipTile(0);
                }
            }
        };

        mOnLongClick = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startSettingsActivity(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                return true;
            }
        };
    }

    @Override
    void onPostCreate() {
        checkBluetoothState();
        updateTile();
        mController.addStateChangedCallback(this);
        mController.addConnectionStateChangedCallback(this);
        super.onPostCreate();
    }

    @Override
    public void onDestroy() {
        mController.removeStateChangedCallback(this);
        mController.removeConnectionStateChangedCallback(this);
        super.onDestroy();
    }

    @Override
    public void updateResources() {
        updateTile();
        super.updateResources();
    }

    void checkBluetoothState() {
        mEnabled = mBluetoothAdapter.isEnabled() &&
                mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON;
        mConnected = mEnabled &&
                mBluetoothAdapter.getConnectionState() == BluetoothAdapter.STATE_CONNECTED;
    }

    private synchronized void updateTile() {
        if (mEnabled) {
            if (mConnected) {
                final Set<BluetoothDevice> connected = mController.getConnectedBluetoothDevices();

                mDrawable = R.drawable.ic_qs_bluetooth_on;
                if (connected.isEmpty()) {
                    // shouldn't happen, but provide a sane fallback nevertheless
                    mLabel = mContext.getString(R.string.quick_settings_bluetooth_label);
                } else if (connected.size() == 1) {
                    BluetoothDevice device = connected.iterator().next();
                    mLabel = device.getAlias();
                    if (mLabel == null) {
                        mLabel = device.getName();
                    }
                } else {
                    mLabel = mContext.getString(R.string.quick_settings_bluetooth_multi_label,
                            connected.size());
                }
            } else {
                mDrawable = R.drawable.ic_qs_bluetooth_not_connected;
                mLabel = mContext.getString(R.string.quick_settings_bluetooth_label);
            }
        } else {
            mDrawable = R.drawable.ic_qs_bluetooth_off;
            mLabel = mContext.getString(R.string.quick_settings_bluetooth_off_label);
        }
    }

    @Override
    public void onBluetoothStateChange(boolean on) {
        checkBluetoothState();
        updateResources();
    }

    @Override
    public void onDeviceConnectionStateChange(BluetoothDevice device) {
        updateResources();
    }

    @Override
    public void onDeviceNameChange(BluetoothDevice device) {
        if (mController.getConnectedBluetoothDevices().size() == 1) {
            updateResources();
        }
    }
}
