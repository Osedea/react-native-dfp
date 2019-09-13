#if __has_include(<React/RCTComponent.h>)
#import <React/RCTComponent.h>
#else
#import "RCTComponent.h"
#endif

@import GoogleMobileAds;

@class RCTEventDispatcher;

@interface RNDFPNativeAdView : UIView <GADNativeCustomTemplateAdLoaderDelegate>

@property (nonatomic, copy) NSDictionary *customTargeting;
@property (nonatomic, copy) NSString *adUnitID;
@property (nonatomic, copy) NSString *adTemplateID;
@property (nonatomic, copy) NSString *testDeviceID;

@property (nonatomic, copy) RCTBubblingEventBlock onWillChangeAdSizeTo;

@property (nonatomic, copy) RCTBubblingEventBlock onSizeChange;
@property (nonatomic, copy) RCTBubblingEventBlock onAdmobDispatchAppEvent;
@property (nonatomic, copy) RCTBubblingEventBlock onAdViewDidReceiveAd;
@property (nonatomic, copy) RCTBubblingEventBlock onDidFailToReceiveAdWithError;
@property (nonatomic, copy) RCTBubblingEventBlock onAdViewWillPresentScreen;
@property (nonatomic, copy) RCTBubblingEventBlock onAdViewWillDismissScreen;
@property (nonatomic, copy) RCTBubblingEventBlock onAdViewDidDismissScreen;
@property (nonatomic, copy) RCTBubblingEventBlock onAdViewWillLeaveApplication;

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher NS_DESIGNATED_INITIALIZER;
- (void)loadNativeAd;

@end
