package sa.devming.realrank;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.Map;

public class AddRankWord extends AppCompatActivity implements View.OnClickListener {
    private EditText rankTextET;
    private ImageButton rankAddBT;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_rank_word);

        container = findViewById(R.id.container);
        rankTextET = findViewById(R.id.rankTextET);
        rankAddBT = findViewById(R.id.rankAddBT);
        rankAddBT.setOnClickListener(this);

        adMob();
    }

    @Override
    protected void onPause() {
        super.onPause();

        TextView rowTV;
        SharedPreferences preferences = getSharedPreferences(Constants.PREF_ADD_WORD, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().apply();

        for (int i = 0 ; i < container.getChildCount() ; i++){
            rowTV = container.getChildAt(i).findViewById(R.id.rowTV);
            editor.putString(Constants.TAG + i, rowTV.getText().toString());
        }
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        container.removeAllViews();

        SharedPreferences preferences = getSharedPreferences(Constants.PREF_ADD_WORD, Activity.MODE_PRIVATE);
        Map<String, ?> memoryMap = preferences.getAll();
        for (Map.Entry<String,?> entry : memoryMap.entrySet()){
            addMemoryRows(entry.getValue().toString());
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void addMemoryRows(String rowText){
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View addView = layoutInflater.inflate(R.layout.add_row, null);

        TextView rowTV = addView.findViewById(R.id.rowTV);
        ImageButton rowBT = addView.findViewById(R.id.rowBT);

        rowTV.setText(rowText);
        rowBT.setOnClickListener(v -> ((LinearLayout)addView.getParent()).removeView(addView));
        container.addView(addView);
        rankTextET.setText("");
        rankTextET.requestFocus();
    }

    @Override
    public void onClick(View v) {
        if (!"".equalsIgnoreCase(rankTextET.getText().toString())) {
            addMemoryRows(rankTextET.getText().toString());
        }
    }

    private void adMob(){
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        AdView mAdView = findViewById(R.id.adView2);
        Bundle extras = new Bundle();
        extras.putString("max_ad_content_rating", "G");
        AdRequest adRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();
        mAdView.loadAd(adRequest);
    }
}
