package kr.co.realize.naverautoclick;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NaverThread extends Thread {
	public static final long MAX_WAIT_TIME = 10000;
	private Handler handler = new Handler();
	private List<NaverItem> itemList;
	private WebView webView;
	private BlockingQueue<String> pageFinishedQueue = new ArrayBlockingQueue<String>(1);
	private WebViewClient webViewClient = new NaverWebViewClient(pageFinishedQueue);
	private NaverJavascriptInterface javascriptInterface = new NaverJavascriptInterface(handler);
	private long delay;
	private NaverThreadListener listener;
	private int index;

	public NaverThread(WebView webView, List<NaverItem> itemList, long delay, NaverThreadListener listener) {
		this.webView = webView;
		this.webView.setWebViewClient(webViewClient);
		this.webView.addJavascriptInterface(javascriptInterface, NaverJavascriptInterface.INTERFACE_NAME);
		this.itemList = itemList;
		this.delay = delay;
		this.listener = listener;
	}

	@Override
	public void run() {
		ListIterator<NaverItem> iterator = null;
		
		while (true) {
			if (iterator != null && iterator.hasNext()) {
				final NaverItem item = iterator.next();
				
				if (item == null || item.query.equals("") || item.url.equals("")) {
					continue;
				}
				
				pageFinishedQueue.clear();
				
				index = itemList.indexOf(item) + 1;
				
				Log.i("naverautoclick", index + "번째 링크 시작되었습니다.");
				
				try {
					listener.onLog(index + "번째 링크 시작되었습니다.");
					
					listener.onLog(index + "번째 링크 검색 시작되었습니다.");
					this.search(item);
					listener.onLog(index + "번째 링크 검색 완료되었습니다.");
					
					listener.onLog(index + "번째 링크 페이지 탐색 시작되었습니다.");
					boolean pageFounded = this.find(item);
					listener.onLog(index + "번째 링크 페이지 탐색 완료되었습니다.");
					
					if (pageFounded) {
						listener.onLog(index + "번째 링크 체류를 시작합니다.");
						Thread.sleep(delay + (long) (Math.random() * 10000));
						item.countClicked++;
						listener.onLog(index + "번째 링크 체류가 끝났습니다.");

						for (int i=0; i<2; i++) {
							this.synchronizedLoadUrl("javascript:history.back()");
							this.loadUrl("javascript:" + click(".sch_tab button"));
							int numTab = Integer.parseInt(javascriptInterface.requestResult(webView, querySelectorAll(".sch_tab li") + ".length"));
							if (this.synchronizedLoadUrl("javascript:" + click(".sch_tab li a", (int) (Math.random() * numTab))).contains("&url=")) {
								this.waitLoadUrl();
							}
							this.waitRandomSeconds();
						}
					} else {
						listener.onLog(index + "번째 링크를 찾지 못했습니다.");
					}

					listener.onLog(index + "번째 링크 초기화 중 입니다...");
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							webView.clearCache(true);
						}
					});
					CookieManager.getInstance().removeAllCookie();
					listener.onLog(index + "번째 링크 IP를 변경하는 중 입니다...");
					this.changeNetworkState();
					
					listener.onLog(index + "번째 링크 끝났습니다.");
					listener.onComplete(item);
				} catch (InterruptedException e) {
					break;
				} catch (NullPointerException e) {
					Log.e("naverautoclick", index + "번째 링크 응답이 없어 재시작합니다.");
					listener.onLog(index + "번째 링크 응답이 없어 재시작합니다.");
					iterator.previous();
					
					final BlockingQueue<String> waitChangeNetworkStateQueue = new ArrayBlockingQueue<String>(1);
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							webView.stopLoading();
							try {
								changeNetworkState();
							} catch (Exception e) {
								e.printStackTrace();
							}
							waitChangeNetworkStateQueue.add("");
						}
					});
					try {
						waitChangeNetworkStateQueue.take();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
					listener.onException(e);
					iterator.previous();
				}
			} else {
				iterator = itemList.listIterator();
			}
		}
	}
	
	private boolean find(NaverItem item) throws Exception {
		boolean pageFounded = false;
		
		for (int page=1; page<=14; page++) {
			listener.onLog(index + "번째 링크 " + page + "번째 페이지 탐색 중 입니다..");
			this.waitRandomSeconds();
			
			String type = "uni";
			int countLinkes = Integer.parseInt(javascriptInterface.requestResult(webView, querySelectorAll(".uni li > a") + ".length"));
			if (countLinkes == 0) {
				type = "sp_total";
				countLinkes = Integer.parseInt(javascriptInterface.requestResult(webView, querySelectorAll(".sp_total li > a") + ".length"));
			}
			Log.d("naverautoclick", Integer.toString(countLinkes));
			for (int i=0; i<countLinkes; i++) {
				final String href = javascriptInterface.requestResult(webView, querySelectorAll("." + type + " li > a") + "[" + i + "].getAttribute('href')");
				final String hasId = javascriptInterface.requestResult(webView, querySelectorAll("." + type + " li > a") + "[" + i + "].parentNode.hasAttribute('id')");
				if (hasId.equals("false")) {
					continue;
				}
				final String id = javascriptInterface.requestResult(webView, querySelectorAll("." + type + " li > a") + "[" + i + "].parentNode.getAttribute('id')");
				
				if (href.contains(item.url)) {
					item.rank = Integer.parseInt(id.substring(7));
					listener.onRankChanged(item);
					
					this.synchronizedLoadUrl("javascript:" + click(".sc a[href='" + href + "']"));
					this.waitLoadUrl();
					this.softSynchronizedLoadUrl("javascript:void()");
					
					pageFounded = true;
					break;
				}
			}
			
			if (pageFounded || page == 14) {
				break;
			}
			
			if (javascriptInterface.requestResult(webView, querySelectorAll(".pg2b_btn") + "[1].getAttribute('class')").contains("dim")) {
				break;
			}
			
			this.synchronizedLoadUrl("javascript:" + click(".pg2b_btn", 1));
		}
		
		return pageFounded;
	}

	private void search(NaverItem item) throws Exception {
		this.synchronizedLoadUrl("http://m.naver.com");

		this.softSynchronizedLoadUrl("javascript:" + click("#query"));
		this.loadUrl("javascript:void(" + querySelector("#query") + ".value = '" + item.query + "');");
		
		this.waitRandomSeconds();
		
		this.synchronizedLoadUrl("javascript:" + click("button[type=submit]"));
	}

	private void waitRandomSeconds() throws InterruptedException {
		Thread.sleep((long) (Math.random() * 1000) + 1000);
	}

	private void loadUrl(final String url) throws InterruptedException {
		Thread.sleep(500);
		Log.d("naverautoclick", url);
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				webView.loadUrl(url);
			}
		});
	}
	
	private String synchronizedLoadUrl(String url) throws Exception {
		return synchronizedLoadUrl(url, MAX_WAIT_TIME);
	}

	private String synchronizedLoadUrl(String url, long waitTime) throws Exception {
		this.loadUrl(url);
		return this.waitLoadUrl(waitTime);
	}
	
	private String softSynchronizedLoadUrl(String url) {
		return softSynchronizedLoadUrl(url, MAX_WAIT_TIME);
	}
	
	private String softSynchronizedLoadUrl(String url, long waitTime) {
		try {
			return synchronizedLoadUrl(url, waitTime);
		} catch (Exception e) {
			return url;
		}
	}
	
	private String waitLoadUrl() throws Exception {
		return waitLoadUrl(MAX_WAIT_TIME);
	}
	
	private String waitLoadUrl(long waitTime) throws Exception {
		String url = pageFinishedQueue.poll(waitTime, TimeUnit.MILLISECONDS).toString();
		return url;
	}

	private String querySelector(String query) {
		return querySelectorAll(query) + "[0]";
	}
	
	private String querySelectorAll(String query) {
		return "document.querySelectorAll('" + query.replace("'", "\\'") + "')";
	}
	
	private String click(String query) throws Exception {
		return click(query, 0);
	}
	
	private String click(String query, int index) throws Exception {
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
	
	private void changeNetworkState() throws Exception {
		Context context = webView.getContext();
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			Method setMobileDataEnabled = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
			setMobileDataEnabled.setAccessible(true);
			setMobileDataEnabled.invoke(connectivityManager, false);
			setMobileDataEnabled.invoke(connectivityManager, true);

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
}
