//
//  UIBarButtonItem+addAction.swift
//  Pods
//
//  Created by Leland Richardson on 2/14/17.
//
//

import Foundation

public class ClosureWrapper : NSObject {
  @objc let _callback : () -> Void
  @objc init(callback : @escaping () -> Void) {
    _callback = callback
  }

  @objc public func invoke() {
    _callback()
  }
}

var AssociatedClosure: UInt8 = 0

extension UIControl {
  @objc func nn_addAction(forControlEvents events: UIControl.Event, withCallback callback: @escaping () -> Void) {
    let wrapper = ClosureWrapper(callback: callback)
    addTarget(wrapper, action: #selector(ClosureWrapper.invoke), for: events)
    objc_setAssociatedObject(self, &AssociatedClosure, wrapper, objc_AssociationPolicy.OBJC_ASSOCIATION_RETAIN_NONATOMIC)
  }
}

extension UIBarButtonItem {
  @objc func nn_addAction() {

  }
}
