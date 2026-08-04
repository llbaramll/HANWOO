package com.MobileLimc.limcv10;

import android.os.Bundle;
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

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient());

        // Target SDK 36 이상 규격에 맞춘 최신 뒤로가기(Back Button) 처리
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView != null && webView.canGoBack()) {
                    webView.goBack(); // 웹뷰 내에 이전 페이지가 있으면 이전 페이지로 이동
                } else {
                    setEnabled(false); // 더 이상 뒤로 갈 페이지가 없으면 백버튼 처리 해제 후 앱 종료
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // 접속할 주소를 입력해 주세요 (필요시 수정)
        webView.loadUrl("https://www.naver.com");
    }
}
