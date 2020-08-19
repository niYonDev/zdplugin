import Flutter
import UIKit
import SupportSDK
import ZendeskCoreSDK
import ChatSDK
import ChatProvidersSDK
import ZendeskCoreSDK
import MessagingSDK

public class SwiftFlutterZendeskPlugin: NSObject, FlutterPlugin {
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_zendes_plugin", binaryMessenger: registrar.messenger())
        let instance = SwiftFlutterZendeskPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
            case "init":
                // Set logger on debug
                Logger.isEnabled = true
                Logger.defaultLevel = .verbose

                // Get variables from flutter
                guard let dic = call.arguments as? Dictionary<String, Any> else { return }
                let accountKey = dic["accountKey"] as? String ?? ""
                let applicationId = dic["applicationId"] as? String ?? ""
                let clientId = dic["clientId"] as? String ?? ""
                let domainUrl = dic["domainUrl"] as? String ?? ""

                if(accountKey.isEmpty) {
                    result("AccountKey is null")
                    break
                }

                // 1.Init Zendesk SDK instance
                Zendesk.initialize(appId: applicationId,
                                   clientId: clientId,
                                   zendeskUrl: domainUrl)

                // 2.Init Support SDK instance
                Support.initialize(withZendesk: Zendesk.instance)

                // 3.Setting Anonymous identity for Zendesk SDK
                Zendesk.instance?.setIdentity(Identity.createAnonymous())

                // 4.Init ChatV2 SDK Instance
                Chat.initialize(accountKey: accountKey,appId:applicationId)

                result("iOS init completed" )
            case "startChatV2":
                guard let dic = call.arguments as? Dictionary<String, Any> else { return }
                let botLabel = dic["botLabel"] as? String ?? "Anwser Bot"
                let phone = dic["phone"] as? String ?? ""

                let name = dic["name"] as? String ?? ""
                do {
                    try startChatV2(botLabel: botLabel,phone: phone,name: name)
                } catch let error{
                    print("error:\(error)")
                }
            case "helpCenter":
                // Get parameters from flutter
                guard let dic = call.arguments as? Dictionary<String, Any> else { return }
                let contactUsButtonVisible = dic["contactUsButtonVisible"] as? Bool ?? false

                let currentVC = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController
                let hcConfig = HelpCenterUiConfiguration()
                hcConfig.showContactOptions = contactUsButtonVisible

                let articleUiConfig = ArticleUiConfiguration()
                articleUiConfig.showContactOptions = contactUsButtonVisible

                let helpCenter = HelpCenterUi.buildHelpCenterOverviewUi(withConfigs: [hcConfig])
                currentVC?.pushViewController(helpCenter, animated: true)
                result("iOS helpCenter UI:" + helpCenter.description + "   ")
            case "requestView":
                let rootViewController = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController
                let viewController = RequestUi.buildRequestUi(with: [])
                rootViewController?.pushViewController(viewController, animated: true)
            case "requestListView":
                let rootViewController = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController
                let viewController = RequestUi.buildRequestList(with: [])
                rootViewController?.pushViewController(viewController, animated: true)
            case "changeNavStatus":
                let rootViewController = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController
                guard let dic = call.arguments as? Dictionary<String, Any> else { return }

                let isShow = dic["isShow"] as? Bool ?? false
                rootViewController?.setNavigationBarHidden(!isShow, animated: false)
                result("rootViewController?.isNavigationBarHidden = isShow >>>>>")
            case "getPlatformVersion":
                result("iOS " + UIDevice.current.systemVersion)

            default:
                result("method not implemented")
        }
    }
    
    func startChatV2(botLabel:String,phone:String,name:String) throws {
        
        Chat.profileProvider?.addTags(["teste：name："+name+" phone："+phone])
        
        let chatFormConfiguration = ChatSDK.ChatFormConfiguration.init(name: .required, email: .hidden, phoneNumber: .hidden, department: .hidden)
        
        let chatConfiguration = ChatConfiguration()
        //If true, visitors will be prompted at the end of their chat asking them whether they would like a transcript sent by email.
        chatConfiguration.isChatTranscriptPromptEnabled = true
        //If true, visitors are prompted for information in a conversational manner prior to starting the chat. Defaults to true.
        chatConfiguration.isPreChatFormEnabled = true
        //If this flag is enabled (as well as isAgentAvailabilityEnabled) then visitors will be presented with a form allowing them to leave a message if no agents are available. This will create a support ticket. Defaults to true.
        chatConfiguration.isOfflineFormEnabled = true
        //If true, and no agents are available to serve the visitor, they will be presented with a message letting them know that no agents are available. If it's disabled, visitors will remain in a queue waiting for an agent. Defaults to true.
        chatConfiguration.isAgentAvailabilityEnabled = true
        //This property allows you to configure the requirements of each of the pre-chat form fields.
        chatConfiguration.preChatFormConfiguration = chatFormConfiguration
        
        // Name for Bot messages
        let messagingConfiguration = MessagingConfiguration()
        messagingConfiguration.name = botLabel
        
        let chatEngine = try ChatEngine.engine()
        
        let viewController = try Messaging.instance.buildUI(engines: [chatEngine], configs: [chatConfiguration,messagingConfiguration])
        
        
        if let navigationController = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController {
            navigationController.pushViewController(viewController, animated: true)
            let chatAPIConfiguration = ChatAPIConfiguration()
            chatAPIConfiguration.visitorInfo = VisitorInfo(name: name, email: "", phoneNumber: phone)
            Chat.instance?.configuration = chatAPIConfiguration
        }
        
        
    }
    
}
