package com.igqapp.zendapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.igqapp.zendapp.Entidades.Usuario;
import com.igqapp.zendapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ChatListaAdapter extends RecyclerView.Adapter<ChatListaAdapter.viewHolderAdapterchatlist> {

    List<Usuario> usuarioList;
    Context context;

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    SharedPreferences mPref;






    public ChatListaAdapter(List<Usuario> usuarioList, Context context) {
        this.usuarioList = usuarioList;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolderAdapterchatlist onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatlista,parent,false);
        viewHolderAdapterchatlist holder = new viewHolderAdapterchatlist(v);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final viewHolderAdapterchatlist holder, int position) {

        final Usuario usuario = usuarioList.get(position);
        final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        holder.tv_usuario.setText(usuario.getNombre());
        Glide.with(context).load(usuario.getFoto()).into(holder.img_usuario);

        DatabaseReference ref_mis_solicitudes = database.getReference("Solicitudes").child(currentUser.getUid());
        ref_mis_solicitudes.child(usuario.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String estado = dataSnapshot.child("estado").getValue(String.class);
                //PARA MOSTRAR SOLO LOS USUARIOS QUE SON AMIGOS
                if(dataSnapshot.exists()){
                    if(estado.equals("amigos")){
                        holder.cardView.setVisibility(View.VISIBLE);
                    }else{
                        holder.cardView.setVisibility(View.GONE);
                    }
                }else {
                    holder.cardView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //OBTENER ESTADO DEL USUARIO
        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        final String ULT_VEZ= context.getResources().getString(R.string.ultvez);
        final String HOY= context.getResources().getString(R.string.hoy);
        final String A= context.getResources().getString(R.string.a);

        DatabaseReference ref_Estado = database.getReference("Estado").child(usuario.getId());
        ref_Estado.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String estado = dataSnapshot.child("estado").getValue(String.class);
                String fecha = dataSnapshot.child("fecha").getValue(String.class);
                String hora = dataSnapshot.child("hora").getValue(String.class);

                if (dataSnapshot.exists()){
                    if(estado.equals("conectado")){
                        holder.tv_conectado.setVisibility(View.VISIBLE);
                        holder.icon_conectado.setVisibility(View.VISIBLE);
                        holder.icon_desconectado.setVisibility(View.GONE);
                        holder.tv_desconectado.setVisibility(View.GONE);
                    }else{
                        holder.tv_desconectado.setVisibility(View.VISIBLE);
                        holder.icon_desconectado.setVisibility(View.VISIBLE);
                        holder.icon_conectado.setVisibility(View.GONE);
                        holder.tv_conectado.setVisibility(View.GONE);

                        if(fecha.equals(dateFormat.format(c.getTime()))){
                            holder.tv_desconectado.setText(ULT_VEZ+HOY+A+" "+hora);
                        }
                        //AÑADIR EN AYER ELSEIF//
                        else{
                            holder.tv_desconectado.setText(ULT_VEZ+fecha+A+hora);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                mPref=v.getContext().getSharedPreferences("usuario_sp",context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = mPref.edit();

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

    public class viewHolderAdapterchatlist extends RecyclerView.ViewHolder {

        TextView tv_usuario;
        ImageView img_usuario;
        CardView cardView;
        TextView tv_conectado, tv_desconectado;
        ImageView icon_conectado, icon_desconectado;



        public viewHolderAdapterchatlist(@NonNull View itemView) {
            super(itemView);
            tv_usuario = itemView.findViewById(R.id.tv_user);
            img_usuario = itemView.findViewById(R.id.img_usuario);
            cardView = itemView.findViewById(R.id.cardview);
            tv_conectado = itemView.findViewById(R.id.tv_conectado);
            tv_desconectado = itemView.findViewById(R.id.tv_desconectado);
            icon_conectado = itemView.findViewById(R.id.ic_conectado);
            icon_desconectado = itemView.findViewById(R.id.ic_desconectado);

        }
    }
}
