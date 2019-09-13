@import UIKit;
@import GoogleMobileAds;

/// View representing a custom native ad format with template ID 10063170.
@interface SimpleNativeAdView : UIView

// Weak references to this ad's asset views.
@property(weak, nonatomic) IBOutlet UIImageView *mainImageView;

/// Populates the ad view with the custom native ad object.
- (void)populateWithCustomNativeAd:(GADNativeCustomTemplateAd *)customNativeAd;

@end