//
//  Dictionary+FunctionalExtensions.swift
//  NativeNavigation
//
//  Created by Leland Richardson on 8/15/16.
//  Copyright © 2016 Airbnb. All rights reserved.
//

import Foundation

extension Dictionary {
  init(_ pairs: [Element]) {
    self.init()
    for (k, v) in pairs {
      self[k] = v
    }
  }

  public mutating func merge(values: [Key: Value]) -> [Key: Value] {
    for (key, value) in values {
      self[key] = value
    }
    return self
  }

  public func combineWith(values: [Key: Value]) -> [Key: Value] {
    var out = [Key: Value]()
    out.merge(values: self);
    out.merge(values: values);
    return out;
  }

  public func filterValues(predicate: (Value) throws -> Bool) rethrows -> [Key: Value] {
    var dict = [Key: Value]()
    for (key, value) in self {
      if try predicate(value) {
        dict[key] = value
      }
    }
    return dict
  }


  public func mapValues<OutValue>(transform: (Value) throws -> OutValue) rethrows -> [Key: OutValue] {
    var dict = [Key: OutValue]()
    for (key, value) in self {
      dict[key] = try transform(value)
    }
    return dict
  }
}
