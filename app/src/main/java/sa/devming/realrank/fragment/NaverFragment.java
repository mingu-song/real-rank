package sa.devming.realrank.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.baoyz.widget.PullRefreshLayout;

import java.util.ArrayList;

import sa.devming.realrank.Constants;
import sa.devming.realrank.R;
import sa.devming.realrank.adapter.RRListAdapter;
import sa.devming.realrank.adapter.RRListItem;
import sa.devming.realrank.network.RRAsyncTask;

public class NaverFragment extends Fragment implements AdapterView.OnItemClickListener {
    private final RRListAdapter naverAdapter = new RRListAdapter();
    private ListView listView;
    private PullRefreshLayout naverRefresh;
    private ProgressDialog dialog;

    public NaverFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.naver_fragment, container, false);

        TextView empty = rootView.findViewById(R.id.empty);
        listView = rootView.findViewById(R.id.listview);
        listView.setAdapter(naverAdapter);
        listView.setOnItemClickListener(this);
        listView.setEmptyView(empty);

        naverRefresh = rootView.findViewById(R.id.naverRefresh);
        naverRefresh.setOnRefreshListener(() -> {
            dialog = new ProgressDialog(getContext());
            dialog.setMessage(getContext().getString(R.string.dialog_text));
            dialog.setCancelable(false);
            dialog.show();
            final ArrayList<RRListItem> arr = new ArrayList<>();
            new RRAsyncTask(getContext(), arr, Constants.NAVER_SITE, () -> doOnSuccess(arr)).execute();
            naverRefresh.setRefreshing(false);
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dialog = new ProgressDialog(getContext());
        dialog.setMessage(getContext().getString(R.string.dialog_text));
        dialog.setCancelable(false);
        dialog.show();
        final ArrayList<RRListItem> arr = new ArrayList<>();
        new RRAsyncTask(getContext(), arr, Constants.NAVER_SITE, () -> doOnSuccess(arr)).execute();
    }

    private void doOnSuccess(ArrayList<RRListItem> arr) {
        if (arr.size() > 0) {
            naverAdapter.getListItemList().clear();
            for (int i = 0; i < arr.size(); i++) {
                naverAdapter.addItem(arr.get(i));
            }
            naverAdapter.notifyDataSetChanged();
        }
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RRListItem item = (RRListItem)listView.getItemAtPosition(position);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getUrl()));
        startActivity(intent);
    }
}
