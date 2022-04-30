package com.lhester.polendey.trikila;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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

import java.util.HashMap;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class settings2 extends AppCompatActivity {
    private CircleImageView profileImage;
    private EditText ln, fn, phone, address, UID;
    private Spinner gender;
    private ImageView closeButton, saveButton;
    private TextView changePorfile, changeEmail;
    private String checker = "";
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private Uri imageUri;
    private String myurl = "";
    private StorageTask uploadTask;
    private StorageReference storageProfilePicRef;
    RatingBar ratingBar;
    ImageView userpic;
    private static final int GalleryPick = 1;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;
    String cameraPermission[];
    String storagePermission[];
    Uri imageuri;

    TextView click;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings2);

        ratingBar=findViewById(R.id.ratingBar);
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        check_permision();
        storageProfilePicRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures");
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");
        profileImage = findViewById(R.id.profile_image);
        changeEmail = findViewById(R.id.change_email);
        profileImage.setOnClickListener(v -> {
            checker = "clicked";
            crop_image();
        });
        changeEmail.setOnClickListener(v -> {
            change_email();
        });

        ln = findViewById(R.id.lname);
        fn = findViewById(R.id.fname);
        address = findViewById(R.id.address);
        UID = findViewById(R.id.uid);
        gender = findViewById(R.id.gender);

        // Create an ArrayAdapter using the string array and a default spinner
        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                .createFromResource(settings2.this, R.array.gender,
                        R.layout.spinner_gender);
        // Specify the layout to use when the list of choices appears
        staticAdapter
                .setDropDownViewResource(R.layout.spinner_drop_down);
        // Apply the adapter to the spinner
        gender.setAdapter(staticAdapter);

        phone = findViewById(R.id.phone_number);

        closeButton = findViewById(R.id.close_button);
        saveButton = findViewById(R.id.save_button);
        changePorfile = findViewById(R.id.change_picture_button);
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
                //crop_image();
                //   check_permision();
                //crop_image();

            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //  String url = "gs://trikila-laoag.appspot.com/ala.png";

        // FirebaseStorage storage = FirebaseStorage.getInstance();
        //  StorageReference ref = storage.getReferenceFromUrl(url);


        getUserInformation();


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
                .getIntent(settings2.this);
        startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    private void check_permision() {
        if (ActivityCompat.checkSelfPermission(settings2.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(settings2.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(settings2
                    .this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ActivityCompat.checkSelfPermission(settings2.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(settings2.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(settings2
                    .this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
        if (ActivityCompat.checkSelfPermission(settings2.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(settings2.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(settings2
                    .this, new String[]{Manifest.permission.CAMERA}, 4);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // check whether storage permission granted or not.
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // do what you want;
                    }
                }
                break;
            case 2:
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // check whether storage permission granted or not.
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // do what you want;
                    }
                }
            case 4:
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.CAMERA)) {
                    // check whether storage permission granted or not.
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // do what you want;
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

   /*     if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            imageUri = result.getUri();
            profileImage.setImageURI(imageUri);
        }
        else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Toast.makeText(settings2.this, result.getError().toString(), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(settings2.this, "Error Try Again", Toast.LENGTH_SHORT).show();

            finish();

        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profileImage.setImageBitmap(imageBitmap);
        } */


        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            Uri tempUri = data.getData();
            CropImage.activity(tempUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
            Log.e("URI", imageUri.toString());
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            //Uri Of Cropped Image:
            imageUri = result.getUri();
            profileImage.setImageURI(imageUri);
            Log.e("URI", imageUri.toString());
        }
    }

    private void validatecontrolers() {
        if (TextUtils.isEmpty(ln.getText().toString())) {
            Toast.makeText(settings2.this, "Please Enter your name..", Toast.LENGTH_SHORT).show();
            ln.requestFocus();
        } else if (TextUtils.isEmpty(phone.getText().toString())) {
            Toast.makeText(settings2.this, "Please Enter Phone Number..", Toast.LENGTH_SHORT).show();
            phone.requestFocus();
        } else if (checker.equals("clicked")) {
            uploadImage();
            //uploadFile(imageUri.toString());
        }
    }

    private StorageReference fileRef;

    private void validateandsaveonly() {
        if (TextUtils.isEmpty(ln.getText().toString())) {
            Toast.makeText(settings2.this, "Please Enter your name..", Toast.LENGTH_SHORT).show();
            ln.requestFocus();
        } else if (TextUtils.isEmpty(phone.getText().toString())) {
            Toast.makeText(settings2.this, "Please Enter Phone Number..", Toast.LENGTH_SHORT).show();
            phone.requestFocus();
        } else {
            String gen = gender.getSelectedItem().toString();
            final ProgressDialog progressDialog = new ProgressDialog(settings2.this);
            progressDialog.setTitle("Setting Accounts Info");
            progressDialog.setMessage("Please wait...saving your account..");
            progressDialog.show();
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("cid", mAuth.getCurrentUser().getUid());
            userMap.put("last_name", ln.getText().toString());
            userMap.put("first_name", fn.getText().toString());
            userMap.put("address", address.getText().toString());
            userMap.put("gender", gen);
            userMap.put("phone", phone.getText().toString());
            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
            progressDialog.dismiss();
            //  getActivity().finish();
            Toast.makeText(settings2.this, "Updated", Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserInformation() {
        databaseReference.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 2) {
                    if (dataSnapshot.hasChild("cid")) {
                        String uid = dataSnapshot.child("cid").getValue().toString();
                        UID.setText(uid);
                    }
                    if (dataSnapshot.hasChild("last_name")) {
                        String _laname = dataSnapshot.child("last_name").getValue().toString();
                        ln.setText(_laname);
                    }
                    if (dataSnapshot.hasChild("first_name")) {
                        String _fname = dataSnapshot.child("first_name").getValue().toString();
                        fn.setText(_fname);
                    }
                    if (dataSnapshot.hasChild("address")) {
                        String _address = dataSnapshot.child("address").getValue().toString();
                        address.setText(_address);
                    }
                    if (dataSnapshot.hasChild("gender")) {
                        String _gender = dataSnapshot.child("gender").getValue().toString();
                        if (_gender.equals("Male")) {
                            gender.setSelection(0, true);
                        } else {
                            gender.setSelection(1, true);
                        }
                        // gender.setPrompt(_gender);

                    }
                    if (dataSnapshot.hasChild("phone")) {
                        String phone_num = dataSnapshot.child("phone").getValue().toString();
                        phone.setText(phone_num);
                    }
                    if(dataSnapshot.hasChild("rating")) {
                        float rbar = Float.parseFloat(dataSnapshot.child("rating").getValue().toString());
                        ratingBar.setRating(rbar);
                    }

                    if (dataSnapshot.hasChild("image")) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        //  Glide.with(settings2.this).load(image).p.into(profileImage);
                        Picasso.get().load(image)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_error)
                                .into(profileImage);
                        Log.e("image", image);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //  @Override
   /* public boolean onKeyDown(int keyCode, KeyEvent event) {

      if (keyCode == KeyEvent.KEYCODE_BACK) {
        //    Toast.makeText(CustomerMapActivity.this,"Please Cancel The Ride First",Toast.LENGTH_LONG);
        return false;
      }
        return super.onKeyDown(keyCode, event);
    } */
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
                Toast.makeText(settings2.this, "Uploaded", Toast.LENGTH_SHORT).show();

                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());

                        }
                        return ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downUri = task.getResult();
                            Log.d("Final URL", "onComplete: Url: " + downUri.toString());
                            myurl = downUri.toString();
                            String gen = gender.getSelectedItem().toString();
                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("cid", mAuth.getCurrentUser().getUid());
                            userMap.put("last_name", ln.getText().toString());
                            userMap.put("first_name", fn.getText().toString());
                            userMap.put("address", address.getText().toString());
                            userMap.put("gender", gen);
                            userMap.put("phone", phone.getText().toString());
                            userMap.put("image", myurl);
                            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(userMap);
                            progressDialog.dismiss();
                        }
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double current_progress = (snapshot.getBytesTransferred() * 100) / snapshot.getTotalByteCount();
                progressDialog.setMessage("Uploading " + String.format("%.2f", current_progress) + "%");

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(settings2.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                .setConfirmClickListener(sDialog -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String newPassword = cancel_reaon.getText().toString();

                    user.updatePassword(newPassword)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {

                                    new SweetAlertDialog(settings2.this, SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Update Password")
                                            .setContentText("Password Updated successful")
                                            .setConfirmText("  OK  ")
                                            .setConfirmClickListener(sDialog1 -> sDialog1.dismissWithAnimation())
                                            .show();
                                } else {

                                    new SweetAlertDialog(settings2.this, SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText("Update Password")
                                            .setContentText("Update Password Failed")
                                            .setConfirmText("  OK  ")
                                            .setConfirmClickListener(sDialog1 -> sDialog1.dismissWithAnimation())
                                            .show();
                                }
                            });


                    sDialog.dismissWithAnimation();
                })
                .setCancelButton("Cancel", sDialog -> sDialog.dismissWithAnimation())
                .show();
    }

    private EditText change_emails;

    private void change_email() {
        change_emails = new EditText(this);
        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Change Email Address")
                .setConfirmText("Update")
                .setCustomView(change_emails)
                .setConfirmClickListener(sDialog -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String newEmail = change_emails.getText().toString();

                    user.updateEmail(newEmail)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {

                                    new SweetAlertDialog(settings2.this, SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Update Email Address")
                                            .setContentText("Email Address Updated successful")
                                            .setConfirmText("  OK  ")
                                            .setConfirmClickListener(sDialog1 -> sDialog1.dismissWithAnimation())
                                            .show();
                                } else {

                                    new SweetAlertDialog(settings2.this, SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText("Update Email Address")
                                            .setContentText("Email Address Password Failed")
                                            .setConfirmText("  OK  ")
                                            .setConfirmClickListener(sDialog1 -> sDialog1.dismissWithAnimation())
                                            .show();
                                }
                            });


                    sDialog.dismissWithAnimation();
                })
                .setCancelButton("Cancel", sDialog -> sDialog.dismissWithAnimation())
                .show();

    }

}