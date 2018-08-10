package com.jjstudio.jjtank.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjstudio.jjtank.R;
import com.jjstudio.jjtank.listener.RecyclerViewClickListener;
import com.jjstudio.jjtank.model.Tank;

import java.util.List;

public class TankAdapter extends RecyclerView.Adapter<TankAdapter.ViewHolder> {
    private List<Tank> tankList;
    private RecyclerViewClickListener recyclerViewClickListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView tankTitle,tankStatus;
        public Button connectButton;
        public RecyclerViewClickListener recyclerViewClickListener;
        public ViewHolder(TextView view,RecyclerViewClickListener recyclerViewClickListener) {
            super(view);
            this.tankTitle =  view.findViewById(R.id.tankTitle);
            this.tankStatus =  view.findViewById(R.id.tankStatus);
            this.connectButton =  view.findViewById(R.id.connectButton);
            this.recyclerViewClickListener = recyclerViewClickListener;
        }

        @Override
        public void onClick(View view) {
            recyclerViewClickListener.onClick(view, getAdapterPosition());
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public TankAdapter(List<Tank> tankList, RecyclerViewClickListener recyclerViewClickListener) {
        this.tankList = tankList;
        this.recyclerViewClickListener = recyclerViewClickListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TankAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        TextView view = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tank_item_layout, parent, false);
        ViewHolder vh = new ViewHolder(view,recyclerViewClickListener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.tankTitle.setText(tankList.get(position).getTitle());
        holder.tankStatus.setText(tankList.get(position).getStatus().toString());
        holder.connectButton.setOnClickListener(holder);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return tankList.size();
    }
}