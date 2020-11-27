package ca.bcit.comp3717.snappie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

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
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            holder.image = convertView.findViewById(R.id.image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String imagePath = getItem(position);
        Matrix matrix = new Matrix();
        matrix.postRotate(90F);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap,0, 0, bitmap.getWidth(), bitmap.getHeight(),   matrix, true);
        holder.image.setImageBitmap(newBitmap);
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
