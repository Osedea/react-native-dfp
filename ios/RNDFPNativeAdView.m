#import "RNSimpleNativeAdView.h"

#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#import <React/UIView+React.h>
#import <React/RCTLog.h>
#import <React/RCTConvert.h>
#else
#import "RCTBridgeModule.h"
#import "UIView+React.h"
#import "RCTLog.h"
#import "RCTConvert.h"
#import "SimpleNativeAdView.h"
#endif

@implementation RNSimpleNativeAdView {
    SimpleNativeAdView  *_nativeAdView;
}

- (void)insertReactSubview:(UIView *)view atIndex:(NSInteger)atIndex
{
    RCTLogError(@"DFP NativeAd cannot have any subviews");
    return;
}

- (void)removeReactSubview:(UIView *)subview
{
    RCTLogError(@"DFP NativeAd cannot have any subviews");
    return;
}

- (NSArray *)nativeCustomTemplateIDsForAdLoader:(GADAdLoader *)adLoader
{
    NSArray *templateIDs = [[NSArray alloc] init];
    [templateIDs addObject:_adTemplateID];

    return templateIDs;
}

- (void)adLoader:(GADAdLoader *)adLoader
    didReceiveNativeCustomTemplateAd:(GADNativeCustomTemplateAd *)nativeCustomTemplateAd
{
    self.nativeCustomTemplateAd = nativeCustomTemplateAd;

    [self loadNativeAd];
}

-(void)loadNativeAd {
    if (self.adUnitID && self.customTargeting && self.adTemplateID && self.nativeCustomTemplateAd) {
        _nativeAdView = [[SimpleNativeAdView alloc] init];
        [_nativeAdView setAppEventDelegate:self]; //added Admob event dispatch listener

        _nativeAdView.delegate = self;
        _nativeAdView.adSizeDelegate = self;
        _nativeAdView.adUnitID = _adUnitID;
        _nativeAdView.adTemplateID = _adTemplateID;
        _nativeAdView.rootViewController = [UIApplication sharedApplication].delegate.window.rootViewController;

        DFPRequest *request = [DFPRequest request];

        if (self.testDeviceID) {
            if([self.testDeviceID isEqualToString:@"EMULATOR"]) {
                request.testDevices = @[kGADSimulatorID];
            } else {
                request.testDevices = @[self.testDeviceID];
            }
        } else {
            request.testDevices = @[kGADSimulatorID];
        }

        if (self.customTargeting) {
            request.customTargeting = self.customTargeting;
        }
        [_nativeAdView populateWithCustomNativeAd:self.nativeCustomTemplateAd]
        [_nativeAdView loadRequest:request];
    }
}

- (void)adView:(SimpleNativeAdView *)nativeAd
didReceiveAppEvent:(NSString *)name
      withInfo:(NSString *)info {
    NSLog(@"Received app event (%@, %@)", name, info);
    NSMutableDictionary *myDictionary = [[NSMutableDictionary alloc] init];
    myDictionary[name] = info;
    if (self.onAdmobDispatchAppEvent) {
        self.onAdmobDispatchAppEvent(@{ name: info });
    }
}

- (void)setAdUnitID:(NSString *)adUnitID
{
    if(![adUnitID isEqual:_adUnitID]) {
        _adUnitID = adUnitID;
        if (_nativeAdView) {
            [_nativeAdView removeFromSuperview];
        }
        [self loadNativeAd];
    }
}

- (void)setAdTemplateID:(NSString *)adUnitID
{
    if(![adTemplateiD isEqual:_adTemplateiD]) {
        _adTemplateiD = adTemplateiD;
        if (_nativeAdView) {
            [_nativeAdView removeFromSuperview];
        }
        [self loadNativeAd];
    }
}

- (void)setTestDeviceID:(NSString *)testDeviceID
{
    if(![testDeviceID isEqual:_testDeviceID]) {
        _testDeviceID = testDeviceID;
        if (_nativeAdView) {
            [_nativeAdView removeFromSuperview];
        }
        [self loadNativeAd];
    }
}

- (void)setCustomTargeting:(NSDictionary *)customTargeting
{
     if(![customTargeting isEqualToDictionary:_customTargeting]) {
        _customTargeting = customTargeting;
        if (_nativeAdView) {
            [_nativeAdView removeFromSuperview];
        }
        [self loadNativeAd];
     }
}

-(void)layoutSubviews
{
    [super layoutSubviews ];

    _nativeAdView.frame = CGRectMake(
        self.bounds.origin.x,
        self.bounds.origin.x,
        _nativeAdView.frame.size.width,
        _nativeAdView.frame.size.height
    );

    [self addSubview:_nativeAdView];
}

- (void)removeFromSuperview
{
    [super removeFromSuperview];
}

// /// Called before the ad view changes to the new size.
// - (void)adView:(SimpleNativeAdView *)nativeAdView
// willChangeAdSizeTo:(GADAdSize)size {
//     if (self.onWillChangeAdSizeTo) {
//         // nativeAdView calls this method on its adSizeDelegate object before the nativeAd updates it size,
//         // allowing the application to adjust any views that may be affected by the new ad size.
//         self.onWillChangeAdSizeTo(@{
//                                     @"width": [NSNumber numberWithFloat: size.size.width],
//                                     @"height": [NSNumber numberWithFloat: size.size.height]
//                                     });
//     }

// }

/// Tells the delegate an ad request loaded an ad.
- (void)adViewDidReceiveAd:(SimpleNativeAdView *)adView {
    if (self.onAdViewDidReceiveAd) {
        self.onAdViewDidReceiveAd(@{});
    }
}

/// Tells the delegate an ad request failed.
- (void)adView:(SimpleNativeAdView *)adView
didFailToReceiveAdWithError:(GADRequestError *)error {
    if (self.onDidFailToReceiveAdWithError) {
        self.onDidFailToReceiveAdWithError(@{ @"error": [error localizedDescription] });
    }
}

/// Tells the delegate that a full screen view will be presented in response
/// to the user clicking on an ad.
- (void)adViewWillPresentScreen:(SimpleNativeAdView *)adView {
    if (self.onAdViewWillPresentScreen) {
        self.onAdViewWillPresentScreen(@{});
    }
}

/// Tells the delegate that the full screen view will be dismissed.
- (void)adViewWillDismissScreen:(SimpleNativeAdView *)adView {
    if (self.onAdViewWillDismissScreen) {
        self.onAdViewWillDismissScreen(@{});
    }
}

/// Tells the delegate that the full screen view has been dismissed.
- (void)adViewDidDismissScreen:(SimpleNativeAdView *)adView {
    if (self.onAdViewDidDismissScreen) {
        self.onAdViewDidDismissScreen(@{});
    }
}

/// Tells the delegate that a user click will open another app (such as
/// the App Store), backgrounding the current app.
- (void)adViewWillLeaveApplication:(SimpleNativeAdView *)adView {
    if (self.onAdViewWillLeaveApplication) {
        self.onAdViewWillLeaveApplication(@{});
    }
}

@end
