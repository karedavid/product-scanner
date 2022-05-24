package com.kdapps.productscanner.ui.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.ktx.Firebase
import com.kdapps.productscanner.R
import com.kdapps.productscanner.data.DataRepository
import com.kdapps.productscanner.data.Product

class DatabaseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DataRepository

    var databaseSize    : LiveData<Int> private set
    var filteredProduct : LiveData<List<Product>> private set
    var filterQuery         = MutableLiveData<String>()

    init {
        repository = DataRepository(application)
        databaseSize = repository.getDatabaseSize()

        filterQuery.value = ""
        filteredProduct   = Transformations.switchMap(filterQuery){
            repository.searchProduct(it)
        }
    }

    fun getAllProducts(): DatabaseReference {
        return FirebaseDatabase.getInstance("https://product-scanner-7093a-default-rtdb.europe-west1.firebasedatabase.app/")
            .reference
            .child(Firebase.auth.currentUser!!.uid)
            .child("products")
    }
}