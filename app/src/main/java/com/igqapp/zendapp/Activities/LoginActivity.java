package com.igqapp.zendapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.igqapp.zendapp.R;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mfirebaseAuth;
    private FirebaseAuth.AuthStateListener mautListener;
    public static final int SING_IN = 1;

    //LISTA DE PROVEEDORES DE GOOGLE
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.GoogleBuilder().build()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SplashTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //INICIALIZAR VARIABLES
        mfirebaseAuth = FirebaseAuth.getInstance();
        mautListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();//OBTENEMOS LA INFORMACION DEL USUARIO
                if(user!=null){
                    gohome();//IR AL ACTIVITY HOME
                }else {
                    startActivityForResult(
                            AuthUI.getInstance().
                                    createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .setIsSmartLockEnabled(false)
                                    .build(),SING_IN
                    );


                }
            }
        };
    }//ONCREATE END

    @Override
    protected void onResume() {
        super.onResume();
        //PARA CUANDO SE ABRA LA APP NOS HAGA DIRECTAMENTE LOGIN
        mfirebaseAuth.addAuthStateListener(mautListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mfirebaseAuth.removeAuthStateListener(mautListener);
    }

    private void gohome(){
        //TE MANDA A LA ACTIVITY HOME
        Intent i = new Intent(this,HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

    }


}