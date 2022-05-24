package com.kdapps.productscanner.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


object DataRepository {

    interface DataOperation {
        fun onDatabaseModified()
    }

    // Settings constants
    private const val KEY_HAPTIC_SETTINGS   = "haptics"
    private const val KEY_SOUND_SETTINGS    = "sounds"
    private const val KEY_BUTTON_SETTINGS   = "volume_buttons"

    // Firebase constants
    private const val FIREBASE_REALTIME_DATABASE_URL = "https://product-scanner-7093a-default-rtdb.europe-west1.firebasedatabase.app/"
    private const val FIREBASE_STORAGE_URL           = "gs://product-scanner-7093a.appspot.com"

    // Subscribers to notify about changes
    private val subscribers: ArrayList<DataOperation>

    // Data sources
    private lateinit var databaseDao        : DatabaseDao
    private          var firebaseReference  : DatabaseReference
    private          var storageReference   : StorageReference
    private lateinit var sharedPreferences  : SharedPreferences

    operator fun invoke(context: Context): DataRepository {
        databaseDao         = ProductDatabase.getDatabase(context).databaseDao()
        sharedPreferences   = PreferenceManager.getDefaultSharedPreferences(context)
        return this
    }

    init {
        firebaseReference   = FirebaseDatabase.getInstance(FIREBASE_REALTIME_DATABASE_URL).reference
        storageReference    = Firebase.storage(FIREBASE_STORAGE_URL).reference
        subscribers         = ArrayList()
    }

    private fun getProductsTable() : DatabaseReference {
        return firebaseReference.child(Firebase.auth.currentUser!!.uid).child("products")
    }

    private fun getProductReference(code: String) : DatabaseReference {
        return getProductsTable().child(code)
    }

    // Product related queries

    fun getQuantity(code: String): DatabaseReference? {
        return if (Firebase.auth.currentUser != null) {
            getProductReference(code).child("quantity")
        } else {
            null
        }
    }

    fun getDatabaseSize(): LiveData<Int> {
        return databaseDao.getDatabaseSize()
    }

    fun populateWithDummy() {
        if (Firebase.auth.currentUser != null) {
            thread {
                for (i in 1..200) {
                    val random = SecureRandom()
                    val numbers = "0123456789".toCharArray()
                    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
                    val code = (1..10).map { numbers[random.nextInt(numbers.size)] }.joinToString("")
                    getProductReference("PRODUCT$code").setValue(Product("PRODUCT$code", i.toLong()))
                }
            }
        }

    }

    fun addImageToProduct(code: String, uri : Uri) {
        if (Firebase.auth.currentUser != null) {
            thread {
                val productReference = getProductReference(code)

                val readListener = object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val product: Product? = dataSnapshot.getValue(Product::class.java)

                        if (product?.image != null) {
                            val testRef = storageReference.child(Firebase.auth.uid!!).child(product.image!!)
                            testRef.delete()
                        }

                        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

                        val filename = "${code}_${timeStamp}.jpg"
                        val testRef = storageReference.child(Firebase.auth.uid!!).child(filename)
                        val uploadTask = testRef.putFile(uri)

                        uploadTask.addOnFailureListener {
                            //TODO notify user about error
                        }.addOnSuccessListener { taskSnapshot ->
                            //TODO notify user about success
                        }

                        val updates: MutableMap<String, Any> = HashMap()
                        updates["$code/image"] = filename
                        getProductsTable().updateChildren(updates)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                }

                productReference.addListenerForSingleValueEvent(readListener)
            }
        }
    }

    fun incrementProduct(code: String, delta: Long) {
        if (Firebase.auth.currentUser != null) {
            thread {
                val updates: MutableMap<String, Any> = HashMap()
                updates["$code/product_code"] = code
                updates["$code/quantity"] = ServerValue.increment(delta)
                getProductsTable().updateChildren(updates)
            }
        }
    }

    fun deleteProduct(code: String) {
        if (Firebase.auth.currentUser != null) {
            thread {

                val productReference = getProductReference(code)

                val readListener = object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val product: Product? = dataSnapshot.getValue(Product::class.java)

                        if (product?.image != null) {
                            val testRef = storageReference.child(Firebase.auth.uid!!).child(product.image!!)
                            testRef.delete()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                }

                productReference.addListenerForSingleValueEvent(readListener)
                productReference.setValue(null)

                subscribers.forEach {
                    it.onDatabaseModified()
                }
            }
        }
    }

    fun purgeDatabase() {
        if (Firebase.auth.currentUser != null) {
            thread {
                getProductsTable().setValue(null)

                subscribers.forEach {
                    it.onDatabaseModified()
                }

            }
        }

    }

    fun isHapticsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAPTIC_SETTINGS, true)
    }

    fun isSoundEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_SOUND_SETTINGS, true)
    }

    fun isHardwareButtonsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BUTTON_SETTINGS, false)
    }

    fun subscribeChanges(subscriber: DataOperation) {
        subscribers.add(subscriber)
    }

    fun unsubscribeChanges(subscriber: DataOperation) {
        subscribers.remove(subscriber)
    }

    fun searchProduct(filter: String): LiveData<List<Product>> {
        return databaseDao.getMatchingProduct("%$filter%")
    }
}