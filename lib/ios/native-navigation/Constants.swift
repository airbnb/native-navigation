//
//  Constants.swift
//  NativeNavigation
//
//  Created by Yassine Chbani on 1/4/19.
//

public struct Constants {
  // we will wait a maximum of 200ms for the RN view to tell us what the navigation bar should look like.
  // should normally happen much quicker than this... This is just to make sure it transitions in a reasonable
  // time frame even if the react thread takes an extra long time.
  public static let TRANSITION_DELAY: Int64 = Int64(1.2 * Double(NSEC_PER_SEC))
}
