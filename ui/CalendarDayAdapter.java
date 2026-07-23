package com.example.todolist.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClicked(Calendar day);
    }

    public static class DayCell {
        public final Calendar date;
        public final boolean isCurrentMonth;
        public boolean hasTasks;
        public boolean isSelected;

        DayCell(Calendar date, boolean isCurrentMonth) {
            this.date = date;
            this.isCurrentMonth = isCurrentMonth;
        }
    }

    private List<DayCell> days = new ArrayList<>();
    private final OnDayClickListener listener;

    public CalendarDayAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    // Lưới cố định 42 ô/tháng, kích thước rất nhỏ nên notifyDataSetChanged() ở đây
    // hoàn toàn hợp lý, không cần DiffUtil như danh sách Task.
    public void submitDays(List<DayCell> newDays) {
        this.days = newDays;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bind(days.get(position), listener);
    }

    @Override
    public int getItemCount() { return days.size(); }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDayNumber;
        private final View dotIndicator;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tv_day_number);
            dotIndicator = itemView.findViewById(R.id.dot_indicator);
        }

        void bind(DayCell cell, OnDayClickListener listener) {
            tvDayNumber.setText(String.valueOf(cell.date.get(Calendar.DAY_OF_MONTH)));
            dotIndicator.setVisibility(cell.hasTasks ? View.VISIBLE : View.GONE);

            boolean isToday = isSameDay(cell.date, Calendar.getInstance());

            if (cell.isSelected) {
                tvDayNumber.setBackgroundResource(R.drawable.bg_day_selected);
                tvDayNumber.setTextColor(resolveColor(R.color.on_primary));
            } else if (isToday) {
                tvDayNumber.setBackgroundResource(R.drawable.bg_day_today);
                tvDayNumber.setTextColor(resolveColor(R.color.primary));
            } else {
                tvDayNumber.setBackgroundResource(0);
                tvDayNumber.setTextColor(resolveColor(cell.isCurrentMonth ? R.color.text_primary : R.color.text_secondary));
            }
            itemView.setAlpha(cell.isCurrentMonth ? 1f : 0.4f);
            itemView.setOnClickListener(v -> listener.onDayClicked(cell.date));
        }

        private int resolveColor(int colorRes) {
            return ContextCompat.getColor(itemView.getContext(), colorRes);
        }

        private boolean isSameDay(Calendar a, Calendar b) {
            return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
        }
    }
}