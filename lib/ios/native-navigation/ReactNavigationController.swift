import UIKit

public class ReactNavigationController: UINavigationController {

    override public var shouldAutorotate : Bool {
        return true
    }
    
    override public var supportedInterfaceOrientations : UIInterfaceOrientationMask {
        
        if let reactViewController = self.visibleViewController as? ReactViewController {
            return reactViewController.getOrientation()
        }
        return .portrait
    }
    
    override public var preferredInterfaceOrientationForPresentation : UIInterfaceOrientation {
        return UIInterfaceOrientation.portrait
    }
    
}
