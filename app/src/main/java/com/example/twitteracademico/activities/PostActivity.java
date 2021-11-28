package com.example.twitteracademico.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class PostActivity extends AppCompatActivity {

    //Imagenes
    ImageView mImageViewPost1;
    ImageView mImageViewPost2;
    CircleImageView mCircleImageBack;

    File mImageFile;
    File mImageFile2;


    Button mButtonPost;

    //Providers
    ImageProvider mImageProvider;
    PostProvider mPostProvider;
    AuthProvider mAuthProvider;

    TextInputEditText mTextInputTitle;
    TextInputEditText mTextInputDescription;


    private final int GALLERY_REQUEST_CODE = 1;
    private final int GALLERY_REQUEST_CODE_2 = 2;
    private final int PHOTO_REQUEST_CODE = 3;
    private final int PHOTO_REQUEST_CODE2 = 4;

    AlertDialog mDialog;
    AlertDialog.Builder mBuilderSelector;
    CharSequence options[];

    String mTitle = "";
    String mDescrption = "";

    //Foto1
    String mAbsolutePhotoPath;
    File mPhotoFile;
    String mPhotoPath;

    //Foto2
    String mAbsolutePhotoPath2;
    String mPhotoPath2;
    File mPhotoFile2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mImageViewPost1 = findViewById(R.id.imageViewPost1);
        mImageViewPost2 = findViewById(R.id.imageViewPost2);
        mCircleImageBack = findViewById(R.id.circleImageBack);

        mButtonPost = findViewById(R.id.btnPost);

        mImageProvider = new ImageProvider();
        mPostProvider = new PostProvider();
        mAuthProvider = new AuthProvider();

        mTextInputTitle = findViewById(R.id.textInputTema);
        mTextInputDescription = findViewById(R.id.textInputDescription);

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento...")
                .setCancelable(false).build();

        mBuilderSelector = new AlertDialog.Builder(this);
        mBuilderSelector.setTitle("Selecciona una opción");
        options = new CharSequence[]{"Galeria","Tomar foto"};


        mImageViewPost1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectOptionImage(1);
            }
        });

        mImageViewPost2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectOptionImage(2);
            }
        });

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mButtonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPost();
            }
        });
    }

    private void selectOptionImage(final int numberImage) {
        mBuilderSelector.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    if(numberImage == 1){
                        openGallery(GALLERY_REQUEST_CODE);
                    }else{
                        if(numberImage == 2){
                            openGallery(GALLERY_REQUEST_CODE_2);
                        }
                    }
                }else{
                    if(i == 1){
                        if(numberImage == 1){
                            takePhoto(PHOTO_REQUEST_CODE);
                        }else{
                            if(numberImage == 2){
                                takePhoto(PHOTO_REQUEST_CODE2);
                            }
                        }
                    }
                }
            }
        });

        mBuilderSelector.show();
    }

    private void takePhoto(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            File photoFile = null;
            try{
                photoFile = createPhotoFile(requestCode);
            }catch(Exception e){
                Toast.makeText(this, "Hubo un error con el archivo", Toast.LENGTH_LONG).show();
            }

            if(photoFile != null){
                Uri photoUri = FileProvider.getUriForFile(PostActivity.this,"com.example.twitteracademico",photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                startActivityForResult(takePictureIntent,requestCode);
            }
        }
    }

    private File createPhotoFile(int requestCode) throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photoFile = File.createTempFile(new Date()+"_photo",".jpg",storageDir);

        if(requestCode == PHOTO_REQUEST_CODE){
            mPhotoPath = "file:" + photoFile.getAbsolutePath();
            mAbsolutePhotoPath = photoFile.getAbsolutePath();
        }else{
            if(requestCode == PHOTO_REQUEST_CODE2){
                mPhotoPath2 = "file:" + photoFile.getAbsolutePath();
                mAbsolutePhotoPath2 = photoFile.getAbsolutePath();
            }
        }
        return photoFile;
    }

    private void clickPost() {
        mTitle = mTextInputTitle.getText().toString();
        mDescrption = mTextInputDescription.getText().toString();

        if(!mTitle.isEmpty() && !mDescrption.isEmpty() ){
            //Seleccion de ambas imagenes de la galeria
            if(mImageFile != null && mImageFile2 != null){
                saveImage(mImageFile,mImageFile2);
            }else{
                //Slecciono las 2 fotos desde la camara
                if(mPhotoFile != null && mPhotoFile2 != null){
                    saveImage(mPhotoFile,mPhotoFile2);
                }else{
                    if(mImageFile != null && mPhotoFile2 != null){
                        saveImage(mImageFile,mPhotoFile2);
                    }else{
                        if(mPhotoFile != null && mImageFile2 != null){
                            saveImage(mPhotoFile,mImageFile2);
                        }
                    }
                }
            }
        }else{
            Toast.makeText(this, "Ingresa todos los campos para publicar", Toast.LENGTH_LONG).show();
        }

    }

    private void saveImage(File imageFile1, File imageFile2) {
        mDialog.show();
        mImageProvider.save(PostActivity.this,imageFile1).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String url = uri.toString();

                            mImageProvider.save(PostActivity.this, imageFile2).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskImage2) {
                                    if(taskImage2.isSuccessful()){
                                        mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri2) {
                                                String url2 = uri2.toString();

                                                Post post = new Post();
                                                post.setImage1(url);
                                                post.setImage2(url2);
                                                post.setTitle(mTitle);
                                                post.setDescription(mDescrption);
                                                post.setIdUser(mAuthProvider.getUid());
                                                post.setTimestamp(new Date().getTime());
                                                mPostProvider.save(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> taskSave) {
                                                        mDialog.dismiss();
                                                        if(taskSave.isSuccessful()){
                                                            clearForm();
                                                            Toast.makeText(PostActivity.this, "Publicado", Toast.LENGTH_LONG).show();
                                                        }else{
                                                            Toast.makeText(PostActivity.this, "No se pudo hacer la publicación", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }else{
                                        mDialog.dismiss();
                                        Toast.makeText(PostActivity.this, "La imagen 2 no se pudo guardar", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    });
                }else{
                    mDialog.dismiss();
                    Toast.makeText(PostActivity.this, "Hubo un error al guardar la imagen", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void clearForm() {

        mTextInputTitle.setText("");
        mTextInputDescription.setText("");
        mImageViewPost1.setImageResource(R.drawable.subirimagen);
        mImageViewPost2.setImageResource(R.drawable.subirimagen);
        mTitle = "";
        mDescrption = "";
        mImageFile = null;
        mImageFile2 = null;

    }

    private void openGallery(int requestCode) {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Seleccion de imagen de la galeria
        if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){
            try{
                mPhotoFile = null;
                mImageFile = FileUtil.from(this,data.getData());
                mImageViewPost1.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            }catch (Exception e){
                Log.d("Error", "Se produjo un error" + e.getMessage());
                Toast.makeText(this, "No se ha podido cargar la imagen"+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        if(requestCode == GALLERY_REQUEST_CODE_2 && resultCode == RESULT_OK){
            try{
                mPhotoFile2 = null;
                mImageFile2 = FileUtil.from(this,data.getData());
                mImageViewPost2.setImageBitmap(BitmapFactory.decodeFile(mImageFile2.getAbsolutePath()));
            }catch (Exception e){
                Log.d("Error", "Se produjo un error" + e.getMessage());
                Toast.makeText(this, "No se ha podido cargar la imagen"+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        //Seleccion de tomar fotografia
        if(requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK){
            mImageFile = null;
            mPhotoFile = new File(mAbsolutePhotoPath);
            Picasso.with(PostActivity.this).load(mPhotoPath).into(mImageViewPost1);
        }

        //Seleccion de tomar fotografia en la 2
        if(requestCode == PHOTO_REQUEST_CODE2 && resultCode == RESULT_OK){
            mImageFile2 = null;
            mPhotoFile2 = new File(mAbsolutePhotoPath2);
            Picasso.with(PostActivity.this).load(mPhotoPath2).into(mImageViewPost2);
        }
    }
}