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
    title = "Root"

    let pushButton1 = UIButton(type: .roundedRect)
    pushButton1.setTitle("Push ScreenOne", for: .normal)
    pushButton1.addTarget(self, action: #selector(pushScreenOne(sender:)), for: .touchUpInside)
    pushButton1.frame = CGRect(x: 0, y: 100, width: 320, height: 60)
    view.addSubview(pushButton1)

    let pushButton2 = UIButton(type: .roundedRect)
    pushButton2.setTitle("Push TabScreen", for: .normal)
    pushButton2.addTarget(self, action: #selector(pushTabScreen(sender:)), for: .touchUpInside)
    pushButton2.frame = CGRect(x: 0, y: 160, width: 320, height: 60)
    view.addSubview(pushButton2)

//    let button = UIButton(type: .roundedRect)
//    button.setTitle("Push ScreenOne", for: .normal)
//    button.addTarget(self, action: "pushScreenOne", for: .touchUpInside)
//    view.addSubview(button)

//    UIButton *button = [UIButton buttonWithType:UIButtonTypeRoundedRect];
//    [button setTitle:@"Push ScreenOne" forState:UIControlStateNormal];
//    button.frame = CGRectMake(0, 100, 320, 140);
//    [button addTarget:self action:@selector(pushScreenOne) forControlEvents:UIControlEventTouchUpInside];
//    [self.view addSubview:button];
//
//    UIButton *button2 = [UIButton buttonWithType:UIButtonTypeRoundedRect];
//    [button2 setTitle:@"Push TabBar" forState:UIControlStateNormal];
//    button2.frame = CGRectMake(0, 300, 320, 140);
//    [button2 addTarget:self action:@selector(pushTabBar) forControlEvents:UIControlEventTouchUpInside];
//    [self.view addSubview:button2];


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

