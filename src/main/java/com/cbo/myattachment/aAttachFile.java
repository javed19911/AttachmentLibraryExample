package com.cbo.myattachment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class aAttachFile extends RecyclerView.Adapter<aAttachFile.MyViewHolder> {


    Context context;
    private Boolean isDeleteRequired = true;

    private ArrayList<String> attachments = new ArrayList<String>();

    public ArrayList<String> getAttachments() {
        return attachments;
    }

    public String getAttachmentStr() {
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for(String file : getAttachments()) {
            if (count != 0) {
                sb.append("|^");
            }
            ++count;
            sb.append(file);
        }
        return sb.toString();
    }

    public File[] filesToUpload(){
        List<File> files = new ArrayList();
        for (String file :  getAttachments()) {
            if (!file.toLowerCase().contains("upload/")){
                files.add(new File(file));
            }

        }

        File[] arr = new File[files.size()];
        for (int i=0; i<files.size();i++){
            arr[i] = files.get(i);
        }

        return arr;
    }

    public String getAttachmentName(){
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for(String path :  getAttachments()) {
            if (path.toLowerCase().contains("upload/")){
                sb.append(path);
            }else{
                File file = new File(path);
                if (count != 0) {
                    sb.append("|^");
                }
                ++count;
                sb.append(file.getName());
            }
        }
        return sb.toString();
    }

    public void addAttachment(String attachment){
        getAttachments().add(attachment);
        notifyDataSetChanged();
    }

    public void setAttachments(String attachments){
        setAttachments(attachments.isEmpty() ? new ArrayList() : new ArrayList(Arrays.asList(attachments.split("\\|\\^"))));
    }
    public void setAttachments(ArrayList<String> attachments) {
        this.attachments = attachments;
        notifyDataSetChanged();
    }

    public aAttachFile(Context context) {
        this.context = context;
    }

    public aAttachFile(Context context,Boolean isDeleteRequired) {
        this.context = context;
        this.isDeleteRequired = isDeleteRequired;
    }

    public aAttachFile(Context context, ArrayList<String> attachments) {
        this.context = context;
        this.attachments = attachments;
    }

    public aAttachFile(Context context, ArrayList<String> attachments,Boolean isDeleteRequired) {
        this.context = context;
        this.attachments = attachments;
        this.isDeleteRequired = isDeleteRequired;
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.attachment_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String path="";
        path=getAttachments().get(position);
        holder.thumbnail.setVisibility(View.VISIBLE);
        holder.attachImg.setVisibility(View.GONE);

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();


        switch (path.substring(path.lastIndexOf(".")+1).toLowerCase()){
            case "png":
            case "jpeg":
            case "jpg":
            case "ico":
            case "bmp":
            case "gif":
                holder.thumbnail.setVisibility(View.GONE);
                holder.attachImg.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(path)
                        .placeholder(circularProgressDrawable)
                        .error(R.drawable.no_img)
                        .into(holder.attachImg);

                    break;
            case "mp4":
            case "3gp":
            case "avi":
            case "mov":
                Glide.with(context)
                        .load(R.drawable.mp4_img)
                        .placeholder(circularProgressDrawable)
                        .error(R.drawable.no_img)
                        .into(holder.thumbnail);
                break;

            case "pdf":
                Glide.with(context)
                        .load(R.drawable.pdf_img)
                        .placeholder(circularProgressDrawable)
                        .error(R.drawable.no_img)
                        .into(holder.thumbnail);
                break;
            case "mp3":
                Glide.with(context)
                        .load(R.drawable.music_img)
                        .placeholder(circularProgressDrawable)
                        .error(R.drawable.no_img)
                        .into(holder.thumbnail);
                break;
            case "xls":
                Glide.with(context)
                        .load(R.drawable.xls_img)
                        .placeholder(circularProgressDrawable)
                        .error(R.drawable.no_img)
                        .into(holder.thumbnail);
                break;
                default:
                    Glide.with(context)
                            .load(R.drawable.web_img)
                            .placeholder(circularProgressDrawable)
                            .error(R.drawable.no_img)
                            .into(holder.thumbnail);

        }

    }

    @Override
    public int getItemCount() {
        return getAttachments().size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView attachImg,thumbnail;
        public TextView delete;
        public MyViewHolder(View view) {
            super(view);

            //city = (TextView) view.findViewById(R.id.city);
            attachImg = view.findViewById(R.id.attachImg);
            thumbnail = view.findViewById(R.id.thumbnail);
            delete = view.findViewById(R.id.delete);

            delete.setOnClickListener(this);

            delete.setVisibility(isDeleteRequired?View.VISIBLE:View.GONE);


            view.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.delete){
                try {
                    String filePath = getAttachments().get(getAdapterPosition());
                    getAttachments().remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    /*if (listenter !=null){
                        listenter.OnDeleted(filePath);
                    }*/
                }catch (Exception e){
                    notifyDataSetChanged();
                }


            }
            else {

                try{
                    String path = getAttachments().get(getAdapterPosition());
                    if (!(path.contains("http://") || path.contains("https://"))){
                        path = "file://" + path;
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    switch (path.substring(path.lastIndexOf(".")+1).toLowerCase()){
                        case "png":
                        case "jpeg":
                        case "jpg":
                        case "ico":
                        case "bmp":
                        case "gif":
                            PreviewImage(context,getAttachments().get(getAdapterPosition()));
                            break;
                        case "mp4":
                        case "3gp":
                        case "avi":
                        case "mov":
                            intent.setDataAndType(
                                    Uri.parse(path),
                                    "video/*");

                            context.startActivity(intent);

                            break;

                        case "pdf":

                            intent.setDataAndType(
                                    Uri.parse(path),
                                    "application/pdf");

                            context.startActivity(intent);
                            break;

                        case "mp3":
                            intent.setDataAndType(
                                    Uri.parse(getAttachments().get(getAdapterPosition())),
                                    "audio/*");

                            context.startActivity(intent);
                            break;
                        case "xls":
                            intent.setDataAndType(Uri.parse(path)
                                    , "application/vnd.ms-excel");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(intent);
                            break;
                        default:
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));


                            context.startActivity(intent);
                            break;

                    }
                }catch (ActivityNotFoundException e) {
                    Toast.makeText(context, "No Application Available to View Excel", Toast.LENGTH_SHORT).show();
                }



            }
        }
    }

    private void PreviewImage(Context context,String path){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog dialog = builder.create();
        LayoutInflater inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogLayout = inflater.inflate(R.layout.attachment_pop_up_card, null);
        ImageView attach_img= (ImageView) dialogLayout.findViewById(R.id.attachImg);
        TextView close=  dialogLayout.findViewById(R.id.delete);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();

        dialog.getWindow().getAttributes().windowAnimations = R.style.PopupTheme;
        //attach_img.setImageBitmap(b);
        Glide.with(context)
                .load(path)
                .placeholder(circularProgressDrawable)
                .error(R.drawable.no_img)
                .into(attach_img);
        dialog.setView(dialogLayout);
        dialog.show();
    }
}
