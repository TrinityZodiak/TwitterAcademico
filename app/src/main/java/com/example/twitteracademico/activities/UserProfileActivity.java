package com.example.twitteracademico.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.twitteracademico.R;
import com.example.twitteracademico.adapters.MyPostsAdapter;
import com.example.twitteracademico.models.Follow;
import com.example.twitteracademico.models.Post;
import com.example.twitteracademico.providers.AuthProvider;
import com.example.twitteracademico.providers.FollowsProvider;
import com.example.twitteracademico.providers.PostProvider;
import com.example.twitteracademico.providers.UsersProvider;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    LinearLayout mLinearLayoutEditProfile;
    TextView mTextViewUsername;
    TextView mTextViewPhone;
    TextView mTextViewEmail;
    TextView mTextViewPostNumber;
    TextView mTextFollowers;
    ImageView mImageViewCover;
    CircleImageView mCircleImageProfile;
    CircleImageView mCircleImageViewBack;
    TextView mTextViewPostExist;
    RecyclerView mRecyclerView;
    ImageView mImageViewFollow;

    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    PostProvider mPostProvider;
    FollowsProvider mFollowsProvider;

    String mExtraIdUser;
    MyPostsAdapter mAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mLinearLayoutEditProfile = findViewById(R.id.linearLayoutEditProfile);
        mTextViewEmail = findViewById(R.id.textViewEmail);
        mTextViewPhone = findViewById(R.id.textViewPhone);
        mTextViewPostNumber = findViewById(R.id.textViewPostNumber);
        mTextViewUsername = findViewById(R.id.textViewUsername);
        mCircleImageProfile = findViewById(R.id.circleImageProfile);
        mImageViewCover = findViewById(R.id.imageViewCover);
        mCircleImageViewBack = findViewById(R.id.circleImageBack);
        mTextViewPostExist = findViewById(R.id.textViewPostExist);
        mTextFollowers = findViewById(R.id.textViewFollows);
        mImageViewFollow = findViewById(R.id.imageViewFollow);

        mRecyclerView = findViewById(R.id.recyclerViewMyPost);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UserProfileActivity.this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        mPostProvider = new PostProvider();
        mFollowsProvider = new FollowsProvider();

        mExtraIdUser = getIntent().getStringExtra("idUser");

        mCircleImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mImageViewFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Follow follow = new Follow();
                follow.setIdSeguidor(mAuthProvider.getUid());
                follow.setIdSiguiendo(mExtraIdUser);
                follow.setTimestamp(new Date().getTime());
                follow(follow);
                /*mFollowsProvider.create(follow).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(UserProfileActivity.this, "Siguiendo", Toast.LENGTH_LONG).show();
                        }
                    }
                });*/
            }
        });

        getUser();
        getPostNumber();
        getNumberFollows();
        checkIfExistPost();

        checkIfExistFollow();
    }

    private void follow(Follow follow){
        System.out.println(mAuthProvider.getUid());
        mFollowsProvider.getFollowByUsers(follow.getIdSiguiendo(), mAuthProvider.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int numberDocuments = queryDocumentSnapshots.size();
                if(numberDocuments > 0){
                    String idFollow = queryDocumentSnapshots.getDocuments().get(0).getId();
                    mImageViewFollow.setImageResource(R.mipmap.icon_follow);
                    mFollowsProvider.delete(idFollow);
                }
                else{
                    mImageViewFollow.setImageResource(R.mipmap.icon_unfollow);
                    mFollowsProvider.create(follow);
                }
            }
        });
    }

    private void checkIfExistFollow() {
        mFollowsProvider.getFollowByUsers(mExtraIdUser,mAuthProvider.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int numberDocuments = queryDocumentSnapshots.size();
                if(numberDocuments > 0){
                    mImageViewFollow.setImageResource(R.mipmap.icon_unfollow);
                }else{
                    mImageViewFollow.setImageResource(R.mipmap.icon_follow);
                }
            }
        });
    }

    private void getNumberFollows() {
        mFollowsProvider.getFollowsByUser(mExtraIdUser).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                int numberFollowers = value.size();
                mTextFollowers.setText(numberFollowers + " Seguidores");
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = mPostProvider.getPostByUser(mExtraIdUser);
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();
        mAdapter = new MyPostsAdapter(options,UserProfileActivity.this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    private void checkIfExistPost() {
        mPostProvider.getPostByUser(mExtraIdUser).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                int numberPost = queryDocumentSnapshots.size();
                if(numberPost > 0){
                    mTextViewPostExist.setText("publicaciones");
                    mTextViewPostExist.setTextColor(Color.BLUE);
                }
                else{
                    mTextViewPostExist.setText("no hay publicaciones");
                    mTextViewPostExist.setTextColor(Color.GRAY);
                }
            }
        });
    }

    private void getPostNumber() {
        mPostProvider.getPostByUser(mExtraIdUser).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int numberPost = queryDocumentSnapshots.size();
                mTextViewPostNumber.setText(String.valueOf(numberPost));
            }
        });
    }

    private void getUser() {
        mUsersProvider.getUser(mExtraIdUser).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("email")) {
                        String email = documentSnapshot.getString("email");
                        mTextViewEmail.setText(email);
                    }
                    if (documentSnapshot.contains("phone")) {
                        String phone = documentSnapshot.getString("phone");
                        mTextViewPhone.setText(phone);
                    }
                    if (documentSnapshot.contains("username")) {
                        String username = documentSnapshot.getString("username");
                        mTextViewUsername.setText(username);
                    }
                    if (documentSnapshot.contains("image_profile")) {
                        String imageProfile = documentSnapshot.getString("image_profile");
                        if (imageProfile != null) {
                            if (!imageProfile.isEmpty()) {
                                Picasso.with(UserProfileActivity.this).load(imageProfile).into(mCircleImageProfile);
                            }
                        }
                    }
                    if (documentSnapshot.contains("image_cover")) {
                        String imageCover = documentSnapshot.getString("image_cover");
                        if (imageCover != null) {
                            if (!imageCover.isEmpty()) {
                                Picasso.with(UserProfileActivity.this).load(imageCover).into(mImageViewCover);
                            }
                        }
                    }

                }
            }
        });
    }

}