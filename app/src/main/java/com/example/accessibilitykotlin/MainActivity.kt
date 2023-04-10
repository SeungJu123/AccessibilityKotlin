package com.example.accessibilitykotlin

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.accessibilitykotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (!checkAccessibilityPermissions()) {
            setAccessibilityPermissions()
        }

        binding.btnApply.setOnClickListener {
            val stringAppName = binding.appName.text.toString()
            val stringPackageName = binding.packageName.text.toString()

            MyAccessibilityService().applySetting(stringAppName, stringPackageName)
        }
    }

//    override fun onStart() {
//        super.onStart()
//
//        // When returning to a setup wizard activity, check to see if another setup process
//        // has intervened and, if so, complete an orderly exit
//        boolean completed = Settings.Secure.getInt(getContentResolver(),
//        Settings.Secure.USER_SETUP_COMPLETE, 0) != 0;
//        if (completed) {
//            startActivity(new Intent(Intent.ACTION_MAIN, null)
//                .addCategory(Intent.CATEGORY_HOME)
//                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
//            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
//            finish();
//        }
//    }

    fun checkAccessibilityPermissions(): Boolean {
        val accessibilityManager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager

        // getEnabledAccessibilityServiceList는 현재 접근성 권한을 가진 리스트를 가져오게 된다
        val list =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.DEFAULT)
        for (i in list.indices) {
            val info = list[i]

            // 접근성 권한을 가진 앱의 패키지 네임과 패키지 네임이 같으면 현재앱이 접근성 권한을 가지고 있다고 판단함
            if (info.resolveInfo.serviceInfo.packageName == application.packageName) {
                return true
            }
        }
        return false
    }

    fun setAccessibilityPermissions() {
        val gsDialog = AlertDialog.Builder(this)
        gsDialog.setTitle("접근성 권한 설정")
        gsDialog.setMessage("접근성 권한을 필요로 합니다")
        gsDialog.setPositiveButton(
            "확인",
            DialogInterface.OnClickListener { dialog, which -> // 설정화면으로 보내는 부분
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                return@OnClickListener
            }).create().show()

        Toast.makeText(applicationContext, "일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십", Toast.LENGTH_LONG).show()
    }
}