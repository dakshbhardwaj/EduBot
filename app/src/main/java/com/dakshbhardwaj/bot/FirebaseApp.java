package com.dakshbhardwaj.bot;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by daksh on 5/1/18.
 */

public class FirebaseApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
