package ca.bcit.comp3717.snappie;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class AlbumFragment extends Fragment {
    private static final String TAG = "AlbumFragment";

    private TextView imageTheme;
    private TextView imageDate;
    private ImageButton shareButton;
    private ImageView albumImage;
    private ProgressBar progressBar;
    private TextView placeholder;
    private GridView gridView;

    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        imageTheme = view.findViewById(R.id.tv_image_theme);
        imageDate = view.findViewById(R.id.tv_image_date);
        shareButton = view.findViewById(R.id.btn_share);
        albumImage = view.findViewById(R.id.iv_album);
        progressBar = view.findViewById(R.id.pb_album);
        placeholder = view.findViewById(R.id.placeholder);
        gridView = view.findViewById(R.id.gv_album);

        shareButton.setOnClickListener(v -> writeImageViewToFirebase());
        setupGridView();

        return view;
    }

    private void setupGridView() {
        Log.d(TAG, "getPhotos() started!");
        String directory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        ArrayList<String> imagePaths = FileSearch.getFilePaths(directory);
        Collections.reverse(imagePaths);

        GridViewAdapter gridViewAdapter = new GridViewAdapter(getContext(), R.layout.item_album_grid, imagePaths);
        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener((parent, view, position, id) -> setImage("file:/" + imagePaths.get(position)));

        if (imagePaths.size() > 0) {
            setImage("file:/" + imagePaths.get(0));
        } else {
            placeholder.setVisibility(View.VISIBLE);
        }
    }

    private void setImageInfo(String imagePath) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(imagePath)), ZoneId.systemDefault());
        String storagePath = ExploreFragment.getStoragePath(localDateTime);
        imageTheme.setText(Themes.themes.get(storagePath));
        imageDate.setText(storagePath);
    }

    public void writeImageViewToFirebase() {
        // Generate data
        Bitmap capture = Bitmap.createBitmap(
                (int) (albumImage.getWidth() * 0.5),
                (int) (albumImage.getHeight() * 0.5),
                Bitmap.Config.ARGB_8888);
        Canvas captureCanvas = new Canvas(capture);
        albumImage.draw(captureCanvas);
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
        uploadTask.addOnCompleteListener(
                getActivity(),
                task -> Snackbar.make(getView(), "Snap shared successfully!", Snackbar.LENGTH_LONG).show()
        );
    }

    private void setImage(String imagePath) {
        String selectedImageEpoch = imagePath.substring(imagePath.length() - 17, imagePath.length() - 4);
        setImageInfo(selectedImageEpoch);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(imagePath, albumImage, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
