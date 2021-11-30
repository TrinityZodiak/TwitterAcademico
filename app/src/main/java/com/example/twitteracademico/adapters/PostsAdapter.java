package com.example.twitteracademico.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.twitteracademico.R;
import com.example.twitteracademico.activities.PostDetailActivity;
import com.example.twitteracademico.models.Like;
import com.example.twitteracademico.models.Post;
import com.example.twitteracademico.providers.AuthProvider;
import com.example.twitteracademico.providers.LikesProvider;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;


import java.util.Date;

public class PostsAdapter extends FirestoreRecyclerAdapter<Post, PostsAdapter.ViewHolder> {

    Context context;
    LikesProvider mLikesProvider;
    AuthProvider mAuthProvider;

    public PostsAdapter(FirestoreRecyclerOptions<Post> options, Context context) {
        super(options);
        this.context = context;
        mLikesProvider = new LikesProvider();
        mAuthProvider = new AuthProvider();
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final Post post) {

        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String postId = document.getId();

        holder.textViewTitle.setText(post.getTitle());
        holder.textViewDescription.setText(post.getDescription());
        if (post.getImage1() != null) {
            if (!post.getImage1().isEmpty()) {
                Picasso.with(context).load(post.getImage1()).into(holder.imageViewPost);
            }
        }
        holder.viewHolder.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra( "id", postId);
                context.startActivity(intent);
            }
        });

        holder.imageViewLike.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Like like = new Like();
                like.setIdUser(mAuthProvider.getUid());
                like.setIdPost(postId);
                like.setTimestamp(new Date().getTime());
                like(like, holder);
            }
        });


        getNumberLikesByPost(postId, holder);
        checkIfExistLike(postId,mAuthProvider.getUid(),holder);
    }

    private void getNumberLikesByPost(String idPost,final ViewHolder holder){
        mLikesProvider.getLikesByPost(idPost).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                int numberLikes = queryDocumentSnapshots.size();
                holder.textViewLikes.setText(String.valueOf(numberLikes)+" Me Gusta ");
            }
        });
    }


    private void like(final Like like, final ViewHolder holder) {
        mLikesProvider.getLikeByPostAndUser(like.getIdPost(), mAuthProvider.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int numberDocuments = queryDocumentSnapshots.size();
                if(numberDocuments > 0){
                    String idLike = queryDocumentSnapshots.getDocuments().get(0).getId();
                    holder.imageViewLike.setImageResource(R.mipmap.icon_like_grey);
                    mLikesProvider.delete(idLike);
                }
                else{
                    holder.imageViewLike.setImageResource(R.mipmap.icon_like_blue);
                    mLikesProvider.create(like);
                }
            }
        });

    }

    private void checkIfExistLike(String idPost,String idUser, final ViewHolder holder) {
        mLikesProvider.getLikeByPostAndUser(idPost,idUser).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int numberDocuments = queryDocumentSnapshots.size();
                if(numberDocuments > 0){
                    holder.imageViewLike.setImageResource(R.mipmap.icon_like_blue);
                }
                else{
                    holder.imageViewLike.setImageResource(R.mipmap.icon_like_grey);
                }
            }
        });

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_post, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDescription;
        ImageView imageViewPost;

        TextView textViewLikes;
        ImageView imageViewLike;
        View viewHolder;

        public ViewHolder(View view) {
            super(view);
            textViewTitle = view.findViewById(R.id.textViewTitlePostCard);
            textViewDescription = view.findViewById(R.id.textViewDescriptionPostCard);
            textViewLikes = view.findViewById(R.id.textViewLikes);
            imageViewPost = view.findViewById(R.id.imageViewPostCard);
            imageViewLike = view.findViewById(R.id.imageViewLike);
            viewHolder= view;
        }

    }

}
