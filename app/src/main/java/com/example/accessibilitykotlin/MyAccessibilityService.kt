package com.example.accessibilitykotlin

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import android.widget.Toast


class MyAccessibilityService : AccessibilityService() {
    private val TAG = "TEST_ACCESSIBILITY_SERVICE"
    private val TAG_NODE_CHECK = "NODEINFOS"

    override fun onServiceConnected() {
        // AccessibilityService 를 ON으로 한 타이밍에 호출된다
        val serviceInfo = AccessibilityServiceInfo()

        // ... some settings
        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK

        setServiceInfo(serviceInfo)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val nodeInfo = event?.source

        nodeInfo?.let {
            testLog(event)

            if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//                nodeInfoCheck(nodeInfo, 0)
            }
        }

    }

    private fun nodeInfoCheck(parentInfo: AccessibilityNodeInfo?, depth: Int) {
        if (parentInfo == null || parentInfo.childCount == 0) {
            return
        }
        var tDepth = depth
        val count = parentInfo.childCount
        for (i in 0 until count) {
            val child = parentInfo.getChild(i)
            if (child != null) {
                customLogNodeInfo(child.text?.toString() ?: child.toString(), tDepth)
                if (child.childCount != 0) {
                    nodeInfoCheck(child, ++tDepth)
                }
            }
        }
    }

    private fun childCheck(parentInfo: AccessibilityNodeInfo?, depth: Int) {
        if (parentInfo == null || parentInfo.childCount == 0) {
            return
        }
        var tDepth = depth
        val count = parentInfo.childCount
        var tCount: Int = 0
        for (i in 0 until count) {
            val child = parentInfo.getChild(i)
            if (child != null) {
                if (child.text?.toString().equals("토스") || child.text?.toString().equals("중지")) {
                    tCount++
                }
                customLogNodeInfo(child.text?.toString() ?: "", tDepth)
                if (child.childCount != 0) {
                    childCheck(child, ++tDepth)
                }
            }
        }
        if (tCount == 2) {
//            performGlobalAction(GLOBAL_ACTION_BACK)
//            performGlobalAction(GLOBAL_ACTION_HOME)
            Toast.makeText(
                applicationContext,
                "일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십일이삼사오육칠팔구십",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun customLogD(header: String, text: String) {
        Log.d(TAG, "$header[$text]")
    }

    private fun customLogNodeInfo(text: String, depth: Int) {
        var tStringBuilder = StringBuilder()
        for (i in 0..depth) {
            tStringBuilder.append("\t")
        }
        Log.d(TAG_NODE_CHECK, "${tStringBuilder} ${text}")
    }

    private fun testLog(event: AccessibilityEvent) {
        val source = event?.source
        val RectParent = Rect()
        val RectScreen = Rect()

//        event?.source.findAccessibilityNodeInfosByText()
//        event?.source.findAccessibilityNodeInfosByViewId()

        source?.getBoundsInParent(RectParent)
        source?.getBoundsInScreen(RectScreen)
//        Log.e(TAG, "Catch Event : " + event.toString())
        Log.e(
            TAG,
            "Catch Event RectParent : (" + RectParent.width() + "/" + RectParent.height() + ")"
        )
        Log.e(
            TAG,
            "Catch Event RectScreen : (" + RectScreen.width() + "/" + RectScreen.height() + ")"
        )
        Log.e(TAG, "Catch Event Package Name : " + event.getPackageName())
        Log.e(TAG, "Catch Event getClass Name : " + event.getClassName())
        Log.e(TAG, "Catch Event nodeInfo getId : " + " ")
//        event.source?.addAction(GLOBAL_ACTION_ACCESSIBILITY_BUTTON)

//        Log.e(TAG, "Catch Event TEXT : " + event.getText())
//        Log.e(TAG, "Catch Event ContentDescription  : " + event.getContentDescription())
//        Log.e(TAG, "Catch Event event TYPE  :  " + event.getContentChangeTypes())
//        Log.e(TAG, "Catch Event getSource : " + event.getSource()?.getText())
        Log.e(TAG, "Catch Event STRING TYPE  :  " + getEventType(event))
//        Log.e(TAG, "Catch Event int TYPE  :  " + event.getEventType())
//        Log.e(TAG, "Catch Event getMovementGranularity  :  " + event.getMovementGranularity())
        val systemService = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = systemService.runningAppProcesses
        Log.e(TAG, "catch Event runningProcess Size : " + runningAppProcesses?.size)
        for (runningProcess in runningAppProcesses) {
            Log.e(TAG, "catch Event runningProcess : " + runningProcess?.processName)
        }

        val appTasks = systemService.appTasks
        Log.e(TAG, "catch Event task Size : " + appTasks?.size)
        for (task in appTasks) {
            Log.e(TAG, "catch Event app Task : " + task?.taskInfo)
        }

        val s = systemService.appTasks.get(0).taskInfo.topActivity?.className

        Log.e(TAG, "catch Event topActivity: " + s?.toString())


        Log.e(
            TAG,
            "Catch Event event Type String :" + AccessibilityEvent.eventTypeToString(event.getEventType())
        )
        Log.e(TAG, "Catch Event ACTION TYPE  :  " + event.getAction())

        Log.e(TAG, "=========================================================================")
        Log.e(TAG, "=========================================================================")
    }

    private fun getEventType(event: AccessibilityEvent): String? {
        when (event.eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> return "알림 상태 변화"
            AccessibilityEvent.TYPE_VIEW_CLICKED -> return "뷰 클릭"
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> return "뷰 포커스"
            AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> return "뷰 롱클릭"
            AccessibilityEvent.TYPE_VIEW_SELECTED -> return "뷰 선택"
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> return "윈도우 상태 변화"
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> return "뷰 텍스트 변화"
            AccessibilityEvent.WINDOWS_CHANGE_PIP -> return "PIP모드 적용"
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> return "윈도우 컨텐츠 변경"
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> return "뷰 스크롤"
        }
        return event.eventType.toString()
    }


    override fun onInterrupt() {
        TODO("Not yet implemented")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
        return super.onUnbind(intent)
    }
}