package com.example.auth;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.example.auth.MacAddr.getMacAddr;
import org.apache.http.util.EncodingUtils;

public class MainActivity extends AppCompatActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    WebView mywebView;
    ImageButton res;
    private TextView mac;
    String ssid = "init";
    private static final int LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swipeRefreshLayout = findViewById(R.id.swipelayout);
        mywebView=findViewById(R.id.webview);
        mywebView.setWebViewClient(new WebViewClient());
        mywebView.getSettings().setAllowFileAccess(true);
        mywebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mywebView.getSettings().setJavaScriptEnabled(true);

        renderPage();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                renderPage();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


    }

    public void renderPage()  {

        tryToReadSSID();
        //String url = "http://192.168.1.161/DP/ssidcheck.php";
        String url = "http://ics.fei.tuke.sk/zamok/labSys/DPback/ssidcheck.php";
        String postData = "mac=" + getMacAddr() + "&ssid=" + ssid;
        mywebView.postUrl(url, EncodingUtils.getBytes(postData, "base64"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == LOCATION){
            tryToReadSSID();
        }
    }

    private void tryToReadSSID() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
                finish();
                System.exit(0);
            }

            final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifi.isConnectedOrConnecting()) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                ssid = wifiInfo.getSSID().toString();
                ssid = ssid.replace("\"", "");
                ssid = ssid.replace("\'", "");
            } else {
                ssid = "NonWiFi";
            }

        } catch (Exception e){
            ssid = String.valueOf(e);
            e.printStackTrace();
        }
    }
}
