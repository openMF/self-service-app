package org.mifos.selfserviceapp;

import android.app.Application;
import android.content.Context;
import android.support.test.runner.AndroidJUnitRunner;

/**
 * Created by dilpreet on 22/7/17.
 */

public class SelfServiceTestRunner extends AndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return super.newApplication(cl, TestMifosSelfServiceApp.class.getCanonicalName(), context);
    }
}
