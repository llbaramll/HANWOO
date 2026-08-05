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

        // 1. 웹뷰 설정 (세션, 쿠키, 알림, 화면 핏 등 기존 모든 기능 유지)
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);             // 자바스크립트 허용
        webSettings.setDomStorageEnabled(true);              // DOM 스토리지 (세션 유지)
        webSettings.setDatabaseEnabled(true);                // DB 허용
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 창 열기 허용
        webSettings.setSupportMultipleWindows(true);         // 팝업 허용
        webSettings.setAllowFileAccess(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); // 기본 캐시

        // 화면 사이즈 핏 설정
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(false);

        // 2. 세션 및 쿠키 동기화 (로그인 세션 유지)
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        // 3. 페이지 및 알림/팝업 클라이언트
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());  // alert, confirm, prompt 처리

        // 4. 뒤로가기(Back Button) 제어 (Target SDK 36 지원)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 웹뷰 내부 이동 히스토리가 있으면 이전 웹 페이지로 이동
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                } else {
                    // 더 이상 뒤로 갈 페이지가 없으면 액티비티를 완벽히 종료(finish)
                    finish();
                }
            }
        });

        // 5. 앱 재실행 시 항상 초기 화면(로그인 페이지)부터 로딩하도록 처리
        webView.loadUrl("https://prod.limc.co.kr/m/login.jsp");
    }
}
