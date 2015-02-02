package kr.co.realize.naverautoclick;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class NaverUtil {
    private int maxWaitTime = 10000;

    private NaverThreadListener listener;

    private BlockingQueue<String> pageFinishedQueue = new ArrayBlockingQueue<String>(1);

    private WebView webView;
    private WebViewClient webViewClient = new NaverWebViewClient(pageFinishedQueue);
    private NaverJavascriptInterface javascriptInterface = new NaverJavascriptInterface();

    public NaverUtil(WebView webView, NaverThreadListener listener) {
        this.webView = webView;
        this.webView.setWebViewClient(webViewClient);
        this.webView.addJavascriptInterface(javascriptInterface, NaverJavascriptInterface.INTERFACE_NAME);
        this.listener = listener;
    }

    public void setMaxWaitTime(int maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public String requestResult(String function) throws Exception {
        return javascriptInterface.requestResult(webView, function);
    }

    public void waitRandomSeconds() throws InterruptedException {
        Thread.sleep((long) (Math.random() * 1000) + 1000);
    }

    public void loadUrl(final String url) throws InterruptedException {
        Thread.sleep(500);
        Log.d("naverautoclick", url);
        webView.post(new Runnable() {

            @Override
            public void run() {
                webView.loadUrl(url);
            }
        });
    }

    public String synchronizedLoadUrl(String url) throws Exception {
        return synchronizedLoadUrl(url, maxWaitTime);
    }

    public String synchronizedLoadUrl(String url, long waitTime) throws Exception {
        this.loadUrl(url);
        return this.waitLoadUrl(waitTime);
    }

    public String softSynchronizedLoadUrl(String url) {
        return softSynchronizedLoadUrl(url, maxWaitTime);
    }

    public String softSynchronizedLoadUrl(String url, long waitTime) {
        try {
            return synchronizedLoadUrl(url, waitTime);
        } catch (Exception e) {
            return url;
        }
    }

    public String waitLoadUrl() throws Exception {
        return waitLoadUrl(maxWaitTime);
    }

    public String waitLoadUrl(long waitTime) throws Exception {
        String url = pageFinishedQueue.poll(waitTime, TimeUnit.MILLISECONDS).toString();
        return url;
    }

    public String querySelector(String query) {
        return querySelectorAll(query) + "[0]";
    }

    public String querySelectorAll(String query) {
        return "document.querySelectorAll('" + query.replace("'", "\\'") + "')";
    }

    public String click(String query) throws Exception {
        return click(query, 0);
    }

    public String click(String query, int index) throws Exception {
        int x = Integer.parseInt(javascriptInterface.requestResult(webView, querySelectorAll(query) + "[" + index + "].offsetLeft"));
        int y = Integer.parseInt(javascriptInterface.requestResult(webView, querySelectorAll(query) + "[" + index + "].offsetTop"));
        int w = Integer.parseInt(javascriptInterface.requestResult(webView, querySelectorAll(query) + "[" + index + "].offsetWidth"));
        int h = Integer.parseInt(javascriptInterface.requestResult(webView, querySelectorAll(query) + "[" + index + "].offsetHeight"));
        int px = (int) (x + Math.random() * w);
        int py = (int) (y + Math.random() * h);

        return querySelectorAll(query) + "[" + index + "].dispatchEvent(new MouseEvent('click', {" +
                "screenX: " + px + ", screenY: " + py + "," +
                "clientX: " + px + ", clientY: " + py + "," +
                "buttons: 1" +
                "}))"
                ;
    }

    public void changeNetworkState() throws Exception {
        Context context = webView.getContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Method setMobileDataEnabled = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
            setMobileDataEnabled.setAccessible(true);
            setMobileDataEnabled.invoke(connectivityManager, false);
            setMobileDataEnabled.invoke(connectivityManager, true);

            Thread.sleep(1000);
            while (true) {
                try {
                    if (Jsoup.connect("http://m.naver.com").execute().statusCode() == 200) {
                        break;
                    }
                } catch (IOException e) {

                }
                Thread.sleep(1000);
            }
        } catch (NoSuchMethodException e) {
            listener.onLog("setMobileDataEnabled를 찾지 못했습니다.");

            Method[] setMobileDataEnabled = ConnectivityManager.class.getDeclaredMethods();
            for (int i=0; i<setMobileDataEnabled.length; i++) {
                if (setMobileDataEnabled[i].getName().equals("setMobileDataEnabled")) {
                    String types = "";
                    Class<?>[] parameterTypes = setMobileDataEnabled[i].getParameterTypes();

                    for (int j=0; j<parameterTypes.length; j++) {
                        types += parameterTypes[j].getName() + " ";
                    }

                    listener.onLog("사용 가능한 setMobileDataEnabled : " + types);
                }
            }
        }
    }

    public void clearPageFinishedQueue() {
        pageFinishedQueue.clear();
    }
}
