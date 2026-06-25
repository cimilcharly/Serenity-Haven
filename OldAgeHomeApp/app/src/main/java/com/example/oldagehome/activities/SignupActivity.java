package com.example.oldagehome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.oldagehome.R;
import com.example.oldagehome.databinding.ActivitySignupBinding;
import com.example.oldagehome.models.UserModel;
import com.example.oldagehome.utils.RoleManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private com.google.firebase.storage.StorageReference storageReference;
    private android.net.Uri imageUri;
    private GoogleSignInClient mGoogleSignInClient;
    private boolean isGoogleSignup = false;
    private String googlePhotoUrl = null;
    private boolean isInvited = false;
    private String invitedCommunityId = null;
    private String invitedCommunityName = null;

    private final androidx.activity.result.ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.ivProfile.setImageURI(imageUri);
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    com.google.android.gms.tasks.Task<GoogleSignInAccount> task =
                            GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        }
                    } catch (ApiException e) {
                        Toast.makeText(this, "Google sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = com.google.firebase.storage.FirebaseStorage.getInstance().getReference("profile_images");

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check if profile completion mode is requested
        if (getIntent().getBooleanExtra("complete_profile", false) && mAuth.getCurrentUser() != null) {
            setupGoogleSignupProfile(mAuth.getCurrentUser());
        }

        // Check if launched via an invitation deep-link
        handleInvitationIntent(getIntent());

        // Setup Role Spinner
        String[] roles = { RoleManager.ROLE_RESIDENT, RoleManager.ROLE_STAFF, RoleManager.ROLE_ME };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.roleSpinner.setAdapter(adapter);

        binding.roleSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedRole = roles[position];
                if (RoleManager.ROLE_RESIDENT.equals(selectedRole)) {
                    binding.communityInputLayout.setVisibility(View.VISIBLE);
                    binding.communityInputLayout.setHint("Community Join Code");
                } else if (RoleManager.ROLE_STAFF.equals(selectedRole)) {
                    binding.communityInputLayout.setVisibility(View.VISIBLE);
                    binding.communityInputLayout.setHint("Community Name");
                } else if (RoleManager.ROLE_ME.equals(selectedRole)) {
                    binding.communityInputLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        binding.ivProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImage.launch(intent);
        });

        binding.signupBtn.setOnClickListener(v -> registerUser());
        binding.googleSignupBtn.setOnClickListener(v -> signInWithGoogle());
        binding.loginTv.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }

    interface OnAuthUserCreatedListener {
        void onCreated(String uid);
    }

    private void createFirebaseAuthUser(OnAuthUserCreatedListener listener, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        listener.onCreated(mAuth.getCurrentUser().getUid());
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Registration Failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String generateJoinCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void registerUser() {
        String name = binding.nameEt.getText().toString().trim();
        String email = binding.emailEt.getText().toString().trim();
        String password = binding.passwordEt.getText().toString().trim();
        String role = binding.roleSpinner.getSelectedItem().toString();
        String communityInput = binding.communityEt.getText().toString().trim();

        boolean isMeRole = RoleManager.ROLE_ME.equals(role);
        if (name.isEmpty() || email.isEmpty() || (!isGoogleSignup && password.isEmpty()) || (!isMeRole && communityInput.isEmpty())) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        if (RoleManager.ROLE_RESIDENT.equals(role)) {
            if (isInvited && invitedCommunityId != null) {
                // Invited Resident: Bypass join code lookup and proceed directly to user creation
                if (isGoogleSignup && mAuth.getCurrentUser() != null) {
                    String uid = mAuth.getCurrentUser().getUid();
                    if (imageUri != null) {
                        uploadImage(uid, name, email, role, invitedCommunityId, invitedCommunityName, null);
                    } else {
                        saveUserToFirestore(uid, name, email, role, googlePhotoUrl, invitedCommunityId, invitedCommunityName, null);
                    }
                } else {
                    createFirebaseAuthUser(uid -> {
                        if (imageUri != null) {
                            uploadImage(uid, name, email, role, invitedCommunityId, invitedCommunityName, null);
                        } else {
                            saveUserToFirestore(uid, name, email, role, null, invitedCommunityId, invitedCommunityName, null);
                        }
                    }, email, password);
                }
            } else {
                // Validate join code first
                db.collection("communities")
                        .whereEqualTo("joinCode", communityInput.toUpperCase())
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                                com.google.firebase.firestore.DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                                String communityId = doc.getId();
                                String communityName = doc.getString("name");

                                if (isGoogleSignup && mAuth.getCurrentUser() != null) {
                                    String uid = mAuth.getCurrentUser().getUid();
                                    if (imageUri != null) {
                                        uploadImage(uid, name, email, role, communityId, communityName, null);
                                    } else {
                                        saveUserToFirestore(uid, name, email, role, googlePhotoUrl, communityId, communityName, null);
                                    }
                                } else {
                                    createFirebaseAuthUser(uid -> {
                                        if (imageUri != null) {
                                            uploadImage(uid, name, email, role, communityId, communityName, null);
                                        } else {
                                            saveUserToFirestore(uid, name, email, role, null, communityId, communityName, null);
                                        }
                                    }, email, password);
                                }
                            } else {
                                binding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Invalid Join Code. Please check with your staff.", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        } else if (RoleManager.ROLE_STAFF.equals(role)) {
            // Main Staff: Create new community
            String communityId = db.collection("communities").document().getId();
            String joinCode = generateJoinCode();

            if (isGoogleSignup && mAuth.getCurrentUser() != null) {
                String uid = mAuth.getCurrentUser().getUid();
                Map<String, Object> communityData = new java.util.HashMap<>();
                communityData.put("id", communityId);
                communityData.put("name", communityInput);
                communityData.put("creatorUid", uid);
                communityData.put("joinCode", joinCode);
                communityData.put("createdAt", System.currentTimeMillis());

                db.collection("communities").document(communityId).set(communityData)
                        .addOnSuccessListener(aVoid -> {
                            if (imageUri != null) {
                                uploadImage(uid, name, email, role, communityId, communityInput, joinCode);
                            } else {
                                saveUserToFirestore(uid, name, email, role, googlePhotoUrl, communityId, communityInput, joinCode);
                            }
                        })
                        .addOnFailureListener(e -> {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Failed to create community: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                createFirebaseAuthUser(uid -> {
                    Map<String, Object> communityData = new java.util.HashMap<>();
                    communityData.put("id", communityId);
                    communityData.put("name", communityInput);
                    communityData.put("creatorUid", uid);
                    communityData.put("joinCode", joinCode);
                    communityData.put("createdAt", System.currentTimeMillis());

                    db.collection("communities").document(communityId).set(communityData)
                            .addOnSuccessListener(aVoid -> {
                                if (imageUri != null) {
                                    uploadImage(uid, name, email, role, communityId, communityInput, joinCode);
                                } else {
                                    saveUserToFirestore(uid, name, email, role, null, communityId, communityInput, joinCode);
                                }
                            })
                            .addOnFailureListener(e -> {
                                binding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Failed to create community: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }, email, password);
            }
        } else if (RoleManager.ROLE_ME.equals(role)) {
            // Personal Use: Auto-create private workspace and approve immediately
            if (isGoogleSignup && mAuth.getCurrentUser() != null) {
                String uid = mAuth.getCurrentUser().getUid();
                String communityId = "personal_" + uid;
                String communityName = "Personal Space";
                if (imageUri != null) {
                    uploadImage(uid, name, email, role, communityId, communityName, null);
                } else {
                    saveUserToFirestore(uid, name, email, role, googlePhotoUrl, communityId, communityName, null);
                }
            } else {
                createFirebaseAuthUser(uid -> {
                    String communityId = "personal_" + uid;
                    String communityName = "Personal Space";
                    if (imageUri != null) {
                        uploadImage(uid, name, email, role, communityId, communityName, null);
                    } else {
                        saveUserToFirestore(uid, name, email, role, null, communityId, communityName, null);
                    }
                }, email, password);
            }
        }
    }

    private void uploadImage(String uid, String name, String email, String role, String communityId, String communityName, String joinCode) {
        com.google.firebase.storage.StorageReference fileRef = storageReference.child(uid + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveUserToFirestore(uid, name, email, role, uri.toString(), communityId, communityName, joinCode);
                })).addOnFailureListener(e -> {
                    Toast.makeText(this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    saveUserToFirestore(uid, name, email, role, null, communityId, communityName, joinCode);
                });
    }

    private void saveUserToFirestore(String uid, String name, String email, String role, String imageUrl,
            String communityId, String communityName, String joinCode) {
        UserModel user = new UserModel(uid, email, role, name);
        user.setCommunityName(communityName);
        user.setCommunityId(communityId);
        if (imageUrl != null) {
            user.setProfileImageUrl(imageUrl);
        }
        user.setStatus((RoleManager.ROLE_RESIDENT.equals(role) && !isInvited) ? "pending" : "approved");
        if (RoleManager.ROLE_RESIDENT.equals(role)) {
            user.setAdmissionDate(System.currentTimeMillis());
            if (isInvited) {
                user.setCreatedBy(RoleManager.ROLE_STAFF);
            } else {
                user.setCreatedBy(RoleManager.ROLE_RESIDENT);
            }
        }

        // 1. Separate "Global Directory" (used for login and finding the community)
        Map<String, Object> globalUser = new HashMap<>();
        globalUser.put("uid", uid);
        globalUser.put("name", name);
        globalUser.put("email", email);
        globalUser.put("role", role);
        globalUser.put("communityName", communityName);
        globalUser.put("communityId", communityId);
        globalUser.put("status", user.getStatus());
        if (imageUrl != null) {
            globalUser.put("profileImageUrl", imageUrl);
        }
        if (RoleManager.ROLE_RESIDENT.equals(role)) {
            globalUser.put("admissionDate", user.getAdmissionDate());
            globalUser.put("createdBy", user.getCreatedBy());
        }

        db.collection("users").document(uid).set(globalUser)
                .addOnSuccessListener(aVoid -> {
                    // 2. Separate "Community Vault" (Private home data)
                    db.collection("communities").document(communityId)
                            .collection("members").document(uid).set(user)
                            .addOnSuccessListener(aVoid2 -> {
                                binding.progressBar.setVisibility(View.GONE);
                                if (RoleManager.ROLE_RESIDENT.equals(role)) {
                                    if (isInvited) {
                                        Toast.makeText(this, "Joined " + communityName + " successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Join Request sent to " + communityName, Toast.LENGTH_SHORT).show();
                                    }
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                } else if (RoleManager.ROLE_ME.equals(role)) {
                                    Toast.makeText(this, "Personal account created successfully!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                } else {
                                    new androidx.appcompat.app.AlertDialog.Builder(this)
                                            .setTitle("Community Created!")
                                            .setMessage("Your community has been created successfully.\n\nShare this Join Code with residents:\n" + joinCode)
                                            .setPositiveButton("OK", (dialog, which) -> {
                                                startActivity(new Intent(this, LoginActivity.class));
                                                finish();
                                            })
                                            .setCancelable(false)
                                            .show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void setupGoogleSignupProfile(FirebaseUser user) {
        isGoogleSignup = true;
        binding.nameEt.setText(user.getDisplayName());
        binding.emailEt.setText(user.getEmail());
        binding.emailEt.setEnabled(false);
        binding.passwordInputLayout.setVisibility(View.GONE);
        binding.signupBtn.setText("Complete Registration");
        binding.googleSignupBtn.setVisibility(View.GONE);

        if (user.getPhotoUrl() != null) {
            com.bumptech.glide.Glide.with(this)
                    .load(user.getPhotoUrl())
                    .into(binding.ivProfile);
            googlePhotoUrl = user.getPhotoUrl().toString();
        }
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        binding.progressBar.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        db.collection("users").document(user.getUid()).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        binding.progressBar.setVisibility(View.GONE);
                                        Toast.makeText(this, "Account exists. Logging you in...", Toast.LENGTH_SHORT).show();
                                        String role = documentSnapshot.getString("role");
                                        String status = documentSnapshot.getString("status");
                                        if ("pending".equals(status)) {
                                            Toast.makeText(this, "Your registration is pending approval.", Toast.LENGTH_LONG).show();
                                            mAuth.signOut();
                                        } else if (status == null || "approved".equals(status)) {
                                            navigateToDashboard(role);
                                        }
                                    } else {
                                        binding.progressBar.setVisibility(View.GONE);
                                        setupGoogleSignupProfile(user);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    binding.progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Authentication Failed: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown Error"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleInvitationIntent(Intent intent) {
        if (intent != null && intent.getData() != null) {
            android.net.Uri data = intent.getData();
            String host = data.getHost();
            if ("invite".equals(host) || "cimilcharly.github.io".equals(host)) {
                invitedCommunityId = data.getQueryParameter("communityId");
                invitedCommunityName = data.getQueryParameter("communityName");
                if (invitedCommunityId != null && invitedCommunityName != null) {
                    isInvited = true;

                    // Hide community input field and pre-fill/disable it since it's already resolved
                    binding.communityInputLayout.setVisibility(View.GONE);
                    binding.communityEt.setText(invitedCommunityName);

                    // Hide role spinner to lock role to resident
                    binding.roleSpinner.setVisibility(View.GONE);

                    // Update Title to show they are joining the community
                    binding.signupTitle.setText("Join " + invitedCommunityName);

                    Toast.makeText(this, "Joining " + invitedCommunityName + " via invitation!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if (RoleManager.ROLE_STAFF.equals(role)) {
            intent = new Intent(this, MainStaffDashboardActivity.class);
        } else if (RoleManager.ROLE_RESIDENT.equals(role) || RoleManager.ROLE_ME.equals(role)) {
            intent = new Intent(this, ResidentDashboardActivity.class);
        } else {
            Toast.makeText(this, "Role not recognized: " + role, Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
        finish();
    }
}
