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
import com.example.twitteracademico.activities.UserProfileActivity;
import com.example.twitteracademico.models.Follow;
import com.example.twitteracademico.models.User;
import com.example.twitteracademico.providers.AuthProvider;
import com.example.twitteracademico.providers.FollowsProvider;
import com.example.twitteracademico.providers.UsersProvider;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileAdapter extends FirestoreRecyclerAdapter<User, ProfileAdapter.ViewHolder> {

    Context context;
    FollowsProvider mFollowsProvider;
    UsersProvider mUsersProvider;

    public ProfileAdapter(FirestoreRecyclerOptions<User> options, Context context) {
        super(options);
        System.out.println("llegué aquí1");
        this.context = context;
        this.mFollowsProvider = new FollowsProvider();
        this.mUsersProvider = new UsersProvider();
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final User user) {

        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String userId = document.getId();

        holder.textViewUsername.setText(user.getUsername().toUpperCase());
        holder.textViewEmail.setText(user.getEmail());
        if (user.getImageProfile() != null){
            if(!user.getImageProfile().isEmpty()){
                Picasso.with(context).load(user.getImageProfile()).into(holder.circleImageProfile);
            }
        }
        if (user.getImageCover() != null){
            if(!user.getImageCover().isEmpty()){
                Picasso.with(context).load(user.getImageCover()).into(holder.imageViewCover);
            }
        }
        holder.textViewPhone.setText(user.getPhone());

        holder.viewHolder.setOnClickListener((view) ->{
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("id", userId);
            context.startActivity(intent);
        });

        holder.imageViewFollow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Llegue aqui2");
                Follow follow = new Follow();
                follow.setIdSeguidor(user.getId());
                follow.setIdSiguiendo(userId);
                follow.setTimestamp(new Date().getTime());
                //follow(follow, holder);
            }
        });

        //getNumberFollowsByUser(userId, holder);
        //checkIfExistFollow(userId, mAuthProvider.getUid(),holder);
    }

    private void follow(Follow follow){
        mFollowsProvider.create(follow);
    }

    /*private void getNumberFollowsByUser(String idSiguiendo, final ViewHolder holder){
        mFollowsProvider.getFollowsByUser(idSiguiendo).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                int numberFollows = queryDocumentSnapshots.size();
                holder.textViewFollows.setText(String.valueOf(numberFollows) + "Seguidores");
            }
        });
    }

    private void follow(final Follow follow, final ViewHolder holder){
        mFollowsProvider.getFollowByUsers(follow.getIdSiguiendo(), mAuthProvider.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int numberDocuments = queryDocumentSnapshots.size();
                if(numberDocuments > 0){
                    String idFollow = queryDocumentSnapshots.getDocuments().get(0).getId();
                    holder.imageViewFollow.setImageResource(R.mipmap.icon_follow);
                    mFollowsProvider.delete(idFollow);
                }else{
                    holder.imageViewFollow.setImageResource(R.mipmap.icon_unfollow);
                    mFollowsProvider.create(follow);
                }
            }
        });
    }

    private void checkIfExistFollow(String idSiguiendo, String idUser, final ViewHolder holder){
        mFollowsProvider.getFollowByUsers(idSiguiendo,idUser).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int numberDocuments = queryDocumentSnapshots.size();
                if(numberDocuments > 0){
                    holder.imageViewFollow.setImageResource(R.mipmap.icon_follow);
                }else{
                    holder.imageViewFollow.setImageResource(R.mipmap.icon_unfollow);
                }
            }
        });
    }*/

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_user_profile, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUsername;
        TextView textViewEmail;
        TextView textViewPostNumber;
        ImageView imageViewCover;
        CircleImageView circleImageProfile;
        TextView textViewPhone;
        TextView textViewPostExist;
        RecyclerView recyclerViewMyPost;

        ImageView imageViewFollow;
        TextView textViewFollows;

        View viewHolder;

        public ViewHolder(View view) {
            super(view);
            textViewUsername = view.findViewById(R.id.textViewUsername);
            textViewEmail = view.findViewById(R.id.textViewEmail);
            textViewPostNumber = view.findViewById(R.id.textViewPostNumber);
            imageViewCover = view.findViewById(R.id.imageViewCover);
            circleImageProfile = view.findViewById(R.id.circleImageProfile);
            textViewPhone = view.findViewById(R.id.textViewPhone);
            textViewPostExist = view.findViewById(R.id.textViewPostExist);
            recyclerViewMyPost = view.findViewById(R.id.recyclerViewMyPost);

            imageViewFollow = view.findViewById(R.id.imageViewFollow);
            textViewFollows = view.findViewById(R.id.textViewFollows);
            viewHolder= view;
        }
    }
}
