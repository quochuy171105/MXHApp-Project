package huynguyen.com.MXHApp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import huynguyen.com.MXHApp.AccountStatusActivity;
import huynguyen.com.MXHApp.Model.User;
import huynguyen.com.MXHApp.databinding.AdminUserItemBinding;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;

    public AdminUserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdminUserItemBinding binding = AdminUserItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // TODO: Bind user data to the views
        // TODO: Set up click listener to open AccountStatusActivity
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        AdminUserItemBinding binding;

        public ViewHolder(AdminUserItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
