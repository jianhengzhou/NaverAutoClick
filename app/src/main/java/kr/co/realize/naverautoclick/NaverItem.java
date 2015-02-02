package kr.co.realize.naverautoclick;

public class NaverItem {
	public String query;
	public String url;
	public int countClicked;
	public int rank;
	
	public NaverItem() {
		
	}

	public NaverItem(String query, String url, int rank) {
		this.query = query;
		this.url = url;
		this.rank = rank;
	}

}
