package com.ftpos.pay.demo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import com.ftpos.pay.demo.R;
import com.jirui.logger.impl.view.LogLine;

/**
 *
 */

public class LogItemAdapter extends AbsRecyclerAdapter<AbsViewBinder<LogLine>, LogLine> {

    public LogItemAdapter(Context context) {
        super(context);
    }


    @Override
    protected AbsViewBinder<LogLine> createViewHolder(View view, int viewType) {
        return new LogItemViewHolder(view);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return inflater.inflate(R.layout.item_simple, parent, false);
    }

    public class LogItemViewHolder extends AbsViewBinder<LogLine> {
        private TextView logContent;
        public LogItemViewHolder(View view) {
            super(view);
        }

        @Override
        protected void getViews() {
            logContent = getView(R.id.content);
            logContent.setTextSize(12);
        }
        @ColorInt
        private int toColorValue(int priority) {
            int color = Color.rgb(0xBB, 0xBB, 0xBB);
            switch (priority) {
                case 2:
                    color = Color.rgb(0xBB, 0xBB, 0xBB);
                    break;
                case 3:
                    color = Color.rgb(0x00, 0x70, 0xBB);
                    break;
                case 4:
                    color = Color.rgb(0x48, 0xBB, 0x31);
                    break;
                case 5:
                    color = Color.rgb(0xBB, 0xBB, 0x23);
                    break;
                case 6:
                    color = Color.rgb(0xFF, 0x00, 0x06);
                    break;
                case 7:
                    color = Color.rgb(0x8F, 0x00, 0x05);
                    break;
                default:
                    color = Color.rgb(0xBB, 0xBB, 0xBB);
                    break;
            }
            return color;
        }
        @Override
        public void bind(LogLine item) {
            logContent.setText(item.getMessage());
            if (item.isExpanded() ) {
                logContent.setSingleLine(false);
                logContent.setTextColor(Color.WHITE);
                itemView.setBackgroundColor(Color.BLACK);

            } else {
                logContent.setSingleLine(true);
                logContent.setTextColor(toColorValue(item.getPriority()));
                itemView.setBackgroundColor(Color.WHITE);
            }
        }

        @Override
        protected void onViewClick(View view, final LogLine item) {
            super.onViewClick(view, item);
            item.setExpanded(!item.isExpanded());
            if (item.isExpanded() ) {
                logContent.setSingleLine(false);
                logContent.setTextColor(Color.WHITE);
                itemView.setBackgroundColor(Color.BLACK);

            } else {
                logContent.setSingleLine(true);
                logContent.setTextColor(toColorValue(item.getPriority()));
                itemView.setBackgroundColor(Color.WHITE);
            }
        }
    }
}