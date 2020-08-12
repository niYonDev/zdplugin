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
            
            let phone = dic["phone"] as? String ?? ""
            let email = dic["email"] as? String ?? ""
            let name = dic["name"] as? String ?? ""
            let departmentName = dic["departmentName"] as? String ?? "Department Name"
            
            Zendesk.initialize(appId: applicationId,
                               clientId: clientId,
                               zendeskUrl: domainUrl)
            Support.initialize(withZendesk: Zendesk.instance)
            Zendesk.instance?.setIdentity(Identity.createAnonymous(name:nameIdentifier, email: emailIdentifier))
            
            
            //V1 Chat
            ZDCChat.initialize(withAccountKey: accountKey)
            ZDCChat.updateVisitor { user in
                user?.phone = phone
                user?.name = name
                user?.email = email
            }
            //CHAT V2 SDK
            Chat.initialize(accountKey: accountKey,appId:applicationId)
//            let chatAPIConfiguration = ChatAPIConfiguration()
//            chatAPIConfiguration.department = departmentName
//            chatAPIConfiguration.visitorInfo = VisitorInfo(name: name, email: email, phoneNumber: "")
//            Chat.instance?.configuration = chatAPIConfiguration
            result("iOS init completed" )
        case "startChatV1":
            startChatV1()
        case "startChatV2":
            guard let dic = call.arguments as? Dictionary<String, Any> else { return }
            let botLabel = dic["botLabel"] as? String ?? "Anwser Bot"
            let phone = dic["phone"] as? String ?? ""
            
            let name = dic["name"] as? String ?? ""
            do {
                try startChatV2(botLabel: botLabel,phone: phone,name: name)
            } catch let error{
                print("error:\(error)")//捕捉到错误，处理错误
            }
        case "helpCenter":
            let currentVC = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController
            let hcConfig = HelpCenterUiConfiguration()
            hcConfig.showContactOptions = true
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
            
        default:
            break
        }
    }
    func startChatV1(){
        //https://developer.zendesk.com/embeddables/docs/ios-chat-sdk/chat
        
        let navigationController = UIApplication.shared.keyWindow?.rootViewController as? UINavigationController
        
        ZDCChat.start(in: navigationController, withConfig: {config in
            config?.preChatDataRequirements.name = .notRequired
            config?.preChatDataRequirements.email = .notRequired
            config?.preChatDataRequirements.phone = .requiredEditable
        })
        
        // Hides the back button because we are in a tab controller
        //        ZDCChat.instance().chatViewController.navigationItem.hidesBackButton = true
    }
    
    func startChatV2(botLabel:String,phone:String,name:String) throws {
        
        Chat.profileProvider?.addTags(["标签：昵称："+name+" 手机号："+phone])
        
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
