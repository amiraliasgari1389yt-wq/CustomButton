package com.custombutton.app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.custombutton.app.databinding.ActivityAppPickerBinding

class AppPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppPickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.settings_title)

        val apps = loadLaunchableApps()
        val currentSelection = PrefsHelper.getTargetPackage(this)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = AppListAdapter(apps, currentSelection) { selectedApp ->
            PrefsHelper.setTargetPackage(this, selectedApp.packageName)
            Toast.makeText(
                this,
                getString(R.string.app_selected, selectedApp.label),
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    private fun loadLaunchableApps(): List<AppInfo> {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolvedApps = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)

        return resolvedApps
            .asSequence()
            .filter { it.activityInfo.packageName != packageName }
            .map { resolveInfo ->
                AppInfo(
                    label = resolveInfo.loadLabel(pm).toString(),
                    packageName = resolveInfo.activityInfo.packageName,
                    icon = resolveInfo.loadIcon(pm)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
}
