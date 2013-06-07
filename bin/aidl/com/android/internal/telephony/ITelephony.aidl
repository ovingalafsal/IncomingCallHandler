package com.android.internal.telephony;

import android.os.Bundle;
    interface ITelephony {
        boolean endCall();
     void dial(String number);
    void answerRingingCall();
    void silenceRinger();
    }