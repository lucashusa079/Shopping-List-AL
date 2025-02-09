package com.aaron.shoppinglistal.ui.home;

import com.aaron.shoppinglistal.ShoppingListAdapter;
import com.aaron.shoppinglistal.Item;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.shoppinglistal.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements ShoppingListAdapter.OnItemRemovedListener {

    private RecyclerView recyclerView;
    private ShoppingListAdapter adapter;
    private List<Item> itemList;
    private DatabaseReference databaseReference;
    private FirebaseUser user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewShoppingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton fabAddItem = view.findViewById(R.id.fab_add_item);
        fabAddItem.setOnClickListener(v -> showAddItemDialog());

        itemList = new ArrayList<>();

        // Obtener usuario actual y referencia a Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("items");

            // 🔹 Escuchar cambios en la base de datos para actualizar la lista en tiempo real
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    itemList.clear();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Item item = data.getValue(Item.class);
                        if (item != null && !item.isBought()) {
                            itemList.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();  // 🔹 Refrescar la vista después de actualizar los datos
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show();
                }
            });
        }

        adapter = new ShoppingListAdapter(getContext(), itemList, databaseReference, this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void addItemToFirebase(String itemName, int itemQuantity) {
        String id = databaseReference.push().getKey();
        Item item = new Item(id, itemName, itemQuantity, false);
        databaseReference.child(id).setValue(item);
    }

    private void showAddItemDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        EditText etItemName = dialogView.findViewById(R.id.et_item_name);
        EditText etItemQuantity = dialogView.findViewById(R.id.et_item_quantity);
        Button btnAddItem = dialogView.findViewById(R.id.btn_add_item);

        AlertDialog dialog = builder.create();

        btnAddItem.setOnClickListener(v -> {
            String itemName = etItemName.getText().toString().trim();
            String itemQuantityString = etItemQuantity.getText().toString().trim();

            if (!itemName.isEmpty() && !itemQuantityString.isEmpty()) {
                int itemQuantity = Integer.parseInt(itemQuantityString);
                addItemToFirebase(itemName, itemQuantity);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Por favor ingresa el nombre y la cantidad", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    @Override
    public void onItemRemoved() {
        // 🔹 Refrescamos la vista después de eliminar un ítem para evitar problemas visuales
        adapter.notifyDataSetChanged();
    }
}
