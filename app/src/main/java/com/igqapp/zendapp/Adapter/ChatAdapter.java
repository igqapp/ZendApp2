package com.igqapp.zendapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.igqapp.zendapp.Entidades.Chats;
import com.igqapp.zendapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.viewHolderAdapter> {
    List<Chats> chatsList;
    Context context;

    public static final int MENSAJE_ENVIADO = 1 ;
    public static final int MENSAJE_RECIBIDO = 0 ;
    boolean soloE = false;
    FirebaseUser fUser;

    public ChatAdapter(List<Chats> chatsList, Context context) {
        this.chatsList = chatsList;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolderAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MENSAJE_ENVIADO){
            View v = LayoutInflater.from(context).inflate(R.layout.chat_item_enviado,parent,false);
            return new ChatAdapter.viewHolderAdapter(v);
        }
        else{

            View v = LayoutInflater.from(context).inflate(R.layout.chat_item_recibido,parent,false);
            return new ChatAdapter.viewHolderAdapter(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolderAdapter holder, int position) {
        Chats chats = chatsList.get(position);

        holder.tv_mensaje.setText(chats.getMensaje());


        if(soloE){
            if(!chatsList.get(position).getUrl().equals("")){
                holder.img_mensaje_enviado.setVisibility(View.VISIBLE);
                holder.tv_mensaje.setVisibility(View.VISIBLE);
                Glide.with(context).load(chatsList.get(position).getUrl()).into(holder.img_mensaje_enviado);
            }else{
                holder.tv_mensaje.setVisibility(View.VISIBLE);
                holder.img_mensaje_enviado.setVisibility(View.GONE);

            }

            if(chats.getVisto().equals("si")){
                holder.img_entregado.setVisibility(View.GONE);
                holder.img_visto.setVisibility(View.VISIBLE);
            }else{
                holder.img_entregado.setVisibility(View.VISIBLE);
                holder.img_visto.setVisibility(View.GONE);
            }
            //FECHA MENSAJES
            final Calendar c = Calendar.getInstance();
            final SimpleDateFormat dataFormat = new SimpleDateFormat("dd/MM/yyyy");
            if(chats.getFecha().equals(dataFormat.format(c.getTime()))){
                holder.tv_hora.setText(chats.getHora());
            }else{
                holder.tv_hora.setText(chats.getFecha()+" "+chats.getHora());

            }


        }//fin soloE
        else{
            if(!chatsList.get(position).getUrl().equals("")){
                holder.img_mensaje_recibido.setVisibility(View.VISIBLE);
                holder.tv_mensaje.setVisibility(View.VISIBLE);
                Glide.with(context).load(chatsList.get(position).getUrl()).into(holder.img_mensaje_recibido);
            }else{
                holder.tv_mensaje.setVisibility(View.VISIBLE);
                holder.img_mensaje_recibido.setVisibility(View.GONE);

            }
        }



    }

    @Override
    public int getItemCount() {
        return chatsList.size();
    }

    public class viewHolderAdapter extends RecyclerView.ViewHolder {
        private TextView tv_mensaje, tv_hora;
        private ImageView img_entregado,img_visto;
        private ImageView img_mensaje_enviado;
        private ImageView img_mensaje_recibido;

        public viewHolderAdapter(@NonNull View itemView) {
            super(itemView);
            tv_mensaje = itemView.findViewById(R.id.tv_mensaje);
            tv_hora = itemView.findViewById(R.id.tv_hora);
            img_entregado = itemView.findViewById(R.id.img_entregado);
            img_visto = itemView.findViewById(R.id.img_visto);
            img_mensaje_enviado = itemView.findViewById(R.id.img_mensaje_enviado);
            img_mensaje_recibido = itemView.findViewById(R.id.img_mensaje_recibido);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatsList.get(position).getEnvia().equals(fUser.getUid())){
            soloE =true;
            return MENSAJE_ENVIADO;
        }
        else{soloE = false;
        return MENSAJE_RECIBIDO;}
    }



}
