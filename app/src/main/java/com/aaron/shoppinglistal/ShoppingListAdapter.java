package com.aaron.shoppinglistal;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {
    private List<Item> shoppingItemList;
    private DatabaseReference shoppingListRef, boughtItemsRef;
    private Context context;
    private OnItemRemovedListener itemRemovedListener;

    public interface OnItemRemovedListener {
        void onItemRemoved();
    }

    public ShoppingListAdapter(Context context, List<Item> shoppingItemList, DatabaseReference shoppingListRef, OnItemRemovedListener listener) {
        this.context = context;
        this.shoppingItemList = shoppingItemList;
        this.shoppingListRef = shoppingListRef;
        this.itemRemovedListener = listener;

        if (shoppingListRef != null) {
            this.boughtItemsRef = shoppingListRef.getParent().child("boughtItems");
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = shoppingItemList.get(position);
        holder.item_name.setText(item.getName());
        holder.item_quantity.setText("Cantidad: " + item.getQuantity());

        // 🔹 Desactivar el listener antes de actualizar el switch (para evitar eventos no deseados)
        holder.switchBought.setOnCheckedChangeListener(null);
        holder.switchBought.setChecked(item.isBought());

        // 🔹 Activar el listener después de asignar el estado correcto
        holder.switchBought.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                item.setBought(true);
                moveItemToBoughtList(item, position);
            }
        });

        // 🔹 Configurar eliminación al mantener pulsado
        holder.itemView.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(position);
            return true;
        });
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Ítem")
                .setMessage("¿Seguro que quieres eliminar este ítem?")
                .setPositiveButton("Sí", (dialog, which) -> removeItem(position))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void removeItem(int position) {
        if (shoppingListRef != null && position >= 0 && position < shoppingItemList.size()) {
            String itemId = shoppingItemList.get(position).getId();

            shoppingListRef.child(itemId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // No eliminamos manualmente de la lista, Firebase se encargará
                    })
                    .addOnFailureListener(e -> Log.e("Firebase", "Error al eliminar el ítem", e));
        } else {
            Log.e("Adapter", "Índice fuera de los límites o lista vacía: " + position);
        }
    }

    private void moveItemToBoughtList(Item item, int position) {
        if (shoppingListRef != null && boughtItemsRef != null) {
            String itemId = item.getId();
            boughtItemsRef.child(itemId).setValue(item).addOnSuccessListener(aVoid -> {
                shoppingListRef.child(itemId).removeValue();
                shoppingItemList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, shoppingItemList.size()); // 🔹 IMPORTANTE
                if (itemRemovedListener != null) {
                    itemRemovedListener.onItemRemoved();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return shoppingItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView item_name, item_quantity;
        Switch switchBought;

        public ViewHolder(View itemView) {
            super(itemView);
            item_name = itemView.findViewById(R.id.item_name);
            item_quantity = itemView.findViewById(R.id.item_quantity);
            switchBought = itemView.findViewById(R.id.switch_bought);
        }
    }
}
