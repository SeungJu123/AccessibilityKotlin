package com.example.accessibilitykotlin

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.ACTION_LONG_CLICK
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction
import android.widget.Toast


class MyAccessibilityService : AccessibilityService() {
    private val TAG = "TEST_ACCESSIBILITY_SERVICE"
    private val TAG_NODE_CHECK = "NODEINFOS"

    override fun onServiceConnected() {
        val serviceInfo = AccessibilityServiceInfo()

        // ... some settings
        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK

        setServiceInfo(serviceInfo)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val nodeInfo = event?.source

        nodeInfo?.let {
//            testLog(event)

            if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//                nodeInfoCheck(nodeInfo, 0)
//                customRectInfo(nodeInfo)
//                childCheck(nodeInfo, 0)
            }
        }

    }

    /**
     * 최근 앱 - '모두 닫기' 버튼 클릭 함수 (보완중..)
     */
    private fun checkAllClearButton(info: AccessibilityNodeInfo?) {
        info?.text?.let {
            if (it.toString().equals("모두 닫기")) {
                info.addAction(AccessibilityAction.ACTION_CLICK)
            }
        }
    }

    /**
     * 받은 노드 정보의 자녀 노드 정보를 검색
     * depth 값을 통한 들여쓰기 (보완중..)
     */
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

    /**
     * 백그라운드 실행 앱에 대한 체크 및 차단
     * 재귀 함수를 통해, 해당 앱명과, 중지버튼이 활성화되어 있는 것을 체크한다. (tCount)
     * tCount가 2일 경우(해당 앱이 백그라운드로 동작하며, 중지 버튼이 활성화되어 노드 정보에 확인 가능한 경우),
     * performGlobalAction (BACK, HOME) 을 통해 홈화면으로 전환한다.
     */
    private fun childCheck(parentInfo: AccessibilityNodeInfo?, depth: Int) {
        if (parentInfo == null || parentInfo.childCount == 0) {
            return
        }
        customLogNodeInfoSimple(parentInfo)
        var tDepth = depth
        val count = parentInfo.childCount
        var tCount: Int = 0
        for (i in 0 until count) {
            val child = parentInfo.getChild(i)
            if (child != null) {
                if (child.text?.toString().equals("체크할 앱명") || child.text?.toString()
                        .equals("중지")
                ) {
                    tCount++
                }
                customLogNodeInfo(child.text?.toString() ?: "", tDepth)
                if (child.childCount != 0) {
                    childCheck(child, ++tDepth)
                }
            }
        }
        if (tCount == 2) {
            performGlobalAction(GLOBAL_ACTION_BACK)
            performGlobalAction(GLOBAL_ACTION_HOME)
            Toast.makeText(
                applicationContext,
                "차단용 토스트 메시지",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun customLogD(header: String, text: String) {
        Log.d(TAG, "$header[$text]")
    }

    /**
     * 해당 노드 정보의 AccessibilityAction 리스트 조회
     *
     * ex)
     * TAG[AccessibilityAction: ACTION_FOCUS - null]
     * TAG[AccessibilityAction: ACTION_SELECT - null]
     * TAG[AccessibilityAction: ACTION_CLEAR_SELECTION - null]
     * TAG[AccessibilityAction: ACTION_CLICK - null]
     * TAG[AccessibilityAction: ACTION_LONG_CLICK - null]
     * TAG[AccessibilityAction: ACTION_ACCESSIBILITY_FOCUS - null]
     * TAG[AccessibilityAction: ACTION_SHOW_ON_SCREEN - null]
     * TAG[AccessibilityAction: ACTION_UNKNOWN - 닫기]
     *
     * 일 경우, nodeInfo.performAction 함수를 통해 동작할 수 있고, actionList의 id로 UNKNOWN에 대한 동작도 가능하다.
     */
    private fun checkNodeInfoActionList(nodeInfo: AccessibilityNodeInfo) {
        nodeInfo.actionList?.let {
            for (action in it) {
                customLogD(TAG, action.toString() ?: "")
            }
            nodeInfo.performAction(nodeInfo.actionList.get(nodeInfo.actionList.size - 1).id)
            nodeInfo.performAction(ACTION_LONG_CLICK)
        }
    }

    private fun customLogNodeInfoSimple(nodeInfo: AccessibilityNodeInfo?) {
        nodeInfo?.let {
            Log.d(TAG_NODE_CHECK, "-----------------------------------------------------------")
            Log.d(TAG_NODE_CHECK, "packageName : " + nodeInfo.packageName ?: "")
            Log.d(TAG_NODE_CHECK, "className : " + nodeInfo.className ?: "")
            Log.d(TAG_NODE_CHECK, "text : " + nodeInfo.text ?: "")
            Log.d(TAG_NODE_CHECK, "contentDescription : " + nodeInfo.contentDescription ?: "")

            /**
             * 최근 앱의 경우, text가 아닌 description에서 문구를 체크할 수 있다.
             */
            nodeInfo.contentDescription?.let {
                if (it.toString()
                        .contains("토스") && nodeInfo.className.equals("android.widget.FrameLayout")
                ) {
                    checkNodeInfoActionList(nodeInfo)
                }
            }
            Log.d(TAG_NODE_CHECK, "-----------------------------------------------------------")
        }
    }

    private fun customLogNodeInfo(text: String, depth: Int) {
        var tStringBuilder = StringBuilder()
        for (i in 0..depth) {
            tStringBuilder.append("\t")
        }
        Log.d(TAG_NODE_CHECK, "${tStringBuilder} ${text}")
    }

    private fun customRectInfo(nodeInfo: AccessibilityNodeInfo) {
        val RectParent = Rect()
        val RectScreen = Rect()

        nodeInfo?.getBoundsInParent(RectParent)
        nodeInfo?.getBoundsInScreen(RectScreen)

        Log.e(
            TAG,
            "nodeInfo RectParent : (" + RectParent.width() + "/" + RectParent.height() + ")"
        )
        Log.e(
            TAG,
            "nodeInfo RectScreen : (" + RectScreen.width() + "/" + RectScreen.height() + ")"
        )
    }

    private fun testLog(event: AccessibilityEvent) {
        val source = event?.source
        Log.e(TAG, "=========================================================================")
        Log.e(TAG, "Catch Event Package Name : " + event.getPackageName())
        Log.e(TAG, "Catch Event getClass Name : " + event.getClassName())
        Log.e(TAG, "Catch Event TEXT : " + event.getText())
        Log.e(TAG, "Catch Event ContentDescription  : " + event.getContentDescription())
        Log.e(TAG, "Catch Event event TYPE  :  " + event.getContentChangeTypes())
        Log.e(TAG, "Catch Event getSource : " + source?.getText())
        Log.e(TAG, "Catch Event STRING TYPE  :  " + getEventType(event))
        Log.e(TAG, "Catch Event int TYPE  :  " + event.getEventType())
        Log.e(TAG, "Catch Event getMovementGranularity  :  " + event.getMovementGranularity())
        Log.e(
            TAG,
            "Catch Event event Type String :" + AccessibilityEvent.eventTypeToString(event.getEventType())
        )
        Log.e(TAG, "Catch Event ACTION TYPE  :  " + event.getAction())

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
        return super.onUnbind(intent)
    }
}