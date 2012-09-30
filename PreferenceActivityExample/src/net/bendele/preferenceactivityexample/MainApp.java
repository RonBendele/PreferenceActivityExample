package net.bendele.preferenceactivityexample;

import android.app.Application;

public class MainApp extends Application {

    private static boolean mTtsAvailable = true;
    private static boolean mUseTTS = true;

    protected static boolean getTtsAvailable (){
        return mTtsAvailable;
    }

    protected static void setTtsAvailable (boolean isTtsAvailable){
        mTtsAvailable = isTtsAvailable;
    }

    protected static boolean getUseTTS (){
        return mUseTTS;
    }

    protected static void setUseTTS (boolean useTTS){
        mUseTTS = useTTS;
    }
}
