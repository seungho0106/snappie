package ca.bcit.comp3717.snappie;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExploreFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ExploreFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private LocalDateTime localDateTime;
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExploreFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExploreFragment newInstance(String param1, String param2) {
        ExploreFragment fragment = new ExploreFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        localDateTime = LocalDateTime.now();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        Button loadPreviousButton = view.findViewById(R.id.btnLoadPrevious);
        loadPreviousButton.setOnClickListener(v -> {
            populateExploreDatePrevious(inflater, view);
        });

        // Populate explore dates // 3 days worth initially
        populateExploreDate(localDateTime, inflater, view);
        for (int i = 0; i < 2; i++) {
            populateExploreDatePrevious(inflater, view);
        }

        return view;
    }

    private void populateExploreDatePrevious(LayoutInflater inflater, View view) {
        localDateTime = localDateTime.minusHours(24);
        populateExploreDate(localDateTime, inflater, view);
    }

    private void populateExploreDate(LocalDateTime exploreDateTime, LayoutInflater inflater, View view) {
        String exploreDateString = getStoragePath(exploreDateTime);
        String theme = Themes.themes.get(exploreDateString);

        if (theme == null) {
            Toast.makeText(getContext(), "End of the list.", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout linearLayoutExplore = view.findViewById(R.id.linearLayoutExplore);

        View dayListView = inflater.inflate(R.layout.linear_layout_explore_day, null, false);
        linearLayoutExplore.addView(dayListView);

        TextView tvTitle = dayListView.findViewById(R.id.textViewTheme);
        tvTitle.setText(theme.toUpperCase());
        TextView tvDate = dayListView.findViewById(R.id.textViewDate);
        tvDate.setText(exploreDateString);

        StorageReference listRef = firebaseStorage.getReference().child(exploreDateString);
        listRef.listAll()
                .addOnSuccessListener(listResult -> {
                    // Get list of storage references of all images in the specified path
                    for (StorageReference item : listResult.getItems()) {
                        View snapView = inflater.inflate(R.layout.image_view_snap, null, false);
                        ImageView imageView = snapView.findViewById(R.id.imageViewSnap);
                        setImageFromFirebaseToImageView(imageView, item);

                        LinearLayout linearLayoutSnaps = dayListView.findViewById(R.id.linearLayoutSnaps);
                        linearLayoutSnaps.addView(snapView);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle error
                    }
                });
    }

    private void setImageFromFirebaseToImageView(ImageView imageView, StorageReference item) {
        // All the items under listRef.
        final long ONE_MEGABYTE = 1024 * 1024;
        item.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(bmp);
//            imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, imageView.getWidth(), imageView.getHeight(), false));
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });
    }

    /* Creates proper path the image is stored in the firebase, according to today's date.
    * IE. if today's date is 11/24/2020, the image should be stored in directory /11.24.2020/ */
    static public String getStoragePath(LocalDateTime localDateTime) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return dtf.format(localDateTime).replace("/", ".");
    }
}
