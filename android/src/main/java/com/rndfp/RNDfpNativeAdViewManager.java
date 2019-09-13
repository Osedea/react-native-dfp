package com.rndfp;

import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RNDfpNativeAdViewManager extends SimpleViewManager<RNDfpReactViewGroup> implements AppEventListener {
  private static final String TAG = RNDfpNativeAdViewManager.class.getSimpleName();

  public static final String REACT_CLASS = "RNDFPNativeAd";

  public static final String PROP_CUSTOM_TARGETING = "customTargeting";
  public static final String PROP_AD_UNIT_ID = "adUnitID";
  public static final String PROP_AD_TEMPLATE_ID = "adTemplateID";
  public static final String PROP_TEST_DEVICE_ID = "testDeviceID";

  private String testDeviceID = null;

  public enum Events {
    EVENT_RECEIVE_AD("onAdViewDidReceiveAd"),
    EVENT_ERROR("onDidFailToReceiveAdWithError"),
    EVENT_WILL_PRESENT("onAdViewWillPresentScreen"),
    EVENT_WILL_DISMISS("onAdViewWillDismissScreen"),
    EVENT_DID_DISMISS("onAdViewDidDismissScreen"),
    EVENT_WILL_LEAVE_APP("onAdViewWillLeaveApplication"),
    EVENT_ADMOB_EVENT_RECEIVED("onAdmobDispatchAppEvent");

    private final String mName;

    Events(final String name) {
      mName = name;
    }

    @Override
    public String toString() {
      return mName;
    }
  }

  private ThemedReactContext mThemedReactContext;
  private RCTEventEmitter mEventEmitter;

  @Override
  public String getName() {
    return REACT_CLASS;
  }


  @Override
  public void onAppEvent(String name, String info) {
    String message = String.format("Received app event (%s, %s)", name, info);
    Log.d("PublisherAdNativeAd", message);
    WritableMap event = Arguments.createMap();
    event.putString(name, info);
    mEventEmitter.receiveEvent(viewID, Events.EVENT_ADMOB_EVENT_RECEIVED.toString(), event);
  }

  @Override
  protected View createViewInstance(ThemedReactContext themedReactContext) {
    mThemedReactContext = themedReactContext;
    mEventEmitter = themedReactContext.getJSModule(RCTEventEmitter.class);

    RNDfpReactViewGroup view = new RNDfpReactViewGroup(themedReactContext);
    return view;
  }

  int viewID = -1;

  protected void attachNewAdView(final RNDfpReactViewGroup view, NativeCustomTemplateAd customTemplateAd) {
    // Inflate a layout and add it to the parent ViewGroup.
    LayoutInflater inflater = (LayoutInflater) parent.getContext()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    final View adView = inflater.inflate(R.layout.custom_template_ad, parent);

    // Locate the ImageView that will hold the value for "Imagephone" and
    // set its drawable.
    // Imagephone : pour affichage sur les smartphone
    // ImageTabletLandscape : pour affichage sur Tablet en mode Landscape
    // ImageTabletPortrait 
    Button myMainImageView = (ImageView) adView.findViewById(R.id.custom_ad_image);
    myMainImageView.setImageDrawable(
      customTemplateAd.getImage("Imagephone").getDrawable());     
    
    adView.setAppEventListener(this);
    // destroy old AdView if present
    View oldAdView = (View) view.getChildAt(0);
    view.removeAllViews();
    if (oldAdView != null) oldAdView.destroy();
    view.addView(adView);
  }

  @ReactProp(name = PROP_CUSTOM_TARGETING)
  public void setCustomTargeting(final RNDfpReactViewGroup view, final ReadableMap customTargeting) {
    view.setCustomTargeting(customTargeting);
    View adView = (View) view.getChildAt(0);
    loadAd(view, adView);
  }

  @ReactProp(name = PROP_AD_UNIT_ID)
  public void setAdUnitID(final RNDfpReactViewGroup view, final String adUnitID) {
    view.setAdUnitId(adUnitID);
    View adView = (View) view.getChildAt(0);
    loadAd(view, adView);
  }

  @ReactProp(name = PROP_AD_TEMPLATE_ID)
  public void setAdTemplateID(final RNDfpReactViewGroup view, final String adTemplateID) {
    view.setAdTemplateId(adTemplateID);
    View adView = (View) view.getChildAt(0);
    loadAd(view, adView);
  }

  @ReactProp(name = PROP_TEST_DEVICE_ID)
  public void setPropTestDeviceID(final RNDfpReactViewGroup view, final String testDeviceID) {
    this.testDeviceID = testDeviceID;
    View adView = (View) view.getChildAt(0);
    loadAd(view, adView);
  }


  private void loadAd(RNDfpReactViewGroup view, final View adView) {
    if (view.getAdUnitId() != null && view.getAdTemplateId() != null && view.getCustomTargeting() != null) {
      if (adView.getAdUnitId() == null) {
        adView.setAdUnitId(view.getAdUnitId());
      }

      PublisherAdRequest.Builder adRequestBuilder = new PublisherAdRequest.Builder();
      if (testDeviceID != null){
        if (testDeviceID.equals("EMULATOR")) {
          adRequestBuilder = adRequestBuilder.addTestDevice(PublisherAdRequest.DEVICE_ID_EMULATOR);
        } else {
          adRequestBuilder = adRequestBuilder.addTestDevice(testDeviceID);
        }
      }

      for (Map.Entry<String, Object> target : view.getCustomTargeting().toHashMap().entrySet()) {
        adRequestBuilder.addCustomTargeting(target.getKey(), target.getValue().toString());
      }

      PublisherAdRequest adRequest = adRequestBuilder.build();
      
      viewID = view.getId();

      AdLoader adLoader = new AdLoader.Builder(context, view.getAdUnitId())
      .forCustomTemplateAd(view.getAdTemplateId(),
        new NativeCustomTemplateAd.OnCustomTemplateAdLoadedListener() {
            @Override
            public void onCustomTemplateAdLoaded(NativeCustomTemplateAd ad) {
                attachNewAdView(view, ad);
            }
        },
        new NativeCustomTemplateAd.OnCustomClickListener() {
            @Override
            public void onCustomClick(NativeCustomTemplateAd ad, String s) {
                // Handle the click action
            }
        })
      .withAdListener(new AdListener() {
        @Override
        public void onAdLoaded() {
          int width = adView.getAdSize().getWidthInPixels(mThemedReactContext);
          int height = adView.getAdSize().getHeightInPixels(mThemedReactContext);
          int left = adView.getLeft();
          int top = adView.getTop();
          adView.measure(width, height);
          adView.layout(left, top, left + width, top + height);
          mEventEmitter.receiveEvent(view.getId(), Events.EVENT_RECEIVE_AD.toString(), null);
          // triggerSizeChange(view, adView);
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
          WritableMap event = Arguments.createMap();
          switch (errorCode) {
            case PublisherAdRequest.ERROR_CODE_INTERNAL_ERROR:
              event.putString("error", "ERROR_CODE_INTERNAL_ERROR");
              break;
            case PublisherAdRequest.ERROR_CODE_INVALID_REQUEST:
              event.putString("error", "ERROR_CODE_INVALID_REQUEST");
              break;
            case PublisherAdRequest.ERROR_CODE_NETWORK_ERROR:
              event.putString("error", "ERROR_CODE_NETWORK_ERROR");
              break;
            case PublisherAdRequest.ERROR_CODE_NO_FILL:
              event.putString("error", "ERROR_CODE_NO_FILL");
              break;
          }

          mEventEmitter.receiveEvent(view.getId(), Events.EVENT_ERROR.toString(), event);
        }

        @Override
        public void onAdOpened() {
          mEventEmitter.receiveEvent(view.getId(), Events.EVENT_WILL_PRESENT.toString(), null);
        }

        @Override
        public void onAdClosed() {
          mEventEmitter.receiveEvent(view.getId(), Events.EVENT_WILL_DISMISS.toString(), null);
        }

        @Override
        public void onAdLeftApplication() {
          mEventEmitter.receiveEvent(view.getId(), Events.EVENT_WILL_LEAVE_APP.toString(), null);
        }
      })
      .build();

      adLoader.loadAd(adRequest);
    }
  }

  private AdSize getAdSizeFromString(String adSize) {
    switch (adSize) {
      case "NativeAd":
        return AdSize.NativeAd;
      case "largeNativeAd":
        return AdSize.LARGE_NativeAd;
      case "mediumRectangle":
        return AdSize.MEDIUM_RECTANGLE;
      case "fullNativeAd":
        return AdSize.FULL_NativeAd;
      case "leaderBoard":
        return AdSize.LEADERBOARD;
      case "smartNativeAdPortrait":
        return AdSize.SMART_NativeAd;
      case "smartNativeAdLandscape":
        return AdSize.SMART_NativeAd;
      case "smartNativeAd":
        return AdSize.SMART_NativeAd;
      default:
        return AdSize.NativeAd;
    }
  }
}
