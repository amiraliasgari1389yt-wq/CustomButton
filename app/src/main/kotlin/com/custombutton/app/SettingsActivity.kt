package com.custombutton.app

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.custombutton.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SHOW_SELECT_PROMPT = "show_select_prompt"
    }

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.app_name)

        if (intent.getBooleanExtra(EXTRA_SHOW_SELECT_PROMPT, false)) {
            Toast.makeText(this, R.string.please_select_app, Toast.LENGTH_LONG).show()
        }

        setupPreview()
        setupLengthSlider()
        setupHoldDurationSlider()
        setupPermissionRow()
        setupChooseAppRow()
        setupMasterSwitch()
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionRow()
        refreshChooseAppRow()
        binding.masterSwitch.isChecked =
            PrefsHelper.isOverlayEnabled(this) && Settings.canDrawOverlays(this)
    }

    private fun setupPreview() {
        binding.previewPill.background = PillAppearance.buildDrawable(this)
        updatePreviewWidth(PrefsHelper.getLengthPercent(this))
    }

    private fun updatePreviewWidth(percent: Int) {
        val widthPx = PillAppearance.dpToPx(this, PillAppearance.widthDp(percent)).toInt()
        val params = binding.previewPill.layoutParams
        params.width = widthPx
        binding.previewPill.layoutParams = params
    }

    private fun setupLengthSlider() {
        val currentPercent = PrefsHelper.getLengthPercent(this)
        binding.lengthSlider.value = currentPercent.toFloat().coerceIn(50f, 200f)

        binding.lengthSlider.addOnChangeListener { _: Slider, value: Float, fromUser: Boolean ->
            val percent = value.toInt()
            updatePreviewWidth(percent)
            if (fromUser) {
                PrefsHelper.setLengthPercent(this, percent)
            }
        }
    }

    private fun setupHoldDurationSlider() {
        val currentMs = PrefsHelper.getHoldDurationMs(this)
        binding.holdDurationSlider.value = currentMs.toFloat().coerceIn(200f, 1500f)
        updateHoldDurationLabel(currentMs)

        binding.holdDurationSlider.addOnChangeListener { _: Slider, value: Float, fromUser: Boolean ->
            val ms = value.toInt()
            updateHoldDurationLabel(ms)
            if (fromUser) {
                PrefsHelper.setHoldDurationMs(this, ms)
            }
        }
    }

    private fun updateHoldDurationLabel(ms: Int) {
        val seconds = ms / 1000f
        binding.holdDurationLabel.text = getString(R.string.hold_duration_slider_label, seconds)
    }

    private fun setupPermissionRow() {
        binding.permissionRow.setOnClickListener {
            openOverlayPermissionSettings()
        }
        refreshPermissionRow()
    }

    private fun openOverlayPermissionSettings() {
        val uri = Uri.parse("package:$packageName")
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
        startActivity(intent)
    }

    private fun refreshPermissionRow() {
        val granted = Settings.canDrawOverlays(this)
        binding.permissionStatus.text = if (granted) {
            getString(R.string.permission_granted)
        } else {
            getString(R.string.permission_not_granted)
        }
    }

    private fun setupChooseAppRow() {
        binding.chooseAppRow.setOnClickListener {
            startActivity(Intent(this, AppPickerActivity::class.java))
        }
        refreshChooseAppRow()
    }

    private fun refreshChooseAppRow() {
        val targetPackage = PrefsHelper.getTargetPackage(this)
        if (targetPackage == null) {
            binding.chooseAppStatus.text = getString(R.string.no_app_selected)
            return
        }

        val label: String? = try {
            val appInfo = packageManager.getApplicationInfo(targetPackage, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (notFound: PackageManager.NameNotFoundException) {
            null
        }

        binding.chooseAppStatus.text = label ?: getString(R.string.no_app_selected)
    }

    private fun setupMasterSwitch() {
        binding.masterSwitch.isChecked =
            PrefsHelper.isOverlayEnabled(this) && Settings.canDrawOverlays(this)

        binding.masterSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!Settings.canDrawOverlays(this)) {
                    binding.masterSwitch.isChecked = false
                    Toast.makeText(
                        this,
                        R.string.overlay_permission_needed_toast,
                        Toast.LENGTH_LONG
                    ).show()
                    openOverlayPermissionSettings()
                    return@setOnCheckedChangeListener
                }
                PrefsHelper.setOverlayEnabled(this, true)
                OverlayService.start(this)
            } else {
                PrefsHelper.setOverlayEnabled(this, false)
                OverlayService.stop(this)
            }
        }
    }
}
