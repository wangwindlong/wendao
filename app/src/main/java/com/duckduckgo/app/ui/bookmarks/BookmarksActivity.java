/*
 * Copyright (c) 2017 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.app.ui.bookmarks;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.duckduckgo.app.Injector;
import com.duckduckgo.app.ui.base.itemtouchhelper.OnStartDragListener;
import com.duckduckgo.app.ui.bookmarks.itemtouchhelper.BookmarksTouchHelperCallback;
import com.duckduckgo.app.ui.editbookmark.EditBookmarkDialogFragment;
import com.hfut235.emberwind.R;
import com.hfut235.emberwind.databinding.ActivityBookmarksBinding;

import java.util.List;


public class BookmarksActivity extends AppCompatActivity implements BookmarksView, OnStartDragListener, EditBookmarkDialogFragment.OnEditBookmarkListener {

    public static Intent getStartIntent(Context context) {
        return new Intent(context, BookmarksActivity.class);
    }

    @Nullable
    public static BookmarkEntity getResultBookmark(Intent intent) {
        return intent.getParcelableExtra(RESULT_BOOKMARK);
    }

    private static final String RESULT_BOOKMARK = "result_bookmark";

    private static final String EXTRA_IS_EDITING = "extra_is_editing";

    Toolbar toolbar;
    RecyclerView recyclerView;
    TextView emptyTextView;

    private BookmarksAdapter adapter;
    private BookmarksPresenter presenter;
    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_bookmarks);
        ActivityBookmarksBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_bookmarks);
        toolbar = binding.bookmarksToolbar;
        recyclerView = binding.bookmarksRecyclerView;
        emptyTextView = binding.bookmarksEmptyTextView;
        initUI();

        presenter = Injector.injectBookmarkPresenter();

        if (savedInstanceState != null) {
            boolean isEditing = savedInstanceState.getBoolean(EXTRA_IS_EDITING);
            presenter.restore(isEditing);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.attachView(this);
        presenter.load();
    }

    @Override
    protected void onPause() {
        presenter.detachView();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_IS_EDITING, adapter.isEditable());
    }

    @Override
    public void onBackPressed() {
        presenter.dismiss();
    }

    @Override
    protected void onDestroy() {
        if (isFinishing()) {
            Injector.clearBookmarksPresenter();
        }
        super.onDestroy();
    }

    @Override
    public void loadBookmarks(@NonNull List<BookmarkEntity> bookmarks) {
        adapter.setBookmarks(bookmarks);
    }

    @Override
    public void showEmpty(boolean empty) {
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyTextView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showEditButton(boolean visible) {
        setEditMenuItemVisible(visible);
    }

    @Override
    public void showEditMode() {
        setEditMenuItemVisible(false);
        adapter.setEditable(true);
    }

    @Override
    public void dismissEditMode() {
        setEditMenuItemVisible(true);
        adapter.setEditable(false);
    }

    @Override
    public void close() {
        finish();
    }

    @Override
    public void showEditBookmark(@NonNull BookmarkEntity bookmarkEntity) {
        EditBookmarkDialogFragment dialog = EditBookmarkDialogFragment.newInstance(R.string.bookmark_dialog_title_edit, bookmarkEntity);
        dialog.show(getSupportFragmentManager(), EditBookmarkDialogFragment.TAG);
    }

    @Override
    public void resultOpenBookmark(@NonNull BookmarkEntity bookmarkEntity) {
        Intent intent = new Intent();
        intent.putExtra(RESULT_BOOKMARK, bookmarkEntity);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onBookmarkEdited(BookmarkEntity bookmark) {
        presenter.saveEditedBookmark(bookmark);
    }

    private void initUI() {
        initToolbar();
        initRecyclerView();
    }

    private void initToolbar() {
        toolbar.setTitle(R.string.bookmarks_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_dark_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.dismiss();
            }
        });
        toolbar.inflateMenu(R.menu.bookmarks);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_edit:
                        presenter.edit();
                        return true;
                }
                return false;
            }
        });
    }

    private void initRecyclerView() {
        adapter = new BookmarksAdapter(this, new BookmarksAdapter.OnBookmarkListener() {
            @Override
            public void onBookmarkSelected(View v, int position) {
                presenter.bookmarkSelected(position);
            }

            @Override
            public void onBookmarkDeleted(View v, int position) {
                presenter.bookmarkDeleted(position);
            }

            @Override
            public void onBookmarksSwap(int fromPosition, int toPosition) {
                presenter.bookmarksMoved(fromPosition, toPosition);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        ItemTouchHelper.Callback callback = new BookmarksTouchHelperCallback(adapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void setEditMenuItemVisible(boolean visible) {
        MenuItem editMenuItem = toolbar.getMenu().findItem(R.id.action_edit);
        editMenuItem.setVisible(visible);
    }
}
