package com.aaron.shoppinglistal.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aaron.shoppinglistal.BoughtListAdapter;
import com.aaron.shoppinglistal.Item;
import com.aaron.shoppinglistal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private RecyclerView recyclerView;
    private BoughtListAdapter adapter;
    private List<Item> boughtItemList;
    private DatabaseReference boughtItemsRef, shoppingListRef;
    private FirebaseUser user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewBoughtItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        boughtItemList = new ArrayList<>();

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            boughtItemsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("boughtItems");
            shoppingListRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("shoppingList");

            // Inicializar el adaptador después de que boughtItemsRef esté listo
            adapter = new BoughtListAdapter(getContext(), boughtItemList, boughtItemsRef);
            recyclerView.setAdapter(adapter);

            boughtItemsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boughtItemList.clear();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Item item = data.getValue(Item.class);
                        if (item != null) {
                            item.setId(data.getKey()); // Asignar el ID del ítem
                            boughtItemList.add(item);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error al cargar los datos", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }


}