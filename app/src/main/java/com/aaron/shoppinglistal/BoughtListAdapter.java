package com.aaron.shoppinglistal;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import java.util.List;

public class BoughtListAdapter extends RecyclerView.Adapter<BoughtListAdapter.ViewHolder> {

    private List<Item> boughtItemList;
    private DatabaseReference boughtItemsRef;
    private Context context;

    public BoughtListAdapter(Context context, List<Item> boughtItemList, DatabaseReference boughtItemsRef) {
        this.context = context;
        this.boughtItemList = boughtItemList;
        this.boughtItemsRef = boughtItemsRef;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bought_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = boughtItemList.get(position);
        holder.item_name.setText(item.getName());
        holder.item_quantity.setText("Cantidad: " + item.getQuantity());

        holder.itemView.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(holder.getAdapterPosition());
            return true;
        });
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar ítem")
                .setMessage("¿Seguro que quieres eliminar este ítem?")
                .setPositiveButton("Sí", (dialog, which) -> removeItem(position))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void removeItem(int position) {
        if (boughtItemsRef != null && position >= 0 && position < boughtItemList.size()) {
            String itemId = boughtItemList.get(position).getId();

            boughtItemsRef.child(itemId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // No eliminamos manualmente de la lista, Firebase se encargará
                    })
                    .addOnFailureListener(e -> Log.e("Firebase", "Error al eliminar el ítem", e));
        } else {
            Log.e("Adapter", "Índice fuera de los límites o lista vacía: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return boughtItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView item_name, item_quantity;

        public ViewHolder(View itemView) {
            super(itemView);
            item_name = itemView.findViewById(R.id.item_name);
            item_quantity = itemView.findViewById(R.id.item_quantity);
        }
    }
}
