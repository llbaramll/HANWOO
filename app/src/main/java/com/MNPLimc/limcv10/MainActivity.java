package com.MNPLimc.limcv10;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        // 쿠키 허용 설정
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient());

        if (savedInstanceState == null) {
            webView.loadUrl("https://prod.limc.co.kr/m/login.jsp");
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    // 스마트폰 하단 '뒤로가기' 버튼 클릭 시 처리
    @Override
    public void onBackPressed() {
        WebView webView = findViewById(R.id.webView);
        // 웹뷰 안에서 뒤로 갈 페이지가 있다면 웹페이지 뒤로가기 실행
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            // 더 이상 뒤로 갈 페이지가 없어서 앱이 최종 종료될 때 쿠키를 전부 삭제
            clearCookiesAndExit();
        }
    }

    // 쿠키를 삭제하고 앱을 완전히 종료하는 함수
    private void clearCookiesAndExit() {
        CookieManager cookieManager = CookieManager.getInstance();
        // 메모리 및 디스크의 모든 쿠키(세션 포함)를 강제로 삭제합니다.
        cookieManager.removeAllCookies(null);
        cookieManager.flush();
        
        // 앱 프로세스 종료
        finish();
    }

    @Override
    protected void onDestroy() {
        // 혹시 모를 비정상 종료 시에도 쿠키가 남지 않도록 한 번 더 안전장치로 작동
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null);
        cookieManager.flush();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        WebView webView = findViewById(R.id.webView);
        if (webView != null) {
            webView.saveState(outState);
        }
    }
}
