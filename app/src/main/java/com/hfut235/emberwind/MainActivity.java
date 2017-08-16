package com.hfut235.emberwind;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.duckduckgo.app.Injector;
import com.duckduckgo.app.domain.suggestion.SuggestionRepository;
import com.duckduckgo.app.ui.autocomplete.AutocompleteTask;
import com.duckduckgo.app.ui.bookmarks.BookmarkEntity;
import com.duckduckgo.app.ui.bookmarks.BookmarksActivity;
import com.duckduckgo.app.ui.browser.Browser;
import com.duckduckgo.app.ui.browser.BrowserFragment;
import com.duckduckgo.app.ui.browser.BrowserPresenter;
import com.duckduckgo.app.ui.editbookmark.EditBookmarkDialogFragment;
import com.duckduckgo.app.ui.main.MainView;
import com.duckduckgo.app.ui.navigator.Navigator;
import com.duckduckgo.app.ui.tabswitcher.TabSwitcherFragment;
import com.hfut235.emberwind.receiver.ServerStatusReceiver;
import com.hfut235.emberwind.service.CoreService;
import com.hfut235.emberwind.utils.LogUtils;
import com.yanzhenjie.nohttp.tools.NetUtil;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MainView, EditBookmarkDialogFragment.OnEditBookmarkListener {

    private static final int REQUEST_PICK_BOOKMARK = 200;
    private static final String TAG = MainActivity.class.getSimpleName();


    private BrowserPresenter browserPresenter;

    private Intent mService;
    /**
     * Accept and server status.
     */
    private ServerStatusReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        browserPresenter = Injector.injectBrowserPresenter();
        BrowserFragment browserFragment = (BrowserFragment) getSupportFragmentManager().findFragmentByTag(BrowserFragment.TAG);
        if (browserFragment == null) browserFragment = BrowserFragment.newInstance();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_container, browserFragment, BrowserFragment.TAG)
                    .commit();
            getSupportFragmentManager().executePendingTransactions();
        }
        handleIntent(getIntent());


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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mReceiver = new ServerStatusReceiver(this);
        mReceiver.register();
        mService = new Intent(this, CoreService.class);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mReceiver != null) {
                mReceiver.unRegister();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (browserPresenter.handleBackNavigation()) {
                return;
            }
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void showConfirmSaveBookmark(@NonNull BookmarkEntity bookmarkEntity) {
        EditBookmarkDialogFragment dialog = EditBookmarkDialogFragment.newInstance(R.string.bookmark_dialog_title_save, bookmarkEntity);
        dialog.show(getSupportFragmentManager(), EditBookmarkDialogFragment.TAG);
    }

    @Override
    public void navigateToBookmarks() {
        Navigator.navigateToBookmarks(this, REQUEST_PICK_BOOKMARK);
    }

    @Override
    public void navigateToTabSwitcher() {
        showTabSwitcher();
    }

    @Override
    public void dismissTabSwitcher() {
        removeTabSwitcher();
    }

    @Override
    public void copyUrlToClipboard(@NonNull String url) {
        copyTextToClipboard(getString(R.string.main_label_copy_url_to_clipboard), url);
        Toast.makeText(this, R.string.main_copy_to_clipboard_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void loadSuggestions(@NonNull SuggestionRepository suggestionRepository, @NonNull String query) {
        new AutocompleteTask(browserPresenter, suggestionRepository).execute(query);
    }

    @Override
    public void onBookmarkEdited(BookmarkEntity bookmark) {
        browserPresenter.saveBookmark(bookmark);
    }

    @Override
    protected void onResume() {
        super.onResume();
        browserPresenter.attachMainview(this);
//        startService(mService);
    }

    @Override
    protected void onPause() {
        browserPresenter.detachMainView();
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PICK_BOOKMARK:
                handleBookmarkResult(resultCode, data);
                break;
        }
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        LogUtils.d(TAG, "handleIntent action=" + action);
        switch (action) {
            case Intent.ACTION_VIEW:
                handleActionView(intent);
                break;
            case Intent.ACTION_WEB_SEARCH:
                handleActionWebSearch(intent);
                break;
            case Intent.ACTION_ASSIST:
                handleActionAssist();
                break;
            case Intent.ACTION_MAIN:
                handleFirstStart();
                break;
        }
    }

    private void handleFirstStart() {
        browserPresenter.requestSearchInNewTab("file:///android_asset/index.html");
    }

    private void handleActionView(Intent intent) {
        String url = intent.getDataString();
        browserPresenter.requestSearchInNewTab(url);
    }

    private void handleActionWebSearch(Intent intent) {
        String query = intent.getStringExtra(SearchManager.QUERY);
        browserPresenter.requestSearchInNewTab(query);
    }

    private void handleActionAssist() {
        browserPresenter.requestAssist();
    }

    private void handleBookmarkResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            BookmarkEntity bookmarkEntity = BookmarksActivity.getResultBookmark(data);
            if (bookmarkEntity != null) {
                browserPresenter.loadBookmark(bookmarkEntity);
            }
        }
    }

    private void showTabSwitcher() {
        TabSwitcherFragment fragment = TabSwitcherFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.content_container, fragment, TabSwitcherFragment.TAG).commit();
    }

    private void removeTabSwitcher() {
        TabSwitcherFragment fragment = (TabSwitcherFragment) getSupportFragmentManager().findFragmentByTag(TabSwitcherFragment.TAG);
        if (fragment == null) return;
        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
    }

    private void copyTextToClipboard(@NonNull String label, @NonNull String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboardManager.setPrimaryClip(clip);
    }


    /**
     * Start notify.
     */
    public void serverStart() {
        closeDialog();
//        String ip = NetUtil.getLocalIPAddress();
//        LogUtils.d(TAG, "serverStart ip="+ip);
//        browserPresenter.requestSearchInNewTab(String.format("http://%s:1080/index.html", ip));
    }

    /**
     * Started notify.
     */
    public void serverHasStarted() {
        closeDialog();
    }

    /**
     * Stop notify.
     */
    public void serverStop() {
        closeDialog();
    }

    private void showDialog() {

    }

    private void closeDialog() {
    }

}
