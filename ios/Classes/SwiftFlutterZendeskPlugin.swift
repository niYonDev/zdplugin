import Flutter
import UIKit
import SupportSDK
import ZDCChat
import ZendeskCoreSDK

//import ChatSDK
//import ChatProvidersSDK

import MessagingSDK

public class SwiftFlutterZendeskPlugin: NSObject, FlutterPlugin {
    
//    var hcConfig: HelpCenterUiConfiguration {
//        let hcConfig = HelpCenterUiConfiguration()
//        return hcConfig
//    }
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_zendes_plugin", binaryMessenger: registrar.messenger())
    let instance = SwiftFlutterZendeskPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS handle:" + call.method)

    switch call.method {
    case "init":
        guard let dic = call.arguments as? Dictionary<String, Any> else { return }

        let accountKey = dic["accountKey"] as? String ?? ""
        let applicationId = dic["applicationId"] as? String ?? ""
        let clientId = dic["clientId"] as? String ?? ""
        let domainUrl = dic["domainUrl"] as? String ?? ""

                            Zendesk.initialize(appId: applicationId,
                           clientId: clientId,
                           zendeskUrl: domainUrl)
        
        Support.initialize(withZendesk: Zendesk.instance)
        
        let ident = Identity.createAnonymous()
        Zendesk.instance?.setIdentity(ident)
        
        
        Zendesk.instance?.setIdentity(Identity.createAnonymous(name: "name", email: "name@email.com"))
        ZDCChat.initialize(withAccountKey: accountKey)
        result("iOS init completed" )
    case "startChat":
        guard let dic = call.arguments as? Dictionary<String, Any> else { return }

        let type = dic["type"] as? Int ?? 0
        let phone = dic["phone"] as? String ?? ""
        let email = dic["email"] as? String ?? ""
        let name = dic["name"] as? String ?? ""
        
//        let chatEngine = try ChatEngine.engine()
//        let viewController = try Messaging.instance.buildUI(engines: [chatEngine], configs: [])
//
//        self.navigationController?.pushViewController(viewController, animated: true)
        let navigationController = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController
        ZDCChat.start(in: navigationController, withConfig: nil)
               
        // Hides the back button because we are in a tab controller
        ZDCChat.instance().chatViewController.navigationItem.hidesBackButton = false
            
    case "helpCenter":
        let currentVC = UIApplication.shared.keyWindow?.rootViewController
        let hcConfig = HelpCenterUiConfiguration()
        let helpCenter = HelpCenterUi.buildHelpCenterOverviewUi(withConfigs: [hcConfig])
        currentVC?.present(helpCenter, animated: true, completion: nil)
        
//        let hcConfig = HelpCenterUiConfiguration()
//        hcConfig.showContactOptions = true
//        hcConfig.labels = ["label"]
////        hcConfig.groupType = .category
////        hcConfig.groupIds = []
//        let helpCenter = HelpCenterUi.buildHelpCenterOverviewUi(withConfigs: [hcConfig])
//        let navigationController = UIApplication.shared.keyWindow?.rootViewController
//        navigationController?.present(helpCenter, animated: true, completion: nil)
//        let ss = (navigationController == nil) ? "no controller" : "No"
        result("iOS helpCenter UI:" + helpCenter.description + "   ")
//
//        print("<<<<<<<<<<<<<<<<< helpCenter")
        
    default:
        break
    }
  }
    
    func startChat() throws {
        /*
        do {

            let chatEngine = try ChatEngine.engine()
            let viewController = try Messaging.instance.buildUI(engines: [chatEngine], configs: [])

            self.navigationController?.pushViewController(viewController, animated: true)
        } catch {
            // handle error
        }
       Name for Bot messages
      
        let viewController = try Messaging.instance.buildUI(engines: [])

       Present view controller
      self.navigationController?.pushViewController(viewController, animated: true)
         ZDCChat.start(in: UINavigationController!, withConfig: <#T##ZDCConfigBlock!##ZDCConfigBlock!##(ZDCConfig?) -> Void#>)
        */
    }
}
