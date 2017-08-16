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

package com.duckduckgo.app.ui.editbookmark;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.duckduckgo.app.ui.bookmarks.BookmarkEntity;
import com.hfut235.emberwind.R;
import com.hfut235.emberwind.databinding.DialogEditBookmarkBinding;


public class EditBookmarkDialogFragment extends AppCompatDialogFragment {

    public static final String TAG = EditBookmarkDialogFragment.class.getSimpleName();

    public static EditBookmarkDialogFragment newInstance(int titleResId, @NonNull BookmarkEntity bookmark) {
        EditBookmarkDialogFragment dialog = new EditBookmarkDialogFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_TITLE, titleResId);
        args.putParcelable(EXTRA_BOOKMARK, bookmark);
        dialog.setArguments(args);
        return dialog;
    }

    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_BOOKMARK = "extra_bookmark";

    public interface OnEditBookmarkListener {
        void onBookmarkEdited(BookmarkEntity bookmark);
    }

    EditText nameEditText;
    EditText urlEditText;

    private OnEditBookmarkListener onEditBookmarkListener;

    private int titleResId;
    private BookmarkEntity bookmarkEntity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onEditBookmarkListener = (OnEditBookmarkListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + OnEditBookmarkListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        onEditBookmarkListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        titleResId = getArguments().getInt(EXTRA_TITLE);
        bookmarkEntity = getArguments().getParcelable(EXTRA_BOOKMARK);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = View.inflate(getContext(), R.layout.dialog_edit_bookmark, null);
        DialogEditBookmarkBinding binding = DialogEditBookmarkBinding.bind(rootView);
        urlEditText = binding.dialogEditBookmarkUrlEditText;
        nameEditText = binding.dialogEditBookmarkNameEditText;
        initUI();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(titleResId)
                .setView(rootView)
                .setPositiveButton(R.string.bookmark_dialog_save_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onSaveButtonClick();
                    }
                })
                .setNegativeButton(R.string.bookmark_dialog_cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        return builder.create();
    }

    private void initUI() {
        nameEditText.setText(bookmarkEntity.getName());
        urlEditText.setText(bookmarkEntity.getUrl());
    }

    private void onSaveButtonClick() {
        onEditBookmarkListener.onBookmarkEdited(getEditedBookmark());
        Toast.makeText(getContext(), R.string.bookmark_saved_prompt, Toast.LENGTH_SHORT).show();
    }

    private BookmarkEntity getEditedBookmark() {
        bookmarkEntity.setName(nameEditText.getText().toString());
        bookmarkEntity.setUrl(urlEditText.getText().toString());
        return bookmarkEntity;
    }
}
