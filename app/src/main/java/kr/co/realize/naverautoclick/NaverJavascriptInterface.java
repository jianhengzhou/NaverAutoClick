package kr.co.realize.naverautoclick;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class NaverJavascriptInterface {
	public static final String INTERFACE_NAME = "android";
	
	private Handler handler;
	private BlockingQueue<String> onResultQueue = new ArrayBlockingQueue<String>(1);
	
	public NaverJavascriptInterface(Handler handler) {
		this.handler = handler;
	}
	
	public String requestResult(final WebView webView, final String function) throws Exception {
		onResultQueue.clear();
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				webView.loadUrl("javascript:void(window." + INTERFACE_NAME + ".onResult(" + function + "));");
			}
		});
		Log.e("naverautoclick", function);
		return onResultQueue.poll(NaverThread.MAX_WAIT_TIME, TimeUnit.MILLISECONDS).toString();
	}
	
	public String requestHTML(WebView webView) throws Exception {
		return requestResult(webView, "document.getElementsByTagName('html')[0].innerHTML");
	}
	
	@JavascriptInterface
	public void onResult(String result) {
		onResultQueue.add(result);
	}

}
