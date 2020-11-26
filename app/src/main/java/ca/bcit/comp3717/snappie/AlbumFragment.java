package ca.bcit.comp3717.snappie;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class AlbumFragment extends Fragment {
    private static final String TAG = "AlbumFragment";

    private static final int NUM_GRID_COLUMNS = 3;

    private ImageView albumImage;
    private ProgressBar progressBar;
    private GridView gridView;

    private ViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        albumImage = view.findViewById(R.id.iv_album);
        progressBar = view.findViewById(R.id.pb_album);
        gridView = view.findViewById(R.id.gv_album);

        setupGridView();

        return view;
    }

    private void setupGridView() {
        Log.d(TAG, "getPhotos() started!");
        String directory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        ArrayList<String> imagePaths = FileSearch.getFilePaths(directory);

        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        GridViewAdapter gridViewAdapter = new GridViewAdapter(getContext(), R.layout.grid_view_layout, imagePaths);
        gridView.setAdapter(gridViewAdapter);

        setImage(imagePaths.get(0));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setImage(imagePaths.get(position));
            }
        });
    }

    private void setImage(String imagePath) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90F);
        progressBar.setVisibility(View.VISIBLE);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap,0, 0, bitmap.getWidth(), bitmap.getHeight(),   matrix, true);
        albumImage.setImageBitmap(newBitmap);
        progressBar.setVisibility(View.GONE);
    }
}
