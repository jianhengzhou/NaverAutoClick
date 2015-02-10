package kr.co.realize.naverautoclick;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kr.co.realize.naverautoclick.NaverItem;
import kr.co.realize.naverautoclick.R;

public class NaverAdapter extends ArrayAdapter<NaverItem>{
    private LayoutInflater inflater = null;
    private ViewHolder viewHolder = null;
    private View.OnClickListener deleteListener = null;
    private View.OnClickListener modifyListener = null;

    public NaverAdapter(Context context, int textViewResourceId, List<NaverItem> arrays, View.OnClickListener deleteListener, View.OnClickListener modifyListener) {
        super(context, textViewResourceId, arrays);
        this.inflater = LayoutInflater.from(context);
        this.deleteListener = deleteListener;
        this.modifyListener = modifyListener;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public NaverItem getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        View v = convertView;
        if(v==null){
            viewHolder = new ViewHolder();
            v = inflater.inflate(R.layout.list, null);
            viewHolder.textView_kind = (TextView)v.findViewById(R.id.list_textView_category);
            viewHolder.textView_url = (TextView)v.findViewById(R.id.list_textView_url);
            viewHolder.textView_query = (TextView)v.findViewById(R.id.list_textView_query);
            viewHolder.textView_rank = (TextView)v.findViewById(R.id.list_textView_rank);
            viewHolder.button_delete = (Button)v.findViewById(R.id.list_button_delete);
            viewHolder.button_modify= (Button)v.findViewById(R.id.list_button_modify);
            v.setTag(viewHolder);
        } else viewHolder = (ViewHolder)v.getTag();

        NaverItem item = getItem(position);
        viewHolder.textView_query.setText(item.query);
        viewHolder.textView_url.setText(item.url);
        viewHolder.textView_rank.setText("조회수 " + item.countClicked + "회 (현재 " + item.rank + "위)");
        viewHolder.textView_kind.setText("유형: "+ item.getKindStr());

        viewHolder.button_delete.setTag(position);
        viewHolder.button_delete.setOnClickListener(modifyListener);

        viewHolder.button_modify.setTag(position);
        viewHolder.button_modify.setOnClickListener(deleteListener);
        return v;
    }

    class ViewHolder{
        public TextView textView_rank;
        public TextView textView_url;
        public TextView textView_kind;
        public TextView textView_query;
        public Button button_delete;
        public Button button_modify;
    }

    @Override
    protected void finalize() throws Throwable{
        free();
        super.finalize();
    }
    private void free(){
        inflater = null;
        viewHolder = null;
        deleteListener = null;
        modifyListener = null;
    }
}
