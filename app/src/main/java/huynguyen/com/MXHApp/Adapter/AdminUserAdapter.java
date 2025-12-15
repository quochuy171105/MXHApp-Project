package huynguyen.com.MXHApp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.databinding.AdminUserItemBinding;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;
    private FirebaseFirestore firestore;

    public AdminUserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdminUserItemBinding binding = AdminUserItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.binding.userEmailTextView.setText("Email: " + user.getEmail());
        holder.binding.userNameTextView.setText("Username: " + user.getUsername());
        holder.binding.userStatusTextView.setText("Status: " + user.getAccountStatus());

        holder.binding.blockButton.setOnClickListener(v -> showBlockDialog(user));
        holder.binding.activateButton.setOnClickListener(v -> updateUserStatus(user, "active", ""));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private void showBlockDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Block User");

        final EditText reasonInput = new EditText(context);
        reasonInput.setHint("Reason for blocking");
        builder.setView(reasonInput);

        builder.setPositiveButton("Block", (dialog, which) -> {
            String reason = reasonInput.getText().toString();
            updateUserStatus(user, "blocked", reason);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateUserStatus(User user, String status, String reason) {
        DocumentReference userRef = firestore.collection("users").document(user.getUser_id());
        Map<String, Object> updates = new HashMap<>();
        updates.put("accountStatus", status);
        updates.put("statusReason", reason);
        userRef.update(updates);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AdminUserItemBinding binding;

        public ViewHolder(AdminUserItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
