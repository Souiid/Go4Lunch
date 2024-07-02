package com.idrisssouissi.go4lunch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.idrisssouissi.go4lunch.databinding.ActivityMainBinding;

import java.util.Arrays;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;


public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;


    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Glide.with(this)
                .load(R.drawable.pic)
                .transform(new CenterCrop(), new BlurTransformation(25))
                .into(binding.imageView);

        signIn();
    }

    private void signIn() {
        // Choisissez les providers pour l'authentification
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build()
        );

        // Lancez l'activité de connexion
        // Lancez l'activité de connexion
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Connexion réussie
                Log.d("aaa", "Result ok ");

               // FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                // Vous pouvez maintenant accéder à l'utilisateur connecté
            } else {
                Log.d("aaa", "Result failed ");
                if (response == null) {
                    Log.d("aaa", "response null ");
                    return;
                }
                // Affichez les erreurs de connexion
            }
        }
    }
}