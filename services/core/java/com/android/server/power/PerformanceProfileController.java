package com.android.server.power;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.DeviceConfig;
import android.provider.Settings;

/** Helper to manage performance profile state. */
public final class PerformanceProfileController {
    private static final String NAMESPACE = "performance_profile";
    private static final String KEY_CPU_CAP_LIGHT = "cpu_cap_light";
    private static final String KEY_GPU_CAP_LIGHT = "gpu_cap_light";

    public interface Listener {
        void onModeChanged(int mode);
    }

    private final Context mContext;
    private final ContentResolver mResolver;
    private final Listener mListener;
    private int mMode;

    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            updateMode();
        }
    };

    private final DeviceConfig.OnPropertiesChangedListener mDeviceConfigListener =
            properties -> {
                if (NAMESPACE.equals(properties.getNamespace()) && mListener != null) {
                    mListener.onModeChanged(mMode);
                }
            };

    public PerformanceProfileController(Context context, Listener listener) {
        mContext = context;
        mResolver = context.getContentResolver();
        mListener = listener;
        mResolver.registerContentObserver(
                Settings.Global.getUriFor(Settings.Global.PERFORMANCE_PROFILE_MODE),
                false, mObserver);
        DeviceConfig.addOnPropertiesChangedListener(NAMESPACE,
                context.getMainExecutor(), mDeviceConfigListener);
        updateMode();
    }

    private void updateMode() {
        int mode = Settings.Global.getInt(mResolver,
                Settings.Global.PERFORMANCE_PROFILE_MODE, 0);
        if (mode != mMode) {
            mMode = mode;
            if (mListener != null) {
                mListener.onModeChanged(mMode);
            }
        }
    }

    public int getMode() {
        return mMode;
    }

    public void setMode(int mode) {
        Settings.Global.putInt(mResolver, Settings.Global.PERFORMANCE_PROFILE_MODE, mode);
    }

    public int getCpuCapLight() {
        return DeviceConfig.getInt(NAMESPACE, KEY_CPU_CAP_LIGHT, 100);
    }

    public int getGpuCapLight() {
        return DeviceConfig.getInt(NAMESPACE, KEY_GPU_CAP_LIGHT, 100);
    }

    public ProfileState getProfileState() {
        return new ProfileState(mMode, getCpuCapLight(), getGpuCapLight());
    }

    public static final class ProfileState {
        public final int mode;
        public final int cpuCap;
        public final int gpuCap;
        ProfileState(int mode, int cpuCap, int gpuCap) {
            this.mode = mode;
            this.cpuCap = cpuCap;
            this.gpuCap = gpuCap;
        }
    }
}
