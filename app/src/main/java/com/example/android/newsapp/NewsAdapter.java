package com.example.android.newsapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private final List<NewsItem> newsList;
    private final Context context;

    public NewsAdapter(List<NewsItem> newsList, Context context) {
        this.newsList = newsList;
        this.context = context;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.news_card, parent, false);

        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    /**
     * Clears the {@link #newsList} and notifies this adapter
     */
    public void clearNewsList() {
        newsList.clear();
        notifyDataSetChanged();
    }

    /**
     * Clears the {@link #newsList}, adds all the elements from passed list then notifies the adapter
     *
     * @param newsList List of new elements to be added
     */
    public void addAllToNewsList(List<NewsItem> newsList) {
        this.newsList.clear();
        this.newsList.addAll(newsList);
        notifyDataSetChanged();
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder {
        private final View currentItemView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            this.currentItemView = itemView;
        }

        private void bind(int position) {
            NewsItem currentNewsItem = newsList.get(position);

            TextView title = currentItemView.findViewById(R.id.newsCardTitle);
            TextView author = currentItemView.findViewById(R.id.newsCardAuthor);
            TextView section = currentItemView.findViewById(R.id.newsCardSection);
            TextView datePublished = currentItemView.findViewById(R.id.newsCardDatePublished);
            TextView trailText = currentItemView.findViewById(R.id.newsCardTrailText);

            //region Tie ViewHolder with currentNewsItem

            title.setText(currentNewsItem.getTitle());

            //if there is no author, exclude the author textView from view holder
            if (currentNewsItem.getAuthor().isEmpty())
                author.setVisibility(View.GONE);
            else {
                author.setVisibility(View.VISIBLE);
                String authorStr = "By: " + currentNewsItem.getAuthor();
                author.setText(authorStr);
            }
            section.setText(currentNewsItem.getSection());
            datePublished.setText(currentNewsItem.getDatePublished());
            trailText.setText(Html.fromHtml(currentNewsItem.getTrailText()));

            //endregion

            currentItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentNewsItem.getWebUrlString()));
                    context.startActivity(newsIntent);
                }
            });
        }
    }
}
