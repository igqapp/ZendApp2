package com.igqapp.zendapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.igqapp.zendapp.Adapter.FragmentAdapter;
import com.igqapp.zendapp.Entidades.Estado;
import com.igqapp.zendapp.Entidades.Usuario;
import com.igqapp.zendapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {
    /**
     ImageView img_foto;
     TextView tv_user,tv_mail;**/
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    DatabaseReference ref_user = database.getReference("Users").child(user.getUid());
    //PARA LAS SOLICITUDES
    DatabaseReference ref_solicitudes_count = database.getReference("Contador").child(user.getUid());
    //PARA EL ESTADO DEL USUARIO
    DatabaseReference ref_estado = database.getReference("Estado").child(user.getUid());



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //
        ViewPager2 viewPager2 = findViewById(R.id.viewpager);
        viewPager2.setAdapter(new FragmentAdapter(this));

        //Configuramos los tab para desplazar las pantallas.
        final TabLayout tabLayout= findViewById(R.id.tabLayout);
        TabLayoutMediator tabLayoutMediator=new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:{
                        tab.setText(R.string.users);
                        tab.setIcon(R.drawable.ic_usuarios);
                        break;
                    }
                    case 1:{
                        tab.setText(R.string.chats);
                        tab.setIcon(R.drawable.ic_chat);
                        break;
                    }
                    case 2:{
                        tab.setText(R.string.solicitudes);
                        tab.setIcon(R.drawable.ic_solicitudes);
                        //ICONO de nuevas notificaciones
                        final BadgeDrawable badgeDrawable = tab.getOrCreateBadge();
                        badgeDrawable.setBackgroundColor(
                                ContextCompat.getColor(getApplicationContext(),R.color.colorAccent)
                        );
                        badgeDrawable.setVisible(true);
                        //badgeDrawable.setNumber(1);
                        ref_solicitudes_count.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    Integer val = dataSnapshot.getValue(Integer.class);
                                    badgeDrawable.setVisible(true);
                                    if(val.equals("0")){
                                        badgeDrawable.setVisible(false);
                                    }
                                    else{
                                        badgeDrawable.setNumber(val);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        //
                        break;
                    }
                    case 3:{
                        tab.setText(R.string.missolicutdes);
                        tab.setIcon(R.drawable.ic_mis_solicitudes);
                        break;
                    }


                }
            }
        });
        tabLayoutMediator.attach();//LO LLAMAMOS UNA VEZ CREADO
        //TOMA LA CONFIGURACION PARA ELIMINAR EL ICONO DENTRO DE LA PESTAÑA
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                BadgeDrawable badgeDrawable = tabLayout.getTabAt(position).getOrCreateBadge();
                badgeDrawable.setVisible(false);

                if(position == 2){

                    countacero();
                }
            }

        });

        //USUARIO
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        /**VARIABLES PARA OBTENER FOTO Y DATOS USUARIO
         img_foto = findViewById(R.id.imagen_user);
         tv_user = findViewById(R.id.tv_usuario);
         tv_mail = findViewById(R.id.tv_mail);
         //Para mostrar la foto
         Glide.with(this).load(user.getPhotoUrl()).into(img_foto);
         tv_user.setText(user.getDisplayName());
         tv_mail.setText(user.getEmail());
         **/

        userUnico();


    }//ONCREATE FIN


    private void countacero() {
        ref_solicitudes_count.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ref_solicitudes_count.setValue(0);
                    Toast.makeText(HomeActivity.this, "Count badge a 0", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void estadousuario(final String estado){
        ref_estado.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Estado est = new Estado(estado,"","","");
                ref_estado.setValue(est);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        estadousuario("conectado");
    }

    @Override
    protected void onPause() {
        super.onPause();
        estadousuario("desconectado");
        ultimafecha();
    }

    private void ultimafecha(){
        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        ref_estado.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ref_estado.child("fecha").setValue(dateFormat.format(c.getTime()));
                ref_estado.child("hora").setValue(timeformat.format(c.getTime()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void userUnico() {
        ref_user.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    Usuario uu = new Usuario(
                            user.getUid(),
                            user.getDisplayName(),
                            user.getEmail(),
                            user.getPhotoUrl().toString(),
                            "desconectado",
                            "00/00/00",
                            "12:00",
                            0,
                            0);
                    ref_user.setValue(uu);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_activity,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_cerrar : //OPCION CERRAR SESION DEL MENU
                AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(HomeActivity.this, "cerrar sesion", Toast.LENGTH_SHORT).show();
                        finish();
                        gologin();
                    }
                });
        }
        return super.onOptionsItemSelected(item);
    }



    private void gologin(){
        //TE MANDA A LA ACTIVITY LOGIN
        Intent i = new Intent(this,LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}