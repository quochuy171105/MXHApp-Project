package huynguyen.com.MXHApp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// **THỐNG NHẤT THƯ VIỆN: Chuyển sang dùng Glide**
import com.bumptech.glide.Glide;

import huynguyen.com.MXHApp.Model.Posts;
import huynguyen.com.MXHApp.PostDetails;
import huynguyen.com.MXHApp.R;

import java.util.List;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {
    Context context;
    List<Posts> postsList;

    public PhotosAdapter(Context context, List<Posts> postsList) {
        this.context = context;
        this.postsList = postsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.photos_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Posts posts=postsList.get(position);

        // **THỐNG NHẤT THƯ VIỆN: Chuyển sang dùng Glide**
        Glide.with(context).load(posts.getPostImage()).into(holder.postImage);

        holder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, PostDetails.class);
                intent.putExtra("postid",posts.getPostid());
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView postImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            postImage=itemView.findViewById(R.id.my_photos);

        }
    }
}
