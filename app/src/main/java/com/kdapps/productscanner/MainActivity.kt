package com.kdapps.productscanner

import android.Manifest
import android.graphics.Color.WHITE
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.kdapps.productscanner.databinding.ActivityMainBinding
import com.kdapps.productscanner.ui.database.DatabaseFragment
import com.kdapps.productscanner.ui.scanner.ScannerFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var scannerFragment : ScannerFragment
    private lateinit var databaseFragment : DatabaseFragment
    private lateinit var active : Fragment

    private lateinit var navView : BottomNavigationView

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Snackbar.make(binding.root.rootView,getString(R.string.camera_denied), Snackbar.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestCameraPermission.launch(Manifest.permission.CAMERA)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navView = binding.navView

        if (savedInstanceState == null) {
            scannerFragment = ScannerFragment()
            databaseFragment = DatabaseFragment()
            active  = scannerFragment

            supportFragmentManager.beginTransaction().add(R.id.nav_host_fragment_activity_main,scannerFragment, "SCANNER").commit()
            supportFragmentManager.beginTransaction().add(R.id.nav_host_fragment_activity_main,databaseFragment, "DATABASE").hide(databaseFragment).commit()
        }else{
            scannerFragment = supportFragmentManager.findFragmentByTag("SCANNER") as ScannerFragment
            databaseFragment = supportFragmentManager.findFragmentByTag("DATABASE") as DatabaseFragment

            active = if(savedInstanceState.getInt("PAGE") == R.id.navigation_scanner){
                scannerFragment
            }else{
                databaseFragment
            }

        }

        navView.setOnItemSelectedListener {
            if(it.itemId == R.id.navigation_scanner){
                supportFragmentManager.beginTransaction().hide(active).show(scannerFragment).commit()
                active = scannerFragment
            }else{
                supportFragmentManager.beginTransaction().hide(active).show(databaseFragment).commit()
                active = databaseFragment
            }
            return@setOnItemSelectedListener true
        }



        Firebase.auth.addAuthStateListener {

            val loginSnackbar = Snackbar.make(scannerFragment.view as View, "", Snackbar.LENGTH_SHORT)
                .setAnimationMode(ANIMATION_MODE_SLIDE)
                .setBackgroundTint(getColor(R.color.navigationBarColor))
                .setTextColor(WHITE)

            if(it.currentUser != null){
                loginSnackbar.setText(getString(R.string.status_signed_in_long, Firebase.auth.currentUser!!.email))

                val ref = FirebaseDatabase.getInstance("https://product-scanner-7093a-default-rtdb.europe-west1.firebasedatabase.app/").reference.child(it.currentUser!!.uid).child("products")
                ref.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val size = dataSnapshot.childrenCount.toInt()
                        if(size>0){
                            binding.navView.getOrCreateBadge(R.id.navigation_database).number = size
                            binding.navView.getOrCreateBadge(R.id.navigation_database).backgroundColor = getColor(R.color.md_theme_light_primaryInverse)
                            binding.navView.getOrCreateBadge(R.id.navigation_database).isVisible = true
                        }else{
                            binding.navView.getOrCreateBadge(R.id.navigation_database).isVisible = false
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })

            }else{
                loginSnackbar.setText(getString(R.string.status_signed_out))
                binding.navView.removeBadge(R.id.navigation_database)
            }

            loginSnackbar.show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("PAGE", navView.selectedItemId)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                scannerFragment.add()
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                scannerFragment.remove()
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

}