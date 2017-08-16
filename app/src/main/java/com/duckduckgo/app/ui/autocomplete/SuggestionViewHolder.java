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

package com.duckduckgo.app.ui.autocomplete;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.duckduckgo.app.util.UrlUtils;
import com.hfut235.emberwind.R;
import com.hfut235.emberwind.databinding.ViewholderSuggestionBinding;


public class SuggestionViewHolder extends RecyclerView.ViewHolder {

    public interface OnSuggestionListener {
        void onSuggestionSelected(View v, int position);

        void onAddToQuerySelected(View v, int position);
    }


    private ImageView iconImageView;
    private ImageButton addToQueryImageButton;
    private TextView suggestionTextView;

    public SuggestionViewHolder(View itemView, final OnSuggestionListener onSuggestionListener) {
        super(itemView);
        ViewholderSuggestionBinding data = ViewholderSuggestionBinding.bind(itemView);
        iconImageView = data.suggestionIconImageView;
        addToQueryImageButton = data.suggestionAddImageButton;
        suggestionTextView = data.suggestionTitleTextView;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSuggestionListener.onSuggestionSelected(v, getAdapterPosition());
            }
        });
        addToQueryImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSuggestionListener.onAddToQuerySelected(v, getAdapterPosition());
            }
        });

    }

    public void setSuggestion(@NonNull SuggestionEntity suggestion, @NonNull String filter) {
        Spannable filterStyled = getColoredText(itemView.getContext(), filter, R.color.suggestion_text_primary);

        suggestionTextView.setText(filterStyled);

        String suggestionText = suggestion.getSuggestion().replace(filter, "");
        Spannable suggestionStyled = getColoredText(itemView.getContext(), suggestionText, R.color.suggestion_text_secondary);

        suggestionTextView.append(suggestionStyled);

        int iconResId = UrlUtils.isUrl(suggestionText) ? R.drawable.ic_globe : R.drawable.ic_small_loupe;
        iconImageView.setImageResource(iconResId);
    }

    private Spannable getColoredText(Context context, @NonNull String text, @ColorRes int color) {
        Spannable textStyled = new SpannableString(text);
        textStyled.setSpan(
                new ForegroundColorSpan(
                        ContextCompat.getColor(context, color)),
                0,
                text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return textStyled;
    }

    public static SuggestionViewHolder inflate(ViewGroup parent, OnSuggestionListener onSuggestionListener) {
        return new SuggestionViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.viewholder_suggestion, parent, false),
                onSuggestionListener);
    }
}
