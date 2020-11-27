package com.igqapp.zendapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.igqapp.zendapp.Adapter.ChatAdapter;
import com.igqapp.zendapp.Entidades.Chats;
import com.igqapp.zendapp.Entidades.Estado;
import com.igqapp.zendapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class MensajesActivity extends AppCompatActivity {
    private  CircleImageView img_user_c, img_user_d;
    private TextView username;
    private  SharedPreferences mPref;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref_estado= database.getReference("Estado").child(user.getUid());
    private DatabaseReference ref_chat= database.getReference("Chats");
    //FLOAT BUTTON
    private FloatingActionsMenu floatMenu;
    private FloatingActionButton btn_galeria;
    //ELEMENTOS LAYOUT
    private EditText txt_mensaje;
    private FloatingActionButton btn_enviar;
    //ID CHAT GLOBAL
    private String id_chat_global;
    private Boolean online = false;
    //RV
    private RecyclerView rv_chats;
    private ChatAdapter adapter;
    private ArrayList<Chats> chatList;
    //IMAGEN
    String imgurl;
    private static final int PHOTO_SEND = 1;//PARA el request code para saber que se envió la foto
    private FirebaseStorage storageImg;
    private StorageReference storageReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajes);
        //ADMINISTRACION DEL TOOLBAR
        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //INICIALIZAMOS LAS VARIABLES
        mPref = getApplicationContext().getSharedPreferences("usuario_sp",MODE_PRIVATE);
        img_user_c = findViewById(R.id.img_usuario_c);//USUARIO CONECTADO
        img_user_d = findViewById(R.id.img_usuario_d);//USUARIO DESCONECTADO
        username = findViewById(R.id.tv_user);
        floatMenu =  findViewById(R.id.menu);
        btn_galeria =  findViewById(R.id.btn_galeria);
        //
        //INICIALIZAR LA BASE DE IMAGENES
        storageImg = FirebaseStorage.getInstance();

        //

        String usuario = getIntent().getExtras().getString("nombre");//OBTENEMOS NOMBRE DE USER DE LA BD
        String foto = getIntent().getExtras().getString("img_user");//OBTENEMOS IMAGEN DE USER DE LA BD
        final String id_user = getIntent().getExtras().getString("id_user");
        id_chat_global = getIntent().getExtras().getString("id_unico");

        colocarVisto();

        txt_mensaje = findViewById(R.id.txt_mensaje);
        btn_enviar = findViewById(R.id.btn_enviar_mensaje);

        //ELIMINAR MENSAJE

        //BOTON ENVIAR
        btn_enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    String mensaje = txt_mensaje.getText().toString();//TEXTO MENSAJE
                    if (!mensaje.isEmpty()){
                        String idpush =  ref_chat.push().getKey();
                        final Calendar c = Calendar.getInstance();
                        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        if(online){
                            Chats chatsmsg = new Chats(idpush,user.getUid(),id_user,mensaje,"si",dateFormat.format(c.getTime()),timeFormat.format(c.getTime()),"");
                            ref_chat.child(id_chat_global).child(idpush).setValue(chatsmsg);
                            txt_mensaje.setText("");
                        }else{
                            Chats chatsmsg = new Chats(idpush,user.getUid(),id_user,mensaje,"no",dateFormat.format(c.getTime()),timeFormat.format(c.getTime()),"");
                            ref_chat.child(id_chat_global).child(idpush).setValue(chatsmsg);
                            txt_mensaje.setText("");
                        }
                    }else{
                        Toast.makeText(MensajesActivity.this, "Mensaje vacio", Toast.LENGTH_SHORT).show();
                    }


            }
        });//FIN BOTON ENVIAR

        final String id_user_sp = mPref.getString("usuario_sp","");


        username.setText(usuario);
        Glide.with(this).load(foto).into(img_user_c);
        Glide.with(this).load(foto).into(img_user_d);
        final DatabaseReference ref = database.getReference("Estado").child(id_user_sp).child("chatcon");
        //REFERENCIA ESTADO USUARIO
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String chatcon = dataSnapshot.getValue(String.class);
                if (dataSnapshot.exists()){
                    if(chatcon.equals(user.getUid())){
                        online=true;
                        img_user_c.setVisibility(View.VISIBLE);
                        img_user_d.setVisibility(View.GONE);
                    }else{
                        online=false;
                        img_user_d.setVisibility(View.VISIBLE);
                        img_user_c.setVisibility(View.GONE);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });//FIN REFERENCIA ESTADO USUARIO

        //RV
        rv_chats = findViewById(R.id.rv_mensajes);
        rv_chats.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        rv_chats.setLayoutManager(linearLayoutManager);
        chatList= new ArrayList<>();
        adapter =  new  ChatAdapter (chatList,this);
        rv_chats.setAdapter(adapter);

        leerMensajes();
        //BOTON GALERIA
        btn_galeria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/jpeg");
                i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(i, "Selecciona una imagen"),PHOTO_SEND);
            }
        });



    }//FIN ONCREATE

    private void colocarVisto() {
        ref_chat.child(id_chat_global).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chats chats = snapshot.getValue(Chats.class);
                    if(chats.getRecibe().equals(user.getUid())){
                        ref_chat.child(id_chat_global).child(chats.getId()).child("visto").setValue("si");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void leerMensajes() {
        ref_chat.child(id_chat_global).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    chatList.removeAll(chatList);
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                        Chats chat = snapshot.getValue(Chats.class);
                        chatList.add(chat);
                        setScroll();
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setScroll() {
        rv_chats.scrollToPosition(adapter.getItemCount()-1);
    }




    private void estadousuario(final String estado){
        ref_estado.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String id_user_sp = mPref.getString("usuario_sp","");
                Estado est = new Estado(estado,"","",id_user_sp);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final String id_user = getIntent().getExtras().getString("id_user");
        if(requestCode==PHOTO_SEND && resultCode==RESULT_OK){
            //UNA VEZ SELECCIONADA LA FOTO VAMOS A CREAR UN URI CON LA IMAGEN
            Uri uri = data.getData();
            storageReference = storageImg.getReference("Imagenes");
            final StorageReference fotoRef = storageReference.child(uri.getLastPathSegment());
            fotoRef.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>(){

                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fotoRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                String mensaje = txt_mensaje.getText().toString();
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    txt_mensaje.setText("");
                    if(task.isSuccessful()){

                        Uri uri =task.getResult();
                        String idpush =  ref_chat.push().getKey();
                        final Calendar c = Calendar.getInstance();
                        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        if(online){
                            Chats chatsmsg = new Chats(idpush,user.getUid(),id_user,mensaje,"si",dateFormat.format(c.getTime()),timeFormat.format(c.getTime()),uri.toString());
                            ref_chat.child(id_chat_global).child(idpush).setValue(chatsmsg);

                        }else{
                            Chats chatsmsg = new Chats(idpush,user.getUid(),id_user,mensaje,"no",dateFormat.format(c.getTime()),timeFormat.format(c.getTime()),uri.toString());
                            ref_chat.child(id_chat_global).child(idpush).setValue(chatsmsg);
                        }



                    }
                }
            });

        }
    }
}