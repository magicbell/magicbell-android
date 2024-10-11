# MagicBell Android SDK

## 3.0.0

### Major Changes

- f3915c7: Updated topic and category filtering APIs in `StorePredicate`

  Previously one was able to filter for multiple topics and categories, but this functionality is now deprecated in favor of only filtering for a single topic and category.
  Please reach out to us via the [Community](http://www.magicbell.com/community) if you need the previous functionality.

### Minor Changes

- 54f5bc5: Updated the `targetSDK` of both, `sdk` and `sdk-compose` to version `35`

  Please follow the _Android SDK Upgrade Assistant_ for upgrading your own apps.

- f3915c7: Fixed a bug where a topic filter value would be passed as a category

## 2.0.0

This release is mostly compatible with version 1.0.0 of the SDK. It introduces two breaking changes though. Please consult the Readme for detailed reference.

### Breaking: Updated Notification Preferences API

The shape of the returned preferences object changed and now contains categories and channels.

### Breaking: HMAC validation

Instead of being required to pass the API secret, the HMAC should be computed on backend and passed to the frontend, where it is expected as an argument on the connectUser call.
Also the MagicBellClient does not have a enableHMAC flag anymore. The behaviour whether to send an HMAC header is now defined by whether one was passed as an argument to the connectUser call.

### APNS Integration

The previous SDK was registering device tokens using the `/push_subscriptions` API endpoint. Since version 2, the SDK uses the `/integrations/mobile_push/apns`

### What's Changed

- Upgrading Android and Gradle Tooling by @stigi in https://github.com/magicbell-io/magicbell-android/pull/13
- Remove API secret in favor of explicitly passing user HMAC by @stigi in https://github.com/magicbell-io/magicbell-android/pull/14
- Notification preferences update by @stigi in https://github.com/magicbell-io/magicbell-android/pull/15
- fix: Use correct keys when parsing action URL and custom Attributes from notification response by @stigi in https://github.com/magicbell-io/magicbell-android/pull/16
- chore: Migrate from Push Subscriptions to FCM Integration by @stigi in https://github.com/magicbell-io/magicbell-android/pull/17
- chore: Bump SDK version to 2.0.0 by @stigi in https://github.com/magicbell-io/magicbell-android/pull/18

### New Contributors

- @stigi made their first contribution in https://github.com/magicbell-io/magicbell-android/pull/13

**Full Changelog**: https://github.com/magicbell-io/magicbell-android/compare/1.0.0...2.0.0
