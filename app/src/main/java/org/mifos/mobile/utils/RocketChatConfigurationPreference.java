package org.mifos.mobile.utils;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import org.mifos.mobile.api.local.PreferencesHelper;

/**
 * Created by dilpreet on 11/03/18.
 */

public class RocketChatConfigurationPreference extends DialogPreference {

    private PreferencesHelper preferencesHelper;

    public RocketChatConfigurationPreference(Context context, AttributeSet attrs,
                                             int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RocketChatConfigurationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RocketChatConfigurationPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        setPersistent(false);
        preferencesHelper = new PreferencesHelper(getContext());
    }

    public String getDomain() {
        return preferencesHelper.getRocketChatServerDomain();
    }

    public void updateConfigurations(String protocol, String domain) {
        preferencesHelper.saveRocketChatServerConfiguration(protocol, domain);
    }

    public String getProtocol() {
        return preferencesHelper.getRocketChatServerProtocol();
    }
}
