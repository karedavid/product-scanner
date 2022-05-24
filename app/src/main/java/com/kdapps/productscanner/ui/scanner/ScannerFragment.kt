package com.kdapps.productscanner.ui.scanner

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.budiyev.android.codescanner.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.zxing.BarcodeFormat
import com.kdapps.productscanner.LoginActivity
import com.kdapps.productscanner.R
import com.kdapps.productscanner.SettingsActivity
import com.kdapps.productscanner.databinding.FragmentScannerBinding
import java.io.File
import kotlin.concurrent.thread


class ScannerFragment : Fragment() {

    private var _binding: FragmentScannerBinding? = null
    private lateinit var codeScanner: CodeScanner
    private lateinit var beep: MediaPlayer
    private lateinit var popupAccount: PopupWindow
    private lateinit var signInButton: Button
    private lateinit var signOutButton: Button
    private lateinit var userState: TextView
    private lateinit var userID: TextView

    private val binding get() = _binding!!
    private lateinit var scannerViewModel: ScannerViewModel

    private lateinit var imageUri: Uri

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSaved ->
        if (isSaved) {
            scannerViewModel.addImageToScannedItem(scannerViewModel.imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        scannerViewModel = ViewModelProvider(this).get(ScannerViewModel::class.java)

        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initializing scanner
        val scannerView = binding.scannerView
        codeScanner = CodeScanner(requireContext(), scannerView)

        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = listOf(BarcodeFormat.QR_CODE) // list of type BarcodeFormat, ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.CONTINUOUS // or CONTINUOUS
        codeScanner.scanMode = ScanMode.CONTINUOUS // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        // Sound feedback
        beep = MediaPlayer.create(requireActivity(), R.raw.beep)
        beep.setVolume(0.3f, 0.3f)

        // On successful scan
        codeScanner.decodeCallback = DecodeCallback {
            activity?.runOnUiThread {
                if (scannerViewModel.currentProduct.value != it.text) {
                    scannerViewModel.currentProduct.value = it.text

                    if (scannerViewModel.getSoundSettings()) {
                        if (beep.isPlaying) {
                            beep.pause()
                            beep.seekTo(0)
                        }
                        beep.start()
                    }
                }
            }
        }

        // On scan error
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            activity?.runOnUiThread {
                Log.e("ScannerView", "Camera initialization error: ${it.message}")
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }

        scannerViewModel.codeValid.observe(viewLifecycleOwner) {
            val result = scannerViewModel.currentProduct.value
            binding.codeView.text = result
            binding.codeView.setTextColor(Color.WHITE)

            if (it != null) {
                binding.plusButton.isEnabled = it
                binding.removeButton.isEnabled = it

                binding.cameraButton.isEnabled = scannerViewModel.currentProductQuantity.value != null && scannerViewModel.currentProductQuantity.value!! > 0.toLong() && it


                if (it) {
                    // Product code verified

                    // Make haptics if supported and enabled
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && scannerViewModel.getHapticsSettings()) {
                        binding.codeView.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    }
                    binding.codeView.text = result
                    binding.codeView.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.valid, null))
                    binding.codeView.startAnimation(loadAnimation(requireActivity(), R.anim.scaleup))
                    binding.quantityView.visibility = VISIBLE

                } else {
                    // Product code denied

                    // Make haptics if supported and enabled
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && scannerViewModel.getHapticsSettings()) {
                        binding.codeView.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    }
                    binding.codeView.text = getString(R.string.invalidProductCode, result)
                    binding.codeView.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.invalid, null))
                    binding.codeView.startAnimation(loadAnimation(requireActivity(), R.anim.shake))
                    binding.quantityView.visibility = GONE
                }
            }
        }

        scannerViewModel.currentProductQuantity.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.removeButton.isEnabled = it > 0.toLong()
                binding.cameraButton.isEnabled = it > 0.toLong()
                binding.quantityView.text = resources.getString(R.string.current_quantity_format, it)
            }
        }

        binding.plusButton.setOnClickListener {

            if (scannerViewModel.currentProductQuantity.value == 0.toLong()) {
                val photoFile = File.createTempFile(
                    "IMG_",
                    ".jpg",
                    requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                )

                scannerViewModel.imageUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    photoFile
                )

                takePicture.launch(scannerViewModel.imageUri)

            }
            scannerViewModel.addScannedItem()
            binding.quantityView.startAnimation(loadAnimation(requireActivity(), R.anim.scaleup))
        }

        binding.cameraButton.setOnClickListener {
            val photoFile = File.createTempFile(
                "IMG_",
                ".jpg",
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )

            scannerViewModel.imageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )

            takePicture.launch(scannerViewModel.imageUri)
        }

        binding.removeButton.setOnClickListener {
            scannerViewModel.removeScannedItem(false)
            binding.quantityView.startAnimation(loadAnimation(requireActivity(), R.anim.scaledown))
        }

        binding.removeButton.setOnLongClickListener {
            scannerViewModel.removeScannedItem(true)
            binding.quantityView.startAnimation(loadAnimation(requireActivity(), R.anim.scaledown))
            return@setOnLongClickListener true
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (context as AppCompatActivity).setSupportActionBar(binding.topAppBar)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        codeScanner.releaseResources()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        thread {
            codeScanner.stopPreview()
            codeScanner.releaseResources()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden) {
            codeScanner.startPreview()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isHidden) {
            thread {
                synchronized(codeScanner) {
                    codeScanner.stopPreview()
                }

            }
        } else {
            thread {
                synchronized(codeScanner) {
                    codeScanner.startPreview()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.scanner_top_menu, menu)
        val layout = layoutInflater.inflate(R.layout.user_account_popup, null)
        popupAccount = PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        popupAccount.isFocusable = true
        signInButton = popupAccount.contentView.findViewById(R.id.btnSignIn) as Button
        signOutButton = popupAccount.contentView.findViewById(R.id.btnSignOut) as Button
        userState = popupAccount.contentView.findViewById(R.id.twHeader) as TextView
        userID = popupAccount.contentView.findViewById(R.id.twUserId) as TextView

        signOutButton.setOnClickListener {
            Firebase.auth.signOut()
            popupAccount.dismiss()
        }

        signInButton.setOnClickListener {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }

        Firebase.auth.addAuthStateListener {
            if (it.currentUser != null) {
                userState.text = getString(R.string.status_signed_in)
                userID.text = Firebase.auth.currentUser!!.email
                signInButton.visibility = GONE
                signOutButton.visibility = VISIBLE

            } else {
                userState.text = getString(R.string.status_signed_out)
                userID.text = getString(R.string.no_user)
                signOutButton.visibility = GONE
                signInButton.visibility = VISIBLE
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.navigation_settings) {
            startActivity(Intent(activity, SettingsActivity::class.java))
        } else if (item.itemId == R.id.account_popup) {
            popupAccount.showAsDropDown(activity?.findViewById(R.id.account_popup), 50, 0)
        }

        return super.onOptionsItemSelected(item)
    }

    fun add(): Boolean {
        if (scannerViewModel.getHardwareButtonSettings()) {
            if (binding.plusButton.isEnabled)
                binding.plusButton.performClick()
            return true
        }
        return false
    }

    fun remove(): Boolean {
        if (scannerViewModel.getHardwareButtonSettings()) {
            if (binding.removeButton.isEnabled)
                binding.removeButton.performClick()
            return true
        }
        return false
    }


}