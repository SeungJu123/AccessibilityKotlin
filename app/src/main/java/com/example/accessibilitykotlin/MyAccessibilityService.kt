package com.example.accessibilitykotlin

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction
import android.widget.Toast


class MyAccessibilityService : AccessibilityService() {
    private val TAG = "TEST_ACCESSIBILITY_SERVICE"
    private val TAG_WINDOW_TEST = "WINDOW_TEST"
    private val TAG_NODE_CHECK = "NODEINFOS"

    companion object {
        lateinit var applyAppName: String
        lateinit var applyPackageName: String

        var isPopupCheck = false
        var isAccessibility = false
    }

    override fun onServiceConnected() {
        val serviceInfo = AccessibilityServiceInfo()
        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        setServiceInfo(serviceInfo)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val nodeInfo = event?.source

        nodeInfo?.let {
            if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event?.eventType) {
                if (isPopupCheck) {
                    checkNodeInfoForPopup(nodeInfo)
                } else if (isAccessibility) {
                    checkNodeInfoForPopupForAccessibility(nodeInfo)
                } else if (checkRectInfoForAccessibility(nodeInfo, event.text.toString())) {
                    isAccessibility = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        setAnimationScale(0.0f)
                    }
                    performGlobalAction(GLOBAL_ACTION_RECENTS)
                    return
                } else if (checkRectInfo(nodeInfo)) {
                    isPopupCheck = true
                    performGlobalAction(GLOBAL_ACTION_RECENTS)
                }

            } else if (AccessibilityEvent.TYPE_WINDOWS_CHANGED == event.eventType) {

            }
        }

    }

    fun windowInfoTest(event: AccessibilityEvent?) {
        customLogD(TAG_WINDOW_TEST, "eventType: " + getEventType(event ?: null))
        customLogD(TAG_WINDOW_TEST, "windowId: " + event?.windowId ?: "null")
        customLogD(TAG_WINDOW_TEST, "source: " + event?.source ?: "null")
        customLogD(TAG_WINDOW_TEST, "rootInActiveWindow: " + rootInActiveWindow ?: "null")
        customLogD(TAG_WINDOW_TEST, "---------------------------------------\n")
    }

    fun applySetting(appName: String, packageName: String) {
        applyAppName = appName
        applyPackageName = packageName
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
    private fun defaultNodeInfoCheck(parentInfo: AccessibilityNodeInfo?, depth: Int) {
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
                    defaultNodeInfoCheck(child, ++tDepth)
                    tDepth--
                }
            }
        }
    }

    /**
     * 노드 정보를 조회한다.
     * 보기 편하게 함수를 분할함.
     */
    private fun checkNodeInfoForPopup(parentInfo: AccessibilityNodeInfo?) {
        if (parentInfo == null || parentInfo.childCount == 0) {
            return
        }
        val count = parentInfo.childCount
        checkAppOrPackage(parentInfo)
        for (i in 0 until count) {
            val child = parentInfo.getChild(i)
            child?.let {
                if (it.childCount != 0) {
                    checkNodeInfoForPopup(child)
                }
            }
        }
    }

    /**
     * 접근성 무력화 차단 노드 정보 확인
     */
    private fun checkNodeInfoForPopupForAccessibility(parentInfo: AccessibilityNodeInfo?) {
        if (parentInfo == null || parentInfo.childCount == 0) {
            return
        }
        val count = parentInfo.childCount
        checkAppOrPackageForAccessibility(parentInfo)
        for (i in 0 until count) {
            val child = parentInfo.getChild(i)
            child?.let {
                if (it.childCount != 0) {
                    checkNodeInfoForPopupForAccessibility(child)
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

    private fun showToastShort(toastString: String) {
        Toast.makeText(
            applicationContext,
            toastString,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showToastLong(toastString: String) {
        Toast.makeText(
            applicationContext,
            toastString,
            Toast.LENGTH_LONG
        ).show()
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
     */
    private fun checkNodeInfoActionList(nodeInfo: AccessibilityNodeInfo) {
        nodeInfo.actionList?.let {
            for (action in it) {
                customLogD(TAG, action.toString() ?: "")
            }
        }
    }

    /**
     * 최근 앱 닫기 실행
     * 참고 : ACTION_UNKNOWN - 닫기 (label)
     * performAction은 해당 id로 실행할 수 있다.
     */
    private fun triggerRecentClose(nodeInfo: AccessibilityNodeInfo) {
        nodeInfo.actionList?.let {
            for (action in it) {
                if (action.label?.toString().equals("닫기")) {
                    nodeInfo.performAction(action.id)
                    showToastShort("팝업모드를 차단합니다.")
                    isPopupCheck = false
                    isAccessibility = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        setAnimationScale(1.0f)
                    }
                    return
                }
            }
        }
    }

    /**
     * 앱명, 패키지명, 팝업화면 체크
     * 최근 앱의 경우, text가 아닌 description에서 문구를 체크할 수 있다.
     */
    private fun checkAppOrPackage(nodeInfo: AccessibilityNodeInfo?) {
        nodeInfo?.let {
            checkNodeInfoActionList(nodeInfo)
            nodeInfo.contentDescription?.let {
                if (isPopupCheck &&
                    nodeInfo.className.equals("android.widget.FrameLayout") &&
                    (it.toString().lowercase()
                        .equals(applyAppName)) // 최근 앱의 레이아웃은 프레임 레이아웃으로 찍힌다. //|| nodeInfo.packageName.equals(applyPackageName)
                ) {
                    triggerRecentClose(nodeInfo)
                }
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        return isAccessibility
    }

    private fun checkAppOrPackageForAccessibility(nodeInfo: AccessibilityNodeInfo?) {
        nodeInfo?.let {
//            checkNodeInfoActionList(nodeInfo)
            nodeInfo.contentDescription?.let {

                if (isAccessibility &&
                    nodeInfo.className.equals("android.widget.FrameLayout") && // 최근 앱의 레이아웃은 프레임 레이아웃으로 찍힌다.
                    (it.toString().lowercase().equals("접근성") || it.toString().lowercase()
                        .equals("설정")) // || nodeInfo.packageName.equals("com.samsung.accessibility")
                ) {
                    checkNodeInfoActionList(nodeInfo)
                    triggerRecentClose(nodeInfo)
                }
            }
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
        if (text.length != 0) {
            var tStringBuilder = StringBuilder()
            for (i in 0..depth) {
                tStringBuilder.append("\t")
            }
            Log.d(TAG_WINDOW_TEST, "${tStringBuilder} ${text}")
        }
    }

    /**
     * 팝업화면 체크
     * 팝업화면으로 변경할 경우에는 임의로 위치를 수정하지 않는다면, Rect의 시작점이 0,0 으로 시작하지 않는다.
     */
    private fun checkRectInfo(nodeInfo: AccessibilityNodeInfo): Boolean {
        val rectScreen = Rect()
        nodeInfo?.getBoundsInScreen(rectScreen)

        Log.e(
            TAG,
            "nodeInfo RectScreen : (" + rectScreen.width() + "/" + rectScreen.height() + ")" + rectScreen.toString()
        )
        Log.e(TAG, "packageName : " + nodeInfo.packageName)

        var check = (!(rectScreen.left == 0 || rectScreen.top == 0) && nodeInfo.getPackageName()
            .equals(applyPackageName))

        return check
    }

    /**
     * 접근성 무력화 차단
     */
    private fun checkRectInfoForAccessibility(
        nodeInfo: AccessibilityNodeInfo,
        text: String
    ): Boolean {
        val rectScreen = Rect()
        nodeInfo?.getBoundsInScreen(rectScreen)

        var check = (!(rectScreen.left == 0 || rectScreen.top == 0) && text.lowercase()
            .contains("accessibilitykotlin"))
        return check
    }

    private fun customRectInfoAll(nodeInfo: AccessibilityNodeInfo) {
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
        Log.e(
            TAG_NODE_CHECK,
            "========================================================================="
        )
        Log.e(TAG_NODE_CHECK, "Catch Event Package Name : " + event.getPackageName())
        Log.e(TAG_NODE_CHECK, "Catch Event getClass Name : " + event.getClassName())
        Log.e(TAG_NODE_CHECK, "Catch Event TEXT : " + event.getText())
        Log.e(TAG_NODE_CHECK, "Catch Event ContentDescription  : " + event.getContentDescription())
        Log.e(TAG_NODE_CHECK, "Catch Event event TYPE  :  " + event.getContentChangeTypes())
        Log.e(TAG_NODE_CHECK, "Catch Event getSource : " + source ?: "null")
        Log.e(TAG_NODE_CHECK, "Catch Event STRING TYPE  :  " + getEventTypeKorean(event))
        Log.e(TAG_NODE_CHECK, "Catch Event int TYPE  :  " + event.getEventType())
        Log.e(
            TAG_NODE_CHECK,
            "Catch Event getMovementGranularity  :  " + event.getMovementGranularity()
        )
        Log.e(
            TAG_NODE_CHECK,
            "Catch Event event Type String :" + AccessibilityEvent.eventTypeToString(event.getEventType())
        )
        Log.e(TAG_NODE_CHECK, "Catch Event ACTION TYPE  :  " + event.getAction())

        Log.e(
            TAG_NODE_CHECK,
            "========================================================================="
        )
    }

    private fun getEventTypeKorean(event: AccessibilityEvent): String? {
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

    private fun getEventType(event: AccessibilityEvent?): String? {
        event?.let { event ->
            when (event?.eventType) {
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> return "TYPE_NOTIFICATION_STATE_CHANGED"
                AccessibilityEvent.TYPE_VIEW_CLICKED -> return "TYPE_VIEW_CLICKED"
                AccessibilityEvent.TYPE_VIEW_FOCUSED -> return "TYPE_VIEW_FOCUSED"
                AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> return "TYPE_VIEW_LONG_CLICKED"
                AccessibilityEvent.TYPE_VIEW_SELECTED -> return "TYPE_VIEW_SELECTED"
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> return "TYPE_WINDOW_STATE_CHANGED"
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> return "TYPE_VIEW_TEXT_CHANGED"
                AccessibilityEvent.WINDOWS_CHANGE_PIP -> return "WINDOWS_CHANGE_PIP"
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> return "TYPE_WINDOW_CONTENT_CHANGED"
                AccessibilityEvent.TYPE_VIEW_SCROLLED -> return "TYPE_VIEW_SCROLLED"
            }
            return event?.eventType.toString()
        }
        return "EVENT NULL"

    }


    override fun onInterrupt() {
        TODO("Not yet implemented")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }
}