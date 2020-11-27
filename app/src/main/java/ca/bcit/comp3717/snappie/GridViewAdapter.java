package ca.bcit.comp3717.snappie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

public class GridViewAdapter extends ArrayAdapter<String> {

    private Context context;
    private LayoutInflater inflater;
    private int layoutResource;
    private List<String> imagePaths;

    public GridViewAdapter(@NonNull Context context, int layoutResource, @NonNull List<String> imagePaths) {
        super(context, layoutResource, imagePaths);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.layoutResource = layoutResource;
        this.imagePaths = imagePaths;

    }

    private static class ViewHolder {
        ImageView image;
        ProgressBar progressBar;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            holder.image = convertView.findViewById(R.id.image);
            holder.progressBar = convertView.findViewById(R.id.progress_bar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageLoader imageLoader = ImageLoader.getInstance();
        String imagePath = "file:/" + getItem(position);
        imageLoader.displayImage(imagePath, holder.image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                holder.progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                holder.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                holder.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                holder.progressBar.setVisibility(View.GONE);
            }

        });
        return convertView;
    }
}

// class ImageViewHolder extends RecyclerView.ViewHolder {
//    private View rootView;
//    private ImageView imageView;
//
//    public ImageViewHolder(@NonNull View itemView) {
//        super(itemView);
//        rootView = itemView;
//        imageView = itemView.findViewById(R.id.image);
////        imageView.setOnClickListener(onClickListener);
//    }
//}
