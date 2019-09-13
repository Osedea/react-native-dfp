#import "SimpleNativeAdView.h"

@implementation SimpleNativeAdView

- (void)populateWithCustomNativeAd:(GADNativeCustomTemplateAd *)customNativeAd {
  self.customNativeAd = customNativeAd;

  // Populate the custom native ad assets.
  self.mainImageView.image = [customNativeAd imageForKey:@"MainImage"].image;
}

@end
