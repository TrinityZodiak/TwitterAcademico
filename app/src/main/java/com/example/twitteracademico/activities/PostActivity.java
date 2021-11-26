package com.example.twitteracademico.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.twitteracademico.R;
import com.example.twitteracademico.models.Post;
import com.example.twitteracademico.providers.AuthProvider;
import com.example.twitteracademico.providers.ImageProvider;
import com.example.twitteracademico.providers.PostProvider;
import com.example.twitteracademico.utils.FileUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class PostActivity extends AppCompatActivity {

    ImageView mImageViewPost1;
    File mImageFile;
    Button mButtonPost;

    ImageProvider mImageProvider;
    PostProvider mPostProvider;
    AuthProvider mAuthProvider;

    TextInputEditText mTextInputTitle;
    TextInputEditText mTextInputDescription;


    private final int GALLERY_REQUEST_CODE = 1;

    String mTitle = "";
    String mDescrption = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mImageViewPost1 = findViewById(R.id.imageViewPost1);
        mButtonPost = findViewById(R.id.btnPost);

        mImageProvider = new ImageProvider();
        mPostProvider = new PostProvider();
        mAuthProvider = new AuthProvider();

        mTextInputTitle = findViewById(R.id.textInputTema);
        mTextInputDescription = findViewById(R.id.textInputDescription);

        mImageViewPost1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        mButtonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPost();
            }
        });
    }

    private void clickPost() {
        mTitle = mTextInputTitle.getText().toString();
        mDescrption = mTextInputDescription.getText().toString();

        if(!mTitle.isEmpty() && !mDescrption.isEmpty() ){
            if(mImageFile != null){
                saveImage();
            }else{
                Toast.makeText(this, "Ingrese una imagen para su post", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this, "Ingresa todos los campos para publicar", Toast.LENGTH_LONG).show();
        }

    }

    private void saveImage() {
        mImageProvider.save(PostActivity.this,mImageFile).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            Post post = new Post();
                            post.setImage1(url);
                            post.setTitle(mTitle);
                            post.setDescription(mDescrption);
                            post.setIdUser(mAuthProvider.getUid());
                            mPostProvider.save(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> taskSave) {
                                    if(taskSave.isSuccessful()){
                                        Toast.makeText(PostActivity.this, "Publicado", Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(PostActivity.this, "No se pudo hacer la publicación", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    });
                }else{
                    Toast.makeText(PostActivity.this, "Hubo un error al guardar la imagen", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){
            try{
                mImageFile = FileUtil.from(this,data.getData());
                mImageViewPost1.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            }catch (Exception e){
                Log.d("Error", "Se produjo un error" + e.getMessage());
                Toast.makeText(this, "No se ha podido cargar la imagen"+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}