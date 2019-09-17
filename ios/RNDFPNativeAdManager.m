#import "RNDFPNativeAdManager.h"
#import "RNDFPNativeAdView.h"

#if __has_include(<React/RCTBridge.h>)
#import <React/RCTBridge.h>
#else
#import "RCTBridge.h"
#endif

@implementation RNDFPNativeAdManager

RCT_EXPORT_MODULE();

- (UIView *)view
{
    return [[RNDFPNativeAdView alloc] init];
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_VIEW_PROPERTY(customTargeting, NSDictionary);
RCT_EXPORT_VIEW_PROPERTY(adUnitID, NSString);
RCT_EXPORT_VIEW_PROPERTY(adTemplateID, NSString);
RCT_EXPORT_VIEW_PROPERTY(assetName, NSString);
RCT_EXPORT_VIEW_PROPERTY(testDeviceID, NSString);

RCT_EXPORT_VIEW_PROPERTY(onAdmobDispatchAppEvent, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewDidReceiveAd, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onDidFailToReceiveAdWithError, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewWillPresentScreen, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewWillDismissScreen, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewDidDismissScreen, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onAdViewWillLeaveApplication, RCTBubblingEventBlock)

@end