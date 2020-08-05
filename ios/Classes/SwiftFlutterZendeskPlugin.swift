import Flutter
import UIKit
import SupportSDK
import ChatSDK
import ChatProvidersSDK
import ZendeskCoreSDK
import ZDCChat
import MessagingSDK

public class SwiftFlutterZendeskPlugin: NSObject, FlutterPlugin {
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_zendes_plugin", binaryMessenger: registrar.messenger())
        let instance = SwiftFlutterZendeskPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        result("iOS handle:" + call.method)
        
        switch call.method {
        case "init":
            Logger.isEnabled = true
            Logger.defaultLevel = .verbose
            guard let dic = call.arguments as? Dictionary<String, Any> else { return }
            
            let accountKey = dic["accountKey"] as? String ?? ""
            let applicationId = dic["applicationId"] as? String ?? ""
            let clientId = dic["clientId"] as? String ?? ""
            let domainUrl = dic["domainUrl"] as? String ?? ""
            let emailIdentifier = dic["emailIdentifier"] as? String ?? "emailIdentifier"
            let nameIdentifier = dic["nameIdentifier"] as? String ?? "nameIdentifier"
            
            Zendesk.initialize(appId: applicationId,
                               clientId: clientId,
                               zendeskUrl: domainUrl)
            Support.initialize(withZendesk: Zendesk.instance)
            Zendesk.instance?.setIdentity(Identity.createAnonymous(name:nameIdentifier, email: emailIdentifier))
            
            
            //CHAT SDK
            Chat.initialize(accountKey: accountKey)
            ZDCChat.initialize(withAccountKey: accountKey)
            
            result("iOS init completed" )
        case "startChat":
            guard let dic = call.arguments as? Dictionary<String, Any> else { return }
            
            let phone = dic["phone"] as? String ?? ""
            let email = dic["email"] as? String ?? ""
            let name = dic["name"] as? String ?? ""
            do {
//                try startChatV2(name: name, email: email, phone: phone)
                try startChatV1(name: name, email: email, phone: phone)
            } catch let error{
                print("error:\(error)")//捕捉到错误，处理错误
            }
            
        case "helpCenter":
            let currentVC = UIApplication.shared.keyWindow?.rootViewController
            let hcConfig = HelpCenterUiConfiguration()
            let helpCenter = HelpCenterUi.buildHelpCenterOverviewUi(withConfigs: [hcConfig])
            currentVC?.present(helpCenter, animated: true, completion: nil)
            result("iOS helpCenter UI:" + helpCenter.description + "   ")
        default:
            break
        }
    }
    func startChatV1(name:String,email:String,phone:String){
        //https://developer.zendesk.com/embeddables/docs/ios-chat-sdk/chat
        
        let navigationController = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController
        ZDCChat.updateVisitor { user in
          user?.phone = phone
          user?.name = name
          user?.email = email
          user?.addNote("This is a Swift note")
        }
        ZDCChat.start(in: navigationController, withConfig: {config in
            config?.preChatDataRequirements.name = .optional
            config?.preChatDataRequirements.email = .optional
            config?.preChatDataRequirements.phone = .optional
        })
               
        // Hides the back button because we are in a tab controller
        ZDCChat.instance().chatViewController.navigationItem.hidesBackButton = true
    }
    
    func startChatV2(name:String,email:String,phone:String) throws {
        let chatConfiguration = ChatConfiguration()
        chatConfiguration.isAgentAvailabilityEnabled = true
        chatConfiguration.isPreChatFormEnabled = true
        
        let chatAPIConfiguration = ChatAPIConfiguration()
        chatAPIConfiguration.department = "Department name"
        chatAPIConfiguration.visitorInfo = VisitorInfo(name: name, email: email, phoneNumber: phone)
        Chat.instance?.configuration = chatAPIConfiguration
        // Name for Bot messages
        let messagingConfiguration = MessagingConfiguration()
        messagingConfiguration.name = "Chat Bot"
        
        
        
        // Build view controller
        let chatEngine = try ChatEngine.engine()
        let viewController = try Messaging.instance.buildUI(engines: [chatEngine], configs: [messagingConfiguration, chatConfiguration])
        
        
        if let navigationController = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController {
            navigationController.pushViewController(viewController, animated: true)
        }else{
            let navigationController = UIApplication.shared.keyWindow?.rootViewController
            navigationController?.present(viewController
                , animated: true, completion: nil)
        }
        
    }
}
