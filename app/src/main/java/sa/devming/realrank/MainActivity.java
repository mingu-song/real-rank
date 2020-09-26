package sa.devming.realrank;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import sa.devming.realrank.adapter.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private ImageView naverSelect;
    private ImageView daumSelect;
    public final int PICK_NAVER = 0;
    public final int PICK_DAUM = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adMob();
        initialize();
        //setFloatingAction();
    }

    private void initialize() {
        naverSelect = findViewById(R.id.naverBT);
        daumSelect = findViewById(R.id.daumBT);
        viewPager = findViewById(R.id.container);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        naverSelect.setOnClickListener(v -> changePager(PICK_NAVER));
        daumSelect.setOnClickListener(v -> changePager(PICK_DAUM));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) { changePager(position); }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        changePager(PICK_NAVER); //첫화면 호출
    }

    private void changePager(int what) {
        if (what == PICK_NAVER) {
            //버튼선택 처리
            naverSelect.setSelected(true);
            daumSelect.setSelected(false);
            viewPager.setCurrentItem(PICK_NAVER);
        } else {
            naverSelect.setSelected(false);
            daumSelect.setSelected(true);
            viewPager.setCurrentItem(PICK_DAUM);
        }
    }

    /*private void setFloatingAction() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent activity = new Intent(view.getContext(), AddRankWord.class);
                activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(view.getContext());
                stackBuilder.addParentStack(AddRankWord.class);
                stackBuilder.addNextIntent(activity);
                stackBuilder.startActivities();
            }
        });
    }*/

    private void adMob(){
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        AdView mAdView = findViewById(R.id.adView);
        Bundle extras = new Bundle();
        extras.putString("max_ad_content_rating", "G");
        AdRequest adRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();
        mAdView.loadAd(adRequest);
    }
}
