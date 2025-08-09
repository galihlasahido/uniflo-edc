package id.uniflo.uniedc.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.uniflo.uniedc.R;

public class QuickActionAdapter extends RecyclerView.Adapter<QuickActionAdapter.ViewHolder> {
    
    private List<DashboardFragment.QuickAction> actions;
    private OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onItemClick(DashboardFragment.QuickAction action);
    }
    
    public QuickActionAdapter(List<DashboardFragment.QuickAction> actions, OnItemClickListener listener) {
        this.actions = actions;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_quick_action, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DashboardFragment.QuickAction action = actions.get(position);
        holder.bind(action, listener);
    }
    
    @Override
    public int getItemCount() {
        return actions.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView icon;
        TextView title;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_action);
            icon = itemView.findViewById(R.id.iv_icon);
            title = itemView.findViewById(R.id.tv_title);
        }
        
        void bind(DashboardFragment.QuickAction action, OnItemClickListener listener) {
            icon.setImageResource(action.getIconResource());
            title.setText(action.getTitle());
            
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(action);
                }
            });
        }
    }
}