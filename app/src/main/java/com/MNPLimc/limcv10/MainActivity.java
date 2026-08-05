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

        // 1. 웹뷰 기본 및 세션/쿠키/파라미터 설정
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);             // 자바스크립트 허용
        webSettings.setDomStorageEnabled(true);              // DOM 스토리지 (세션/로컬스토리지 유지)
        webSettings.setDatabaseEnabled(true);                // 데이터베이스 허용
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 자바스크립트 창 열기 허용
        webSettings.setSupportMultipleWindows(true);         // 팝업창/새창 열기 허용
        webSettings.setAllowFileAccess(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); // 기본 캐시 정책 사용

        // 2. 세션 및 쿠키 동기화 설정 (로그인 세션 유지)
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true); // 제3자 쿠키 허용

        // 3. 페이지 이동 및 URL 처리
        webView.setWebViewClient(new WebViewClient());

        // 4. 자바스크립트 대화창(alert, confirm, prompt) 및 팝업 처리를 위한 WebChromeClient 설정
        webView.setWebChromeClient(new WebChromeClient());

        // 5. Target SDK 36 지원 뒤로가기(Back Button) 제어 로직
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView != null && webView.canGoBack()) {
                    webView.goBack(); // 웹뷰 이력이 있으면 이전 페이지로 이동
                } else {
                    setEnabled(false); // 더 이상 뒤로 갈 수 없으면 백버튼 처리 해제 후 앱 종료
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // 6. 접속 URL
        webView.loadUrl("https://prod.limc.co.kr/m/login.jsp");
    }
}
