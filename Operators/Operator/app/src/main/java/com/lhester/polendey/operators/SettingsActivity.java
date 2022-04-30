package com.lhester.polendey.operators;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.audiofx.EnvironmentalReverb;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private CircleImageView profileImage;
    private EditText nameEditext, phoneEditxt, plate_noEditext, address, ln, mn;
    private ImageView closeButton, saveButton;
    private TextView changePorfile, sendV;
    private String checker = "";

    private Uri imageUri;
    private String myurl = "";
    private StorageTask uploadTask;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageProfilePicRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        storageProfilePicRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures");
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Operators");
        profileImage = findViewById(R.id.profile_image);

        nameEditext = findViewById(R.id.fname);
        ln = findViewById(R.id.lastname);
        address = findViewById(R.id.address);
        mn = findViewById(R.id.mname);
        phoneEditxt = findViewById(R.id.phone_number);
        //   plate_noEditext=findViewById(R.id.driver_plate_no);

        closeButton = findViewById(R.id.close_button);
        saveButton = findViewById(R.id.save_button);
        changePorfile = findViewById(R.id.change_picture_button);
        sendV = findViewById(R.id.verify);
        sendV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //    sendVerificationEmail();
                change_email();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "clicked";
                crop_image2();
            }
        });
//        plate_noEditext.setVisibility(View.VISIBLE);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checker.equals("clicked")) {
                    validatecontrolers();
                } else {
                    validateandsaveonly();
                }
            }
        });
        changePorfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                change_password();
                //  crop_image();
                //     pic_image();
            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //    RequestManager requestManager = Glide.with(SettingsActivity.this);
// if you need transformations or other options specific for the load, chain them here
        //   RequestBuilder<Drawable> request;

        //       String URL_="https://firebasestorage.googleapis.com/v0/b/trikila-laoag.appspot.com/o/Profile%20Pictures%2FB9RffzVh5XTc8nOjU2TGPJbC7CJ3?alt=media&token=9cc8fc64-65fe-41ca-9f36-112771b307c1";
