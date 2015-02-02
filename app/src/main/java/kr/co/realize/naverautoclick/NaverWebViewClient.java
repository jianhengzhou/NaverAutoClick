package kr.co.realize.naverautoclick;

import java.util.concurrent.BlockingQueue;

import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NaverWebViewClient extends WebViewClient {
	BlockingQueue<String> pageFinishedQueue;
	
	public NaverWebViewClient(BlockingQueue<String> pageFinishedQueue) {
		this.pageFinishedQueue = pageFinishedQueue;
	}
	
	@Override
	public void onPageFinished(WebView view, String url) {
		try {
			Log.i("naverautoclick", url);
			pageFinishedQueue.add(url);
		} catch (Exception e) {
			
		}
	}
	/*
	@Override
	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		try {
			pageFinishedQueue.remove();
			view.reload();
		} catch (Exception e) {
			
		}
	}
	*/
	/*
	@Override
	public synchronized boolean shouldOverrideUrlLoading(WebView view, String url) {
		Log.i("naverautoclick", "2 : " + url);
		return false;
	}
	*/
}
