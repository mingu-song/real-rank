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

public class DaumFragment extends Fragment implements AdapterView.OnItemClickListener {
    private final RRListAdapter daumAdapter = new RRListAdapter();
    private ListView listView;
    private PullRefreshLayout daumRefresh;
    private ProgressDialog dialog;

    public DaumFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.daum_fragment, container, false);

        TextView empty = rootView.findViewById(R.id.empty);
        listView = rootView.findViewById(R.id.listview);
        listView.setAdapter(daumAdapter);
        listView.setOnItemClickListener(this);
        listView.setEmptyView(empty);

        daumRefresh = rootView.findViewById(R.id.daumRefresh);
        daumRefresh.setOnRefreshListener(() -> {
            dialog = new ProgressDialog(getContext());
            dialog.setMessage(getContext().getString(R.string.dialog_text));
            dialog.setCancelable(false);
            dialog.show();
            final ArrayList<RRListItem> arr = new ArrayList<>();
            new RRAsyncTask(getContext(), arr, Constants.DAUM_SITE, () -> doOnSuccess(arr)).execute();
            daumRefresh.setRefreshing(false);
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
        new RRAsyncTask(getContext(), arr, Constants.DAUM_SITE, () -> doOnSuccess(arr)).execute();
    }

    private void doOnSuccess(ArrayList<RRListItem> arr) {
        if (arr.size() > 0) {
            daumAdapter.getListItemList().clear();
            for (int i = 0; i < arr.size(); i++) {
                daumAdapter.addItem(arr.get(i));
            }
            daumAdapter.notifyDataSetChanged();
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
