package com.bcit.orderly;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;


public class ItemTaskAdapter extends RecyclerView.Adapter<ItemTaskAdapter.ViewHolder> {

    private Task[] tasks;

    /**
     * Provide a reference to the type of views that you are using
     * Called by TaskListFragment.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView_taskName;
        private final TextView textView_taskDesc;
        private final LinearLayout taskItem;
        private final ProjectActivity projectActivity;

        public ViewHolder(View view) {
            super(view);

            textView_taskName = view.findViewById(R.id.textView_itemTask_taskName);
            textView_taskDesc = view.findViewById(R.id.textView_itemTask_taskDesc);
            taskItem = view.findViewById(R.id.linearLayout_item_task);
            projectActivity = (ProjectActivity) view.getContext();

        }

        public TextView getTextView_taskName() {
            return textView_taskName;
        }
        public TextView getTextView_taskDesc() {
            return textView_taskDesc;
        }
        public LinearLayout getTaskItem() { return taskItem; }
    }

    /**
     * Initialize the dataset of the Adapter.
     *  @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public ItemTaskAdapter(Task[] dataSet) {
        tasks = dataSet;
    }

    /**
     * Create new views (invoked by the layout manager).
     *
     * @param viewGroup ViewGroup.
     * @param viewType int.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_task, viewGroup, false);

        return new ViewHolder(view);
    }

    /**
     * Replace the contents of a view (invoked by the layout manager).
     *
     * @param viewHolder ViewHolder.
     * @param position int.
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTextView_taskName().setText(tasks[position].getName());
        viewHolder.getTextView_taskDesc().setText(tasks[position].getDescription());

        String taskId = tasks[position].getId();
        String projectId = tasks[position].getProjectId();
        viewHolder.getTaskItem().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = viewHolder.projectActivity.getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragmentContainerView_tasks, TaskDetailsFragment.newInstance(projectId, taskId, tasks[position]));
                ft.commit();
            }
        });
    }

    /**
     * Return the size of your dataset (invoked by the layout manager).
     *
     * @return int.
     */
    @Override
    public int getItemCount() {
        return tasks.length;
    }

}
