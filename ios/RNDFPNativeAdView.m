#import "RNDFPNativeAdView.h"

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

@implementation RNDFPNativeAdView;

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

- (NSArray *)nativeCustomTemplateIDsForAdLoader:(GADAdLoader *)_adLoader
{
    if (_adTemplateID) {
        NSArray *templateIDs = @[_adTemplateID];
        return templateIDs;
    } else {
        return nil;
    }
}

- (void)adLoader:(GADAdLoader *)_adLoader
    didReceiveNativeCustomTemplateAd:(GADNativeCustomTemplateAd *)nativeCustomTemplateAd
{
    UIImage *image = [nativeCustomTemplateAd imageForKey:_assetName].image;
    _mainImageView = [[UIImageView alloc] initWithImage:image];
    
    [self layoutSubviews];
}

-(void)loadNativeAd {
    if (self.adUnitID && self.customTargeting && self.adTemplateID && self.assetName) {
        _adLoader = [[GADAdLoader alloc] initWithAdUnitID:self.adUnitID rootViewController:[UIApplication sharedApplication].delegate.window.rootViewController adTypes:@[kGADAdLoaderAdTypeNativeCustomTemplate] options:nil];
        
        [_adLoader setDelegate:self];

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
        
        [_adLoader loadRequest:request];
    }
}

- (void)setAdUnitID:(NSString *)adUnitID
{
    if(![adUnitID isEqual:_adUnitID]) {
        _adUnitID = adUnitID;
        if (_mainImageView) {
            [_mainImageView removeFromSuperview];
        }
        [self loadNativeAd];
    }
}

- (void)setAdTemplateID:(NSString *)adTemplateID
{
    if(![adTemplateID isEqual:_adTemplateID]) {
        _adTemplateID = adTemplateID;
        if (_mainImageView) {
            [_mainImageView removeFromSuperview];
        }
        [self loadNativeAd];
    }
}

- (void)setAssetName:(NSString *)assetName
{
    if(![assetName isEqual:_assetName]) {
        _assetName = assetName;
        if (_mainImageView) {
            [_mainImageView removeFromSuperview];
        }
        [self loadNativeAd];
    }
}

- (void)setTestDeviceID:(NSString *)testDeviceID
{
    if(![testDeviceID isEqual:_testDeviceID]) {
        _testDeviceID = testDeviceID;
        if (_mainImageView) {
            [_mainImageView removeFromSuperview];
        }
        [self loadNativeAd];
    }
}

- (void)setCustomTargeting:(NSDictionary *)customTargeting
{
     if(![customTargeting isEqualToDictionary:_customTargeting]) {
        _customTargeting = customTargeting;
        if (_mainImageView) {
            [_mainImageView removeFromSuperview];
        }
        [self loadNativeAd];
     }
}

-(void)layoutSubviews
{
    [super layoutSubviews];
    _mainImageView.frame = CGRectMake(
        self.bounds.origin.x,
        self.bounds.origin.x,
        self.frame.size.width,
        self.frame.size.height
    );

    [self addSubview:_mainImageView];
}

- (void)removeFromSuperview
{
    [super removeFromSuperview];
}

- (void)adView:(nonnull GADAdLoader *)_adLoader
didReceiveAppEvent:(NSString *)name
      withInfo:(NSString *)info {
    NSLog(@"Received app event (%@, %@)", name, info);
    NSMutableDictionary *myDictionary = [[NSMutableDictionary alloc] init];
    myDictionary[name] = info;
    if (self.onAdmobDispatchAppEvent) {
        self.onAdmobDispatchAppEvent(@{ name: info });
    }
}

/// Tells the delegate an ad request loaded an ad.
- (void)adLoaderDidFinishLoading:(nonnull GADAdLoader *)_adLoader {
    NSLog(@"** Finished");
    if (self.onAdViewDidReceiveAd) {
        self.onAdViewDidReceiveAd(@{});
    }
}


/// Tells the delegate an ad request failed.
- (void)adLoader:(nonnull GADAdLoader *)_adLoader didFailToReceiveAdWithError:(nonnull GADRequestError *)error {
    if (self.onDidFailToReceiveAdWithError) {
        self.onDidFailToReceiveAdWithError(@{ @"error": [error localizedDescription] });
    }
}

@end
