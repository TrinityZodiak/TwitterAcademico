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
import com.example.twitteracademico.models.User;
import com.example.twitteracademico.providers.AuthProvider;
import com.example.twitteracademico.providers.ImageProvider;
import com.example.twitteracademico.providers.UsersProvider;
import com.example.twitteracademico.utils.FileUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class EditProfileActivity extends AppCompatActivity {

    CircleImageView mCircleImageViewBack;
    CircleImageView mCircleImageViewProfile;
    ImageView mImageViewCover;
    TextInputEditText mTextInputUsername;
    TextInputEditText mTextInputPhone;
    Button mButtonEditProfile;
    String mUsername = "";
    String mPhone = "";
    String mImageProfile = "";
    String mImageCover = "";

    ImageProvider mImageProvider;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;

    private final int GALLERY_REQUEST_CODE_PROFILE = 1;
    private final int GALLERY_REQUEST_CODE_COVER = 2;
    private final int PHOTO_REQUEST_CODE_PROFILE = 3;
    private final int PHOTO_REQUEST_CODE_COVER = 4;

    AlertDialog mDialog;
    AlertDialog.Builder mBuilderSelector;
    CharSequence options[];

    File mImageFile;
    File mImageFile2;

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
        setContentView(R.layout.activity_edit_profile);

        mCircleImageViewBack = findViewById(R.id.circleImageBack);
        mCircleImageViewProfile = findViewById(R.id.circleImageProfile);
        mImageViewCover = findViewById(R.id.imageViewCover);
        mTextInputUsername = findViewById(R.id.textInputUsername);
        mTextInputPhone = findViewById(R.id.textInputPhone);
        mButtonEditProfile = findViewById(R.id.btnEditProfile);

        mBuilderSelector = new AlertDialog.Builder(this);
        mBuilderSelector.setTitle("Selecciona una opción");
        options = new CharSequence[]{"Galeria","Tomar foto"};
        mImageProvider = new ImageProvider();
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Espere un momento...")
                .setCancelable(false).build();

        mButtonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickEditProfile();
            }
        });

        mImageViewCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectOptionImage(2);
            }
        });

        mCircleImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectOptionImage(1);
            }
        });

        mCircleImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getUser();
    }

    private void getUser() {
        mUsersProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("username")) {
                        mUsername = documentSnapshot.getString("username");
                        mTextInputUsername.setText(mUsername);
                    }
                    if (documentSnapshot.contains("phone")) {
                        mPhone = documentSnapshot.getString("phone");
                        mTextInputPhone.setText(mPhone);
                    }
                    if (documentSnapshot.contains("image_profile")) {
                        mImageProfile = documentSnapshot.getString("image_profile");
                        if (mImageProfile != null) {
                            if (!mImageProfile.isEmpty()) {
                                Picasso.with(EditProfileActivity.this).load(mImageProfile).into(mCircleImageViewProfile);
                            }
                        }
                    }
                    if (documentSnapshot.contains("image_cover")) {
                        mImageCover = documentSnapshot.getString("image_cover");
                        if (mImageCover != null) {
                            if (!mImageCover.isEmpty()) {
                                Picasso.with(EditProfileActivity.this).load(mImageCover).into(mImageViewCover);
                            }
                        }
                    }
                }
            }
        });
    }

    private void clickEditProfile() {
         mUsername = mTextInputUsername.getText().toString();
         mPhone = mTextInputPhone.getText().toString();
         if (!mUsername.isEmpty() && !mPhone.isEmpty()) {
             //Seleccion de ambas imagenes de la galeria
             if(mImageFile != null && mImageFile2 != null){
                 saveImageCoverAndProfile(mImageFile,mImageFile2);
                 //Slecciono las 2 fotos desde la camara
             }else if(mPhotoFile != null && mPhotoFile2 != null){
                     saveImageCoverAndProfile(mPhotoFile,mPhotoFile2);
                 }else if(mImageFile != null && mPhotoFile2 != null){
                         saveImageCoverAndProfile(mImageFile,mPhotoFile2);
                     }else if(mPhotoFile != null && mImageFile2 != null){
                             saveImageCoverAndProfile(mPhotoFile,mImageFile2);
                         } else if (mPhotoFile != null) {
                 saveImage(mPhotoFile, true);
             } else if (mPhotoFile2 != null) {
                 saveImage(mPhotoFile2, false);
             } else if (mImageFile != null) {
                 saveImage(mImageFile, true);
             } else if (mImageFile2 != null) {
                 saveImage(mImageFile2, false);
             }
             else {
                 User user = new User();
                 user.setUsername(mUsername);
                 user.setPhone(mPhone);
                 user.setId(mAuthProvider.getUid());
                 updateInfo(user);
             }
         } else {
             Toast.makeText(this, "Ingrese el nombre de usuario y el teléfono", Toast.LENGTH_SHORT).show();
         }
    }

    private void saveImageCoverAndProfile(File imageFile1, File imageFile2) {
        mDialog.show();
        mImageProvider.save(EditProfileActivity.this,imageFile1).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String urlProfile = uri.toString();

                            mImageProvider.save(EditProfileActivity.this, imageFile2).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskImage2) {
                                    if(taskImage2.isSuccessful()){
                                        mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri2) {
                                                String urlCover = uri2.toString();
                                                User user = new User();
                                                user.setUsername(mUsername);
                                                user.setPhone(mPhone);
                                                user.setImageProfile(urlProfile);
                                                user.setImageCover(urlCover);
                                                user.setId(mAuthProvider.getUid());
                                                updateInfo(user);
                                            }
                                        });
                                    }else{
                                        mDialog.dismiss();
                                        Toast.makeText(EditProfileActivity.this, "La imagen 2 no se pudo guardar", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    });
                }else{
                    mDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Hubo un error al guardar la imagen", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void saveImage(File image, boolean isProfileImage) {
        mDialog.show();
        mImageProvider.save(EditProfileActivity.this,image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String url = uri.toString();
                            User user = new User();
                            user.setUsername(mUsername);
                            user.setPhone(mPhone);
                            if (isProfileImage) {
                                user.setImageProfile(url);
                                user.setImageCover(mImageCover);
                            } else {
                                user.setImageCover(url);
                                user.setImageProfile(mImageProfile);
                            }
                            user.setId(mAuthProvider.getUid());
                            updateInfo(user);
                        }
                    });
                }else{
                    mDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Hubo un error al guardar la imagen", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateInfo(User user) {
        if (mDialog.isShowing()) {
            mDialog.show();
        }
        mUsersProvider.update(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "La información ha sido actualizada correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditProfileActivity.this, "La información no se pudo actualizar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void selectOptionImage(final int numberImage) {
        mBuilderSelector.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    if(numberImage == 1){
                        openGallery(GALLERY_REQUEST_CODE_PROFILE);
                    }else{
                        if(numberImage == 2){
                            openGallery(GALLERY_REQUEST_CODE_COVER);
                        }
                    }
                }else{
                    if(i == 1){
                        if(numberImage == 1){
                            takePhoto(PHOTO_REQUEST_CODE_PROFILE);
                        }else{
                            if(numberImage == 2){
                                takePhoto(PHOTO_REQUEST_CODE_COVER);
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
                Uri photoUri = FileProvider.getUriForFile(EditProfileActivity.this,"com.example.twitteracademico",photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                startActivityForResult(takePictureIntent,requestCode);
            }
        }
    }

    private File createPhotoFile(int requestCode) throws IOException {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File photoFile = File.createTempFile(new Date()+"_photo",".jpg",storageDir);

        if(requestCode == PHOTO_REQUEST_CODE_PROFILE){
            mPhotoPath = "file:" + photoFile.getAbsolutePath();
            mAbsolutePhotoPath = photoFile.getAbsolutePath();
        }else{
            if(requestCode == PHOTO_REQUEST_CODE_COVER){
                mPhotoPath2 = "file:" + photoFile.getAbsolutePath();
                mAbsolutePhotoPath2 = photoFile.getAbsolutePath();
            }
        }
        return photoFile;
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
        if(requestCode == GALLERY_REQUEST_CODE_PROFILE && resultCode == RESULT_OK){
            try{
                mPhotoFile = null;
                mImageFile = FileUtil.from(this,data.getData());
                mCircleImageViewProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            }catch (Exception e){
                Log.d("Error", "Se produjo un error" + e.getMessage());
                Toast.makeText(this, "No se ha podido cargar la imagen"+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        if(requestCode == GALLERY_REQUEST_CODE_COVER && resultCode == RESULT_OK){
            try{
                mPhotoFile2 = null;
                mImageFile2 = FileUtil.from(this,data.getData());
                mImageViewCover.setImageBitmap(BitmapFactory.decodeFile(mImageFile2.getAbsolutePath()));
            }catch (Exception e){
                Log.d("Error", "Se produjo un error" + e.getMessage());
                Toast.makeText(this, "No se ha podido cargar la imagen"+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        //Seleccion de tomar fotografia
        if(requestCode == PHOTO_REQUEST_CODE_PROFILE && resultCode == RESULT_OK){
            mImageFile = null;
            mPhotoFile = new File(mAbsolutePhotoPath);
            Picasso.with(EditProfileActivity.this).load(mPhotoPath).into(mCircleImageViewProfile);
        }

        //Seleccion de tomar fotografia en la 2
        if(requestCode == PHOTO_REQUEST_CODE_COVER && resultCode == RESULT_OK){
            mImageFile2 = null;
            mPhotoFile2 = new File(mAbsolutePhotoPath2);
            Picasso.with(EditProfileActivity.this).load(mPhotoPath2).into(mImageViewCover);
        }
    }

}