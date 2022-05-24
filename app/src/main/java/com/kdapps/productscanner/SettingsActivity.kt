package com.kdapps.productscanner

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kdapps.productscanner.data.DataRepository
import com.kdapps.productscanner.databinding.SettingsActivityBinding

import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.firebase.ui.auth.util.ExtraConstants
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SettingsActivityBinding.inflate(layoutInflater)

        setSupportActionBar(binding.topAppBar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        window.navigationBarColor = getColor(R.color.navigationBarColorSettings)

        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment(application))
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    class SettingsFragment(val application: Application) : PreferenceFragmentCompat() {

        lateinit var repository: DataRepository

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            repository = DataRepository(application)

            val purgeButton = preferenceManager.findPreference<Preference>("purge_database")
            val demoButton  = preferenceManager.findPreference<Preference>("demo_data")

            if (purgeButton != null) {
                purgeButton.onPreferenceClickListener = Preference.OnPreferenceClickListener {

                    AlertDialog.Builder(activity as SettingsActivity)
                        .setTitle(getString(R.string.database_clearing_title))
                        .setMessage(getString(R.string.database_clearing_message))
                        .setPositiveButton(getString(R.string.yes)) { _, _ -> repository.purgeDatabase() }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()

                    true
                }
            }

            if (demoButton != null) {
                demoButton.onPreferenceClickListener = Preference.OnPreferenceClickListener {

                    AlertDialog.Builder(activity as SettingsActivity)
                        .setTitle(getString(R.string.demo_data_title))
                        .setMessage(getString(R.string.demo_data_message))
                        .setPositiveButton(getString(R.string.yes)) { _, _ -> repository.populateWithDummy() }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()

                    true
                }
            }
        }
    }

}