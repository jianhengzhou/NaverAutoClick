package kr.co.realize.naverautoclick;


public class NaverItem {
    public String query;
    public String url;
    public int countClicked;
    public int rank;
    public int kind;

    static int KIND_FUSION = 0;
    static int KIND_SITE = 1;
    static int KIND_BLOG = 2;
    static int KIND_IMAGE = 3;
    static int KIND_MAP = 4;

    public NaverItem() { }

    public NaverItem(String query, String url, int rank, int kind) {
        this.query = query;
        this.url = url;
        this.rank = rank;
        this.kind = kind;
    }

    public String getKindStr(){
        String str = "";
        if(kind == KIND_FUSION) str="통합검색";
        else if(kind == KIND_SITE) str="사이트";
        else if(kind == KIND_BLOG) str="블로그";
        else if(kind == KIND_IMAGE) str="이미지";
        else if(kind == KIND_MAP) str="지도";
        return str;
    }

    @Override
    public String toString() {
        return "{\"query\":\"" + query + "\",\"url\":\"" + url + "\", \"kind\":\"" + kind + "\"}";
        //return "{\"query\":\"" + query + "\",\"url\":\"" + url + "\"}";
    }
}

