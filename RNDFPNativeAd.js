import React from 'react';
import {
  requireNativeComponent,
  View,
} from 'react-native';
import PropTypes from 'proptypes';

const RNNativeAd = requireNativeComponent('RNDFPNativeAd', DFPNativeAd);

export default class DFPNativeAd extends React.Component {

  render() {
    const { adUnitID, adTemplateID, testDeviceID, didFailToReceiveAdWithError, admobDispatchAppEvent } = this.props;
    let { customTargeting } = this.props;

    if (!customTargeting || Object.keys(customTargeting).length === 0) {
        customTargeting = {}
    }

    return (
      <View style={this.props.style}>
        <RNNativeAd
          onAdViewDidReceiveAd={this.props.adViewDidReceiveAd}
          onDidFailToReceiveAdWithError={(event) => didFailToReceiveAdWithError(event.nativeEvent.error)}
          onAdViewWillPresentScreen={this.props.adViewWillPresentScreen}
          onAdViewWillDismissScreen={this.props.adViewWillDismissScreen}
          onAdViewDidDismissScreen={this.props.adViewDidDismissScreen}
          onAdViewWillLeaveApplication={this.props.adViewWillLeaveApplication}
          onAdmobDispatchAppEvent={(event) => admobDispatchAppEvent(event)}
          customTargeting={customTargeting}
          testDeviceID={testDeviceID}
          adUnitID={adUnitID}
          adTemplateID={adTemplateId}
        />
      </View>
    );
  }
}

DFPNativeAd.propTypes = {
  // style: View.propTypes.style,

  /**
   * Custom targeting params to be sent along with the ad request
   */
  customTargeting: PropTypes.object,

  /**
   * DFP ad unit ID
   */
  adUnitID: PropTypes.string,

  /**
   * Template for the native ad
   */
  adTemplateID: PropTypes.string,
  
  /**
   * Test device ID
   */
  testDeviceID: PropTypes.string,

  /**
   * DFP iOS (?) library events
   */
  adViewDidReceiveAd: PropTypes.func,
  didFailToReceiveAdWithError: PropTypes.func,
  adViewWillPresentScreen: PropTypes.func,
  adViewWillDismissScreen: PropTypes.func,
  adViewDidDismissScreen: PropTypes.func,
  adViewWillLeaveApplication: PropTypes.func,
  admobDispatchAppEvent: PropTypes.func,
  // ...View.propTypes,
};

DFPNativeAd.defaultProps = {
    didFailToReceiveAdWithError: () => {},
    admobDispatchAppEvent: () => {}
};
