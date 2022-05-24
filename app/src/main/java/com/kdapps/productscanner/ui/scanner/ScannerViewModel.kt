package com.kdapps.productscanner.ui.scanner

import android.app.Application
import android.net.Uri
import android.os.Looper
import android.widget.Toast
import androidx.core.content.contentValuesOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.kdapps.productscanner.data.DataRepository
import kotlin.concurrent.thread

class ScannerViewModel(application: Application) : AndroidViewModel(application), DataRepository.DataOperation {

    private val repository: DataRepository

    var currentProduct          : MutableLiveData<String>
    var currentProductQuantity  : MutableLiveData<Long>     private set
    var codeValid               : MutableLiveData<Boolean>  private set

    lateinit var imageUri: Uri

    init {
        repository              = DataRepository(application)
        repository.subscribeChanges(this)
        currentProduct          = MutableLiveData<String>()
        currentProductQuantity  = MutableLiveData<Long>()
        codeValid               = MutableLiveData<Boolean>()

        currentProduct.observeForever{
            requestVerification(it)
            getCurrentQuantity()
        }

        Firebase.auth.addAuthStateListener {
            getCurrentQuantity()
        }
    }

    private fun requestVerification(input : String) {
        val code  = input.trim()
        val pattern = Regex("[0-9a-zA-z]*")

        if(code.isEmpty()){
            codeValid.value = false
        }else{
            codeValid.value = pattern.matches(code)
        }
    }

    private fun getCurrentQuantity(){
        thread {
            if(currentProduct.value != null && codeValid.value == true && Firebase.auth.currentUser != null) {

                val ref = DataRepository(getApplication()).getQuantity(currentProduct.value!!)

                val productQuantityListener = object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val quantity = snapshot.getValue<Long>()
                        if( quantity != null ){
                            currentProductQuantity.postValue(quantity)
                        }else{
                            currentProductQuantity.postValue(0)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                }
                ref?.addValueEventListener(productQuantityListener)
            }
        }
    }

    fun addScannedItem(){
        thread {
            if (currentProduct.value != null && codeValid.value == true) {
                repository.incrementProduct(currentProduct.value!!,1)
            }
        }
    }

    fun removeScannedItem(removeAll : Boolean = false){
        thread {
            if (currentProduct.value != null && codeValid.value == true && currentProductQuantity.value != null) {
                if(currentProductQuantity.value == 1.toLong() || removeAll){
                    repository.deleteProduct(currentProduct.value!!)
                }else {
                    repository.incrementProduct(currentProduct.value!!, -1)
                }
            }
        }
    }

    fun getHapticsSettings(): Boolean{
        return repository.isHapticsEnabled()
    }

    fun getSoundSettings(): Boolean{
        return repository.isSoundEnabled()
    }

    fun getHardwareButtonSettings(): Boolean{
        return repository.isHardwareButtonsEnabled()
    }
    override fun onDatabaseModified() {
        getCurrentQuantity()
    }

    fun addImageToScannedItem(uri: Uri){
        repository.addImageToProduct(currentProduct.value!!, uri)
    }
}

