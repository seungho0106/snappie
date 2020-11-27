package ca.bcit.comp3717.snappie;

import android.os.Bundle;

import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.time.LocalDateTime;

public class CameraFragment extends Fragment {
    private static final String TAG = "AlbumFragment";

    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView textureView;
    ImageButton imageButton;
    ImageButton switchCam;
    CameraX.LensFacing cameraLensFacing = CameraX.LensFacing.BACK;;
    ImageCapture imgCap = null;

    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_camera, container, false);
        textureView = view.findViewById(R.id.view_finder);
        imageButton = view.findViewById(R.id.imgCapture);
        switchCam = view.findViewById(R.id.switch_cam);

        // Set today's theme
        String exploreDateString = ExploreFragment.getStoragePath(LocalDateTime.now());
        String theme = Themes.themes.get(exploreDateString);
        TextView tvThemeToday = view.findViewById(R.id.textViewThemeToday);
        tvThemeToday.setText(String.format("%s%s", getString(R.string.todaysTheme), theme));

        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS);
        }
        return view;
    }

    private void startCamera() {
        cameraLensFacing = CameraX.LensFacing.BACK;

        bindCameraUseCases();

        switchCam.setOnClickListener(v -> {
            if (CameraX.LensFacing.FRONT == cameraLensFacing) {
                cameraLensFacing = CameraX.LensFacing.BACK;
            } else {
                cameraLensFacing = CameraX.LensFacing.FRONT;
            }

            try {
                // Only bind use cases if we can query a camera with this orientation
//                CameraX.getCameraWithLensFacing(cameraLensFacing);
                bindCameraUseCases();
            } catch (Exception e) {
                Toast.makeText(
                        getContext(),
                        "Unable to switch",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
        );

        imageButton.setOnClickListener(v -> {
            String path = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    .toString();
            File file = new File(path + "/" + System.currentTimeMillis() + ".png");
            Log.i("path", path);

            imgCap.takePicture(file, new ImageCapture.OnImageSavedListener() {
                @Override
                public void onImageSaved(@NonNull File file) {
                    String msg = "Snapped!";
//                        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                    Log.d(TAG, getView().toString());
                    Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT).show();
                }

                @Override
                public void onError(@NonNull ImageCapture.UseCaseError useCaseError,
                                    @NonNull String message,
                                    @Nullable Throwable cause) {
                    String msg = "Pic capture failed : " + message + useCaseError.toString();
                    Toast.makeText(
                            getContext(),
                            msg,
                            Toast.LENGTH_LONG).show();
                    if (cause != null) {
                        cause.printStackTrace();
                    }
                }
            });
        });


        //bind to lifecycle:
//        CameraX.bindToLifecycle((LifecycleOwner) this, preview, imgCap);
    }


    private void bindCameraUseCases() {
        // Make sure that there are no other use cases bound to CameraX
        CameraX.unbindAll();

        Rational aspectRatio = new Rational(textureView.getWidth(), textureView.getHeight());
        Size screen = new Size(textureView.getWidth(), textureView.getHeight()); //size of the screen

        PreviewConfig pConfig = new PreviewConfig.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setTargetResolution(screen)
                .setLensFacing(cameraLensFacing)
                .build();
        Preview preview = new Preview(pConfig);

        //to update the surface texture we  have to destroy it first then re-add it
        preview.setOnPreviewOutputUpdateListener(output -> {
            ViewGroup parent = (ViewGroup) textureView.getParent();
            parent.removeView(textureView);
            parent.addView(textureView, 0);

            textureView.setSurfaceTexture(output.getSurfaceTexture());
            updateTransform();
        });

        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig
                .Builder()
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setLensFacing(cameraLensFacing)
                .build();
        imgCap = new ImageCapture(imageCaptureConfig);

        // Apply declared configs to CameraX using the same lifecycle owner
        CameraX.bindToLifecycle(this, preview, imgCap);
    }

    private void updateTransform() {
        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int) textureView.getRotation();

        switch (rotation) {
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float) rotationDgr, cX, cY);
        textureView.setTransform(mx);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(getContext(),
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}