package com.example.todolist.ui;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.R;
import com.example.todolist.model.SubTask;
import com.example.todolist.model.Task;
import com.example.todolist.model.TaskWithSubTasks;
import com.example.todolist.util.DateTimeUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class TaskAdapter extends ListAdapter<TaskWithSubTasks, TaskAdapter.TaskViewHolder> {

    public interface OnTaskInteractionListener {
        void onTaskCheckedChanged(Task task, boolean isChecked);
        void onSubTaskCheckedChanged(TaskWithSubTasks parentItem, SubTask subTask, boolean isChecked);
        void onEditClicked(TaskWithSubTasks item);
        void onDeleteClicked(TaskWithSubTasks item);
    }

    private final OnTaskInteractionListener listener;
    private final Set<Integer> expandedTaskIds = new HashSet<>();

    public TaskAdapter(OnTaskInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<TaskWithSubTasks> DIFF_CALLBACK = new DiffUtil.ItemCallback<TaskWithSubTasks>() {
        @Override
        public boolean areItemsTheSame(@NonNull TaskWithSubTasks oldItem, @NonNull TaskWithSubTasks newItem) {
            return oldItem.task.getId() == newItem.task.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TaskWithSubTasks oldItem, @NonNull TaskWithSubTasks newItem) {
            boolean taskSame = oldItem.task.getTitle().equals(newItem.task.getTitle())
                    && Objects.equals(oldItem.task.getDescription(), newItem.task.getDescription())
                    && oldItem.task.getDueDate() == newItem.task.getDueDate()
                    && oldItem.task.isCompleted() == newItem.task.isCompleted()
                    && oldItem.task.getPriority() == newItem.task.getPriority();
            if (!taskSame) return false;
            if (oldItem.subTasks.size() != newItem.subTasks.size()) return false;

            // So khớp TỪNG mục con theo id (Room không đảm bảo thứ tự trả về của @Relation).
            // Trước đây chỉ so tổng số hoàn thành -> 2 tổ hợp tick khác nhau nhưng cùng
            // tổng số bị coi là "không đổi", UI không vẽ lại. Đây là lỗi vừa tìm thấy.
            for (SubTask newSub : newItem.subTasks) {
                boolean matched = false;
                for (SubTask oldSub : oldItem.subTasks) {
                    if (oldSub.getId() == newSub.getId()) {
                        if (oldSub.isCompleted() != newSub.isCompleted() || !oldSub.getTitle().equals(newSub.getTitle())) {
                            return false;
                        }
                        matched = true;
                        break;
                    }
                }
                if (!matched) return false;
            }
            return true;
        }
    };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskWithSubTasks item = getItem(position);
        boolean isExpanded = expandedTaskIds.contains(item.task.getId());
        holder.bind(item, listener, isExpanded, () -> toggleExpand(item.task.getId(), holder.getBindingAdapterPosition()));
    }

    private void toggleExpand(int taskId, int position) {
        if (!expandedTaskIds.add(taskId)) expandedTaskIds.remove(taskId);
        notifyItemChanged(position);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvDescription, tvDueDate, tvSubtaskCount;
        private final CheckBox cbCompleted;
        private final View viewPriorityStrip, headerClickArea;
        private final ImageButton btnEdit, btnDelete;
        private final LinearLayout containerSubtasks;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvSubtaskCount = itemView.findViewById(R.id.tv_subtask_count);
            cbCompleted = itemView.findViewById(R.id.cb_completed);
            viewPriorityStrip = itemView.findViewById(R.id.view_priority_strip);
            headerClickArea = itemView.findViewById(R.id.header_click_area);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            containerSubtasks = itemView.findViewById(R.id.container_subtasks);
        }

        void bind(TaskWithSubTasks item, OnTaskInteractionListener listener, boolean isExpanded, Runnable onToggleExpand) {
            Task task = item.task;
            tvTitle.setText(task.getTitle());
            tvDescription.setText(task.getDescription());
            tvDescription.setVisibility(
                    task.getDescription() == null || task.getDescription().isEmpty() ? View.GONE : View.VISIBLE);
            tvDueDate.setText(DateTimeUtils.formatDateTime(task.getDueDate()));

            int priorityColorRes;
            switch (task.getPriority()) {
                case Task.PRIORITY_HIGH: priorityColorRes = R.color.priority_high; break;
                case Task.PRIORITY_MEDIUM: priorityColorRes = R.color.priority_medium; break;
                default: priorityColorRes = R.color.priority_low; break;
            }
            viewPriorityStrip.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), priorityColorRes));

            if (task.isCompleted()) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                itemView.setAlpha(0.6f);
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                itemView.setAlpha(1f);
            }

            boolean hasSubtasks = !item.subTasks.isEmpty();
            tvSubtaskCount.setVisibility(hasSubtasks ? View.VISIBLE : View.GONE);
            if (hasSubtasks) tvSubtaskCount.setText(item.getCompletedSubTaskCount() + "/" + item.subTasks.size());

            containerSubtasks.removeAllViews();
            if (isExpanded && hasSubtasks) {
                containerSubtasks.setVisibility(View.VISIBLE);
                LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
                for (SubTask subTask : item.subTasks) {
                    View row = inflater.inflate(R.layout.item_subtask, containerSubtasks, false);
                    CheckBox cbSub = row.findViewById(R.id.cb_subtask);
                    TextView tvSub = row.findViewById(R.id.tv_subtask_title);
                    tvSub.setText(subTask.getTitle());
                    tvSub.setPaintFlags(subTask.isCompleted()
                            ? tvSub.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                            : tvSub.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                    cbSub.setOnCheckedChangeListener(null);
                    cbSub.setChecked(subTask.isCompleted());
                    cbSub.setOnCheckedChangeListener((b, isChecked) -> {
                        if (listener != null) listener.onSubTaskCheckedChanged(item, subTask, isChecked);
                    });
                    containerSubtasks.addView(row);
                }
            } else {
                containerSubtasks.setVisibility(View.GONE);
            }

            headerClickArea.setOnClickListener(v -> onToggleExpand.run());

            cbCompleted.setOnCheckedChangeListener(null);
            cbCompleted.setChecked(task.isCompleted());
            cbCompleted.setOnCheckedChangeListener((b, isChecked) -> {
                if (listener != null) listener.onTaskCheckedChanged(task, isChecked);
            });

            btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEditClicked(item); });
            btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDeleteClicked(item); });
        }
    }
}
