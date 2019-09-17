package com.rndfp;

import android.content.Context;
import android.util.Log;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeCustomTemplateAd;

import java.util.Map;

public class RNDfpNativeAdViewManager extends SimpleViewManager<RNDfpReactViewGroup> implements AppEventListener {
    private static final String TAG = RNDfpNativeAdViewManager.class.getSimpleName();

    public static final String REACT_CLASS = "RNDFPNativeAd";

    public static final String PROP_CUSTOM_TARGETING = "customTargeting";
    public static final String PROP_AD_UNIT_ID = "adUnitID";
    public static final String PROP_AD_TEMPLATE_ID = "adTemplateID";
    public static final String PROP_TEST_DEVICE_ID = "testDeviceID";
    public static final String PROP_ASSET_NAME = "assetName";

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
    protected RNDfpReactViewGroup createViewInstance(ThemedReactContext themedReactContext) {
        Log.d("PublisherAdNativeAd", "Create View Instance");

        mThemedReactContext = themedReactContext;
        mEventEmitter = themedReactContext.getJSModule(RCTEventEmitter.class);

        RNDfpReactViewGroup view = new RNDfpReactViewGroup(themedReactContext);
        viewGroup = view;
        return view;
    }

    ReactViewGroup viewGroup = null;

    int viewID = -1;

    private void setupLayoutHack() {
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                manuallyLayoutChildren();
                viewGroup.getViewTreeObserver().dispatchOnGlobalLayout();
                Choreographer.getInstance().postFrameCallback(this);
            }
        });
    }

    private void manuallyLayoutChildren() {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            child.measure(View.MeasureSpec.makeMeasureSpec(viewGroup.getMeasuredWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(viewGroup.getMeasuredHeight(), View.MeasureSpec.EXACTLY));
            child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
        }
    }

    protected void attachNewAdView(final RNDfpReactViewGroup view, NativeCustomTemplateAd customTemplateAd) {
        Log.d("PublisherAdNativeAd", "Attach new view");
        // Inflate a layout and add it to the parent ViewGroup.
        LayoutInflater inflater = (LayoutInflater) view.getContext()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View adView = inflater.inflate(R.layout.custom_template_ad, null);

        // Locate the ImageView that will hold the value for "Imagephone" and
        // set its drawable.
        ImageView myMainImageView = (ImageView) adView.findViewById(R.id.custom_ad_image);
        NativeAd.Image image = customTemplateAd.getImage(view.getAssetName());
        if (image != null) {
            Log.d("PublisherAdNativeAd", "There's an image!");
            myMainImageView.setImageDrawable(image.getDrawable());
        }

        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();
        adView.measure(width, height);
        adView.layout(0, 0, width, height);

        myMainImageView.measure(width, height);
        myMainImageView.layout(0, 0, width, height);

        // destroy old AdView if present
        View oldAdView = (View) view.getChildAt(0);
        view.removeView(oldAdView);
        view.removeAllViews();
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

    @ReactProp(name = PROP_ASSET_NAME)
    public void setAssetName(final RNDfpReactViewGroup view, final String assetName) {
        view.setAssetName(assetName);
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


    private void loadAd(final RNDfpReactViewGroup view, final View adView) {
        Log.d("PublisherAdNativeAd", "Load ad before if : adUnitID = " + view.getAdUnitId() + ", adTemplateId = " + view.getAdTemplateId() + ", assetName = " + view.getAssetName());

        if (view.getAdUnitId() != null && view.getAdTemplateId() != null && view.getCustomTargeting() != null && view.getAssetName() != null) {
            Log.d("PublisherAdNativeAd", "Loading ad");

            PublisherAdRequest.Builder adRequestBuilder = new PublisherAdRequest.Builder();
            if (testDeviceID != null) {
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

            AdLoader adLoader = new AdLoader.Builder(mThemedReactContext, view.getAdUnitId())
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
                        mEventEmitter.receiveEvent(viewID, Events.EVENT_RECEIVE_AD.toString(), null);
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
}
