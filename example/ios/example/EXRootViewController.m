//
//  EXRootViewController.m
//  example
//
//  Created by Leland Richardson on 12/20/16.
//  Copyright © 2016 Facebook. All rights reserved.
//

#import "EXRootViewController.h"

@interface EXRootViewController ()

@end

@implementation EXRootViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark RCTBridgeDelegate

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge {
  return [NSURL URLWithString:@"http://localhost:8081/example/index.bundle?platform=ios&dev=true"];
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
