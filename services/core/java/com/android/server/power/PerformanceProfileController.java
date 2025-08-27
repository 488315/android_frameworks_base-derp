/*
 * Copyright (C) 2025
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

package com.android.server.power;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.DeviceConfig;
import android.provider.Settings;

import java.util.Objects;

/**
 * Helper to manage performance profile state.
 *
 * <p>Observes {@link Settings.Global#PERFORMANCE_PROFILE_MODE} and values in the
 * {@code DeviceConfig} namespace {@code "performance_profile"}.</p>
 *
 * <p>Call {@link #destroy()} when finished to avoid leaks.</p>
 */
public final class PerformanceProfileController {

    private static final String NAMESPACE = "performance_profile";
    private static final String KEY_CPU_CAP_LIGHT = "cpu_cap_light";
    private static final String KEY_GPU_CAP_LIGHT = "gpu_cap_light";

    /** Listener for mode changes. */
    public interface Listener {
        /** Called when the profile mode changes. */
        void onModeChanged(int mode);
    }

    private final Context mContext;
    private final ContentResolver mResolver;
    private final Listener mListener;

    /** Observes Global setting updates. */
    private ContentObserver mObserver;

    /** Observes DeviceConfig updates for this namespace. */
    private DeviceConfig.OnPropertiesChangedListener mDeviceConfigListener;

    /** Last known profile mode. */
    private volatile int mMode;

    /** True once observers are registered. */
    private boolean mStarted;

    /**
     * Constructs a controller. Call {@link #start()} after system is ready to begin observing
     * sources of truth.
     *
     * @param context non-null context
     * @param listener optional listener for mode changes, may be {@code null}
     */
    public PerformanceProfileController(Context context, Listener listener) {
        mContext = Objects.requireNonNull(context, "context");
        mResolver = mContext.getContentResolver();
        mListener = listener;

        // Create observers after all fields are assigned.
        mObserver =
                new ContentObserver(new Handler(Looper.getMainLooper())) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        updateMode();
                    }
                };

        mDeviceConfigListener =
                properties -> {
                    if (!NAMESPACE.equals(properties.getNamespace())) {
                        return;
                    }
                    // DeviceConfig changed; if the current mode influences caps, surface state.
                    Listener l = mListener;
                    if (l != null) {
                        l.onModeChanged(mMode);
                    }
                };
    }

    /** Starts observing settings and DeviceConfig. */
    public void start() {
        if (mStarted) {
            return;
        }
        mResolver.registerContentObserver(
                Settings.Global.getUriFor(Settings.Global.PERFORMANCE_PROFILE_MODE),
                false,
                mObserver);

        try {
            DeviceConfig.addOnPropertiesChangedListener(
                    NAMESPACE, mContext.getMainExecutor(), mDeviceConfigListener);
        } catch (IllegalArgumentException e) {
            // Defensive: should not happen for a valid namespace.
            // Keep running with Settings observer only.
        }

        // Seed initial state.
        updateMode();
        mStarted = true;
    }

    /**
     * Unregisters observers. Must be called when the controller is no longer needed.
     */
    public void destroy() {
        if (!mStarted) {
            return;
        }
        if (mObserver != null) {
            mResolver.unregisterContentObserver(mObserver);
        }
        if (mDeviceConfigListener != null) {
            DeviceConfig.removeOnPropertiesChangedListener(mDeviceConfigListener);
        }
        mStarted = false;
    }

    private void updateMode() {
        int mode =
                Settings.Global.getInt(mResolver, Settings.Global.PERFORMANCE_PROFILE_MODE, 0);
        if (mode != mMode) {
            mMode = mode;
            Listener l = mListener;
            if (l != null) {
                l.onModeChanged(mMode);
            }
        }
    }

    /** Returns the current profile mode. */
    public int getMode() {
        return mMode;
    }

    /**
     * Writes the profile mode to {@link Settings.Global}.
     *
     * <p>Caller must hold appropriate permission to write Global settings.</p>
     */
    public void setMode(int mode) {
        Settings.Global.putInt(mResolver, Settings.Global.PERFORMANCE_PROFILE_MODE, mode);
        // Optimistically reflect local state; Settings observer will confirm.
        if (mMode != mode) {
            mMode = mode;
            Listener l = mListener;
            if (l != null) {
                l.onModeChanged(mMode);
            }
        }
    }

    /** Returns the light CPU cap, default 100. */
    public int getCpuCapLight() {
        return DeviceConfig.getInt(NAMESPACE, KEY_CPU_CAP_LIGHT, /* defaultValue= */ 100);
    }

    /** Returns the light GPU cap, default 100. */
    public int getGpuCapLight() {
        return DeviceConfig.getInt(NAMESPACE, KEY_GPU_CAP_LIGHT, /* defaultValue= */ 100);
    }

    /** Snapshot of current state and caps. */
    public ProfileState getProfileState() {
        return new ProfileState(mMode, getCpuCapLight(), getGpuCapLight());
    }

    /** Immutable value object for profile state. */
    public static final class ProfileState {
        public final int mode;
        public final int cpuCap;
        public final int gpuCap;

        public ProfileState(int mode, int cpuCap, int gpuCap) {
            this.mode = mode;
            this.cpuCap = cpuCap;
            this.gpuCap = gpuCap;
        }
    }
}