// if you need transformations or other options specific for the load, chain them here

        //request.into(profileImage);
        getUserInformation();
        //  checkIfEmailVerified();

    }

    private int REQ_CODE = 5;
    Uri uri;

    private void crop_image2() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setDataAndType(uri, "image/*");
        startActivityForResult(intent, REQ_CODE);
    }

    private void crop_image() {
        Intent intent = CropImage.activity()
                .setAspectRatio(1, 1)
                .getIntent(SettingsActivity.this);
        startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
      /*  if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                imageUri = result.getUri();
                profileImage.setImageURI(imageUri);
                Log.e("URI",imageUri.toString());
            }

        }
        else {
            finish();
            Toast.makeText(this, "Error Try Again", Toast.LENGTH_SHORT).show();
        } */
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            Uri tempUri = data.getData();
            CropImage.activity(tempUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
            Log.d("TAG", "MUR: result worked");
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            //Uri Of Cropped Image:
            imageUri = result.getUri();

            profileImage.setImageURI(imageUri);
         /*   try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "cropped", "cropped");

            } catch (IOException e) {
                e.printStackTrace();
            } */

        }

    }

    private void validatecontrolers() {
        if (TextUtils.isEmpty(nameEditext.getText().toString())) {
            Toast.makeText(this, "Please Enter your name..", Toast.LENGTH_SHORT).show();
            nameEditext.requestFocus();
        } else if (TextUtils.isEmpty(phoneEditxt.getText().toString())) {
            Toast.makeText(this, "Please Enter Phone Number..", Toast.LENGTH_SHORT).show();
            phoneEditxt.requestFocus();
        }
        //    else if( TextUtils.isEmpty(plate_noEditext.getText().toString())) {
        //          Toast.makeText(this, "Please Enter Plate Number..", Toast.LENGTH_SHORT).show();
        //        plate_noEditext.requestFocus();
        //     }
        else if (checker.equals("clicked")) {
            //   uploadprofilepicture();
            uploadImage();
        }

    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Setting Accounts Info");
        progressDialog.setMessage("Please wait...saving your account..");
        progressDialog.show();
        final StorageReference ref = storageProfilePicRef.child(mAuth.getCurrentUser().getUid() + ".jpg");

        final UploadTask uploadTask = ref.putFile(imageUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(SettingsActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadurl = (Uri) task.getResult();
                            myurl = downloadurl.toString();
                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("uid", mAuth.getCurrentUser().getUid());
                            userMap.put("firstname", nameEditext.getText().toString());
                            userMap.put("phone", phoneEditxt.getText().toString());
                            userMap.put("lastname", ln.getText().toString());
                            userMap.put("middlename", mn.getText().toString());
                            userMap.put("address", address.getText().toString());
                            userMap.put("image", myurl);
                            //     userMap.put("plate_no",plate_noEditext.getText().toString());
                            //new Async().execute();
                            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
                            progressDialog.dismiss();
                            //  finish();
                        }
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double prencetage_ = (snapshot.getBytesTransferred() * 100.0) / snapshot.getTotalByteCount();
                progressDialog.setMessage("Uploading " + String.format("%.2f", prencetage_) + "%");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void validateandsaveonly() {
        if (TextUtils.isEmpty(nameEditext.getText().toString())) {
            Toast.makeText(this, "Please Enter your name..", Toast.LENGTH_SHORT).show();
            nameEditext.requestFocus();
        } else if (TextUtils.isEmpty(phoneEditxt.getText().toString())) {
            Toast.makeText(this, "Please Enter Phone Number..", Toast.LENGTH_SHORT).show();
            phoneEditxt.requestFocus();
        }


        //     else if(TextUtils.isEmpty(plate_noEditext.getText().toString())){
        //       Toast.makeText(this, "Please Enter Plate Number..",Toast.LENGTH_SHORT).show();
        //          plate_noEditext.requestFocus();
        //     }
        else {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Setting Accounts Info");
            progressDialog.setMessage("Please wait...saving your account..");
            progressDialog.show();
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("uid", mAuth.getCurrentUser().getUid());
            userMap.put("firstname", nameEditext.getText().toString());
            userMap.put("phone", phoneEditxt.getText().toString());
            userMap.put("lastname", ln.getText().toString());
            userMap.put("middlename", mn.getText().toString());
            userMap.put("address", address.getText().toString());
            //   userMap.put("plate_no",plate_noEditext.getText().toString());
            //   new Async().execute();
            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
            progressDialog.dismiss();
            finish();
        }
    }

    private void getUserInformation() {
        databaseReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 2) {
                    if (dataSnapshot.hasChild("firstname")) {
                        String fullname = dataSnapshot.child("firstname").getValue().toString();
                        nameEditext.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("lastname")) {
                        String lastname = dataSnapshot.child("lastname").getValue().toString();
                        ln.setText(lastname);
                    }
                    if (dataSnapshot.hasChild("middlename")) {
                        String middlename = dataSnapshot.child("middlename").getValue().toString();
                        mn.setText(middlename);
                    }
                    if (dataSnapshot.hasChild("address")) {
                        String ad = dataSnapshot.child("address").getValue().toString();
                        address.setText(ad);
                    }
                    if (dataSnapshot.hasChild("phone")) {
                        String phone = dataSnapshot.child("phone").getValue().toString();
                        phoneEditxt.setText(phone);
                    }


                    // //       if(dataSnapshot.hasChild("plate_no")) {
                    //         String plate = dataSnapshot.child("plate_no").getValue().toString();
                    //          plate_noEditext.setText(plate);
                    //  }

                    if (dataSnapshot.hasChild("image")) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_error)
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private EditText cancel_reaon;

    private void change_password() {
        cancel_reaon = new EditText(this);
        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Change Password")
                .setConfirmText("Update")
                .setCustomView(cancel_reaon)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String newPassword = cancel_reaon.getText().toString();

                        user.updatePassword(newPassword)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            new SweetAlertDialog(SettingsActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                                    .setTitleText("Update Password")
                                                    .setContentText("Password Updated successful")
                                                    .setConfirmText("  OK  ")
                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                            sweetAlertDialog.dismiss();
                                                        }
                                                    })
                                                    .show();
                                        } else {

                                            new SweetAlertDialog(SettingsActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                    .setTitleText("Update Password")
                                                    .setContentText("Update Password Failed")
                                                    .setConfirmText("  OK  ")
                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                            sweetAlertDialog.dismiss();
                                                        }
                                                    })
                                                    .show();
                                        }
                                    }
                                });
                        sweetAlertDialog.dismissWithAnimation();
                    }

                })
                .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                })
                .show();
    }

    private EditText change_emails;

    private void change_email() {
        change_emails = new EditText(this);
        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Change Email Address")
                .setConfirmText("Update")
                .setCustomView(change_emails)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String newEmail = change_emails.getText().toString();

                        user.updateEmail(newEmail)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            new SweetAlertDialog(SettingsActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                                    .setTitleText("Update Email Address")
                                                    .setContentText("Email Address Updated successful")
                                                    .setConfirmText("  OK  ")
                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                            sweetAlertDialog.dismissWithAnimation();
                                                        }
                                                    })
                                                    .show();
                                        } else {

                                            new SweetAlertDialog(SettingsActivity.this, SweetAlertDialog.ERROR_TYPE)
                                                    .setTitleText("Update Email Address")
                                                    .setContentText("Email Address Password Failed")
                                                    .setConfirmText("  OK  ")
                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                            sweetAlertDialog.dismissWithAnimation();
                                                        }
                                                    })
                                                    .show();
                                        }
                                    }
                                });
                        sweetAlertDialog.dismissWithAnimation();
                    }
                })
                .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                })
                .show();

    }
}