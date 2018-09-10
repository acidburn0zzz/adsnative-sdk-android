## v3.1.1
1. Added delayed lazy loading support for banner ads
2. Updated FB adapter to handle exception in fetching icon images

## v3.1.0
1. Added support for the latest FB SDK (v 4.99.3)
2. FB SDK support with DFP Header bidding
3. AdChoices Icon support in DFP Header bidding
4. Allow opening landing urls with redirects in PM sdk

## v3.0.0
1. Added support for Banner ads
2. Added support for DFP Banner In-App client server bidding
3. Updated Mopub bidder file for In-App client server bidding

## v2.7.0
1. Added support for DFP client server bidding

## v2.6.2
1. Handle scenarios when one of the device info is empty

## v2.6.1
1. Added support for MoPub client server bidding
2. Added Polymorph adapter to work with MoPub SDK mediation

## v2.6.0
1. Bug Fix - Fallback not happening if there's an exception in one of the networks
2. Made SDK crash proof by handling various negative scenarios
3. Updated adapter files (Fb, MoPub, ShareThrough) with better error handling

## v2.5.4
1. Bug Fix - SDK crash in content stream ads when placement is paused.

## v2.5.3
1. Bug Fix - Upon loading video, back up image loads after few seconds and overrides the video
2. Bug Fix - Proguard was not keeping ANRequestParameters$Builder class
3. Bug Fix - ad choices icon when clicked was not redirecting to the ad choices url
4. Bug Fix - SDK crash while destroying AdRenderer
5. Bug Fix - SDK crash while fetching advertising id due to a bug in Google Play Services
6. Replaced deprecated GooglePlayServicesUtil class with GoogleApiAvailability class

## v2.5.2
1. Added ShareThrough Network.
2. Added support for Facebook videos.
3. Updated Gradle to 2.2.2

## v2.5.1
1. Added AdColony Network
2. Added support to render the complete Views provided by Custom Networks.
3. Updated Gradle to 2.1.0

## v2.5.0
1. Added support for in-line videos with autoplay
2. S2S (API) mediation support added. Currently, the networks supported are Smaato, MobFox and AppNext.
3. An optional Ad Choices icon has been added to the native elements that can be rendered. This is required for third party networks that mandate it.
4. Impression and Click callbacks are now exposed on ANListAdapter and ANRecyclerAdapter
5. Publishers can override click-through landing url for their direct sold campaigns
6. Bug fixes and performance improvements
