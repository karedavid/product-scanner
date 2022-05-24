package com.kdapps.productscanner

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class ProductScannerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance("https://product-scanner-7093a-default-rtdb.europe-west1.firebasedatabase.app/").setPersistenceEnabled(false)
    }
}