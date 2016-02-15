package com.example.przemek.downloadtest;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.notification.BaseNotificationItem;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationHelper;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationListener;
import com.liulishuo.filedownloader.util.FileDownloadHelper;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MAIN_ACTIVITY";

    private FileDownloadNotificationHelper<NotificationItem> notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        notificationHelper = new FileDownloadNotificationHelper<>();1
        assignViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        this.notificationHelper.clear();
        clear();
        super.onDestroy();
    }

    private int downloadId = 0;

    public void downloadAction(View sender) {
        button.setEnabled(false);
        downloadId = FileDownloader.getImpl().create("http://releases.ubuntu.com/15.10/ubuntu-15.10-desktop-amd64.iso")
                .setPath(getExternalCacheDir().getAbsolutePath() + "/ubuntu.iso")
                .setListener(new FileDownloadNotificationListener(notificationHelper) {
                    @Override
                    protected BaseNotificationItem create(
                            BaseDownloadTask task) {
                        return new NotificationItem(task.getDownloadId(), "demo title", "demo desc");
                    }

                    @Override
                    protected boolean interceptCancel(BaseDownloadTask task,
                                                      BaseNotificationItem n) {
                        // in this demo, I don't want to cancel the notification, just show for the test
                        // so return true
                        return true;
                    }

                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        super.pending(task, soFarBytes, totalBytes);
                        progressBar.setIndeterminate(true);
                        textView.setText("pending");
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        super.progress(task, soFarBytes, totalBytes);
                        progressBar.setIndeterminate(false);
                        progressBar.setMax(totalBytes);
                        progressBar.setProgress(soFarBytes);
                        textView.setText(soFarBytes + " " + totalBytes);
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        super.completed(task);
                        progressBar.setIndeterminate(false);
                        progressBar.setProgress(task.getSmallFileTotalBytes());
                        textView.setText("completed");
                        button.setEnabled(true);
                    }
                })
                .start();
    }


    private void clear() {
        if (downloadId == 0) {
            return;
        }
        /**
         * why not use {@link FileDownloadNotificationHelper#clear()} directly?
         * @see FileDownloadNotificationListener#interceptCancel(BaseDownloadTask, BaseNotificationItem)
         */
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).
                cancel(downloadId);
    }

    private ProgressBar progressBar;
    private TextView textView;
    private Button button;

    private void assignViews() {
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        textView = (TextView) findViewById(R.id.progresstext);
        button = (Button) findViewById(R.id.button);
    }

    public static class NotificationItem extends BaseNotificationItem {

        private NotificationItem(int id, String title, String desc) {
            super(id, title, desc);
        }

        @Override
        public void show(boolean statusChanged, int status, boolean isShowProgress) {
            NotificationCompat.Builder builder = new NotificationCompat.
                    Builder(FileDownloadHelper.getAppContext());

            String desc = getDesc();
            switch (status) {
                case FileDownloadStatus.pending:
                    desc += " pending";
                    break;
                case FileDownloadStatus.progress:
                    desc += " progress";
                    break;
                case FileDownloadStatus.retry:
                    desc += " retry";
                    break;
                case FileDownloadStatus.error:
                    desc += " error";
                    break;
                case FileDownloadStatus.paused:
                    desc += " paused";
                    break;
                case FileDownloadStatus.completed:
                    desc += " completed";
                    break;
                case FileDownloadStatus.warn:
                    desc += " warn";
                    break;
            }

            builder.setDefaults(Notification.DEFAULT_LIGHTS)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setContentTitle(getTitle())
                    .setContentText(desc)
                    .setSmallIcon(R.mipmap.ic_launcher);

            if (statusChanged) {
                builder.setTicker(desc);
            }

            builder.setProgress(getTotal(), getSofar(), !isShowProgress);
            getManager().notify(getId(), builder.build());
        }
    }


}
