package com.igqapp.zendapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.igqapp.zendapp.Activities.MensajesActivity;
import com.igqapp.zendapp.Entidades.Solicitudes;
import com.igqapp.zendapp.Entidades.Usuario;
import com.igqapp.zendapp.R;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.viewHolderAdapter> {

    List<Usuario> usuarioList;
    Context context;

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    SharedPreferences mPref;//Usamos esta clase para obtener si el user conectado o no.


    public UsuarioAdapter(List<Usuario> usuarioList, Context context) {
        this.usuarioList = usuarioList;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolderAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_usuarios,parent,false);
        viewHolderAdapter holder = new viewHolderAdapter(v);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final viewHolderAdapter holder, int position) {

        final Usuario usuario = usuarioList.get(position);
        final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        Glide.with(context).load(usuario.getFoto()).into(holder.img_usuario);
        holder.tv_usuario.setText(usuario.getNombre());

        if(usuario.getId().equals(currentUser.getUid())){
            holder.cardView.setVisibility(View.GONE);
        }else{
            holder.cardView.setVisibility(View.VISIBLE);
        }
        //PARA DETECTAR LOS CAMBIOS EN LA BASE DE DATOS HAREMOS UN LISTENER
        final DatabaseReference ref_botones = database.getReference("Solicitudes").child(currentUser.getUid());
        ref_botones.child(usuario.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String estado = dataSnapshot.child("estado").getValue(String.class);
                //PARA SABER SI TIENE ALGUNA SOLICITUD O AUN NO RECIBIO NINGUNA
                if(dataSnapshot.exists()){
                    if(estado.equals("enviado")){
                        holder.send.setVisibility(View.VISIBLE);
                        holder.amigos.setVisibility(View.GONE);
                        holder.add.setVisibility(View.GONE);
                        holder.tengosolicitud.setVisibility(View.GONE);
                        holder.progressBar.setVisibility(View.GONE);
                    }
                    if(estado.equals("amigos")){
                        holder.amigos.setVisibility(View.VISIBLE);
                        holder.send.setVisibility(View.GONE);
                        holder.add.setVisibility(View.GONE);
                        holder.tengosolicitud.setVisibility(View.GONE);
                        holder.progressBar.setVisibility(View.GONE);
                    }
                    if(estado.equals("solicitud")){
                        holder.tengosolicitud.setVisibility(View.VISIBLE);
                        holder.amigos.setVisibility(View.GONE);
                        holder.add.setVisibility(View.GONE);
                        holder.send.setVisibility(View.GONE);
                        holder.progressBar.setVisibility(View.GONE);
                    }

                }else{
                        holder.send.setVisibility(View.GONE);
                        holder.amigos.setVisibility(View.GONE);
                        holder.add.setVisibility(View.VISIBLE);
                        holder.tengosolicitud.setVisibility(View.GONE);
                        holder.progressBar.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //PARA REALIZAR LA ACION DE AGREGAR
        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //ENTRAMOS EN LA BASE DE DATOS EN EL USUARIO PRINCIPAL PARA ACCEDER A LAS SOLICITUDES
                final DatabaseReference A = database.getReference("Solicitudes").child(currentUser.getUid());
                A.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Solicitudes sol = new Solicitudes("enviado","");
                        //CREAMOS LAS SOLICITUDES
                        A.child(usuario.getId()).setValue(sol);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                //EDITAMOS EN EL USER QUE RECIBE LA SOLICITUD
                final DatabaseReference B = database.getReference("Solicitudes").child(usuario.getId());
                B.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Solicitudes sol = new Solicitudes("solicitud","");
                        //CREAMOS LAS SOLICITUDES
                        B.child(currentUser.getUid()).setValue(sol);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                //MECANICA DE ADD SOLICITUDES AL USUARIO
                final DatabaseReference count = database.getReference("Contador").child(usuario.getId());
                count.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Integer val = dataSnapshot.getValue(Integer.class);
                            if(val==0){
                                count.setValue(1);

                            }else{
                                count.setValue(val+1);
                            }
                        }else{
                            count.setValue(1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            vibrator.vibrate(300);
            }//FIN ONCLICK
        });



        holder.tengosolicitud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String idchat =  ref_botones.push().getKey();
                final DatabaseReference A = database.getReference("Solicitudes").child(usuario.getId()).child(currentUser.getUid());
                A.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Solicitudes sol = new Solicitudes("amigos",idchat);
                        A.setValue(sol);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                final DatabaseReference B = database.getReference("Solicitudes").child(currentUser.getUid()).child(usuario.getId());
                B.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Solicitudes sol = new Solicitudes("amigos",idchat);
                        B.setValue(sol);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            vibrator.vibrate(300);
            }//FIN ON CLICK
        });

        holder.amigos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                mPref=v.getContext().getSharedPreferences("usuario_sp",context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = mPref.edit();

                //CLick en amigos
                final DatabaseReference ref = database.getReference("Solicitudes").child(currentUser.getUid()).child(usuario.getId()).child("idchat");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String id = dataSnapshot.getValue(String.class);
                        if(dataSnapshot.exists()){
                            Intent i = new Intent(v.getContext(), MensajesActivity.class);
                            i.putExtra("nombre", usuario.getNombre());
                            i.putExtra("img_user", usuario.getFoto());
                            i.putExtra("id_user", usuario.getId());
                            i.putExtra("id_unico", id);
                            //DE ESTA FORMA AL ENTRAR EN EL CHAT CONTROLAMOS SI ESTA EN LINEA
                            editor.putString("usuario_sp",usuario.getId());
                            editor.apply();

                            v.getContext().startActivity(i);


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


    }//fin onBindViewHolder

    @Override
    public int getItemCount() {
        return usuarioList.size();
    }

    public class viewHolderAdapter extends RecyclerView.ViewHolder {

        TextView tv_usuario;
        ImageView img_usuario;
        CardView cardView;
        Button add,send,amigos,tengosolicitud;
        ProgressBar progressBar;



        public viewHolderAdapter(@NonNull View itemView) {
            super(itemView);
            tv_usuario = itemView.findViewById(R.id.tv_user);
            img_usuario = itemView.findViewById(R.id.img_usuario);
            cardView = itemView.findViewById(R.id.cardview);
            add = itemView.findViewById(R.id.btn_add);
            send = itemView.findViewById(R.id.btn_send);
            amigos = itemView.findViewById(R.id.btn_amigos);
            tengosolicitud = itemView.findViewById(R.id.btn_solicitud);
            progressBar = itemView.findViewById(R.id.progressbar);

        }
    }
}
