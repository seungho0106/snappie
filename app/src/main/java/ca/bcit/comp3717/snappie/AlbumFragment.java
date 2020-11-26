package ca.bcit.comp3717.snappie;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;

public class AlbumFragment extends Fragment {
    private static final String TAG = "AlbumFragment";

    private static final int NUM_GRID_COLUMNS = 3;

    private ImageView albumImage;
    private ProgressBar progressBar;
    private GridView gridView;

    private ViewModel viewModel;

    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private TextView textViewCurrentTheme;
    private TextView textViewCurrentDate;
    private String selectedImageEpoch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        textViewCurrentTheme = view.findViewById(R.id.textViewThemeAlbum);
        textViewCurrentDate = view.findViewById(R.id.textViewDateAlbum);

        albumImage = view.findViewById(R.id.iv_album);
        progressBar = view.findViewById(R.id.pb_album);
        gridView = view.findViewById(R.id.gv_album);

        setupGridView();

        // Set up upload button
        Button btnUpload = view.findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeImageViewToFirebase(albumImage);
            }
        });

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

    private String getThemeFromImagePath(String imagePath) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(imagePath)), ZoneId.systemDefault());
        return Themes.themes.get(ExploreFragment.getStoragePath(localDateTime));
    }

    private String getFormattedDateFromImagePath(String imagePath) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(imagePath)), ZoneId.systemDefault());
        return ExploreFragment.getStoragePath(localDateTime);
    }

    public void writeImageViewToFirebase(ImageView imageView) {
        // Generate data
        Bitmap capture = Bitmap.createBitmap(
                imageView.getWidth(),
                imageView.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas captureCanvas = new Canvas(capture);
        imageView.draw(captureCanvas);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        capture.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] data = outputStream.toByteArray();

        // Create upload task
        String path = ExploreFragment.getStoragePath(LocalDateTime.now()) + "/" + UUID.randomUUID() + ".png";
        StorageReference storageReference = firebaseStorage.getReference(path);
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("caption", "some caption here")
                .build();
        UploadTask uploadTask = storageReference.putBytes(data, metadata);

        // Handle success
        uploadTask.addOnCompleteListener(getActivity(), task ->
                Toast.makeText(getContext(), "Shared snap!.", Toast.LENGTH_LONG).show());
    }

    private void setImage(String imagePath) {
        selectedImageEpoch = imagePath.substring(imagePath.length() - 17, imagePath.length() - 4);
        textViewCurrentTheme.setText(getThemeFromImagePath(selectedImageEpoch));
        textViewCurrentDate.setText(getFormattedDateFromImagePath(selectedImageEpoch));

        Matrix matrix = new Matrix();
        matrix.postRotate(90F);

        progressBar.setVisibility(View.VISIBLE);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap,0, 0, bitmap.getWidth(), bitmap.getHeight(),   matrix, true);
        albumImage.setImageBitmap(newBitmap);
        progressBar.setVisibility(View.GONE);
    }
}
