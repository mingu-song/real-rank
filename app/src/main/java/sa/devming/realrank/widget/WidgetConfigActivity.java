package sa.devming.realrank.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import sa.devming.realrank.Constants;
import sa.devming.realrank.R;
import sa.devming.realrank.Utils;
import sa.devming.realrank.adapter.KeyValueArrayAdapter;

public class WidgetConfigActivity extends AppCompatActivity {

    private int appWidgetId;

    private WidgetConfig widgetConfig;

    private Spinner configInterval;
    private Switch configDisturbYn;
    private LinearLayout configDisturbLayout;
    private NumberPicker configDisturbFrom;
    private NumberPicker configDisturbTo;
    private Switch configSleepYn;
    private Button configOk;

    // 업데이트 주기 key value
    private final String[] VALUES = {"None", "15 min", "30 min", "60 min"};
    private final String[] KEYS = {"0", "15", "30", "60"};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.widget_config);
        setContentView(R.layout.widget_config_layout);
        adMob();
        initialize();
    }

    private void initialize() {
        // 위젯 ID 가 없으면 종료
        appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        // preference 값을 가져온다
        widgetConfig = Utils.getWidgetPreference(this);
        if (widgetConfig.getWidgetId() == -1) {
            widgetConfig.setWidgetId(appWidgetId);
        }
        Log.w(Constants.TAG, widgetConfig.toString());

        // 이미 생성된 위젯이 아니면 종료
        if (appWidgetId != widgetConfig.getWidgetId()) {
            Toast.makeText(this, R.string.alreadyExist, Toast.LENGTH_LONG).show();
            finish();
        }

        // layout 처리
        configInterval = findViewById(R.id.widget_interval);
        configDisturbYn = findViewById(R.id.widget_disturb);
        configDisturbLayout = findViewById(R.id.widget_disturb_sub);
        configDisturbFrom = findViewById(R.id.widget_disturb_from);
        configDisturbTo = findViewById(R.id.widget_disturb_to);
        configSleepYn = findViewById(R.id.widget_sleep);
        configOk = findViewById(R.id.widget_config_ok);

        // 업데이트 주기를 설정한다.
        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.setEntries(VALUES);
        adapter.setEntryValues(KEYS);
        configInterval.setAdapter(adapter);
        configInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) adapterView.getAdapter();
                widgetConfig.setInterval(Integer.parseInt(adapter.getEntryValue(i)));
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        for (int i=0 ; i<KEYS.length ; i++) {
            if (KEYS[i].equals(String.valueOf(widgetConfig.getInterval()))) {
                configInterval.setSelection(i);
                break;
            }
        }

        // 방해금지
        configDisturbYn.setChecked(widgetConfig.isDisturb());
        if (widgetConfig.isDisturb()) {
            configDisturbLayout.setVisibility(View.VISIBLE);
        } else {
            configDisturbLayout.setVisibility(View.GONE);
        }
        configDisturbYn.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                configDisturbLayout.setVisibility(View.VISIBLE);
            } else {
                configDisturbLayout.setVisibility(View.GONE);
            }
        });

        // 방해금지 시간설정
        configDisturbFrom.setMinValue(0);
        configDisturbFrom.setMaxValue(23);
        configDisturbFrom.setWrapSelectorWheel(false);
        configDisturbFrom.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        configDisturbFrom.setValue(widgetConfig.getDisturbFrom());
        configDisturbTo.setMinValue(0);
        configDisturbTo.setMaxValue(23);
        configDisturbTo.setWrapSelectorWheel(false);
        configDisturbTo.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        configDisturbTo.setValue(widgetConfig.getDisturbTo());

        // 화면꺼짐에도 데이터 동기화
        configSleepYn.setChecked(widgetConfig.isUpdateScreenOff());

        // config 완료
        configOk.setOnClickListener(view -> configFinish());
    }

    private void configFinish() {
        // preference 에 저장
        widgetConfig.setDisturb(configDisturbYn.isChecked());
        widgetConfig.setDisturbFrom(configDisturbFrom.getValue());
        widgetConfig.setDisturbTo(configDisturbTo.getValue());
        widgetConfig.setUpdateScreenOff(configSleepYn.isChecked());
        Utils.saveWidgetPreference(this, widgetConfig);

        //위젯 업데이트 처리
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, WidgetProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {appWidgetId});
        sendBroadcast(intent);

        //config 종료
        Intent intentFinish = new Intent();
        intentFinish.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, intentFinish);
        finish();
    }

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
