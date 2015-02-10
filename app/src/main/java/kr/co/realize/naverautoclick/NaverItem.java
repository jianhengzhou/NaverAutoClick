package kr.co.realize.naverautoclick;

public class NaverItem {
	public String query;
	public String url;
	public int countClicked;
	public int rank;
    public int kind;

    public static int KIND_FUSION = 0;
    public static int KIND_IMAGE = 1;
    public static int KIND_BLOG = 2;
    public static int KIND_SITE = 3;
    public static int KIND_MAP = 4;
	
	public NaverItem() {
		
	}

	public NaverItem(String query, String url, int rank, int kind) {
		this.query = query;
		this.url = url;
		this.rank = rank;
        this.kind = kind;
	}

    @Override
    public String toString() {
        return "{\"query\":\"" + query + "\",\"url\":\"" + url + "\", \"kind\":\""+kind+"\"}";
    }

    public String getKindStr(){
        switch(kind){
            case 0:
                return "통합검색";
            case 1:
                return "이미지";
            case 2:
                return "블로그";
            case 3:
                return "사이트";
            case 4:
                return "지도";
            default:
                return null;
        }
    }
}
