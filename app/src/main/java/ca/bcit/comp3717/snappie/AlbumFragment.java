package ca.bcit.comp3717.snappie;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.google.firebase.storage.FirebaseStorage;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

public class AlbumFragment extends Fragment {
    private static final String TAG = "AlbumFragment";

    private static final int NUM_GRID_COLUMNS = 3;

    private ImageView albumImage;
    private ProgressBar progressBar;
    private GridView gridView;

//    private ViewModel viewModel;

    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
//    private TextView textViewCurrentTheme;
//    private TextView textViewCurrentDate;
//    private String selectedImageEpoch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container, false);

//        textViewCurrentTheme = view.findViewById(R.id.textViewThemeAlbum);
//        textViewCurrentDate = view.findViewById(R.id.textViewDateAlbum);

        albumImage = view.findViewById(R.id.iv_album);
        progressBar = view.findViewById(R.id.pb_album);
        gridView = view.findViewById(R.id.gv_album);

        setupGridView();

        // Set up upload button
//        Button btnUpload = view.findViewById(R.id.btnUpload);
//        btnUpload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                writeImageViewToFirebase(albumImage);
//            }
//        });
        return view;
    }

    private void setupGridView() {
        Log.d(TAG, "getPhotos() started!");
        String directory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
        ArrayList<String> imagePaths = FileSearch.getFilePaths(directory);

        GridViewAdapter gridViewAdapter = new GridViewAdapter(getContext(), R.layout.item_album_grid, imagePaths);
        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener((parent, view, position, id) -> setImage("file:/" + imagePaths.get(position)));

        setImage("file:/" + imagePaths.get(0));
    }

//    private String getThemeFromImagePath(String imagePath) {
//        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(imagePath)), ZoneId.systemDefault());
//        return Themes.themes.get(ExploreFragment.getStoragePath(localDateTime));
//    }

//    private String getFormattedDateFromImagePath(String imagePath) {
//        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(imagePath)), ZoneId.systemDefault());
//        return ExploreFragment.getStoragePath(localDateTime);
//    }

//    public void writeImageViewToFirebase(ImageView imageView) {
//        // Generate data
//        Bitmap capture = Bitmap.createBitmap(
//                imageView.getWidth(),
//                imageView.getHeight(),
//                Bitmap.Config.ARGB_8888);
//        Canvas captureCanvas = new Canvas(capture);
//        imageView.draw(captureCanvas);
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        capture.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
//        byte[] data = outputStream.toByteArray();
//
//        // Create upload task
//        String path = ExploreFragment.getStoragePath(LocalDateTime.now()) + "/" + UUID.randomUUID() + ".png";
//        StorageReference storageReference = firebaseStorage.getReference(path);
//        StorageMetadata metadata = new StorageMetadata.Builder()
//                .setCustomMetadata("caption", "some caption here")
//                .build();
//        UploadTask uploadTask = storageReference.putBytes(data, metadata);
//
//        // Handle success
//        uploadTask.addOnCompleteListener(getActivity(), task ->
//                Toast.makeText(getContext(), "Shared snap!", Toast.LENGTH_LONG).show());
//    }

    private void setImage(String imagePath) {
//        selectedImageEpoch = imagePath.substring(imagePath.length() - 17, imagePath.length() - 4);
//        textViewCurrentTheme.setText(getThemeFromImagePath(selectedImageEpoch));
//        textViewCurrentDate.setText(getFormattedDateFromImagePath(selectedImageEpoch));

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
