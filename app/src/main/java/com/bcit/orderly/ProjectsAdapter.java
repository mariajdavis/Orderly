package com.bcit.orderly;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ViewHolder> {

    private static Project[] projectsData;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private Button groupListItem;

        public ViewHolder(View view) {
            super(view);
            groupListItem = view.findViewById(R.id.button_groups_groupListItem);
            groupListItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), ProjectActivity.class);
                    intent.putExtra("PROJECT_OBJECT", (Serializable) projectsData[getAdapterPosition()].getId());
                    view.getContext().startActivity(intent);
                }
            });
        }

        public Button getGroupListItem() {
            return groupListItem;
        }
    }

    public ProjectsAdapter(Project[] dataSet) {
        projectsData = dataSet;
    }

    /**
     * Create new views (invoked by the layout manager).
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_groups, viewGroup, false);

        return new ViewHolder(view);
    }

    /**
     * Replace the contents of a view (invoked by the layout manager).
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.groupListItem.setText(projectsData[position].getName());
    }

    /**
     * Return the size of your dataset (invoked by the layout manager).
     *
     * @return the size of the dateset.
     */
    @Override
    public int getItemCount() {
        return projectsData.length;
    }
}