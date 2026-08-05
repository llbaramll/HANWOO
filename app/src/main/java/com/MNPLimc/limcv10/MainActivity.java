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

        // 1. 웹뷰 설정 (세션, 쿠키, 알림, 화면 핏 유지)
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);             // 자바스크립트 허용
        webSettings.setDomStorageEnabled(true);              // DOM 스토리지
        webSettings.setDatabaseEnabled(true);                // DB 허용
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);         // 팝업 허용
        webSettings.setAllowFileAccess(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); // 기본 캐시

        // 화면 사이즈 핏 설정
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(false);

        // 2. 세션 및 쿠키 동기화
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        // 3. 페이지 및 알림/팝업 클라이언트 설정
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        // 4. 뒤로가기(Back Button) 동작 처리
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 웹뷰에서 뒤로 갈 수 있는 히스토리가 있는 경우
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                } else {
                    // 더 이상 뒤로 갈 화면이 없으면 앱 종료 로직 실행
                    closeAppWithCleanUp();
                }
            }
        });

        // 5. 앱 실행 시 초기화 및 메인 URL 로딩
        webView.clearHistory(); // 이전 실행 히스토리 잔재 제거
        webView.loadUrl("https://prod.limc.co.kr/m/login.jsp");
    }

    // 앱 완전 종료 및 상태 초기화 메소드
    private void closeAppWithCleanUp() {
        if (webView != null) {
            webView.clearHistory(); // 웹뷰 이동 기록 완전 삭제
            webView.clearCache(true); // 웹뷰 캐시 삭제
        }
        finishAffinity(); // 현재 앱의 모든 액티비티를 안전하게 완전 종료
    }
}
