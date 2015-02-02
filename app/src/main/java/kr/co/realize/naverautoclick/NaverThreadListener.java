package kr.co.realize.naverautoclick;

public interface NaverThreadListener {
	public void onLog(String log);
	public void onException(Exception e);
	public void onRankChanged(NaverItem item);
	public void onComplete(NaverItem item);
}
