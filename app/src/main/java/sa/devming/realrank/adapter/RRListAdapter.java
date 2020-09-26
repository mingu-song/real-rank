package sa.devming.realrank.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import sa.devming.realrank.R;

public class RRListAdapter extends BaseAdapter {
    private ArrayList<RRListItem> listItemList = new ArrayList<>();

    public RRListAdapter() {
    }

    public ArrayList<RRListItem> getListItemList(){
        return listItemList;
    }

    @Override
    public int getCount() {
        return listItemList.size();
    }

    @Override
    public Object getItem(int i) {
        return listItemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final Context context = viewGroup.getContext();
        ViewHolder holder;

        if (view != null) {
            holder = (ViewHolder)view.getTag();
        } else {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.rank_listview_item, null);
            holder.titleView = view.findViewById(R.id.item_text);

            view.setTag(holder);
        }

        final RRListItem listItem = listItemList.get(i);
        holder.titleView.setText(listItem.getRank()+listItem.getTitle());

        return view;
    }

    public void addItem(RRListItem item){
        listItemList.add(item);
    }


    private static class ViewHolder {
        private TextView titleView;
    }
}
