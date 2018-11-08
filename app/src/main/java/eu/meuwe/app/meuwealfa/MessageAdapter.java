package eu.meuwe.app.meuwealfa;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    static final int SENT =0;
    static final int RECEIVED =1;
    Post post;
    List<Message> Messages;
    private String localuser;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView text_message_name,text_message_body,text_message_time;
        public ImageView image_message_profile;
        public MyViewHolder(View itemView) {
            super(itemView);
            text_message_body = itemView.findViewById(R.id.text_message_body);
            text_message_time = itemView.findViewById(R.id.text_message_time);
            text_message_name = itemView.findViewById(R.id.text_message_name);
            image_message_profile = itemView.findViewById(R.id.image_message_profile);

        }
    }

    public MessageAdapter(Post post, String user) {
        localuser = user;
        Messages = new ArrayList<>();
        Messages = post.getMessages();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType)
        {
            case RECEIVED:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_received,parent,false);
                break;
            case SENT:
            default:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_sent,parent,false);
                break;
        }
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.text_message_body.setText(Messages.get(position).getText());
        holder.text_message_name.setText(Messages.get(position).getUser());
        Calendar currentTime = Calendar.getInstance();
        Calendar messageTime = Calendar.getInstance();
        Date messageDate = new Date();
        messageDate.setTime(Messages.get(position).getTime());
        messageTime.setTime(messageDate);
        SimpleDateFormat simpleDateFormat;
        if(currentTime.get(currentTime.DAY_OF_YEAR)==messageTime.get(messageTime.DAY_OF_YEAR)
                && currentTime.get(Calendar.YEAR)==messageTime.get(Calendar.YEAR))
        {
            simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
        }
        else
        {
            simpleDateFormat = new SimpleDateFormat("dd/MM/yy hh:mm:ss");
        }

        holder.text_message_time.setText(simpleDateFormat.format(Messages.get(position).getTime()));
    }

    @Override
    public int getItemViewType(int position) {
        String msgUsr = Messages.get(position).getUser();
        if(msgUsr.compareToIgnoreCase(localuser)==0) {//if the same
            return SENT;
        }
        else {
            return RECEIVED;
        }
    }

    @Override
    public int getItemCount() {
        return Messages.size();
    }
}
