//
//  ZoomVideoSDKPhoneHelper.h
//  ZoomVideoSDK
//
//  Created by Zoom Video Communications on 2021/12/21.
//  Copyright © 2021 Zoom Video Communications, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

/*!
 @class ZoomVideoSDKPhoneSupportCountryInfo
 @brief support country information interface.
 */
@interface ZoomVideoSDKPhoneSupportCountryInfo : NSObject
/*!
 @brief The country ID.
 */
@property (nonatomic, strong) NSString     * _Nullable countryID;
/*!
 @brief The country name.
 */
@property (nonatomic, strong) NSString     * _Nullable countryName;
/*!
 @brief The country code.
 */
@property (nonatomic, strong) NSString     * _Nullable countryCode;
@end


@interface ZoomVideoSDKPhoneHelper : NSObject

/*!
 @brief Determine whether the session supports to join by the phone or not.
 @return YES indicates to support to join by phone. NO not.
 */
- (BOOL)isSupportPhoneFeature;

/*!
 @brief Get the list of the country information where the session supports to join by telephone.
 @return List of the country information returns if the session supports to join by telephone. Otherwise nil.
 */
- (NSArray <ZoomVideoSDKPhoneSupportCountryInfo *>* _Nullable)getSupportCountryInfo;

/*!
 @brief Invite the specified user to join the session by call out.
 @param countryCode The country code of the specified user must be in the support list.
 @param phoneNumber The phone number of specified user.
 @param name The screen name of the specified user in the session.
 @return If the function succeeds, the return value is Errors_Success.Otherwise failed.
 */
- (ZoomVideoSDKError)inviteByPhone:(NSString *_Nonnull)countryCode phoneNumber:(NSString *_Nonnull)phoneNumber name:(NSString *_Nonnull)name;

/*!
 @brief Cancel the invitation that is being called out by phone.
 @return If the function succeeds, the return value is Errors_Success.Otherwise failed
 */
- (ZoomVideoSDKError)cancelInviteByPhone;

/*!
 @brief Get the process of the invitation by call out.
 @return If the function succeeds, the return value is the current call-out process. To get extended error information, see [ZoomVideoSDKPhoneStatus]
 */
- (ZoomVideoSDKPhoneStatus)getInviteByPhoneStatus;

@end
