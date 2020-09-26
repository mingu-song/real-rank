package sa.devming.realrank.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import sa.devming.realrank.Constants;
import sa.devming.realrank.adapter.RRListItem;

import static sa.devming.realrank.Constants.DAUM_SITE;
import static sa.devming.realrank.Constants.FROM_APPLICATION;
import static sa.devming.realrank.Constants.FROM_WIDGET;
import static sa.devming.realrank.Constants.NAVER_SITE;
import static sa.devming.realrank.Constants.NETWORK_TIME_OUT;

class TaskContext {
    private final Context context;
    TaskContext(Context context) {
        this.context = context;
    }
    Context getContext() {
        return this.context;
    }
}

public class RRAsyncTask extends AsyncTask<Void, Void, Void> {
    private TaskContext taskContext;
    private String taskSite;
    private int fromWhere;
    private ArrayList<RRListItem> naverArr, daumArr;
    private AsyncTaskCallBack callBack;

    public RRAsyncTask(Context context, ArrayList<RRListItem> naverArr, ArrayList<RRListItem> daumArr, AsyncTaskCallBack callBack) {
        super();
        this.taskContext = new TaskContext(context);
        this.fromWhere = FROM_WIDGET;
        this.naverArr = naverArr;
        this.daumArr = daumArr;
        this.callBack = callBack;
    }

    public RRAsyncTask(Context context, ArrayList<RRListItem> arr, String site, AsyncTaskCallBack callBack){
        super();
        this.taskContext = new TaskContext(context);
        this.taskSite = site;
        this.fromWhere = FROM_APPLICATION;
        this.naverArr = taskSite.equalsIgnoreCase(NAVER_SITE) ? arr : null;
        this.daumArr = taskSite.equalsIgnoreCase(DAUM_SITE) ? arr : null;
        this.callBack = callBack;
    }

    private boolean isConnected() {
        if (taskContext == null)  return false;

        ConnectivityManager conn = (ConnectivityManager) taskContext.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (conn != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = conn.getNetworkCapabilities(conn.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    } else {
                        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
                    }
                }
            } else {
                try {
                    NetworkInfo networkInfo = conn.getActiveNetworkInfo();
                    return networkInfo != null && networkInfo.isConnectedOrConnecting();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (isConnected()) {
            Log.w(Constants.TAG, ">>>>> Start get data");
            if (fromWhere == FROM_APPLICATION) {
                getRealRank(taskSite);
            } else {
                getRealRank(NAVER_SITE);
                getRealRank(DAUM_SITE);
            }
            Log.w(Constants.TAG, "<<<<< END get data");
        } else {
            Log.w(Constants.TAG, "Network is not connected");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (callBack != null) {
            callBack.onSuccess();
        }
    }

    /*private void sendNotification(RRListItem item) {
        Resources res = taskContext.getContext().getResources();
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getUrl()));
        PendingIntent pendingIntent = PendingIntent.getActivity(taskContext.getContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) taskContext.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) return;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(Constants.NOTI_CHANNEL_ID, Constants.NOTI_CHANNEL_NAME, importance);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(taskContext.getContext(), Constants.NOTI_CHANNEL_ID);
        builder.setContentTitle(res.getString(R.string.app_name))
                .setContentText(item.getTitle())
                .setTicker(res.getString(R.string.ranking_text_push))
                .setSmallIcon(R.drawable.ic_stat_name)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        }
        notificationManager.notify(160604,builder.build());
    }*/

    private void getRealRank(String whatSite){
        RRListItem item;
        if (NAVER_SITE.equalsIgnoreCase(whatSite)){
            naverArr.clear();
            try {
                Document document = Jsoup.connect("https://datalab.naver.com/keyword/realtimeList.naver?where=main").timeout(NETWORK_TIME_OUT).get();
                if (document != null) {
                    Elements elements = document.select("div.item_box");
                    for (Element element : elements) {
                        item = new RRListItem();
                        item.setRank(element.select("span.item_num").text()+". ");
                        item.setTitle(element.select("span.item_title").text());
                        item.setUrl("https://search.naver.com/search.naver?sm=top_hty&fbm=1&ie=utf8&query="
                                + item.getTitle());
                        naverArr.add(item);
                    }
                }
            } catch (final IOException e) {
                new Handler(taskContext.getContext().getMainLooper()).post(() -> Toast.makeText(taskContext.getContext(),
                        String.format("Fail to load network data. Try again: %s", e.getMessage()),
                        Toast.LENGTH_LONG).show());
                e.printStackTrace();
            }
        } else if (DAUM_SITE.equalsIgnoreCase(whatSite)) {
            daumArr.clear();
            try {
                Document document = Jsoup.connect("https://www.daum.net").timeout(NETWORK_TIME_OUT).get();
                if (document != null) {
                    Elements slideFav = document.select("[class=slide_favorsch]");
                    if (slideFav.size() > 0) {
                        Elements elements = slideFav.get(0).select("a[href]");
                        for (int i = 0; i < elements.size(); i++) {
                            item = new RRListItem();
                            item.setRank((i + 1) + ". ");
                            item.setTitle(elements.get(i).text());
                            item.setUrl(elements.get(i).attr("href"));
                            daumArr.add(item);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
