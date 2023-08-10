package ua.in.asilichenko.mapwithpansample;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
    implements OnStreetViewPanoramaReadyCallback, OnMapReadyCallback {

  // panoId=CAoSLEFGMVFpcFBfUGZObkF0QS1yTTJPWlJLQ2Z3SlF6bFc2aXBRTmtRTGFOdUp6
  private static final LatLng KYIV = new LatLng(50.44989013671875, 30.523759841918945);

  /**
   * Icon of pegman
   */
  private static final int PEGMAN_ID = R.drawable.pegman;

  /**
   * Peg rotation correction, should be subtracted from azimuth angle.
   * <p>
   * peg direction is the West and +30 deg to the South
   * <p>
   * 270 - azimuth angle of the west direction
   * <p>
   * 30 - angle between the West direction and real peg direction
   */
  private static final float PEG_ROTATION_CORR = 270 - 30;
  private static final float PEG_ANCHOR_HOR = .6458f;
  private static final float PEG_ANCHOR_VERT = .7267f;
  private static final String PEG_TITLE = "I'm here";

  /**
   * Minimum delta value to process the peg rotation.
   */
  private static final float PEG_ROTATION_EPS = 1e-6f;

  private Marker peg;
  private GoogleMap map;
  private StreetViewPanorama pan;

  //
  private static final LatLng position = KYIV;
  private static final float bearing = 115;
  private static final float zoom = 18f;
  //

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    log("---------------------- Start -------------------------------");
    log("You are using API KEY: " + getString(R.string.maps_api_key));

    ((SupportMapFragment) Objects.requireNonNull(getSupportFragmentManager()
        .findFragmentById(R.id.map)))
        .getMapAsync(this);
    ((SupportStreetViewPanoramaFragment) Objects.requireNonNull(getSupportFragmentManager()
        .findFragmentById(R.id.panorama)))
        .getStreetViewPanoramaAsync(this);
  }

  @Override
  public void onStreetViewPanoramaReady(@NonNull StreetViewPanorama pan) {
    this.pan = pan;
    pan.setPosition(position);
    pan.animateTo(new StreetViewPanoramaCamera.Builder().bearing(bearing).build(), 0);
    // listeners
    pan.setOnStreetViewPanoramaChangeListener(this::onPanLocationChange);
    pan.setOnStreetViewPanoramaCameraChangeListener(this::onPanCameraChange);
  }

  @Override
  public void onMapReady(@NonNull GoogleMap map) {
    this.map = map;
    map.getUiSettings().setZoomControlsEnabled(true);
    //
    map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
    peg = map.addMarker(new MarkerOptions()
        .icon(BitmapDescriptorFactory.fromResource(PEGMAN_ID))
        .title(PEG_TITLE)
        .position(map.getCameraPosition().target)
        .anchor(PEG_ANCHOR_HOR, PEG_ANCHOR_VERT)
        .draggable(true)
    );
    // listeners
    map.setOnCameraMoveListener(this::onMapCameraChange);
    map.setOnMarkerDragListener((PegmanDragListener) MainActivity.this::onMarkerDragEnd);
  }

  /**
   * It's very important to use this service method instead of manually setting the rotation.
   * <p>
   * Pegman's rotation depends on the Panorama bearing as well as the Map bearing.
   * <p>
   * Another important aspect is the pointing direction of Pegman itself.
   * <p>
   * Currently, the bearing of Pegman's pointer is (270 - 30) - 270 is for the West,
   * and -30 is for the shift to the South.
   * <p>
   * Panorama rotation has a positive effect, while the map rotation has a negative effect.
   */
  private void updatePegRotation() {
    final float panBearing = pan.getPanoramaCamera().bearing;
    final float mapBearing = map.getCameraPosition().bearing;
    final float newRotation = panBearing - mapBearing - PEG_ROTATION_CORR;
    if (Math.abs(newRotation - peg.getRotation()) > PEG_ROTATION_EPS) peg.setRotation(newRotation);
  }

  // ===========================================================

  /**
   * on rotate camera: is called multiple times
   *
   * @param camera panorama camera
   */
  private void onPanCameraChange(StreetViewPanoramaCamera camera) {
    log("onPanCameraChange: " + camera);
    updatePegRotation();
  }

  /**
   * move to the other panorama: is called once when moving is finished
   *
   * @param location may be null if no panorama at position was set
   */
  private void onPanLocationChange(@Nullable StreetViewPanoramaLocation location) {
    log("onPanLocationChange: " + location);
    if (null == location) return;
    if (!peg.getPosition().equals(location.position)) peg.setPosition(location.position);
    map.moveCamera(CameraUpdateFactory.newLatLng(location.position));
  }

  /**
   * <p>
   * on zoom: is called multiple times
   * <p>
   * on drag view: is called multiple times
   */
  private void onMapCameraChange() {
    log("onMapCameraChange: " + map.getCameraPosition());
    updatePegRotation();
  }

  /**
   * on drag marker: is called once when marker is released
   *
   * @param marker marker was dragged
   */
  private void onMarkerDragEnd(Marker marker) {
    log("onMarkerDragEnd");
    pan.setPosition(marker.getPosition());
  }

  // ===========================================================

  @SuppressWarnings("unused")
  private void toast(String msg) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
  }

  private void log(String msg) {
    Log.d(getLocalClassName(), msg);
  }

  @FunctionalInterface
  private interface PegmanDragListener extends GoogleMap.OnMarkerDragListener {

    @Override
    default void onMarkerDrag(@NonNull Marker marker) {
    }

    @Override
    void onMarkerDragEnd(@NonNull Marker marker);

    @Override
    default void onMarkerDragStart(@NonNull Marker marker) {
    }
  }
}