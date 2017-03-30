//
//  ViewController.swift
//  native-navigation
//
//  Created by bachand on 02/11/2017.
//  Copyright (c) 2017 bachand. All rights reserved.
//

import UIKit
import NativeNavigation

final class ViewController: UIViewController {

  override func viewDidLoad() {
    super.viewDidLoad()
    title = "Example"

    let pushButton1 = UIButton(type: .roundedRect)
    pushButton1.setTitle("Push ScreenOne", for: .normal)
    pushButton1.addTarget(self, action: #selector(pushScreenOne(sender:)), for: .touchUpInside)
    pushButton1.translatesAutoresizingMaskIntoConstraints = false
    view.addSubview(pushButton1)
    pushButton1.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
    pushButton1.topAnchor.constraint(equalTo: self.topLayoutGuide.bottomAnchor, constant: 100).isActive = true
    

    let pushButton2 = UIButton(type: .roundedRect)
    pushButton2.setTitle("Push TabScreen", for: .normal)
    pushButton2.addTarget(self, action: #selector(pushTabScreen(sender:)), for: .touchUpInside)
    pushButton2.translatesAutoresizingMaskIntoConstraints = false
    view.addSubview(pushButton2)
    pushButton2.centerXAnchor.constraint(equalTo: view.centerXAnchor).isActive = true
    pushButton2.topAnchor.constraint(equalTo: pushButton1.bottomAnchor, constant: 40).isActive = true

    view.backgroundColor = .white
  }

  func pushScreenOne(sender: UIButton) {
    let screenOne = ReactViewController(moduleName: "ScreenOne")
    navigationController?.pushReactViewController(screenOne, animated: true)
  }

  func pushTabScreen(sender: UIButton) {
    let tabScreen = ReactTabBarController(moduleName: "TabScreen")
    self.presentReactViewController(tabScreen, animated: true, completion: nil)
  }
}

