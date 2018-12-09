package eu.meuwe.app.meuwealfa;


import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;



import java.io.File;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;




public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    static final int SENT =0;
    static final int RECEIVED =1;
    static final int HEADER =2;
    Post post;
    List<Message> Messages;
    private String localuser;
    private BitmapDrawable eventImageDrawable;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView text_message_name,text_message_body,text_message_time,number_of_views,number_of_responses,author,edit;
        public ImageView image_message_profile;
        public MyViewHolder(View itemView) {
            super(itemView);
            text_message_body = itemView.findViewById(R.id.text_message_body);
            text_message_time = itemView.findViewById(R.id.text_message_time);
            text_message_name = itemView.findViewById(R.id.text_message_name);
            image_message_profile = itemView.findViewById(R.id.image_message_profile);
            number_of_views = itemView.findViewById(R.id.number_of_views);
            number_of_responses = itemView.findViewById(R.id.number_of_responses);
            author = itemView.findViewById(R.id.author);
            edit = itemView.findViewById(R.id.edit);
        }
    }

    public MessageAdapter(Post inPost, String user) {
        localuser = user;
        post = new Post();
        post = inPost;
        Messages = new ArrayList<>();
        Messages = post.getMessages();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType)
        {
            case HEADER:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.meuwe_header,parent,false);
                break;
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

    /** Definition of how the recycler view looks like
     * position 0 - header with event description, date, user, etc
     * position 1+ - chat
     * @param holder instance of view of created position
     * @param position this is what is filled at this moment
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if(position ==0) {//this is header
            //fill forum body
            String tmp = holder.text_message_body.getContext()
                    .getString(R.string.text_message_body,
                            post.getTitle(),
                            post.getText(),
                            android.text.TextUtils.join(" #",post.getTags()));
            holder.text_message_body.setText(tmp);
            if(post.getImageUrl()!=null&&!post.getImageUrl().isEmpty())
            {
                //Get post image from cache
                String filename = holder.image_message_profile.getResources().getString(R.string.postbitmapCache);
                File cacheDir = holder.image_message_profile.getContext().getCacheDir();
                String path = cacheDir.getPath()+"/"+filename;
                eventImageDrawable = new BitmapDrawable(holder.image_message_profile.getResources(), path);
                eventImageDrawable.setAntiAlias(true);
                eventImageDrawable.setFilterBitmap(true);
                holder.image_message_profile.setImageDrawable(eventImageDrawable);
            }

            //get localized string for number of views
            tmp = holder.number_of_views.getContext().getString(R.string.numberOfViews,post.getViewsCounter());
            holder.number_of_views.setText(tmp);
            //get localized string for number of responses
            tmp = holder.number_of_responses.getContext().getString(R.string.numberOfResponses,Messages.size());
            holder.number_of_responses.setText(tmp);
            //fill author
            holder.author.setText(post.getUser());
            //if user is the creator of post then activate edit button
            if(post.getUser().compareToIgnoreCase(localuser)==0) holder.edit.setVisibility(View.VISIBLE);

        }
        else {//this is chat message
                int messageNumber = position -1;
                holder.text_message_body.setText(Messages.get(messageNumber).getText());
                holder.text_message_name.setText(Messages.get(messageNumber).getUser());
                Calendar currentTime = Calendar.getInstance();
                Calendar messageTime = Calendar.getInstance();
                Date messageDate = new Date();
                messageDate.setTime(Messages.get(messageNumber).getTime());
                messageTime.setTime(messageDate);
                SimpleDateFormat simpleDateFormat;
                if (currentTime.get(currentTime.DAY_OF_YEAR) == messageTime.get(messageTime.DAY_OF_YEAR)
                        && currentTime.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR)) {
                    simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
                } else {
                    simpleDateFormat = new SimpleDateFormat("dd/MM/yy hh:mm:ss");
                }

                holder.text_message_time.setText(simpleDateFormat.format(Messages.get(messageNumber).getTime()));
            }
    }


    @Override
    public int getItemViewType(int position) {
        if(position==0)return HEADER;
        else {
            int messageNumber = position -1;
            String msgUsr = Messages.get(messageNumber).getUser();
            if (msgUsr.compareToIgnoreCase(localuser) == 0) {//if the same
                return SENT;
            } else {
                return RECEIVED;
            }
        }
    }

    @Override
    public int getItemCount() {
        return Messages.size()+1;
    }
}
