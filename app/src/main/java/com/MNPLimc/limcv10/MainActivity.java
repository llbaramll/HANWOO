package com.MNPLimc.limcv10;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);

        // 1. 웹뷰 설정 (세션/쿠키 + 팝업 + 화면 사이즈 핏 추가)
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);             // 자바스크립트 허용
        webSettings.setDomStorageEnabled(true);              // DOM 스토리지 (세션/로컬스토리지)
        webSettings.setDatabaseEnabled(true);                // DB 허용
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 창 열기 허용
        webSettings.setSupportMultipleWindows(true);         // 팝업 허용
        webSettings.setAllowFileAccess(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); // 기본 캐시

        // ★ 화면 사이즈 자동 조정 (웹페이지 비율을 앱 화면 크기에 맞춤)
        webSettings.setUseWideViewPort(true);                // Viewport meta 태그 지원
        webSettings.setLoadWithOverviewMode(true);           // 화면 크기에 맞게 컨텐츠 축소/확대
        webSettings.setBuiltInZoomControls(false);           // 화면 줌 컨트롤 미표시

        // 2. 세션 및 쿠키 동기화 (로그인 유지)
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        // 3. 페이지 및 알림/팝업 클라이언트
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());  // alert, confirm, prompt 처리

        // 4. Target SDK 36 지원 뒤로가기 제어
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // 5. 최초 1회만 URL 로딩 (회전 시 초기화 방지)
        if (savedInstanceState == null) {
            webView.loadUrl("https://prod.limc.co.kr/m/login.jsp");
        }
    }
}
