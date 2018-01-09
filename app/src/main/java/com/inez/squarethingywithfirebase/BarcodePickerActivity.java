package com.inez.squarethingywithfirebase;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.scandit.barcodepicker.BarcodePicker;
import com.scandit.barcodepicker.OnScanListener;
import com.scandit.barcodepicker.ScanOverlay;
import com.scandit.barcodepicker.ScanSession;
import com.scandit.barcodepicker.ScanSettings;
import com.scandit.barcodepicker.ScanditLicense;
import com.scandit.recognition.Barcode;

public class BarcodePickerActivity extends Activity implements OnScanListener {
    public static final String EXTRA_APP_KEY = "appKey";
    public static final String EXTRA_ENABLED_SYMBOLOGIES = "enabledSymbologies";
    public static final String EXTRA_CAMERA_FACING_PREFERENCE = "cameraFacingPreference";
    public static final String EXTRA_RESTRICT_SCANNING_AREA = "restrictScanningArea";
    public static final String EXTRA_SCANNING_AREA_HEIGHT = "scanningAreaHeight";
    public static final String EXTRA_SHOW_TORCH_BUTTON = "showTorchButton";
    public static final String EXTRA_CAMERA_SWITCH_VISIBILITY = "cameraSwitchVisibility";
    public static final String EXTRA_GUI_STYLE = "guiStyle";
    private BarcodePicker mBarcodePicker;
    private final int CAMERA_PERMISSION_REQUEST = 5;
    private boolean mCameraAccessDenied = false;
    private boolean mPaused = true;

    public BarcodePickerActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(128);
        if(this.getIntent().hasExtra("appKey")) {
            ScanditLicense.setAppKey(this.getIntent().getStringExtra("appKey"));
        }

        this.initializeAndStartBarcodeScanning(this.settingsFromIntent(this.getIntent()));
    }

    protected ScanSettings settingsFromIntent(Intent intent) {
        ScanSettings settings = ScanSettings.create();
        if(intent.hasExtra("enabledSymbologies")) {
            int[] enabledSymbologies = intent.getIntArrayExtra("enabledSymbologies");
            if(enabledSymbologies != null) {
                int[] var4 = enabledSymbologies;
                int var5 = enabledSymbologies.length;

                for(int var6 = 0; var6 < var5; ++var6) {
                    int sym = var4[var6];
                    settings.setSymbologyEnabled(sym, true);
                }
            }
        }

        settings.setCameraFacingPreference(intent.getIntExtra("cameraFacingPreference", 0));
        settings.setRestrictedAreaScanningEnabled(intent.getBooleanExtra("restrictScanningArea", false));
        if(intent.hasExtra("scanningAreaHeight")) {
            settings.setScanningHotSpotHeight(intent.getFloatExtra("scanningAreaHeight", 0.1F));
        }

        return settings;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 5) {
            if(grantResults.length > 0 && grantResults[0] == 0) {
                this.mCameraAccessDenied = false;
                if(!this.mPaused) {
                    this.mBarcodePicker.startScanning();
                }
            } else {
                this.mCameraAccessDenied = true;
            }

        }
    }

    protected void setupScanUIFromIntent(BarcodePicker picker, Intent intent) {
        ScanOverlay overlay = picker.getOverlayView();
        overlay.setBeepEnabled(false);
        overlay.setTorchEnabled(intent.getBooleanExtra("showTorchButton", false));
        overlay.setCameraSwitchVisibility(intent.getIntExtra("cameraSwitchVisibility", 0));
        overlay.setGuiStyle(intent.getIntExtra("guiStyle", 0));
    }

    protected void onPause() {
        super.onPause();
        this.mPaused = true;
        if(this.mBarcodePicker != null) {
            this.mBarcodePicker.stopScanning();
        }

    }

    @TargetApi(23)
    private void grantCameraPermissionsThenStartScanning() {
        if(this.checkSelfPermission("android.permission.CAMERA") != 0) {
            if(!this.mCameraAccessDenied) {
                this.requestPermissions(new String[]{"android.permission.CAMERA"}, 5);
            }
        } else {
            this.mBarcodePicker.startScanning();
        }

    }

    protected void onResume() {
        super.onResume();
        this.mPaused = false;
        if(this.mBarcodePicker != null) {
            if(Build.VERSION.SDK_INT >= 23) {
                this.grantCameraPermissionsThenStartScanning();
            } else {
                this.mBarcodePicker.startScanning();
            }
        }

    }

    void initializeAndStartBarcodeScanning(ScanSettings settings) {
        this.getWindow().setFlags(1024, 1024);
        this.requestWindowFeature(1);
        boolean emulatePortraitMode = !BarcodePicker.canRunPortraitPicker();
        if(emulatePortraitMode) {
            this.setRequestedOrientation(0);
        }

        this.mBarcodePicker = new BarcodePicker(this, settings);
        this.mBarcodePicker.setOnScanListener(this);
        this.setupScanUIFromIntent(this.mBarcodePicker, this.getIntent());
        this.setContentView(this.mBarcodePicker);
    }

    public void didScan(ScanSession session) {
        Intent resultIntent = new Intent();
        this.buildSuccessResult(resultIntent, session);
        this.setResult(-1, resultIntent);
        session.pauseScanning();
        this.finish();
    }

    protected void buildSuccessResult(Intent resultIntent, ScanSession session) {
        Barcode code = (Barcode)session.getNewlyRecognizedCodes().get(0);
        resultIntent.putExtra("barcodeRecognized", true);
        resultIntent.putExtra("barcodeRawData", code.getRawData());
        resultIntent.putExtra("barcodeData", code.getData());
        resultIntent.putExtra("barcodeSymbologyName", code.getSymbologyName());
        resultIntent.putExtra("barcodeSymbology", code.getSymbology());
    }

    public void onBackPressed() {
        if(this.mBarcodePicker != null) {
            this.mBarcodePicker.stopScanning();
        }

        Intent result = new Intent();
        this.buildCancelResult(result);
        this.setResult(0, result);
        this.finish();
    }

    protected void buildCancelResult(Intent resultIntent) {
        resultIntent.putExtra("barcodeRecognized", false);
    }
}
